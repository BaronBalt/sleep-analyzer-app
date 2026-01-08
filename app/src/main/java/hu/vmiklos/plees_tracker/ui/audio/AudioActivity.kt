package hu.vmiklos.plees_tracker.ui.audio

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import hu.vmiklos.plees_tracker.R
import hu.vmiklos.plees_tracker.data.local.AppDatabase
import hu.vmiklos.plees_tracker.data.repository.PreferencesRepository
import hu.vmiklos.plees_tracker.data.repository.SleepRepository
import hu.vmiklos.plees_tracker.ui.util.WindowInsetsUtil.handleWindowInsets

class AudioActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var viewModel: AudioViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val preferencesRepository = PreferencesRepository(preferences)
        val database = AppDatabase.getDatabase(applicationContext)
        val sleepRepository = SleepRepository(database.sleepDao(), preferencesRepository)

        val factory = AudioViewModelFactory(sleepRepository, preferencesRepository)
        viewModel = ViewModelProvider(this, factory)[AudioViewModel::class.java]

        setContentView(R.layout.activity_audio)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        handleWindowInsets(this)

        actionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.audio_title)

        findViewById<TextView>(R.id.sleep_status_value).text = "Great!"
    }

    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}