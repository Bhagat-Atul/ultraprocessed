package com.b2.ultraprocessed.storage.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ScanResult::class],
    version = 5,
    exportSchema = true
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
                ).addMigrations(*MIGRATIONS).build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE scan_results ADD COLUMN productName TEXT NOT NULL DEFAULT 'Scanned label'")
                db.execSQL("ALTER TABLE scan_results ADD COLUMN novaGroup INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE scan_results ADD COLUMN capturedImagePath TEXT")
                db.execSQL("ALTER TABLE scan_results ADD COLUMN isBarcodeLookupOnly INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE scan_results ADD COLUMN allergens TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE scan_results ADD COLUMN modelId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE scan_results ADD COLUMN modelName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE scan_results ADD COLUMN provider TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE scan_results ADD COLUMN estimatedInputTokens INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE scan_results ADD COLUMN estimatedOutputTokens INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE scan_results ADD COLUMN estimatedTotalTokens INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE scan_results ADD COLUMN estimatedCostUsd REAL NOT NULL DEFAULT 0.0")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE scan_results ADD COLUMN isFailed INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE scan_results ADD COLUMN failureMessage TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATIONS: Array<Migration> = arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
        )
    }
}
