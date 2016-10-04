package ru.ltst.u2020mvp.ui.bugreport

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater

import ru.ltst.u2020mvp.R

class BugReportDialog(context: Context) : AlertDialog(context) {

    private var listener: ((BugReportView.Report) -> Unit)? = null

    init {

        val view = LayoutInflater.from(context).inflate(R.layout.bugreport_view, null) as BugReportView
        view.setBugReportListener({onStateChanged(it)})

        setTitle("Report a bug")
        setView(view)
        setButton(Dialog.BUTTON_NEGATIVE, "Cancel", null as DialogInterface.OnClickListener)
        setButton(Dialog.BUTTON_POSITIVE, "Submit") { dialog, which ->
            listener?.invoke(view.report)
        }
    }

    fun setReportListener(listener: (BugReportView.Report) -> Unit) {
        this.listener = listener
    }

    override fun onStart() {
        getButton(Dialog.BUTTON_POSITIVE).isEnabled = false
    }

    fun onStateChanged(valid: Boolean) {
        getButton(Dialog.BUTTON_POSITIVE).isEnabled = valid
    }
}
