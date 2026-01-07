/*
 * Copyright 2023 Miklos Vajna
 *
 * SPDX-License-Identifier: MIT
 */

package hu.vmiklos.plees_tracker.domain.model

import android.content.SharedPreferences
import androidx.core.content.edit
import hu.vmiklos.plees_tracker.data.local.AppDatabase
import java.util.Date

/**
 * Data model is the singleton shared state between the activity and the
 * service.
 */
object DataModel {

    lateinit var preferences: SharedPreferences

    var start: Date? = null
        set(start) {
            field = start
            // Save start timestamp in case the foreground service is killed.
            preferences.edit {
                field?.let {
                    putLong("start", it.time)
                }
            }
        }

    var stop: Date? = null

    lateinit var database: AppDatabase

}

/* vim:set shiftwidth=4 softtabstop=4 expandtab: */
