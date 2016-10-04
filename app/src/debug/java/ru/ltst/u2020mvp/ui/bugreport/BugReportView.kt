package ru.ltst.u2020mvp.ui.bugreport

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import ru.ltst.u2020mvp.R
import ru.ltst.u2020mvp.ui.misc.EmptyTextWatcher
import ru.ltst.u2020mvp.ui.misc.bindView

class BugReportView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    internal val titleView: EditText by bindView(R.id.title)
    internal val descriptionView: EditText by bindView(R.id.description)
    internal val screenshotView: CheckBox by bindView(R.id.screenshot)
    internal val logsView: CheckBox by bindView(R.id.logs)

    private var listener: ((Boolean) -> Unit)? = null

    override fun onFinishInflate() {
        super.onFinishInflate()

        titleView.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                titleView.error = if (titleView.text.isNullOrBlank()) "Cannot be empty." else null
            }
        }
        titleView.addTextChangedListener(object : EmptyTextWatcher() {
            override fun afterTextChanged(s: Editable) {
                listener?.invoke(!s.isNullOrBlank())
            }
        })

        screenshotView.isChecked = true
        logsView.isChecked = true
    }

    fun setBugReportListener(listener: (Boolean) -> Unit) {
        this.listener = listener
    }

    val report: Report
        get() = Report(titleView.text.toString(),
                descriptionView.text.toString(), screenshotView.isChecked,
                logsView.isChecked)

    class Report(val title: String, val description: String, val includeScreenshot: Boolean,
                 val includeLogs: Boolean)
}
