/*
 * Copyright 2023 Miklos Vajna
 *
 * SPDX-License-Identifier: MIT
 */

package hu.vmiklos.plees_tracker.data.local

import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import hu.vmiklos.plees_tracker.R
import hu.vmiklos.plees_tracker.ui.preferences.PreferencesActivity

class SharedPreferencesChangeListener(
    private val activity: Activity
) : SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            "auto_backup" -> {
                val autoBackup = sharedPreferences.getBoolean("auto_backup", false)
                val autoBackupPath = sharedPreferences.getString("auto_backup_path", "")
                if (autoBackup) {
                    if (autoBackupPath.isNullOrEmpty()) {
                        if (activity is PreferencesActivity) {
                            activity.openFolderChooser()
                        }
                    }
                } else {
                    sharedPreferences.edit {
                        remove("auto_backup_path")
                    }
                }
            }
            "daily_reminder" -> {
                if (sharedPreferences.getBoolean("daily_reminder", false)) {
                    if (activity is PreferencesActivity) {
                        activity.showBedtimeDialog()
                    }
                }
            }
            "enable_dnd" -> {
                handleDndPermission(sharedPreferences)
            }
        }
        applyTheme(sharedPreferences)
    }

    private fun handleDndPermission(sharedPreferences: SharedPreferences) {
        if (!sharedPreferences.getBoolean("enable_dnd", false)) return

        val dndManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!dndManager.isNotificationPolicyAccessGranted) {
            AlertDialog.Builder(activity)
                .setTitle(R.string.settings_enable_dnd_q_title)
                .setMessage(R.string.settings_enable_dnd_q_message)
                .setPositiveButton(R.string.settings_enable_dnd_q_ok) { _, _ ->
                    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                    activity.startActivity(intent)
                }
                .setNegativeButton(R.string.settings_enable_dnd_q_cancel) { _, _ ->
                    // Optionally uncheck the preference if they cancel
                    sharedPreferences.edit { putBoolean("enable_dnd", false) }
                }
                .show()
        }
    }

    fun applyTheme(sharedPreferences: SharedPreferences) {
        val themeFollowSystem = sharedPreferences.getBoolean("follow_system_theme", true)
        val darkTheme = sharedPreferences.getBoolean("dark_mode", false)
        val mode = when {
            themeFollowSystem -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            darkTheme -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}

/* vim:set shiftwidth=4 softtabstop=4 expandtab: */
