package ru.ltst.u2020mvp.ui.bugreport

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.DisplayMetrics
import android.widget.Toast
import com.mattprecious.telescope.Lens
import ru.ltst.u2020mvp.BuildConfig
import ru.ltst.u2020mvp.data.LumberYard
import ru.ltst.u2020mvp.util.Intents
import ru.ltst.u2020mvp.util.Strings
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.util.*

/**
 * Pops a dialog asking for more information about the bug report and then creates an email with a
 * JIRA-formatted body.
 */
class BugReportLens(private val context: Context, private val lumberYard: LumberYard) :
        Lens() {

    private var screenshot: File? = null

    override fun onCapture(screenshot: File?) {
        this.screenshot = screenshot

        val dialog = BugReportDialog(context)
        dialog.setReportListener({onBugReportSubmit(it)})
        dialog.show()
    }

    fun onBugReportSubmit(report: BugReportView.Report) {
        if (report.includeLogs) {
            lumberYard.save().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(object : Subscriber<File>() {
                override fun onCompleted() {
                    // NO-OP.
                }

                override fun onError(e: Throwable) {
                    Toast.makeText(context, "Couldn't attach the logs.", Toast.LENGTH_SHORT).show()
                    submitReport(report, null)
                }

                override fun onNext(logs: File) {
                    submitReport(report, logs)
                }
            })
        } else {
            submitReport(report, null)
        }
    }

    private fun submitReport(report: BugReportView.Report, logs: File?) {
        val dm = context.resources.displayMetrics
        val densityBucket = getDensityString(dm)

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.type = "message/rfc822"
        // TODO: intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "u2020-bugs@blackhole.io" });
        intent.putExtra(Intent.EXTRA_SUBJECT, report.title)

        val body = StringBuilder()
        if (!report.description.isNullOrBlank()) {
            body.append("{panel:title=Description}\n").append(report.description).append("\n{panel}\n\n")
        }

        body.append("{panel:title=App}\n")
        body.append("Version: ").append(BuildConfig.VERSION_NAME).append('\n')
        body.append("Version code: ").append(BuildConfig.VERSION_CODE).append('\n')
        body.append("{panel}\n\n")

        body.append("{panel:title=Device}\n")
        body.append("Make: ").append(Build.MANUFACTURER).append('\n')
        body.append("Model: ").append(Build.MODEL).append('\n')
        body.append("Resolution: ").append(dm.heightPixels).append("x").append(dm.widthPixels).append('\n')
        body.append("Density: ").append(dm.densityDpi).append("dpi (").append(densityBucket).append(")\n")
        body.append("Release: ").append(Build.VERSION.RELEASE).append('\n')
        body.append("API: ").append(Build.VERSION.SDK_INT).append('\n')
        body.append("{panel}")

        intent.putExtra(Intent.EXTRA_TEXT, body.toString())

        val attachments = ArrayList<Uri>()
        if (screenshot != null && report.includeScreenshot) {
            attachments.add(Uri.fromFile(screenshot))
        }
        if (logs != null) {
            attachments.add(Uri.fromFile(logs))
        }

        if (!attachments.isEmpty()) {
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachments)
        }

        Intents.maybeStartActivity(context, intent)
    }

    private fun getDensityString(displayMetrics: DisplayMetrics): String {
        when (displayMetrics.densityDpi) {
            DisplayMetrics.DENSITY_LOW -> return "ldpi"
            DisplayMetrics.DENSITY_MEDIUM -> return "mdpi"
            DisplayMetrics.DENSITY_HIGH -> return "hdpi"
            DisplayMetrics.DENSITY_XHIGH -> return "xhdpi"
            DisplayMetrics.DENSITY_XXHIGH -> return "xxhdpi"
            DisplayMetrics.DENSITY_XXXHIGH -> return "xxxhdpi"
            DisplayMetrics.DENSITY_TV -> return "tvdpi"
            else -> return displayMetrics.densityDpi.toString()
        }
    }
}
