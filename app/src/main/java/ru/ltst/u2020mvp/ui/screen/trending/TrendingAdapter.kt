package ru.ltst.u2020mvp.ui.screen.trending

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.squareup.picasso.Picasso

import java.util.Collections

import ru.ltst.u2020mvp.R
import ru.ltst.u2020mvp.data.api.model.Repository
import rx.functions.Action1

class TrendingAdapter(private val picasso: Picasso,
                      private val repositoryClickListener: (Repository) -> Unit)
    : RecyclerView.Adapter<TrendingAdapter.ViewHolder>(),
        Action1<List<Repository>> {

    private var repositories = emptyList<Repository>()

    init {
        setHasStableIds(true)
    }

    override fun call(repositories: List<Repository>) {
        this.repositories = repositories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.trending_view_repository, viewGroup, false) as TrendingItemView

        return ViewHolder(view, repositoryClickListener)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.bindTo(repositories[i])
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return repositories.size
    }

    inner class ViewHolder(itemView: TrendingItemView,
                           repositoryClickListener: (Repository) -> Unit)
    : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener({
                val repository = repositories[adapterPosition]
                repositoryClickListener(repository)
            })
        }

        fun bindTo(repository: Repository) {
            (itemView as TrendingItemView).bindTo(repository, picasso)
        }
    }
}
