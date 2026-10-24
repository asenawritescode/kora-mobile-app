package com.kora.mobile.data.offline

import com.kora.mobile.domain.model.FieldSchema
import org.json.JSONArray
import org.json.JSONObject

/** One JSON representation for local records, drafts and the server, including child rows. */
object FormPayloadSerializer {
    private val layoutFields = setOf("Section Break", "Column Break", "Heading", "Tab Break", "HTML", "Button")

    fun serialize(fields: List<FieldSchema>, values: Map<String, Any?>, childSchemas: Map<String, List<FieldSchema>> = emptyMap(), draft: Boolean = false, isNew: Boolean = false): JSONObject = JSONObject().apply {
        for (field in fields) {
            if (field.fieldtype in layoutFields || (!draft && field.readOnly && !(isNew && field.editableOnCreate)) || !values.containsKey(field.fieldname)) continue
            val value = values[field.fieldname]
            val normalized: Any? = when (field.fieldtype) {
                "Table" -> {
                    val rows = array(value)
                    JSONArray().apply {
                        for (i in 0 until rows.length()) {
                            val row = rows.optJSONObject(i) ?: continue
                            val schema = childSchemas[field.options].orEmpty()
                            val clean = if (schema.isEmpty()) OfflineRecordRepository.stripLocalMetadata(row) else serialize(schema, row.keys().asSequence().associateWith { row.opt(it) }, childSchemas, draft, isNew = row.optString("name").isBlank() || row.optString("name").startsWith("local:"))
                            row.optString("name").takeIf { it.isNotBlank() && !it.startsWith("local:") }?.let { clean.put("name", it) }
                            put(clean)
                        }
                    }
                }
                "Check" -> value == true || value?.toString() == "1" || value?.toString().equals("true", true)
                "Int" -> if (value == null || value == JSONObject.NULL || value.toString().isBlank()) JSONObject.NULL else value.toString().toLongOrNull() ?: value
                "Float", "Currency", "Percent" -> if (value == null || value == JSONObject.NULL || value.toString().isBlank()) JSONObject.NULL else value.toString().toBigDecimalOrNull()?.toPlainString() ?: value
                "JSON" -> if (value is String) runCatching { org.json.JSONTokener(value).nextValue() }.getOrElse { value } else wrap(value)
                else -> wrap(value)
            }
            put(field.fieldname, normalized ?: JSONObject.NULL)
        }
    }

    private fun array(value: Any?): JSONArray = when (value) {
        is JSONArray -> value
        is Iterable<*> -> JSONArray().apply { value.forEach { put(wrap(it)) } }
        else -> JSONArray()
    }
    private fun wrap(value: Any?): Any = when (value) {
        null -> JSONObject.NULL
        is Map<*, *> -> JSONObject().apply { value.forEach { (key, item) -> put(key.toString(), wrap(item)) } }
        is Iterable<*> -> JSONArray().apply { value.forEach { put(wrap(it)) } }
        else -> value
    }
}
