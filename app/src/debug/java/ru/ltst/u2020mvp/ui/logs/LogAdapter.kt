package ru.ltst.u2020mvp.ui.logs

import android.content.Context
import android.support.annotation.DrawableRes
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ru.ltst.u2020mvp.R
import ru.ltst.u2020mvp.data.LumberYard.Entry
import ru.ltst.u2020mvp.ui.misc.BindableAdapter
import ru.ltst.u2020mvp.ui.misc.KotterKnife
import ru.ltst.u2020mvp.ui.misc.bindView
import rx.functions.Action1
import java.util.*


internal class LogAdapter(context: Context) : BindableAdapter<Entry>(context), Action1<Entry> {
    private var logs: MutableList<Entry>

    init {
        logs = mutableListOf<Entry>()
    }

    fun setLogs(logs: List<Entry>) {
        this.logs = ArrayList(logs)
    }

    override fun call(entry: Entry) {
        logs.add(entry)
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return logs.size
    }

    override fun getItem(position: Int): Entry? {
        return logs[position]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun newView(inflater: LayoutInflater, position: Int, container: ViewGroup): View? {
        val view = inflater.inflate(R.layout.debug_logs_list_item, container, false)
        val viewHolder = LogItemViewHolder(view)
        view.tag = viewHolder
        return view
    }

    override fun bindView(item: Entry?, position: Int, view: View) {
        val viewHolder = view.tag as LogItemViewHolder
        viewHolder.setEntry(item!!)
    }

    internal class LogItemViewHolder(override val rootView: View) : KotterKnife {
        val levelView: TextView by bindView(R.id.debug_log_level)
        val tagView: TextView by bindView(R.id.debug_log_tag)
        val messageView: TextView by bindView(R.id.debug_log_message)

        fun setEntry(entry: Entry) {
            rootView.setBackgroundResource(backgroundForLevel(entry.level))
            levelView.text = entry.displayLevel()
            tagView.text = entry.tag
            messageView.text = entry.message
        }
    }

    companion object {

        @DrawableRes
        fun backgroundForLevel(level: Int): Int {
            when (level) {
                Log.VERBOSE, Log.DEBUG -> return R.color.debug_log_accent_debug
                Log.INFO -> return R.color.debug_log_accent_info
                Log.WARN -> return R.color.debug_log_accent_warn
                Log.ERROR, Log.ASSERT -> return R.color.debug_log_accent_error
                else -> return R.color.debug_log_accent_unknown
            }
        }
    }
}
