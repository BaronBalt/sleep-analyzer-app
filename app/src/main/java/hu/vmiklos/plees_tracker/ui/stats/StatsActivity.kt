package hu.vmiklos.plees_tracker.ui.stats

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import hu.vmiklos.plees_tracker.R
import hu.vmiklos.plees_tracker.data.local.AppDatabase
import hu.vmiklos.plees_tracker.data.repository.PreferencesRepository
import hu.vmiklos.plees_tracker.data.repository.SleepRepository
import hu.vmiklos.plees_tracker.domain.usecase.StatsUseCases
import hu.vmiklos.plees_tracker.ui.util.TimeFormatter
import hu.vmiklos.plees_tracker.ui.util.WindowInsetsUtil

class StatsActivity : AppCompatActivity() {

    private lateinit var viewModel: StatsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        val sleepDao = AppDatabase.getDatabase(applicationContext).sleepDao()

        val preferencesRepository = PreferencesRepository(preferences = prefs)
        val sleepRepository = SleepRepository(
            sleepDao = sleepDao,
            preferencesRepository = preferencesRepository
        )

        val statsUseCases = StatsUseCases()
        val formatter = TimeFormatter

        val factory = StatsViewModelFactory(
            sleepRepository,
            preferencesRepository,
            statsUseCases,
            formatter
        )
        viewModel = ViewModelProvider(this, factory)[StatsViewModel::class.java]

        WindowInsetsUtil.applyEdgeToEdge(this)

        title = getString(R.string.stats)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.stats.observe(this) { buckets ->
            populateStatsBucket(
                R.id.last_week_sleeps,
                R.id.last_week_average,
                R.id.last_week_daily,
                buckets.lastWeek
            )
            populateStatsBucket(
                R.id.last_two_week_sleeps,
                R.id.last_two_week_average,
                R.id.last_two_week_daily,
                buckets.lastTwoWeeks
            )
            populateStatsBucket(
                R.id.last_month_sleeps,
                R.id.last_month_average,
                R.id.last_month_daily,
                buckets.lastMonth
            )
            populateStatsBucket(
                R.id.last_year_sleeps,
                R.id.last_year_average,
                R.id.last_year_daily,
                buckets.lastYear
            )
            populateStatsBucket(
                R.id.all_time_sleeps,
                R.id.all_time_average,
                R.id.all_time_daily,
                buckets.allTime
            )
        }
    }

    private fun populateStatsBucket(
        sleepsId: Int,
        averageId: Int,
        dailyId: Int,
        uiModel: StatsUiModel
    ) {
        findViewById<TextView>(sleepsId).text = uiModel.sleepCount
        findViewById<TextView>(averageId).text = uiModel.averageDuration
        findViewById<TextView>(dailyId).text = uiModel.dailyDuration
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
