package com.kora.mobile.core.contracts

import android.content.Context
import com.kora.mobile.generated.EngineContractManifest
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.time.Instant

/** Strict schema-based decoder for the Engine v2 wire contracts. */
class EngineContractDecoder(private val schema: JSONObject) {
    data class Violation(val path: String, val message: String)

    fun validate(contract: String, value: JSONObject): List<Violation> {
        val definition = schema.optJSONObject("\$defs")?.optJSONObject(contract)
            ?: return listOf(Violation("$", "unknown Engine contract: $contract"))
        return validateNode(definition, value, "$")
    }

    fun decode(contract: String, json: String): JSONObject {
        val value = try {
            JSONObject(json)
        } catch (error: Exception) {
            throw IllegalArgumentException("invalid JSON object for $contract", error)
        }
        val violations = validate(contract, value)
        if (violations.isNotEmpty()) {
            throw ContractDecodeException(contract, violations)
        }
        return value
    }

    private fun validateNode(node: JSONObject, value: Any?, path: String): List<Violation> {
        if (node.has("\$ref")) {
            val ref = node.optString("\$ref")
            val name = ref.substringAfterLast('/')
            val target = schema.optJSONObject("\$defs")?.optJSONObject(name)
                ?: return listOf(Violation(path, "unresolved schema reference $ref"))
            return validateNode(target, value, path)
        }
        if (node.has("const") && node.opt("const") != value) {
            return listOf(Violation(path, "value does not match required constant"))
        }
        if (node.has("enum")) {
            val values = node.optJSONArray("enum") ?: JSONArray()
            if ((0 until values.length()).none { values.opt(it) == value }) {
                return listOf(Violation(path, "value is not in the allowed enum"))
            }
        }
        node.optJSONArray("oneOf")?.let { alternatives ->
            val matching = (0 until alternatives.length()).count { index ->
                validateNode(alternatives.getJSONObject(index), value, path).isEmpty()
            }
            if (matching != 1) return listOf(Violation(path, "value must match exactly one schema variant"))
        }
        node.optJSONArray("anyOf")?.let { alternatives ->
            if ((0 until alternatives.length()).none { validateNode(alternatives.getJSONObject(it), value, path).isEmpty() }) {
                return listOf(Violation(path, "value does not match any allowed schema variant"))
            }
        }
        node.optJSONObject("not")?.let { forbidden ->
            if (validateNode(forbidden, value, path).isEmpty()) return listOf(Violation(path, "value matches a forbidden schema variant"))
        }

        val type = node.optString("type")
        val errors = mutableListOf<Violation>()
        when (type) {
            "object" -> {
                if (value !is JSONObject) return listOf(Violation(path, "expected object"))
                val properties = node.optJSONObject("properties") ?: JSONObject()
                val required = node.optJSONArray("required") ?: JSONArray()
                for (index in 0 until required.length()) {
                    val key = required.optString(index)
                    if (!value.has(key) || value.isNull(key)) errors += Violation("$path.$key", "required field is missing")
                }
                val keys = value.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    if (!properties.has(key)) {
                        if (node.opt("additionalProperties") == JSONObject.NULL || node.optBoolean("additionalProperties", true).not()) {
                            errors += Violation("$path.$key", "unknown field")
                        }
                        continue
                    }
                    val childSchema = properties.optJSONObject(key) ?: continue
                    errors += validateNode(childSchema, value.opt(key), "$path.$key")
                }
            }
            "array" -> {
                if (value !is JSONArray) return listOf(Violation(path, "expected array"))
                if (node.has("minItems") && value.length() < node.optInt("minItems")) errors += Violation(path, "array has too few items")
                if (node.has("maxItems") && value.length() > node.optInt("maxItems")) errors += Violation(path, "array has too many items")
                node.optJSONObject("items")?.let { itemSchema ->
                    for (index in 0 until value.length()) errors += validateNode(itemSchema, value.opt(index), "$path[$index]")
                }
            }
            "string" -> {
                if (value !is String) return listOf(Violation(path, "expected string"))
                if (node.has("minLength") && value.length < node.optInt("minLength")) errors += Violation(path, "string is too short")
                if (node.has("maxLength") && value.length > node.optInt("maxLength")) errors += Violation(path, "string is too long")
                if (node.has("pattern") && !Regex(node.optString("pattern")).containsMatchIn(value)) errors += Violation(path, "string does not match required pattern")
                if (node.optString("format") == "date-time" && runCatching { Instant.parse(value) }.isFailure) errors += Violation(path, "invalid date-time")
                if (node.has("x-max-utf8-bytes") && value.toByteArray(Charsets.UTF_8).size > node.optInt("x-max-utf8-bytes")) errors += Violation(path, "string exceeds UTF-8 byte limit")
            }
            "integer" -> {
                if (value !is Number || value.toDouble() % 1.0 != 0.0) return listOf(Violation(path, "expected integer"))
                if (node.has("minimum") && value.toDouble() < node.optDouble("minimum")) errors += Violation(path, "integer is below minimum")
                if (node.has("maximum") && value.toDouble() > node.optDouble("maximum")) errors += Violation(path, "integer is above maximum")
            }
            "number" -> {
                if (value !is Number) return listOf(Violation(path, "expected number"))
                if (node.has("minimum") && value.toDouble() < node.optDouble("minimum")) errors += Violation(path, "number is below minimum")
                if (node.has("maximum") && value.toDouble() > node.optDouble("maximum")) errors += Violation(path, "number is above maximum")
            }
            "boolean" -> if (value !is Boolean) return listOf(Violation(path, "expected boolean"))
            "" -> Unit
            else -> return listOf(Violation(path, "unsupported schema type $type"))
        }
        if (node.has("x-max-json-bytes")) {
            val encoded = when (value) {
                is JSONObject -> value.toString()
                is JSONArray -> value.toString()
                else -> JSONObject.quote(value?.toString() ?: "null")
            }
            if (encoded.toByteArray(Charsets.UTF_8).size > node.optInt("x-max-json-bytes")) errors += Violation(path, "JSON value exceeds byte limit")
        }
        return errors
    }

    companion object {
        fun fromAssets(context: Context): EngineContractDecoder {
            val assetPath = EngineContractManifest.artifact.substringAfter("app/src/main/assets/")
            val bytes = context.assets.open(assetPath).use { it.readBytes() }
            val actual = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
            require(actual == EngineContractManifest.schemaSHA256) { "Engine contract schema digest does not match its generated manifest" }
            return EngineContractDecoder(JSONObject(bytes.toString(Charsets.UTF_8)))
        }
    }
}

class ContractDecodeException(val contract: String, val violations: List<EngineContractDecoder.Violation>) :
    IllegalArgumentException("$contract failed Engine schema validation: ${violations.joinToString { it.path + ": " + it.message }}")
