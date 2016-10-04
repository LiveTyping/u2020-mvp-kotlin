package ru.ltst.u2020mvp.ui.misc

import android.content.Context
import android.util.AttributeSet
import android.widget.ViewAnimator

class BetterViewAnimator(context: Context, attrs: AttributeSet) : ViewAnimator(context, attrs) {

    var displayedChildId: Int
        get() = getChildAt(displayedChild).id
        set(id) {
            if (displayedChildId == id) {
                return
            }
            var i = 0
            val count = childCount
            while (i < count) {
                if (getChildAt(i).id == id) {
                    displayedChild = i
                    return
                }
                i++
            }
            throw IllegalArgumentException("No view with ID " + id)
        }
}
