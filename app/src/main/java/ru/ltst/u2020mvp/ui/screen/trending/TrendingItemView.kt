package ru.ltst.u2020mvp.ui.screen.trending

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.RelativeLayout
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.trending_view_repository.view.*
import ru.ltst.u2020mvp.R
import ru.ltst.u2020mvp.data.api.model.Repository
import ru.ltst.u2020mvp.ui.misc.Truss
import ru.ltst.u2020mvp.ui.transform.CircleStrokeTransformation

class TrendingItemView(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {
    private val avatarTransformation: CircleStrokeTransformation
    private val descriptionColor: Int

    init {

        val outValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.textColorSecondary, outValue, true)
        descriptionColor = outValue.data

        // TODO: Make this a singleton.
        avatarTransformation = CircleStrokeTransformation(context,
                ContextCompat.getColor(context, R.color.avatar_stroke), 1)
    }

    fun bindTo(repository: Repository, picasso: Picasso) {
        picasso.load(repository.owner.avatar_url)
                .placeholder(R.drawable.avatar)
                .fit()
                .transform(avatarTransformation)
                .into(trending_repository_avatar)
        trending_repository_name.text = repository.name
        trending_repository_stars.text = repository.watchers.toString()
        trending_repository_forks.text = repository.forks.toString()

        val description = Truss()
        description.append(repository.owner.login)

        if (!repository.description.isNullOrEmpty()) {
            description.pushSpan(ForegroundColorSpan(descriptionColor))
            description.append(" — ")
            description.append(repository.description!!)
            description.popSpan()
        }

        trending_repository_description.text = description.build()
    }
}
