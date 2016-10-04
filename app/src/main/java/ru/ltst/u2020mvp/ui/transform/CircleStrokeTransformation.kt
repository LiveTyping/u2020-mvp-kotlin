package ru.ltst.u2020mvp.ui.transform

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

import com.squareup.picasso.Transformation

import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Shader.TileMode.CLAMP

class CircleStrokeTransformation(context: Context,
                                 private val strokeColor: Int,
                                 strokeWidthDp: Int) : Transformation {
    private val strokeWidth: Float
    private val strokePaint: Paint

    init {
        this.strokeWidth = strokeWidthDp * context.resources.displayMetrics.density

        strokePaint = Paint(ANTI_ALIAS_FLAG)
        strokePaint.style = Paint.Style.STROKE
        strokePaint.color = strokeColor
    }

    override fun transform(bitmap: Bitmap): Bitmap {
        val size = bitmap.width
        val rounded = Bitmap.createBitmap(size, size, ARGB_8888)
        val canvas = Canvas(rounded)

        val shader = BitmapShader(bitmap, CLAMP, CLAMP)
        val shaderPaint = Paint(ANTI_ALIAS_FLAG)
        shaderPaint.shader = shader

        val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())
        val radius = size / 2f
        canvas.drawRoundRect(rect, radius, radius, shaderPaint)

        strokePaint.strokeWidth = strokeWidth

        val strokeInset = strokeWidth / 2f
        rect.inset(strokeInset, strokeInset)
        val strokeRadius = radius - strokeInset
        canvas.drawRoundRect(rect, strokeRadius, strokeRadius, strokePaint)

        bitmap.recycle()
        return rounded
    }

    override fun key(): String {
        return "circle_stroke($strokeColor,$strokeWidth)"
    }
}
