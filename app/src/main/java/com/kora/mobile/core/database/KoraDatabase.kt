package com.kora.mobile.core.database

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "local_entries", primaryKeys = ["scope_key", "entry_key"], indices = [Index(value = ["scope_key", "category"], name = "local_entries_category")])
data class LocalEntry(
    @ColumnInfo(name = "scope_key") val scopeKey: String,
    @ColumnInfo(name = "entry_key") val entryKey: String,
    @ColumnInfo(name = "payload") val encryptedPayload: String,
    @ColumnInfo(name = "fetched_at") val fetchedAt: Long,
    val category: String,
)

/** Immutable, actor-scoped E4 command. The input and receipt are encrypted at rest. */
@Entity(tableName = "offline_intents", primaryKeys = ["scope_key", "local_operation_id"], indices = [Index(value = ["scope_key", "created_at"], name = "offline_intents_order")])
data class OfflineIntentEntity(
    @ColumnInfo(name = "scope_key") val scopeKey: String,
    @ColumnInfo(name = "local_operation_id") val localOperationId: String,
    @ColumnInfo(name = "idempotency_key") val idempotencyKey: String,
    @ColumnInfo(name = "business_scope") val businessScope: String,
    @ColumnInfo(name = "catalog_revision") val catalogRevision: Long,
    @ColumnInfo(name = "operation_ref") val operationRef: String,
    @ColumnInfo(name = "input_schema") val inputSchema: String,
    @ColumnInfo(name = "encrypted_input") val encryptedInput: String,
    @ColumnInfo(name = "expected_revision") val expectedRevision: String?,
    val status: String = "queued",
    @ColumnInfo(name = "receipt") val encryptedReceipt: String? = null,
    @ColumnInfo(name = "last_error") val lastError: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

/** Feed facts remain separate from pending commands and optimistic changes. */
@Entity(tableName = "offline_facts", primaryKeys = ["scope_key", "owner_module_id", "fact_key"], indices = [Index(value = ["scope_key", "owner_module_id"], name = "offline_facts_owner")])
data class OfflineFactEntity(
    @ColumnInfo(name = "scope_key") val scopeKey: String,
    @ColumnInfo(name = "owner_module_id") val ownerModuleId: String,
    @ColumnInfo(name = "fact_key") val factKey: String,
    @ColumnInfo(name = "encrypted_data") val encryptedData: String?,
    val deleted: Boolean,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

@Entity(tableName = "offline_checkpoints", primaryKeys = ["scope_key", "owner_module_id", "query_name", "input_hash"])
data class OfflineCheckpointEntity(
    @ColumnInfo(name = "scope_key") val scopeKey: String,
    @ColumnInfo(name = "owner_module_id") val ownerModuleId: String,
    @ColumnInfo(name = "query_name") val queryName: String,
    @ColumnInfo(name = "input_hash") val inputHash: String,
    @ColumnInfo(name = "encrypted_checkpoint") val encryptedCheckpoint: String,
    @ColumnInfo(name = "catalog_revision") val catalogRevision: Long,
)

@Dao
abstract class LocalDao {
    @Query("SELECT * FROM local_entries WHERE scope_key=:scope AND entry_key=:key") abstract suspend fun getEntry(scope: String, key: String): LocalEntry?
    @Query("SELECT * FROM local_entries WHERE scope_key=:scope AND category=:category") abstract fun observeEntries(scope: String, category: String): Flow<List<LocalEntry>>
    @Query("SELECT * FROM local_entries WHERE scope_key=:scope AND category=:category") abstract suspend fun entries(scope: String, category: String): List<LocalEntry>
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun putEntry(entry: LocalEntry)
    @Query("DELETE FROM local_entries WHERE scope_key=:scope AND category=:category") abstract suspend fun deleteEntries(scope: String, category: String)
    @Query("DELETE FROM local_entries WHERE scope_key=:scope AND entry_key=:key") abstract suspend fun deleteEntry(scope: String, key: String)
    @Query("DELETE FROM local_entries WHERE scope_key=:scope") abstract suspend fun clearScope(scope: String)

    @Insert(onConflict = OnConflictStrategy.ABORT) abstract suspend fun insertIntent(intent: OfflineIntentEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun putIntent(intent: OfflineIntentEntity)
    @Query("SELECT * FROM offline_intents WHERE scope_key=:scope AND status IN ('queued','sent','accepted','outcome_unknown') ORDER BY created_at, local_operation_id") abstract suspend fun pendingIntents(scope: String): List<OfflineIntentEntity>
    @Query("SELECT * FROM offline_intents WHERE scope_key=:scope AND status='blocked' ORDER BY created_at, local_operation_id") abstract suspend fun blockedIntents(scope: String): List<OfflineIntentEntity>
    @Query("SELECT * FROM offline_intents WHERE scope_key=:scope AND local_operation_id=:id") abstract suspend fun intent(scope: String, id: String): OfflineIntentEntity?
    @Query("SELECT * FROM offline_intents WHERE scope_key=:scope ORDER BY created_at, local_operation_id") abstract fun observeIntents(scope: String): Flow<List<OfflineIntentEntity>>
    @Query("DELETE FROM offline_intents WHERE scope_key=:scope AND local_operation_id=:id") abstract suspend fun deleteIntent(scope: String, id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun putFact(fact: OfflineFactEntity)
    @Query("SELECT * FROM offline_facts WHERE scope_key=:scope AND owner_module_id=:owner AND deleted=0 ORDER BY fact_key") abstract suspend fun facts(scope: String, owner: String): List<OfflineFactEntity>
    @Query("SELECT * FROM offline_facts WHERE scope_key=:scope AND owner_module_id=:owner ORDER BY fact_key") abstract fun observeFacts(scope: String, owner: String): Flow<List<OfflineFactEntity>>
    @Query("DELETE FROM offline_facts WHERE scope_key=:scope AND owner_module_id=:owner") abstract suspend fun clearFacts(scope: String, owner: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun putCheckpoint(checkpoint: OfflineCheckpointEntity)
    @Query("SELECT * FROM offline_checkpoints WHERE scope_key=:scope AND owner_module_id=:owner AND query_name=:query AND input_hash=:hash") abstract suspend fun checkpoint(scope: String, owner: String, query: String, hash: String): OfflineCheckpointEntity?
}

@Database(entities = [LocalEntry::class, OfflineIntentEntity::class, OfflineFactEntity::class, OfflineCheckpointEntity::class], version = 1, exportSchema = false)
abstract class KoraDatabase : RoomDatabase() {
    abstract val localDao: LocalDao
    companion object {
        @Volatile private var instance: KoraDatabase? = null
        fun get(context: Context): KoraDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, KoraDatabase::class.java, "kora_mobile_v2.db").build().also { instance = it }
        }
    }
}
