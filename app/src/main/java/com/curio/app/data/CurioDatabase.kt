package com.curio.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [CaptureEntity::class],
    version = 2,
    exportSchema = false
)
abstract class CurioDatabase : RoomDatabase() {

    abstract fun captureDao(): CaptureDao

    companion object {
        @Volatile
        private var INSTANCE: CurioDatabase? = null

        /**
         * v1 → v2 (v7.17): custom user tags. Adds the `tagsJson` column to
         * every saved capture with an empty-array default so existing entries
         * read as tag-less (the entity's Kotlin default matches this string).
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE captures ADD COLUMN tagsJson TEXT NOT NULL DEFAULT '[]'")
            }
        }

        fun getInstance(context: Context): CurioDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CurioDatabase::class.java,
                    "curio_database"
                )
                    // TRUNCATE journal mode (not WAL): Android Auto Backup can
                    // restore a WAL-mode database in an inconsistent state because
                    // the -wal/-shm files aren't guaranteed to be backed up in sync
                    // with the main .db file. Curio's DB is a small single-table
                    // text store, so the write-throughput tradeoff is negligible —
                    // backup integrity wins.
                    .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
