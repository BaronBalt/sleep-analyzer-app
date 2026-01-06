/*
 * Copyright 2023 Miklos Vajna
 *
 * SPDX-License-Identifier: MIT
 */

package hu.vmiklos.plees_tracker.ui.sleep

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.MenuItem
import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import hu.vmiklos.plees_tracker.R
import hu.vmiklos.plees_tracker.data.local.AppDatabase
import hu.vmiklos.plees_tracker.data.repository.PreferencesRepository
import hu.vmiklos.plees_tracker.data.repository.SleepRepository
import hu.vmiklos.plees_tracker.domain.model.Sleep
import hu.vmiklos.plees_tracker.ui.sleep.callback.SleepCommentCallback
import hu.vmiklos.plees_tracker.ui.sleep.callback.SleepRateCallback
import hu.vmiklos.plees_tracker.ui.util.TimeFormatter
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

/**
 * The activity is the editing UI of a single sleep.
 */
class SleepActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var viewModel: SleepViewModel
    private var sid: Int = 0

    private var sleepCommentCallback: SleepCommentCallback? = null

    private val formatter = TimeFormatter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup Repositories (Just like MainActivity)
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val preferencesRepository = PreferencesRepository(preferences)
        val database = AppDatabase.getDatabase(applicationContext)
        val sleepRepository = SleepRepository(database.sleepDao(), preferencesRepository)
        val formatter = TimeFormatter // Assuming object or singleton

        // Use a Factory!
        val factory = SleepViewModelFactory(sleepRepository, preferencesRepository, formatter)
        viewModel = ViewModelProvider(this, factory)[SleepViewModel::class.java]

        setContentView(R.layout.activity_sleep)
        // ...

        // Instead of viewModel.showSleep(this, sid), we fetch and display
        lifecycleScope.launch {
            val sleep = viewModel.getSleep(sid)
            updateUI(sleep)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.sleep_start_date -> showDatePicker(isStart = true)
            R.id.sleep_start_time -> showTimePicker(isStart = true)
            R.id.sleep_stop_date -> showDatePicker(isStart = false)
            R.id.sleep_stop_time -> showTimePicker(isStart = false)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            this.finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDatePicker(isStart: Boolean) {
        lifecycleScope.launch {
            val sleep = viewModel.getSleep(sid)
            val calendar = Calendar.getInstance().apply {
                timeInMillis = if (isStart) sleep.start else sleep.stop
            }

            DatePickerDialog(
                this@SleepActivity,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    if (isStart) sleep.start = calendar.timeInMillis
                    else sleep.stop = calendar.timeInMillis

                    // Hand the logic back to ViewModel
                    viewModel.onDateTimeChanged(sleep, applicationContext)
                    updateUI(sleep) // Refresh text views
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun showTimePicker(isStart: Boolean) {
        lifecycleScope.launch {
            // 1. Get the current sleep data from the ViewModel
            val sleep = viewModel.getSleep(sid)
            val calendar = Calendar.getInstance().apply {
                timeInMillis = if (isStart) sleep.start else sleep.stop
            }

            // 2. Open the Dialog
            TimePickerDialog(
                this@SleepActivity,
                { _, hourOfDay, minute ->
                    // Update the calendar with the picked time
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)

                    // Update the sleep object
                    if (isStart) {
                        sleep.start = calendar.timeInMillis
                    } else {
                        sleep.stop = calendar.timeInMillis
                    }

                    // 3. Tell ViewModel to validate and save
                    viewModel.onDateTimeChanged(sleep, applicationContext)

                    // 4. Refresh the UI labels
                    updateUI(sleep)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(this@SleepActivity)
            ).show()
        }
    }

    private fun updateUI(sleep: Sleep) {
        val isCompact = viewModel.isCompactView()

        findViewById<TextView>(R.id.sleep_start_date).text =
            formatter.formatDateTime(Date(sleep.start), false, isCompact)
        findViewById<TextView>(R.id.sleep_start_time).text =
            formatter.formatDateTime(Date(sleep.start), true, isCompact)
        findViewById<TextView>(R.id.sleep_stop_date).text =
            formatter.formatDateTime(Date(sleep.stop), false, isCompact)
        findViewById<TextView>(R.id.sleep_stop_time).text =
            formatter.formatDateTime(Date(sleep.stop), true, isCompact)

        // 2. Update RatingBar
        val ratingBar = findViewById<RatingBar>(R.id.sleep_item_rating)
        // Temporarily remove listener to avoid infinite loop if you have one
        ratingBar.onRatingBarChangeListener = null
        ratingBar.rating = sleep.rating.toFloat()

        // Re-attach the listener (SleepRateCallback)
        ratingBar.onRatingBarChangeListener = SleepRateCallback(viewModel, sleep)

        // 3. Update Comment (EditText)
        val commentEdit = findViewById<AppCompatEditText>(R.id.sleep_item_comment)

        // Only update text if it's actually different to avoid cursor jumps
        if (commentEdit.text.toString() != sleep.comment) {
            commentEdit.setText(sleep.comment)
        }

        // Handle the Comment Callback (TextWatcher)
        // Note: You should keep a reference to your callback in the Activity
        // to add/remove it properly as shown in your original ViewModel code.
        commentEdit.removeTextChangedListener(sleepCommentCallback)
        sleepCommentCallback = SleepCommentCallback(viewModel, sleep)
        commentEdit.addTextChangedListener(sleepCommentCallback)
    }
}

/* vim:set shiftwidth=4 softtabstop=4 expandtab: */
