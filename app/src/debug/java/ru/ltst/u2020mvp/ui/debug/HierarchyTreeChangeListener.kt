package ru.ltst.u2020mvp.ui.debug

import android.view.View
import android.view.ViewGroup

/**
 * A [hierarchy change listener][ViewGroup.OnHierarchyChangeListener] which recursively
 * monitors an entire tree of views.
 */
class HierarchyTreeChangeListener
    private constructor(private val delegate: ViewGroup.OnHierarchyChangeListener?) :
        ViewGroup.OnHierarchyChangeListener {

    init {
        if (delegate == null) {
            throw NullPointerException("Delegate must not be null.")
        }
    }

    override fun onChildViewAdded(parent: View, child: View) {
        delegate?.onChildViewAdded(parent, child)

        if (child is ViewGroup) {
            child.setOnHierarchyChangeListener(this)
            for (i in 0..child.childCount - 1) {
                onChildViewAdded(child, child.getChildAt(i))
            }
        }
    }

    override fun onChildViewRemoved(parent: View, child: View) {
        if (child is ViewGroup) {
            for (i in 0..child.childCount - 1) {
                onChildViewRemoved(child, child.getChildAt(i))
            }
            child.setOnHierarchyChangeListener(null)
        }

        delegate?.onChildViewRemoved(parent, child)
    }

    companion object {
        /**
         * Wrap a regular [hierarchy change listener][ViewGroup.OnHierarchyChangeListener] with one
         * that monitors an entire tree of views.
         */
        fun wrap(delegate: ViewGroup.OnHierarchyChangeListener): HierarchyTreeChangeListener {
            return HierarchyTreeChangeListener(delegate)
        }
    }
}
