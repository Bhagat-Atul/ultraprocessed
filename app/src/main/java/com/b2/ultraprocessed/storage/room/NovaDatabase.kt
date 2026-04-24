package com.b2.ultraprocessed.storage.secrets

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ScanResult::class],
    version = 1,
    exportSchema = false
)
abstract class NovaDatabase : RoomDatabase() {

    abstract fun scanResultDao(): ScanResultDao

    companion object {
        @Volatile
        private var INSTANCE: NovaDatabase? = null

        fun getDatabase(context: Context): NovaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NovaDatabase::class.java,
                    "nova_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

