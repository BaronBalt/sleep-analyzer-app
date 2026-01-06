package hu.vmiklos.plees_tracker.domain.usecase

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import hu.vmiklos.plees_tracker.data.calendar.CalendarExport
import hu.vmiklos.plees_tracker.data.calendar.CalendarImport
import hu.vmiklos.plees_tracker.data.repository.PreferencesRepository
import hu.vmiklos.plees_tracker.data.repository.SleepRepository
import hu.vmiklos.plees_tracker.domain.model.Sleep
import hu.vmiklos.plees_tracker.ui.util.TimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.util.Date

class ImportExportUseCases(
    private val sleepRepository: SleepRepository,
    private val preferencesRepository: PreferencesRepository
) {
    companion object {
        private const val TAG = "ImportExportUseCases"
    }

    suspend fun importCsv(cr: ContentResolver, uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        withContext(Dispatchers.IO) {
            val inputStream = cr.openInputStream(uri)
            val records: Iterable<CSVRecord> =
                CSVFormat.DEFAULT.parse(InputStreamReader(inputStream))
            // We have a speed vs memory usage trade-off here. Pay the cost of keeping all sleeps in
            // memory: the benefit is that inserting all of them once triggers a single notification of
            // observers. This means that importing 100s of sleeps is still ~instant, while it used to
            // take ~forever.
            val importedSleeps = mutableListOf<Sleep>()
            try {
                var first = true
                for (cells in records) {
                    if (first) {
                        // Ignore the header.
                        first = false
                        continue
                    }
                    val sleep = Sleep()
                    sleep.start = cells[1].toLong()
                    sleep.stop = cells[2].toLong()
                    if (cells.isSet(3)) {
                        sleep.rating = cells[3].toLong()
                    }
                    if (cells.isSet(4)) {
                        sleep.comment = cells[4]
                    }
                    importedSleeps.add(sleep)
                }
                val oldSleeps = sleepRepository.getAllSleeps()
                val newSleeps = importedSleeps.subtract(oldSleeps.toSet()).toList()
                sleepRepository.insertSleeps(newSleeps)
                Result.success(newSleeps.size)
            } catch (e: Exception) {
                Log.e(TAG, "importCsv, failed: $e")
                Result.failure(e)
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close()
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    suspend fun exportToFile(cr: ContentResolver, uri: Uri, formatter: TimeFormatter): Result<Unit> {
        val prettyBackup = preferencesRepository.prettyBackup()
        val sleeps = sleepRepository.getAllSleeps()

        try {
            cr.takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        } catch (e: SecurityException) {
            Log.e(TAG, "exportData: takePersistableUriPermission() failed for write")
            // We can continue here, but often this might lead to os == null below
        }

        var os: OutputStream? = null
        try {
            os = cr.openOutputStream(uri) ?: return Result.failure(IOException("Failed to open output stream"))

            val writer = CSVPrinter(OutputStreamWriter(os, "UTF-8"), CSVFormat.DEFAULT)

            if (prettyBackup) {
                writer.printRecord("sid", "start", "stop", "length", "rating", "comment")
            } else {
                writer.printRecord("sid", "start", "stop", "rating", "comment")
            }
            for (sleep in sleeps) {
                if (prettyBackup) {
                    val durationMS = sleep.stop - sleep.start

                    writer.printRecord(
                        sleep.sid,
                        formatter.formatTimestamp(Date(sleep.start), preferencesRepository.compactView()),
                        formatter.formatTimestamp(Date(sleep.stop), preferencesRepository.compactView()),
                        formatter.formatDuration(durationMS / 1000, preferencesRepository.compactView()),
                        sleep.rating,
                        sleep.comment
                    )
                } else {
                    writer.printRecord(
                        sleep.sid,
                        sleep.start,
                        sleep.stop,
                        sleep.rating,
                        sleep.comment
                    )
                }
            }

            writer.close()
            return Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "exportDataToFile, failed: $e")
            return Result.failure(e)
        } finally {
            try {
                os?.close()
            } catch (_: Exception) { }
        }
    }

    suspend fun importFromCalendar(context: Context, calendarId: String) : Result<Int> {
        // Query the calendar for events
        val importedSleeps = CalendarImport.queryForEvents(
            context, calendarId
        ).map(CalendarImport::mapEventToSleep)
        val oldSleeps = sleepRepository.getAllSleeps()
        val newSleeps = importedSleeps.subtract(oldSleeps.toSet())

        // Insert the list of Sleep into DB
        try {
            sleepRepository.insertSleeps(newSleeps.toList())
            return Result.success(newSleeps.size)
        } catch (e: Exception) {
            Log.e(TAG, "importFromCalender, failed $e")
            return Result.failure(e)
        }
    }

    suspend fun exportToCalendar(context: Context, calendarId: String) : Result<Unit> {
        val calendarSleeps = CalendarImport.queryForEvents(
            context, calendarId
        ).map(CalendarImport::mapEventToSleep)
        val sleeps = sleepRepository.getAllSleeps()
        val exportedSleeps = sleeps.subtract(calendarSleeps.toSet())

        try {
            CalendarExport.exportSleep(context, calendarId, exportedSleeps.toList())
            return Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "exportToCalender, failed $e")
            return Result.failure(e)
        }
    }

    suspend fun performAutoBackup(
        context: Context,
        cr: ContentResolver,
        formatter: TimeFormatter
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val autoBackup = preferencesRepository.isAutoBackupEnabled()
        val autoBackupPath = preferencesRepository.getAutoBackupPath()

        if (!autoBackup || autoBackupPath.isNullOrEmpty()) {
            return@withContext Result.success(Unit)
        }

        try {
            val folder = DocumentFile.fromTreeUri(context, autoBackupPath.toUri())
                ?: return@withContext Result.failure(Exception("Could not resolve backup folder"))

            // Clean up old backup to prevent "backup (1).csv"
            val oldBackup = folder.findFile("backup.csv")
            if (oldBackup != null && oldBackup.exists()) {
                oldBackup.delete()
            }

            val backup = folder.createFile("text/csv", "backup.csv")
                ?: return@withContext Result.failure(Exception("Could not create backup file"))

            // Call your existing export function
            return@withContext exportToFile(cr, backup.uri, formatter)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}