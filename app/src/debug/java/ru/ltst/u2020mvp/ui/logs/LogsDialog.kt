package ru.ltst.u2020mvp.ui.logs

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.widget.ListView
import android.widget.Toast

import java.io.File

import ru.ltst.u2020mvp.data.LumberYard
import ru.ltst.u2020mvp.util.Intents
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class LogsDialog(context: Context, private val lumberYard: LumberYard) : AlertDialog(context) {
    private val adapter: LogAdapter

    private var subscriptions: CompositeSubscription? = null

    init {

        adapter = LogAdapter(context)

        val listView = ListView(context)
        listView.transcriptMode = ListView.TRANSCRIPT_MODE_NORMAL
        listView.adapter = adapter

        setTitle("Logs")
        setView(listView)
        setButton(DialogInterface.BUTTON_NEGATIVE, "Close") { dialog, which ->
            // NO-OP.
        }
        setButton(DialogInterface.BUTTON_POSITIVE, "Share") { dialog, which -> share() }
    }

    override fun onStart() {
        super.onStart()

        adapter.setLogs(lumberYard.bufferedLogs())

        subscriptions = CompositeSubscription()
        subscriptions!!.add(lumberYard.logs() //
                .observeOn(AndroidSchedulers.mainThread()) //
                .subscribe(adapter))
    }

    override fun onStop() {
        super.onStop()
        subscriptions!!.unsubscribe()
    }

    private fun share() {
        lumberYard.save() //
                .subscribeOn(Schedulers.io()) //
                .observeOn(AndroidSchedulers.mainThread()) //
                .subscribe(object : Subscriber<File>() {
                    override fun onCompleted() {
                        // NO-OP.
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(context, "Couldn't save the logs for sharing.", Toast.LENGTH_SHORT).show()
                    }

                    override fun onNext(file: File) {
                        val sendIntent = Intent(Intent.ACTION_SEND)
                        sendIntent.type = "text/plain"
                        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                        Intents.maybeStartChooser(context, sendIntent)
                    }
                })
    }
}
