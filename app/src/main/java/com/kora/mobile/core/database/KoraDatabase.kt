package com.kora.mobile.core.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "local_entries", primaryKeys = ["scope_key", "entry_key"], indices = [Index(value = ["scope_key", "category"], name = "local_entries_category")])
data class LocalEntry(
    @ColumnInfo(name = "scope_key") val scopeKey: String,
    @ColumnInfo(name = "entry_key") val entryKey: String,
    @ColumnInfo(name = "payload") val encryptedPayload: String,
    @ColumnInfo(name = "fetched_at") val fetchedAt: Long,
    val category: String,
)

@Entity(tableName = "local_drafts", primaryKeys = ["scope_key", "id"])
data class LocalDraft(
    val id: String,
    @ColumnInfo(name = "scope_key") val scopeKey: String,
    val doctype: String,
    @ColumnInfo(name = "record_name") val recordName: String?,
    @ColumnInfo(name = "payload") val encryptedPayload: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

@Entity(tableName = "local_outbox", primaryKeys = ["scope_key", "id"], indices = [Index(value = ["scope_key", "created_at"], name = "local_outbox_order")])
data class LocalMutation(
    val id: String,
    @ColumnInfo(name = "scope_key") val scopeKey: String,
    val doctype: String,
    @ColumnInfo(name = "record_name") val recordName: String?,
    @ColumnInfo(name = "idempotency_key") val idempotencyKey: String,
    @ColumnInfo(name = "payload") val encryptedPayload: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "display_name") val displayName: String? = null,
    @ColumnInfo(defaultValue = "'queued'") val status: String = "queued",
    @ColumnInfo(name = "last_error") val lastError: String? = null,
    @ColumnInfo(name = "local_id") val localId: String? = null,
    @ColumnInfo(name = "base_revision") val baseRevision: String? = null,
    @ColumnInfo(defaultValue = "0") val attempts: Int = 0,
    @ColumnInfo(name = "next_attempt_at", defaultValue = "0") val nextAttemptAt: Long = 0,
)

@Entity(tableName = "local_records", primaryKeys = ["scope_key", "doctype", "local_id"], indices = [Index(value = ["scope_key", "doctype", "server_id"], unique = true)])
data class LocalRecord(
    @ColumnInfo(name = "scope_key") val scopeKey: String,
    val doctype: String,
    @ColumnInfo(name = "local_id") val localId: String,
    @ColumnInfo(name = "server_id") val serverId: String?,
    @ColumnInfo(name = "payload") val encryptedPayload: String,
    @ColumnInfo(name = "server_payload") val encryptedServerPayload: String?,
    @ColumnInfo(name = "server_revision") val serverRevision: String?,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
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
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun putDraft(draft: LocalDraft)
    @Query("SELECT * FROM local_drafts WHERE scope_key=:scope ORDER BY updated_at DESC") abstract fun observeDrafts(scope: String): Flow<List<LocalDraft>>
    @Query("SELECT * FROM local_drafts WHERE scope_key=:scope AND id=:id") abstract suspend fun getDraft(scope: String, id: String): LocalDraft?
    @Query("DELETE FROM local_drafts WHERE scope_key=:scope AND id=:id") abstract suspend fun deleteDraft(scope: String, id: String)
    @Query("DELETE FROM local_drafts WHERE scope_key=:scope") abstract suspend fun clearDrafts(scope: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun putMutation(mutation: LocalMutation)
    @Query("SELECT * FROM local_outbox WHERE scope_key=:scope AND status NOT IN ('acked','discarded') ORDER BY created_at, rowid") abstract fun observeMutations(scope: String): Flow<List<LocalMutation>>
    @Query("SELECT * FROM local_outbox WHERE scope_key=:scope AND status NOT IN ('acked','discarded') ORDER BY created_at, rowid") abstract suspend fun queuedMutations(scope: String): List<LocalMutation>
    @Query("SELECT * FROM local_outbox WHERE scope_key=:scope AND id=:id") abstract suspend fun mutation(scope: String, id: String): LocalMutation?
    @Query("DELETE FROM local_outbox WHERE scope_key=:scope AND id=:id") abstract suspend fun deleteMutation(scope: String, id: String)
    @Query("SELECT * FROM local_records WHERE scope_key=:scope AND doctype=:doctype ORDER BY updated_at DESC, local_id") abstract suspend fun records(scope: String, doctype: String): List<LocalRecord>
    @Query("SELECT * FROM local_records WHERE scope_key=:scope AND doctype=:doctype ORDER BY updated_at DESC, local_id") abstract fun observeRecords(scope: String, doctype: String): Flow<List<LocalRecord>>
    @Query("SELECT * FROM local_records WHERE scope_key=:scope AND doctype=:doctype AND (local_id=:id OR server_id=:id) LIMIT 1") abstract suspend fun record(scope: String, doctype: String, id: String): LocalRecord?
    @Query("SELECT * FROM local_records WHERE scope_key=:scope AND local_id=:id LIMIT 1") abstract suspend fun recordByLocalId(scope: String, id: String): LocalRecord?
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun putRecord(record: LocalRecord)
    @Query("DELETE FROM local_records WHERE scope_key=:scope AND doctype=:doctype AND local_id=:id") abstract suspend fun deleteRecord(scope: String, doctype: String, id: String)

    @Transaction
    open suspend fun commitLocal(record: LocalRecord, mutation: LocalMutation, draftId: String?) {
        putRecord(record)
        putMutation(mutation)
        if (draftId != null) deleteDraft(record.scopeKey, draftId)
    }

    @Transaction
    open suspend fun acknowledge(record: LocalRecord?, mutation: LocalMutation) {
        if (record != null) putRecord(record)
        putMutation(mutation.copy(status = "acked", lastError = null))
        // Never delete a newer draft created while an earlier operation was syncing.
    }
}

@Database(entities = [LocalEntry::class, LocalDraft::class, LocalMutation::class, LocalRecord::class], version = 4, exportSchema = false)
abstract class KoraDatabase : RoomDatabase() {
    abstract val localDao: LocalDao
    companion object {
        @Volatile private var instance: KoraDatabase? = null
        val MIGRATION_1_4 = migration(1)
        val MIGRATION_2_4 = migration(2)
        val MIGRATION_3_4 = migration(3)
        private fun migration(from: Int) = object : Migration(from, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                if (from < 2) db.execSQL("CREATE TABLE IF NOT EXISTS local_outbox (id TEXT NOT NULL, scope_key TEXT NOT NULL, doctype TEXT NOT NULL, record_name TEXT, idempotency_key TEXT NOT NULL, payload TEXT NOT NULL, created_at INTEGER NOT NULL, display_name TEXT, PRIMARY KEY(scope_key,id))")
                if (from == 2) db.execSQL("ALTER TABLE local_outbox ADD COLUMN display_name TEXT")
                db.execSQL("CREATE INDEX IF NOT EXISTS local_outbox_order ON local_outbox(scope_key,created_at)")
                db.execSQL("ALTER TABLE local_outbox ADD COLUMN status TEXT NOT NULL DEFAULT 'queued'")
                db.execSQL("ALTER TABLE local_outbox ADD COLUMN last_error TEXT")
                db.execSQL("ALTER TABLE local_outbox ADD COLUMN local_id TEXT")
                db.execSQL("ALTER TABLE local_outbox ADD COLUMN base_revision TEXT")
                db.execSQL("ALTER TABLE local_outbox ADD COLUMN attempts INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE local_outbox ADD COLUMN next_attempt_at INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE TABLE IF NOT EXISTS local_records (scope_key TEXT NOT NULL, doctype TEXT NOT NULL, local_id TEXT NOT NULL, server_id TEXT, payload TEXT NOT NULL, server_payload TEXT, server_revision TEXT, updated_at INTEGER NOT NULL, PRIMARY KEY(scope_key,doctype,local_id))")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_local_records_scope_key_doctype_server_id ON local_records(scope_key,doctype,server_id)")
            }
        }
        fun get(context: Context): KoraDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, KoraDatabase::class.java, "kora_mobile.db")
                .addMigrations(MIGRATION_1_4, MIGRATION_2_4, MIGRATION_3_4).build().also { instance = it }
        }
    }
}
