package com.curio.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CaptureEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CurioDatabase : RoomDatabase() {

    abstract fun captureDao(): CaptureDao

    companion object {
        @Volatile
        private var INSTANCE: CurioDatabase? = null

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
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
