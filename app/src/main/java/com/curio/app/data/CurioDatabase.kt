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
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
