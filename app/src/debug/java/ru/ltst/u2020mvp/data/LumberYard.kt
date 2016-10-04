package ru.ltst.u2020mvp.data

import android.app.Application
import android.os.AsyncTask
import android.util.Log
import okio.BufferedSink
import okio.Okio
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import ru.ltst.u2020mvp.ApplicationScope
import rx.Observable
import rx.subjects.PublishSubject
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject

@ApplicationScope
class LumberYard
@Inject constructor(private val app: Application) {

    private val entries = ArrayDeque<Entry>(BUFFER_SIZE + 1)
    private val entrySubject = PublishSubject.create<Entry>()

    fun tree(): Timber.Tree {
        return object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String, message: String, t: Throwable?) {
                addEntry(Entry(priority, tag, message))
            }
        }
    }

    @Synchronized private fun addEntry(entry: Entry) {
        entries.addLast(entry)
        if (entries.size > BUFFER_SIZE) {
            entries.removeFirst()
        }

        entrySubject.onNext(entry)
    }

    fun bufferedLogs(): List<Entry> {
        return ArrayList(entries)
    }

    fun logs(): Observable<Entry> {
        return entrySubject
    }

    /**  Save the current logs to disk.  */
    fun save(): Observable<File> {
        return Observable.create(Observable.OnSubscribe<java.io.File> { subscriber ->
            val folder = app.getExternalFilesDir(null)
            if (folder == null) {
                subscriber.onError(IOException("External storage is not mounted."))
                return@OnSubscribe
            }

            val fileName = ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())
            val output = File(folder, fileName)

            var sink: BufferedSink? = null
            try {
                sink = Okio.buffer(Okio.sink(output))
                val entries = bufferedLogs()
                for (entry in entries) {
                    sink!!.writeUtf8(entry.prettyPrint()).writeByte('\n'.toInt())
                }

                subscriber.onNext(output)
                subscriber.onCompleted()
            } catch (e: IOException) {
                subscriber.onError(e)
            } finally {
                if (sink != null) {
                    try {
                        sink.close()
                    } catch (e: IOException) {
                        subscriber.onError(e)
                    }

                }
            }
        })
    }

    /**
     * Delete all of the log files saved to disk. Be careful not to call this before any intents have
     * finished using the file reference.
     */
    fun cleanUp() {
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg folders: Void): Void? {
                val folder = app.getExternalFilesDir(null)
                if (folder != null) {
                    for (file in folder.listFiles()) {
                        if (file.name.endsWith(".log")) {
                            file.delete()
                        }
                    }
                }

                return null
            }
        }.execute()
    }

    class Entry(val level: Int, val tag: String, val message: String) {

        fun prettyPrint(): String {
            return String.format("%22s %s %s", tag, displayLevel(),
                    // Indent newlines to match the original indentation.
                    message.replace("\\n".toRegex(), "\n                         "))
        }

        fun displayLevel(): String {
            when (level) {
                Log.VERBOSE -> return "V"
                Log.DEBUG -> return "D"
                Log.INFO -> return "I"
                Log.WARN -> return "W"
                Log.ERROR -> return "E"
                Log.ASSERT -> return "A"
                else -> return "?"
            }
        }
    }

    companion object {
        private val BUFFER_SIZE = 200
    }
}
