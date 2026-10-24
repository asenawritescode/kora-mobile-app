package com.kora.mobile.data.offline

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.kora.mobile.core.database.KoraDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class DatabaseMigrationTest {
    @Test fun v3MigrationPreservesEncryptedCacheDraftAndPendingOperation() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "migration-test.db"
        context.deleteDatabase(name)
        val file = context.getDatabasePath(name).apply { parentFile?.mkdirs() }
        SQLiteDatabase.openOrCreateDatabase(file, null).use { old ->
            old.execSQL("CREATE TABLE local_entries (scope_key TEXT NOT NULL, entry_key TEXT NOT NULL, payload TEXT NOT NULL, fetched_at INTEGER NOT NULL, category TEXT NOT NULL, PRIMARY KEY(scope_key,entry_key))")
            old.execSQL("CREATE INDEX local_entries_category ON local_entries(scope_key,category)")
            old.execSQL("CREATE TABLE local_drafts (id TEXT NOT NULL, scope_key TEXT NOT NULL, doctype TEXT NOT NULL, record_name TEXT, payload TEXT NOT NULL, created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL, PRIMARY KEY(scope_key,id))")
            old.execSQL("CREATE TABLE local_outbox (id TEXT NOT NULL, scope_key TEXT NOT NULL, doctype TEXT NOT NULL, record_name TEXT, idempotency_key TEXT NOT NULL, payload TEXT NOT NULL, created_at INTEGER NOT NULL, display_name TEXT, PRIMARY KEY(scope_key,id))")
            old.execSQL("CREATE INDEX local_outbox_order ON local_outbox(scope_key,created_at)")
            old.execSQL("INSERT INTO local_entries VALUES ('scope','navigation','encrypted-cache',1,'navigation')")
            old.execSQL("INSERT INTO local_drafts VALUES ('Product:new','scope','Product',NULL,'encrypted-draft',1,2)")
            old.execSQL("INSERT INTO local_outbox VALUES ('op-1','scope','Product',NULL,'keep-this-key','encrypted-operation',3,'Milk')")
            old.version = 3
        }
        val migrated = Room.databaseBuilder(context, KoraDatabase::class.java, name).addMigrations(KoraDatabase.MIGRATION_3_4).build()
        try {
            val dao = migrated.localDao
            assertEquals("encrypted-cache", dao.getEntry("scope", "navigation")!!.encryptedPayload)
            assertEquals("encrypted-draft", dao.getDraft("scope", "Product:new")!!.encryptedPayload)
            val operation = dao.queuedMutations("scope").single()
            assertEquals("keep-this-key", operation.idempotencyKey)
            assertEquals("encrypted-operation", operation.encryptedPayload)
            assertEquals("queued", operation.status)
            assertTrue(dao.records("scope", "Product").isEmpty())
        } finally { migrated.close(); context.deleteDatabase(name) }
    }
}
