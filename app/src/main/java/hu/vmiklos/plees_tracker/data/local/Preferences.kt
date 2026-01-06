/*
 * Copyright 2023 Miklos Vajna
 *
 * SPDX-License-Identifier: MIT
 */

package hu.vmiklos.plees_tracker.data.local

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import hu.vmiklos.plees_tracker.R
import hu.vmiklos.plees_tracker.data.repository.PreferencesRepository

class Preferences(
    private val preferencesRepository: PreferencesRepository
) : PreferenceFragmentCompat() {
    private fun padMinute(raw: String): String {
        val fro = ":([0-9])$".toRegex()
        val to = ":0$1"
        return raw.replace(fro, to)
    }
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        val autoBackupPath = findPreference<Preference>("auto_backup_path")
        autoBackupPath?.let {
            val path = preferencesRepository.getAutoBackupPath()
            it.summary = path
        }
        val wakeup = findPreference<Preference>("wakeup")
        wakeup?.let {
            val value = preferencesRepository.getWakeup()
            if (value != null) {
                it.summary = padMinute(value)
            }
        }
        val bedtime = findPreference<Preference>("bedtime")
        bedtime?.let {
            val value = preferencesRepository.getBedTime()
            if (value != null) {
                it.summary = padMinute(value)
            }
        }
    }
}

/* vim:set shiftwidth=4 softtabstop=4 expandtab: */
