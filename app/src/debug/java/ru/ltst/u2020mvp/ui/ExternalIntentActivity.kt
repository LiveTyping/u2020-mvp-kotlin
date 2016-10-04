package ru.ltst.u2020mvp.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.style.StyleSpan
import android.view.MenuItem
import android.widget.TextView
import ru.ltst.u2020mvp.R
import ru.ltst.u2020mvp.ui.misc.Truss
import ru.ltst.u2020mvp.ui.misc.bindView
import ru.ltst.u2020mvp.util.Intents
import timber.log.Timber
import java.util.*

class ExternalIntentActivity : Activity(), Toolbar.OnMenuItemClickListener {

    internal val toolbarView: Toolbar by bindView(R.id.toolbar)
    internal val actionView: TextView by bindView(R.id.action)
    internal val dataView: TextView by bindView(R.id.data)
    internal val extrasView: TextView by bindView(R.id.extras)
    internal val flagsView: TextView by bindView(R.id.flags)

    private lateinit var baseIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.debug_external_intent_activity)

        toolbarView.inflateMenu(R.menu.debug_external_intent)
        toolbarView.setOnMenuItemClickListener(this)

        baseIntent = intent.getParcelableExtra<Intent>(EXTRA_BASE_INTENT)
        fillAction()
        fillData()
        fillExtras()
        fillFlags()
    }

    override fun onMenuItemClick(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.debug_launch -> {
                if (Intents.maybeStartActivity(this, baseIntent)) {
                    finish()
                }
                return true
            }
            else -> return false
        }
    }

    private fun fillAction() {
        val action = baseIntent.action
        actionView.text = action ?: "None!"
    }

    private fun fillData() {
        val data = baseIntent.data
        dataView.text = if (data == null) "None!" else data.toString()
    }

    @Suppress("UNCHECKED_CAST")
    private fun fillExtras() {
        val extras = baseIntent.extras
        if (extras == null) {
            extrasView.text = "None!"
        } else {
            val truss = Truss()
            for (key in extras.keySet()) {
                val value = extras.get(key)

                val valueString: String
                if (value.javaClass.isArray) {
                    valueString = Arrays.toString(value as Array<Any>)
                } else {
                    valueString = value.toString()
                }

                truss.pushSpan(StyleSpan(Typeface.BOLD))
                truss.append(key).append(":\n")
                truss.popSpan()
                truss.append(valueString).append("\n\n")
            }

            extrasView.text = truss.build()
        }
    }

    private fun fillFlags() {
        val flags = baseIntent.flags

        val builder = StringBuilder()
        for (field in Intent::class.java.declaredFields) {
            try {
                if (field.name.startsWith("FLAG_")
                        && field.type == Integer.TYPE
                        && flags and field.getInt(null) != 0) {
                    builder.append(field.name).append('\n')
                }
            } catch (e: IllegalAccessException) {
                Timber.e(e, "Couldn't read value for: %s", field.name)
            }

        }

        flagsView.text = if (builder.length == 0) "None!" else builder.toString()
    }

    companion object {
        val ACTION = "com.jakewharton.u2020.intent.EXTERNAL_INTENT"
        val EXTRA_BASE_INTENT = "debug_base_intent"

        fun createIntent(baseIntent: Intent): Intent {
            val intent = Intent()
            intent.action = ACTION
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(EXTRA_BASE_INTENT, baseIntent)
            return intent
        }
    }
}
