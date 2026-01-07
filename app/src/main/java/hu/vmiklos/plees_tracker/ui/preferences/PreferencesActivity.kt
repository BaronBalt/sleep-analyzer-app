package hu.vmiklos.plees_tracker.ui.preferences

import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import hu.vmiklos.plees_tracker.data.local.Preferences
import hu.vmiklos.plees_tracker.R
import hu.vmiklos.plees_tracker.data.local.SharedPreferencesChangeListener
import hu.vmiklos.plees_tracker.data.repository.PreferencesRepository
import hu.vmiklos.plees_tracker.ui.util.WindowInsetsUtil.handleWindowInsets

class PreferencesActivity: AppCompatActivity() {
    private lateinit var listener: SharedPreferencesChangeListener

    private lateinit var preferences: SharedPreferences
    private lateinit var preferencesRepository: PreferencesRepository

    companion object {
        private const val TAG = "PreferencesActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        preferencesRepository = PreferencesRepository(preferences)

        listener = SharedPreferencesChangeListener(this)
        preferences.registerOnSharedPreferenceChangeListener(listener)

        setContentView(R.layout.activity_settings)
        title = String.format(getString(R.string.settings))
        handleWindowInsets(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.root, Preferences(preferencesRepository))
                .commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private val backupActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            var success = false
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            try {
                result.data?.data?.let { uri ->
                    contentResolver.takePersistableUriPermission(
                        uri,
                        flags
                    )
                    preferences.edit {
                        putString("auto_backup_path", uri.toString())
                    }
                    success = true
                }

                if (!success) {
                    // Disable the bool setting when the user picked no folder.
                    preferences.edit {
                        putBoolean("auto_backup", false)
                    }
                }

                // Refresh the view in case auto_backup or auto_backup_path changed.
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.root, Preferences(preferencesRepository))
                    .commit()
            } catch (e: Exception) {
                Log.e(TAG, "onActivityResult, setting backup path failed: $e")
            }
        }

    fun openFolderChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        backupActivityResult.launch(intent)
    }

    // Show a dialog to set bedtime and wakeup times (using two TimePickerDialogs sequentially)
    fun showBedtimeDialog() {
        // Get current values or use defaults (22:00 for bedtime, 07:00 for wakeup)
        val currentBedHour = preferencesRepository.getBedtimeHour()
        val currentBedMinute = preferencesRepository.getBedtimeMinute()
        val currentWakeHour = preferencesRepository.getWakeupHour()
        val currentWakeMinute = preferencesRepository.getWakeupMinute()

        // First, pick bedtime
        TimePickerDialog(
            this,
            { _, bedHour, bedMinute ->
                // Save bedtime values
                val editor = preferences.edit()
                editor.putString("bedtime", "$bedHour:$bedMinute")
                editor.apply()
                // Then, pick wakeup time
                TimePickerDialog(
                    this,
                    { _, wakeHour, wakeMinute ->
                        // Save wakeup values
                        editor.putString("wakeup", "$wakeHour:$wakeMinute")
                        editor.apply()
                        Toast.makeText(
                            this,
                            getString(R.string.bedtime_toast),
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    currentWakeHour,
                    currentWakeMinute,
                    true
                ).show()
            },
            currentBedHour,
            currentBedMinute,
            true
        ).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}