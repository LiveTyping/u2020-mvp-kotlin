package ru.ltst.u2020mvp.ui.misc

import android.content.Context
import android.util.AttributeSet
import android.view.View.MeasureSpec.EXACTLY
import android.widget.ImageView
import ru.ltst.u2020mvp.R
import timber.log.Timber

class AspectRatioImageView(context: Context, attrs: AttributeSet) : ImageView(context, attrs) {
    private var widthRatio: Float = 0.toFloat()
    private var heightRatio: Float = 0.toFloat()

    init {

        val a = context.obtainStyledAttributes(attrs, R.styleable.AspectRatioImageView)
        widthRatio = a.getFloat(R.styleable.AspectRatioImageView_widthRatio, 1f)
        heightRatio = a.getFloat(R.styleable.AspectRatioImageView_heightRatio, 1f)
        a.recycle()
    }

    fun setWidthRatio(widthRatio: Float) {
        this.widthRatio = widthRatio
    }

    fun setHeightRatio(heightRatio: Float) {
        this.heightRatio = heightRatio
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)

        if (widthMode == EXACTLY) {
            if (heightMode != EXACTLY) {
                heightSize = (widthSize * 1f / widthRatio * heightRatio).toInt()
            }
        } else if (heightMode == EXACTLY) {
            widthSize = (heightSize * 1f / heightRatio * widthRatio).toInt()
        } else {
            throw IllegalStateException("Either width or height must be EXACTLY.")
        }

        widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, EXACTLY)
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, EXACTLY)
        Timber.d("height %d for ratio %f", heightMeasureSpec, heightRatio)

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}
