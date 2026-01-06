/*
 * Copyright 2023 Miklos Vajna
 *
 * SPDX-License-Identifier: MIT
 */

package hu.vmiklos.plees_tracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import hu.vmiklos.plees_tracker.data.local.migration.Migration1To2
import hu.vmiklos.plees_tracker.data.local.migration.Migration2To3
import hu.vmiklos.plees_tracker.data.local.sleep.SleepDao
import hu.vmiklos.plees_tracker.domain.model.Sleep

/**
 * Contains the database holder and serves as the main access point for the
 * stored data.
 */
@Database(entities = [Sleep::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sleepDao(): SleepDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "database"
                )
                    .addMigrations(Migration1To2, Migration2To3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/* vim:set shiftwidth=4 softtabstop=4 expandtab: */
