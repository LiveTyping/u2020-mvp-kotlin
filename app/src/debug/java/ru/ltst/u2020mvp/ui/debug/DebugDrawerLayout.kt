/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package ru.ltst.u2020mvp.ui.debug

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.os.SystemClock
import android.support.annotation.DrawableRes
import android.support.annotation.IntDef
import android.support.v4.content.ContextCompat
import android.support.v4.view.*
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.view.*
import android.view.accessibility.AccessibilityEvent

/** This is a fork of DrawerLayout which propagates insets without applying them.  */
class DebugDrawerLayout @JvmOverloads constructor(context: Context,
                                                  attrs: AttributeSet? = null,
                                                  defStyle: Int = 0) :
        ViewGroup(context, attrs, defStyle), DrawerLayoutImpl {

    @IntDef(STATE_IDLE.toLong(), STATE_DRAGGING.toLong(), STATE_SETTLING.toLong())
    @Retention(AnnotationRetention.SOURCE)
    private annotation class State

    /** @hide
     */
    @IntDef(LOCK_MODE_UNLOCKED.toLong(), LOCK_MODE_LOCKED_CLOSED.toLong(), LOCK_MODE_LOCKED_OPEN.toLong())
    @Retention(AnnotationRetention.SOURCE)
    private annotation class LockMode

    /** @hide
     */
    @IntDef(Gravity.LEFT.toLong(), Gravity.RIGHT.toLong(), GravityCompat.START.toLong(), GravityCompat.END.toLong())
    @Retention(AnnotationRetention.SOURCE)
    private annotation class EdgeGravity

    private val mChildAccessibilityDelegate = ChildAccessibilityDelegate()

    private val mMinDrawerMargin: Int

    private var mScrimColor = DEFAULT_SCRIM_COLOR
    private var mScrimOpacity: Float = 0.toFloat()
    private val mScrimPaint = Paint()

    private val mLeftDragger: ViewDragHelper
    private val mRightDragger: ViewDragHelper
    private val mLeftCallback: ViewDragCallback
    private val mRightCallback: ViewDragCallback
    private var mDrawerState: Int = 0
    private var mInLayout: Boolean = false
    private var mFirstLayout = true
    private var mLockModeLeft: Int = 0
    private var mLockModeRight: Int = 0
    private var mDisallowInterceptRequested: Boolean = false
    private var mChildrenCanceledTouch: Boolean = false

    private var mListener: DrawerListener? = null

    private var mInitialMotionX: Float = 0.toFloat()
    private var mInitialMotionY: Float = 0.toFloat()

    private var mShadowLeft: Drawable? = null
    private var mShadowRight: Drawable? = null
    /**
     * Gets the drawable used to draw in the insets area for the status bar.

     * @return The status bar background drawable, or null if none set
     */
    var statusBarBackgroundDrawable: Drawable? = null
        private set

    private var mTitleLeft: CharSequence? = null
    private var mTitleRight: CharSequence? = null

    private var mLastInsets: Any? = null
    private var mDrawStatusBarBackground: Boolean = false

    /**
     * Listener for monitoring events about drawers.
     */
    interface DrawerListener {
        /**
         * Called when a drawer's position changes.
         * @param drawerView The child view that was moved
         * *
         * @param slideOffset The new offset of this drawer within its range, from 0-1
         */
        fun onDrawerSlide(drawerView: View, slideOffset: Float)

        /**
         * Called when a drawer has settled in a completely open state.
         * The drawer is interactive at this point.

         * @param drawerView Drawer view that is now open
         */
        fun onDrawerOpened(drawerView: View)

        /**
         * Called when a drawer has settled in a completely closed state.

         * @param drawerView Drawer view that is now closed
         */
        fun onDrawerClosed(drawerView: View)

        /**
         * Called when the drawer motion state changes. The new state will
         * be one of [.STATE_IDLE], [.STATE_DRAGGING] or [.STATE_SETTLING].

         * @param newState The new drawer motion state
         */
        fun onDrawerStateChanged(@State newState: Int)
    }

    /**
     * Stub/no-op implementations of all methods of [DrawerListener].
     * Override this if you only care about a few of the available callback methods.
     */
    abstract class SimpleDrawerListener : DrawerListener {
        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        }

        override fun onDrawerOpened(drawerView: View) {
        }

        override fun onDrawerClosed(drawerView: View) {
        }

        override fun onDrawerStateChanged(newState: Int) {
        }
    }

    internal interface DrawerLayoutCompatImpl {
        fun configureApplyInsets(drawerLayout: View)
        fun dispatchChildInsets(child: View, insets: Any, drawerGravity: Int)
        fun applyMarginInsets(lp: ViewGroup.MarginLayoutParams, insets: Any, drawerGravity: Int)
        fun getTopInset(lastInsets: Any): Int
        fun getDefaultStatusBarBackground(context: Context): Drawable?
    }

    internal class DrawerLayoutCompatImplBase : DrawerLayoutCompatImpl {
        override fun configureApplyInsets(drawerLayout: View) {
            // This space for rent
        }

        override fun dispatchChildInsets(child: View, insets: Any, drawerGravity: Int) {
            // This space for rent
        }

        override fun applyMarginInsets(lp: ViewGroup.MarginLayoutParams, insets: Any, drawerGravity: Int) {
            // This space for rent
        }

        override fun getTopInset(insets: Any): Int {
            return 0
        }

        override fun getDefaultStatusBarBackground(context: Context): Drawable? {
            return null
        }
    }

    internal class DrawerLayoutCompatImplApi21 : DrawerLayoutCompatImpl {
        override fun configureApplyInsets(drawerLayout: View) {
            DrawerLayoutCompatApi21.configureApplyInsets(drawerLayout)
        }

        override fun dispatchChildInsets(child: View, insets: Any, drawerGravity: Int) {
            DrawerLayoutCompatApi21.dispatchChildInsets(child, insets, drawerGravity)
        }

        override fun applyMarginInsets(lp: ViewGroup.MarginLayoutParams, insets: Any, drawerGravity: Int) {
            DrawerLayoutCompatApi21.applyMarginInsets(lp, insets, drawerGravity)
        }

        override fun getTopInset(insets: Any): Int {
            return DrawerLayoutCompatApi21.getTopInset(insets)
        }

        override fun getDefaultStatusBarBackground(context: Context): Drawable {
            return DrawerLayoutCompatApi21.getDefaultStatusBarBackground(context)
        }
    }

    init {
        descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        val density = resources.displayMetrics.density
        mMinDrawerMargin = (MIN_DRAWER_MARGIN * density + 0.5f).toInt()
        val minVel = MIN_FLING_VELOCITY * density

        mLeftCallback = ViewDragCallback(Gravity.LEFT)
        mRightCallback = ViewDragCallback(Gravity.RIGHT)

        mLeftDragger = ViewDragHelper.create(this, TOUCH_SLOP_SENSITIVITY, mLeftCallback)
        mLeftDragger.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT)
        mLeftDragger.minVelocity = minVel
        mLeftCallback.setDragger(mLeftDragger)

        mRightDragger = ViewDragHelper.create(this, TOUCH_SLOP_SENSITIVITY, mRightCallback)
        mRightDragger.setEdgeTrackingEnabled(ViewDragHelper.EDGE_RIGHT)
        mRightDragger.minVelocity = minVel
        mRightCallback.setDragger(mRightDragger)

        // So that we can catch the back button
        isFocusableInTouchMode = true

        ViewCompat.setImportantForAccessibility(this,
                ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES)

        ViewCompat.setAccessibilityDelegate(this, AccessibilityDelegate())
        ViewGroupCompat.setMotionEventSplittingEnabled(this, false)
        if (ViewCompat.getFitsSystemWindows(this)) {
            IMPL.configureApplyInsets(this)
            statusBarBackgroundDrawable = IMPL.getDefaultStatusBarBackground(context)
        }
    }

    /**
     * @hide Internal use only; called to apply window insets when configured
     * * with fitsSystemWindows="true"
     */
    override fun setChildInsets(insets: Any, draw: Boolean) {
        mLastInsets = insets
        mDrawStatusBarBackground = draw
        //setWillNotDraw(!draw && getBackground() == null);
        requestLayout()
    }

    /**
     * Set a simple drawable used for the left or right shadow.
     * The drawable provided must have a nonzero intrinsic width.

     * @param shadowDrawable Shadow drawable to use at the edge of a drawer
     * *
     * @param gravity Which drawer the shadow should apply to
     */
    fun setDrawerShadow(shadowDrawable: Drawable, @EdgeGravity gravity: Int) {
        /*
         * TODO Someone someday might want to set more complex drawables here.
         * They're probably nuts, but we might want to consider registering callbacks,
         * setting states, etc. properly.
         */

        val absGravity = GravityCompat.getAbsoluteGravity(gravity,
                ViewCompat.getLayoutDirection(this))
        if (absGravity and Gravity.LEFT == Gravity.LEFT) {
            mShadowLeft = shadowDrawable
            invalidate()
        }
        if (absGravity and Gravity.RIGHT == Gravity.RIGHT) {
            mShadowRight = shadowDrawable
            invalidate()
        }
    }

    /**
     * Set a simple drawable used for the left or right shadow.
     * The drawable provided must have a nonzero intrinsic width.

     * @param resId Resource id of a shadow drawable to use at the edge of a drawer
     * *
     * @param gravity Which drawer the shadow should apply to
     */
    fun setDrawerShadow(@DrawableRes resId: Int, @EdgeGravity gravity: Int) {
        setDrawerShadow(resources.getDrawable(resId), gravity)
    }

    /**
     * Set a color to use for the scrim that obscures primary content while a drawer is open.

     * @param color Color to use in 0xAARRGGBB format.
     */
    fun setScrimColor(color: Int) {
        mScrimColor = color
        invalidate()
    }

    /**
     * Set a listener to be notified of drawer events.

     * @param listener Listener to notify when drawer events occur
     * *
     * @see DrawerListener
     */
    fun setDrawerListener(listener: DrawerListener) {
        mListener = listener
    }

    /**
     * Enable or disable interaction with all drawers.

     *
     * This allows the application to restrict the user's ability to open or close
     * any drawer within this layout. DrawerLayout will still respond to calls to
     * [.openDrawer], [.closeDrawer] and friends if a drawer is locked.

     *
     * Locking drawers open or closed will implicitly open or close
     * any drawers as appropriate.

     * @param lockMode The new lock mode for the given drawer. One of [.LOCK_MODE_UNLOCKED],
     * *                 [.LOCK_MODE_LOCKED_CLOSED] or [.LOCK_MODE_LOCKED_OPEN].
     */
    fun setDrawerLockMode(@LockMode lockMode: Int) {
        setDrawerLockMode(lockMode, Gravity.LEFT)
        setDrawerLockMode(lockMode, Gravity.RIGHT)
    }

    /**
     * Enable or disable interaction with the given drawer.

     *
     * This allows the application to restrict the user's ability to open or close
     * the given drawer. DrawerLayout will still respond to calls to [.openDrawer],
     * [.closeDrawer] and friends if a drawer is locked.

     *
     * Locking a drawer open or closed will implicitly open or close
     * that drawer as appropriate.

     * @param lockMode The new lock mode for the given drawer. One of [.LOCK_MODE_UNLOCKED],
     * *                 [.LOCK_MODE_LOCKED_CLOSED] or [.LOCK_MODE_LOCKED_OPEN].
     * *
     * @param edgeGravity Gravity.LEFT, RIGHT, START or END.
     * *                    Expresses which drawer to change the mode for.
     * *
     * *
     * @see .LOCK_MODE_UNLOCKED

     * @see .LOCK_MODE_LOCKED_CLOSED

     * @see .LOCK_MODE_LOCKED_OPEN
     */
    fun setDrawerLockMode(@LockMode lockMode: Int, @EdgeGravity edgeGravity: Int) {
        val absGravity = GravityCompat.getAbsoluteGravity(edgeGravity,
                ViewCompat.getLayoutDirection(this))
        if (absGravity == Gravity.LEFT) {
            mLockModeLeft = lockMode
        } else if (absGravity == Gravity.RIGHT) {
            mLockModeRight = lockMode
        }
        if (lockMode != LOCK_MODE_UNLOCKED) {
            // Cancel interaction in progress
            val helper = if (absGravity == Gravity.LEFT) mLeftDragger else mRightDragger
            helper.cancel()
        }
        when (lockMode) {
            LOCK_MODE_LOCKED_OPEN -> {
                val toOpen = findDrawerWithGravity(absGravity)
                if (toOpen != null) {
                    openDrawer(toOpen)
                }
            }
            LOCK_MODE_LOCKED_CLOSED -> {
                val toClose = findDrawerWithGravity(absGravity)
                if (toClose != null) {
                    closeDrawer(toClose)
                }
            }
        }// default: do nothing
    }

    /**
     * Enable or disable interaction with the given drawer.

     *
     * This allows the application to restrict the user's ability to open or close
     * the given drawer. DrawerLayout will still respond to calls to [.openDrawer],
     * [.closeDrawer] and friends if a drawer is locked.

     *
     * Locking a drawer open or closed will implicitly open or close
     * that drawer as appropriate.

     * @param lockMode The new lock mode for the given drawer. One of [.LOCK_MODE_UNLOCKED],
     * *                 [.LOCK_MODE_LOCKED_CLOSED] or [.LOCK_MODE_LOCKED_OPEN].
     * *
     * @param drawerView The drawer view to change the lock mode for
     * *
     * *
     * @see .LOCK_MODE_UNLOCKED

     * @see .LOCK_MODE_LOCKED_CLOSED

     * @see .LOCK_MODE_LOCKED_OPEN
     */
    fun setDrawerLockMode(@LockMode lockMode: Int, drawerView: View) {
        if (!isDrawerView(drawerView)) {
            throw IllegalArgumentException("View " + drawerView + " is not a " +
                    "drawer with appropriate layout_gravity")
        }
        val gravity = (drawerView.layoutParams as LayoutParams).gravity
        setDrawerLockMode(lockMode, gravity)
    }

    /**
     * Check the lock mode of the drawer with the given gravity.

     * @param edgeGravity Gravity of the drawer to check
     * *
     * @return one of [.LOCK_MODE_UNLOCKED], [.LOCK_MODE_LOCKED_CLOSED] or
     * *         [.LOCK_MODE_LOCKED_OPEN].
     */
    @LockMode
    fun getDrawerLockMode(@EdgeGravity edgeGravity: Int): Int {
        val absGravity = GravityCompat.getAbsoluteGravity(
                edgeGravity, ViewCompat.getLayoutDirection(this))
        if (absGravity == Gravity.LEFT) {
            return mLockModeLeft
        } else if (absGravity == Gravity.RIGHT) {
            return mLockModeRight
        }
        return LOCK_MODE_UNLOCKED
    }

    /**
     * Check the lock mode of the given drawer view.

     * @param drawerView Drawer view to check lock mode
     * *
     * @return one of [.LOCK_MODE_UNLOCKED], [.LOCK_MODE_LOCKED_CLOSED] or
     * *         [.LOCK_MODE_LOCKED_OPEN].
     */
    @LockMode
    fun getDrawerLockMode(drawerView: View): Int {
        val absGravity = getDrawerViewAbsoluteGravity(drawerView)
        if (absGravity == Gravity.LEFT) {
            return mLockModeLeft
        } else if (absGravity == Gravity.RIGHT) {
            return mLockModeRight
        }
        return LOCK_MODE_UNLOCKED
    }

    /**
     * Sets the title of the drawer with the given gravity.
     *
     *
     * When accessibility is turned on, this is the title that will be used to
     * identify the drawer to the active accessibility service.

     * @param edgeGravity Gravity.LEFT, RIGHT, START or END. Expresses which
     * *            drawer to set the title for.
     * *
     * @param title The title for the drawer.
     */
    fun setDrawerTitle(@EdgeGravity edgeGravity: Int, title: CharSequence) {
        val absGravity = GravityCompat.getAbsoluteGravity(
                edgeGravity, ViewCompat.getLayoutDirection(this))
        if (absGravity == Gravity.LEFT) {
            mTitleLeft = title
        } else if (absGravity == Gravity.RIGHT) {
            mTitleRight = title
        }
    }

    /**
     * Returns the title of the drawer with the given gravity.

     * @param edgeGravity Gravity.LEFT, RIGHT, START or END. Expresses which
     * *            drawer to return the title for.
     * *
     * @return The title of the drawer, or null if none set.
     * *
     * @see .setDrawerTitle
     */
    fun getDrawerTitle(@EdgeGravity edgeGravity: Int): CharSequence? {
        val absGravity = GravityCompat.getAbsoluteGravity(
                edgeGravity, ViewCompat.getLayoutDirection(this))
        if (absGravity == Gravity.LEFT) {
            return mTitleLeft
        } else if (absGravity == Gravity.RIGHT) {
            return mTitleRight
        }
        return null
    }

    /**
     * Resolve the shared state of all drawers from the getComponent ViewDragHelpers.
     * Should be called whenever a ViewDragHelper's state changes.
     */
    internal fun updateDrawerState(forGravity: Int, @State activeState: Int, activeDrawer: View?) {
        val leftState = mLeftDragger.viewDragState
        val rightState = mRightDragger.viewDragState

        val state: Int
        if (leftState == STATE_DRAGGING || rightState == STATE_DRAGGING) {
            state = STATE_DRAGGING
        } else if (leftState == STATE_SETTLING || rightState == STATE_SETTLING) {
            state = STATE_SETTLING
        } else {
            state = STATE_IDLE
        }

        if (activeDrawer != null && activeState == STATE_IDLE) {
            val lp = activeDrawer.layoutParams as LayoutParams
            if (lp.onScreen == 0f) {
                dispatchOnDrawerClosed(activeDrawer)
            } else if (lp.onScreen == 1f) {
                dispatchOnDrawerOpened(activeDrawer)
            }
        }

        if (state != mDrawerState) {
            mDrawerState = state

            if (mListener != null) {
                mListener!!.onDrawerStateChanged(state)
            }
        }
    }

    internal fun dispatchOnDrawerClosed(drawerView: View) {
        val lp = drawerView.layoutParams as LayoutParams
        if (lp.knownOpen) {
            lp.knownOpen = false
            if (mListener != null) {
                mListener!!.onDrawerClosed(drawerView)
            }

            updateChildrenImportantForAccessibility(drawerView, false)

            // Only send WINDOW_STATE_CHANGE if the host has window focus. This
            // may change if support for multiple foreground windows (e.g. IME)
            // improves.
            if (hasWindowFocus()) {
                val rootView = rootView
                rootView?.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
            }
        }
    }

    internal fun dispatchOnDrawerOpened(drawerView: View) {
        val lp = drawerView.layoutParams as LayoutParams
        if (!lp.knownOpen) {
            lp.knownOpen = true
            if (mListener != null) {
                mListener!!.onDrawerOpened(drawerView)
            }

            updateChildrenImportantForAccessibility(drawerView, true)

            // Only send WINDOW_STATE_CHANGE if the host has window focus.
            if (hasWindowFocus()) {
                sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
            }

            drawerView.requestFocus()
        }
    }

    private fun updateChildrenImportantForAccessibility(drawerView: View, isDrawerOpen: Boolean) {
        val childCount = childCount
        for (i in 0..childCount - 1) {
            val child = getChildAt(i)
            if (!isDrawerOpen && !isDrawerView(child) || isDrawerOpen && child === drawerView) {
                // Drawer is closed and this is a content view or this is an
                // open drawer view, so it should be visible.
                ViewCompat.setImportantForAccessibility(child,
                        ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES)
            } else {
                ViewCompat.setImportantForAccessibility(child,
                        ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS)
            }
        }
    }

    internal fun dispatchOnDrawerSlide(drawerView: View, slideOffset: Float) {
        if (mListener != null) {
            mListener!!.onDrawerSlide(drawerView, slideOffset)
        }
    }

    internal fun setDrawerViewOffset(drawerView: View, slideOffset: Float) {
        val lp = drawerView.layoutParams as LayoutParams
        if (slideOffset == lp.onScreen) {
            return
        }

        lp.onScreen = slideOffset
        dispatchOnDrawerSlide(drawerView, slideOffset)
    }

    internal fun getDrawerViewOffset(drawerView: View): Float {
        return (drawerView.layoutParams as LayoutParams).onScreen
    }

    /**
     * @return the absolute gravity of the child drawerView, resolved according
     * *         to the current layout direction
     */
    internal fun getDrawerViewAbsoluteGravity(drawerView: View): Int {
        val gravity = (drawerView.layoutParams as LayoutParams).gravity
        return GravityCompat.getAbsoluteGravity(gravity, ViewCompat.getLayoutDirection(this))
    }

    internal fun checkDrawerViewAbsoluteGravity(drawerView: View, checkFor: Int): Boolean {
        val absGravity = getDrawerViewAbsoluteGravity(drawerView)
        return absGravity and checkFor == checkFor
    }

    internal fun findOpenDrawer(): View? {
        val childCount = childCount
        for (i in 0..childCount - 1) {
            val child = getChildAt(i)
            if ((child.layoutParams as LayoutParams).knownOpen) {
                return child
            }
        }
        return null
    }

    internal fun moveDrawerToOffset(drawerView: View, slideOffset: Float) {
        val oldOffset = getDrawerViewOffset(drawerView)
        val width = drawerView.width
        val oldPos = (width * oldOffset).toInt()
        val newPos = (width * slideOffset).toInt()
        val dx = newPos - oldPos

        drawerView.offsetLeftAndRight(
                if (checkDrawerViewAbsoluteGravity(drawerView, Gravity.LEFT)) dx else -dx)
        setDrawerViewOffset(drawerView, slideOffset)
    }

    /**
     * @param gravity the gravity of the child to return. If specified as a
     * *            relative value, it will be resolved according to the current
     * *            layout direction.
     * *
     * @return the drawer with the specified gravity
     */
    internal fun findDrawerWithGravity(gravity: Int): View? {
        val absHorizGravity = GravityCompat.getAbsoluteGravity(
                gravity, ViewCompat.getLayoutDirection(this)) and Gravity.HORIZONTAL_GRAVITY_MASK
        val childCount = childCount
        for (i in 0..childCount - 1) {
            val child = getChildAt(i)
            val childAbsGravity = getDrawerViewAbsoluteGravity(child)
            if (childAbsGravity and Gravity.HORIZONTAL_GRAVITY_MASK == absHorizGravity) {
                return child
            }
        }
        return null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mFirstLayout = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mFirstLayout = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        var heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        var widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        var heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

        if (widthMode != View.MeasureSpec.EXACTLY || heightMode != View.MeasureSpec.EXACTLY) {
            if (isInEditMode) {
                // Don't crash the layout editor. Consume all of the space if specified
                // or pick a magic number from thin air otherwise.
                // TODO Better communication with tools of this bogus state.
                // It will crash on a real device.
                if (widthMode == View.MeasureSpec.AT_MOST) {
                    widthMode = View.MeasureSpec.EXACTLY
                } else if (widthMode == View.MeasureSpec.UNSPECIFIED) {
                    widthMode = View.MeasureSpec.EXACTLY
                    widthSize = 300
                }
                if (heightMode == View.MeasureSpec.AT_MOST) {
                    heightMode = View.MeasureSpec.EXACTLY
                } else if (heightMode == View.MeasureSpec.UNSPECIFIED) {
                    heightMode = View.MeasureSpec.EXACTLY
                    heightSize = 300
                }
            } else {
                throw IllegalArgumentException(
                        "DrawerLayout must be measured with MeasureSpec.EXACTLY.")
            }
        }

        setMeasuredDimension(widthSize, heightSize)

        val applyInsets = mLastInsets != null && ViewCompat.getFitsSystemWindows(this)
        val layoutDirection = ViewCompat.getLayoutDirection(this)

        // Gravity value for each drawer we've seen. Only one of each permitted.
        val foundDrawers = 0
        val childCount = childCount
        for (i in 0..childCount - 1) {
            val child = getChildAt(i)

            if (child.visibility == View.GONE) {
                continue
            }

            val lp = child.layoutParams as LayoutParams

            if (applyInsets && mLastInsets != null) {
                val cgrav = GravityCompat.getAbsoluteGravity(lp.gravity, layoutDirection)
                //if (ViewCompat.getFitsSystemWindows(child)) {
                IMPL.dispatchChildInsets(child, mLastInsets!!, cgrav)
                //}
                //  IMPL.applyMarginInsets(lp, mLastInsets, cgrav);
                //} else {
            }

            if (isContentView(child)) {
                // Content views get measured at exactly the layout's size.
                val contentWidthSpec = View.MeasureSpec.makeMeasureSpec(
                        widthSize - lp.leftMargin - lp.rightMargin, View.MeasureSpec.EXACTLY)
                val contentHeightSpec = View.MeasureSpec.makeMeasureSpec(
                        heightSize - lp.topMargin - lp.bottomMargin, View.MeasureSpec.EXACTLY)
                child.measure(contentWidthSpec, contentHeightSpec)
            } else if (isDrawerView(child)) {
                val childGravity = getDrawerViewAbsoluteGravity(child) and Gravity.HORIZONTAL_GRAVITY_MASK
                if (foundDrawers and childGravity != 0) {
                    throw IllegalStateException("Child drawer has absolute gravity " +
                            gravityToString(childGravity) + " but this " + TAG + " already has a " +
                            "drawer view along that edge")
                }
                val drawerWidthSpec = ViewGroup.getChildMeasureSpec(widthMeasureSpec,
                        mMinDrawerMargin + lp.leftMargin + lp.rightMargin,
                        lp.width)
                val drawerHeightSpec = ViewGroup.getChildMeasureSpec(heightMeasureSpec,
                        lp.topMargin + lp.bottomMargin,
                        lp.height)
                child.measure(drawerWidthSpec, drawerHeightSpec)
            } else {
                throw IllegalStateException("Child " + child + " at index " + i +
                        " does not have a valid layout_gravity - must be Gravity.LEFT, " +
                        "Gravity.RIGHT or Gravity.NO_GRAVITY")
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        mInLayout = true
        val width = r - l
        val childCount = childCount
        for (i in 0..childCount - 1) {
            val child = getChildAt(i)

            if (child.visibility == View.GONE) {
                continue
            }

            val lp = child.layoutParams as LayoutParams

            if (isContentView(child)) {
                child.layout(lp.leftMargin, lp.topMargin,
                        lp.leftMargin + child.measuredWidth,
                        lp.topMargin + child.measuredHeight)
            } else { // Drawer, if it wasn't onMeasure would have thrown an exception.
                val childWidth = child.measuredWidth
                val childHeight = child.measuredHeight
                val childLeft: Int

                val newOffset: Float
                if (checkDrawerViewAbsoluteGravity(child, Gravity.LEFT)) {
                    childLeft = -childWidth + (childWidth * lp.onScreen).toInt()
                    newOffset = (childWidth + childLeft).toFloat() / childWidth
                } else { // Right; onMeasure checked for us.
                    childLeft = width - (childWidth * lp.onScreen).toInt()
                    newOffset = (width - childLeft).toFloat() / childWidth
                }

                val changeOffset = newOffset != lp.onScreen

                val vgrav = lp.gravity and Gravity.VERTICAL_GRAVITY_MASK

                when (vgrav) {
                    Gravity.BOTTOM -> {
                        val height = b - t
                        child.layout(childLeft,
                                height - lp.bottomMargin - child.measuredHeight,
                                childLeft + childWidth,
                                height - lp.bottomMargin)
                    }

                    Gravity.CENTER_VERTICAL -> {
                        val height = b - t
                        var childTop = (height - childHeight) / 2

                        // Offset for margins. If things don't fit right because of
                        // bad measurement before, oh well.
                        if (childTop < lp.topMargin) {
                            childTop = lp.topMargin
                        } else if (childTop + childHeight > height - lp.bottomMargin) {
                            childTop = height - lp.bottomMargin - childHeight
                        }
                        child.layout(childLeft, childTop, childLeft + childWidth,
                                childTop + childHeight)
                    }
                    else -> {
                        child.layout(childLeft, lp.topMargin, childLeft + childWidth,
                                lp.topMargin + childHeight)
                    }
                }

                if (changeOffset) {
                    setDrawerViewOffset(child, newOffset)
                }

                val newVisibility = if (lp.onScreen > 0) View.VISIBLE else View.INVISIBLE
                if (child.visibility != newVisibility) {
                    child.visibility = newVisibility
                }
            }
        }
        mInLayout = false
        mFirstLayout = false
    }

    override fun requestLayout() {
        if (!mInLayout) {
            super.requestLayout()
        }
    }

    override fun computeScroll() {
        val childCount = childCount
        var scrimOpacity = 0f
        for (i in 0..childCount - 1) {
            val onscreen = (getChildAt(i).layoutParams as LayoutParams).onScreen
            scrimOpacity = Math.max(scrimOpacity, onscreen)
        }
        mScrimOpacity = scrimOpacity

        // "|" used on purpose; both need to run.
        if (mLeftDragger.continueSettling(true) or mRightDragger.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    /**
     * Set a drawable to draw in the insets area for the status bar.
     * Note that this will only be activated if this DrawerLayout fitsSystemWindows.

     * @param bg Background drawable to draw behind the status bar
     */
    fun setStatusBarBackground(bg: Drawable) {
        statusBarBackgroundDrawable = bg
        invalidate()
    }

    /**
     * Set a drawable to draw in the insets area for the status bar.
     * Note that this will only be activated if this DrawerLayout fitsSystemWindows.

     * @param resId Resource id of a background drawable to draw behind the status bar
     */
    fun setStatusBarBackground(resId: Int) {
        statusBarBackgroundDrawable = if (resId != 0) ContextCompat.getDrawable(context, resId) else null
        invalidate()
    }

    /**
     * Set a drawable to draw in the insets area for the status bar.
     * Note that this will only be activated if this DrawerLayout fitsSystemWindows.

     * @param color Color to use as a background drawable to draw behind the status bar
     * *              in 0xAARRGGBB format.
     */
    fun setStatusBarBackgroundColor(color: Int) {
        statusBarBackgroundDrawable = ColorDrawable(color)
        invalidate()
    }

    //@Override
    //public void onDraw(Canvas c) {
    //  super.onDraw(c);
    //  if (mDrawStatusBarBackground && mStatusBarBackground != null) {
    //    final int inset = IMPL.getTopInset(mLastInsets);
    //    if (inset > 0) {
    //      mStatusBarBackground.setBounds(0, 0, getWidth(), inset);
    //      mStatusBarBackground.draw(c);
    //    }
    //  }
    //}

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        val height = height
        val drawingContent = isContentView(child)
        var clipLeft = 0
        var clipRight = width

        val restoreCount = canvas.save()
        if (drawingContent) {
            val childCount = childCount
            for (i in 0..childCount - 1) {
                val v = getChildAt(i)
                if (v === child || v.visibility != View.VISIBLE ||
                        !hasOpaqueBackground(v) || !isDrawerView(v) ||
                        v.height < height) {
                    continue
                }

                if (checkDrawerViewAbsoluteGravity(v, Gravity.LEFT)) {
                    val vright = v.right
                    if (vright > clipLeft) clipLeft = vright
                } else {
                    val vleft = v.left
                    if (vleft < clipRight) clipRight = vleft
                }
            }
            canvas.clipRect(clipLeft, 0, clipRight, getHeight())
        }
        val result = super.drawChild(canvas, child, drawingTime)
        canvas.restoreToCount(restoreCount)

        if (mScrimOpacity > 0 && drawingContent) {
            val baseAlpha = (mScrimColor and 0xff000000.toInt()).ushr(24)
            val imag = (baseAlpha * mScrimOpacity).toInt()
            val color = imag shl 24 or (mScrimColor and 0xffffff)
            mScrimPaint.color = color

            canvas.drawRect(clipLeft.toFloat(), 0f, clipRight.toFloat(), getHeight().toFloat(), mScrimPaint)
        } else if (mShadowLeft != null && checkDrawerViewAbsoluteGravity(child, Gravity.LEFT)) {
            val shadowWidth = mShadowLeft!!.intrinsicWidth
            val childRight = child.right
            val drawerPeekDistance = mLeftDragger.edgeSize
            val alpha = Math.max(0f, Math.min(childRight.toFloat() / drawerPeekDistance, 1.0f))
            mShadowLeft!!.setBounds(childRight, child.top,
                    childRight + shadowWidth, child.bottom)
            mShadowLeft!!.alpha = (0xff * alpha).toInt()
            mShadowLeft!!.draw(canvas)
        } else if (mShadowRight != null && checkDrawerViewAbsoluteGravity(child, Gravity.RIGHT)) {
            val shadowWidth = mShadowRight!!.intrinsicWidth
            val childLeft = child.left
            val showing = width - childLeft
            val drawerPeekDistance = mRightDragger.edgeSize
            val alpha = Math.max(0f, Math.min(showing.toFloat() / drawerPeekDistance, 1.0f))
            mShadowRight!!.setBounds(childLeft - shadowWidth, child.top,
                    childLeft, child.bottom)
            mShadowRight!!.alpha = (0xff * alpha).toInt()
            mShadowRight!!.draw(canvas)
        }
        return result
    }

    internal fun isContentView(child: View): Boolean {
        return (child.layoutParams as LayoutParams).gravity == Gravity.NO_GRAVITY
    }

    internal fun isDrawerView(child: View): Boolean {
        val gravity = (child.layoutParams as LayoutParams).gravity
        val absGravity = GravityCompat.getAbsoluteGravity(gravity,
                ViewCompat.getLayoutDirection(child))
        return absGravity and (Gravity.LEFT or Gravity.RIGHT) != 0
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action = MotionEventCompat.getActionMasked(ev)

        // "|" used deliberately here; both methods should be invoked.
        val interceptForDrag = mLeftDragger.shouldInterceptTouchEvent(ev) or mRightDragger.shouldInterceptTouchEvent(ev)

        var interceptForTap = false

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                val x = ev.x
                val y = ev.y
                mInitialMotionX = x
                mInitialMotionY = y
                if (mScrimOpacity > 0) {
                    val child = mLeftDragger.findTopChildUnder(x.toInt(), y.toInt())
                    if (child != null && isContentView(child)) {
                        interceptForTap = true
                    }
                }
                mDisallowInterceptRequested = false
                mChildrenCanceledTouch = false
            }

            MotionEvent.ACTION_MOVE -> {
                // If we cross the touch slop, don't perform the delayed peek for an edge touch.
                if (mLeftDragger.checkTouchSlop(ViewDragHelper.DIRECTION_ALL)) {
                    mLeftCallback.removeCallbacks()
                    mRightCallback.removeCallbacks()
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                closeDrawers(true)
                mDisallowInterceptRequested = false
                mChildrenCanceledTouch = false
            }
        }

        return interceptForDrag || interceptForTap || hasPeekingDrawer() || mChildrenCanceledTouch
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        mLeftDragger.processTouchEvent(ev)
        mRightDragger.processTouchEvent(ev)

        val action = ev.action
        val wantTouchEvents = true

        when (action and MotionEventCompat.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                val x = ev.x
                val y = ev.y
                mInitialMotionX = x
                mInitialMotionY = y
                mDisallowInterceptRequested = false
                mChildrenCanceledTouch = false
            }

            MotionEvent.ACTION_UP -> {
                val x = ev.x
                val y = ev.y
                var peekingOnly = true
                val touchedView = mLeftDragger.findTopChildUnder(x.toInt(), y.toInt())
                if (touchedView != null && isContentView(touchedView)) {
                    val dx = x - mInitialMotionX
                    val dy = y - mInitialMotionY
                    val slop = mLeftDragger.touchSlop
                    if (dx * dx + dy * dy < slop * slop) {
                        // Taps close a dimmed open drawer but only if it isn't locked open.
                        val openDrawer = findOpenDrawer()
                        if (openDrawer != null) {
                            peekingOnly = getDrawerLockMode(openDrawer) == LOCK_MODE_LOCKED_OPEN
                        }
                    }
                }
                closeDrawers(peekingOnly)
                mDisallowInterceptRequested = false
            }

            MotionEvent.ACTION_CANCEL -> {
                closeDrawers(true)
                mDisallowInterceptRequested = false
                mChildrenCanceledTouch = false
            }
        }

        return wantTouchEvents
    }

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        if (CHILDREN_DISALLOW_INTERCEPT || !mLeftDragger.isEdgeTouched(ViewDragHelper.EDGE_LEFT) && !mRightDragger.isEdgeTouched(ViewDragHelper.EDGE_RIGHT)) {
            // If we have an edge touch we want to skip this and track it for later instead.
            super.requestDisallowInterceptTouchEvent(disallowIntercept)
        }
        mDisallowInterceptRequested = disallowIntercept
        if (disallowIntercept) {
            closeDrawers(true)
        }
    }

    /**
     * Close all currently open drawer views by animating them out of view.
     */
    fun closeDrawers() {
        closeDrawers(false)
    }

    internal fun closeDrawers(peekingOnly: Boolean) {
        var needsInvalidate = false
        val childCount = childCount
        for (i in 0..childCount - 1) {
            val child = getChildAt(i)
            val lp = child.layoutParams as LayoutParams

            if (!isDrawerView(child) || peekingOnly && !lp.isPeeking) {
                continue
            }

            val childWidth = child.width

            if (checkDrawerViewAbsoluteGravity(child, Gravity.LEFT)) {
                needsInvalidate = needsInvalidate or mLeftDragger.smoothSlideViewTo(child,
                        -childWidth, child.top)
            } else {
                needsInvalidate = needsInvalidate or mRightDragger.smoothSlideViewTo(child,
                        width, child.top)
            }

            lp.isPeeking = false
        }

        mLeftCallback.removeCallbacks()
        mRightCallback.removeCallbacks()

        if (needsInvalidate) {
            invalidate()
        }
    }

    /**
     * Open the specified drawer view by animating it into view.

     * @param drawerView Drawer view to open
     */
    fun openDrawer(drawerView: View) {
        if (!isDrawerView(drawerView)) {
            throw IllegalArgumentException("View $drawerView is not a sliding drawer")
        }

        if (mFirstLayout) {
            val lp = drawerView.layoutParams as LayoutParams
            lp.onScreen = 1.0f
            lp.knownOpen = true

            updateChildrenImportantForAccessibility(drawerView, true)
        } else {
            if (checkDrawerViewAbsoluteGravity(drawerView, Gravity.LEFT)) {
                mLeftDragger.smoothSlideViewTo(drawerView, 0, drawerView.top)
            } else {
                mRightDragger.smoothSlideViewTo(drawerView, width - drawerView.width,
                        drawerView.top)
            }
        }
        invalidate()
    }

    /**
     * Open the specified drawer by animating it out of view.

     * @param gravity Gravity.LEFT to move the left drawer or Gravity.RIGHT for the right.
     * *                GravityCompat.START or GravityCompat.END may also be used.
     */
    fun openDrawer(@EdgeGravity gravity: Int) {
        val drawerView = findDrawerWithGravity(gravity) ?: throw IllegalArgumentException("No drawer view found with gravity " + gravityToString(gravity))
        openDrawer(drawerView)
    }

    /**
     * Close the specified drawer view by animating it into view.

     * @param drawerView Drawer view to close
     */
    fun closeDrawer(drawerView: View) {
        if (!isDrawerView(drawerView)) {
            throw IllegalArgumentException("View $drawerView is not a sliding drawer")
        }

        if (mFirstLayout) {
            val lp = drawerView.layoutParams as LayoutParams
            lp.onScreen = 0.0f
            lp.knownOpen = false
        } else {
            if (checkDrawerViewAbsoluteGravity(drawerView, Gravity.LEFT)) {
                mLeftDragger.smoothSlideViewTo(drawerView, -drawerView.width,
                        drawerView.top)
            } else {
                mRightDragger.smoothSlideViewTo(drawerView, width, drawerView.top)
            }
        }
        invalidate()
    }

    /**
     * Close the specified drawer by animating it out of view.

     * @param gravity Gravity.LEFT to move the left drawer or Gravity.RIGHT for the right.
     * *                GravityCompat.START or GravityCompat.END may also be used.
     */
    fun closeDrawer(@EdgeGravity gravity: Int) {
        val drawerView = findDrawerWithGravity(gravity) ?: throw IllegalArgumentException("No drawer view found with gravity " + gravityToString(gravity))
        closeDrawer(drawerView)
    }

    /**
     * Check if the given drawer view is currently in an open state.
     * To be considered "open" the drawer must have settled into its fully
     * visible state. To check for partial visibility use
     * [.isDrawerVisible].

     * @param drawer Drawer view to check
     * *
     * @return true if the given drawer view is in an open state
     * *
     * @see .isDrawerVisible
     */
    fun isDrawerOpen(drawer: View): Boolean {
        if (!isDrawerView(drawer)) {
            throw IllegalArgumentException("View $drawer is not a drawer")
        }
        return (drawer.layoutParams as LayoutParams).knownOpen
    }

    /**
     * Check if the given drawer view is currently in an open state.
     * To be considered "open" the drawer must have settled into its fully
     * visible state. If there is no drawer with the given gravity this method
     * will return false.

     * @param drawerGravity Gravity of the drawer to check
     * *
     * @return true if the given drawer view is in an open state
     */
    fun isDrawerOpen(@EdgeGravity drawerGravity: Int): Boolean {
        val drawerView = findDrawerWithGravity(drawerGravity)
        if (drawerView != null) {
            return isDrawerOpen(drawerView)
        }
        return false
    }

    /**
     * Check if a given drawer view is currently visible on-screen. The drawer
     * may be only peeking onto the screen, fully extended, or anywhere inbetween.

     * @param drawer Drawer view to check
     * *
     * @return true if the given drawer is visible on-screen
     * *
     * @see .isDrawerOpen
     */
    fun isDrawerVisible(drawer: View): Boolean {
        if (!isDrawerView(drawer)) {
            throw IllegalArgumentException("View $drawer is not a drawer")
        }
        return (drawer.layoutParams as LayoutParams).onScreen > 0
    }

    /**
     * Check if a given drawer view is currently visible on-screen. The drawer
     * may be only peeking onto the screen, fully extended, or anywhere in between.
     * If there is no drawer with the given gravity this method will return false.

     * @param drawerGravity Gravity of the drawer to check
     * *
     * @return true if the given drawer is visible on-screen
     */
    fun isDrawerVisible(@EdgeGravity drawerGravity: Int): Boolean {
        val drawerView = findDrawerWithGravity(drawerGravity)
        if (drawerView != null) {
            return isDrawerVisible(drawerView)
        }
        return false
    }

    private fun hasPeekingDrawer(): Boolean {
        val childCount = childCount
        for (i in 0..childCount - 1) {
            val lp = getChildAt(i).layoutParams as LayoutParams
            if (lp.isPeeking) {
                return true
            }
        }
        return false
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT)
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        return if (p is LayoutParams)
            LayoutParams(p)
        else if (p is ViewGroup.MarginLayoutParams)
            LayoutParams(p)
        else
            LayoutParams(p)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams && super.checkLayoutParams(p)
    }

    override fun generateLayoutParams(attrs: AttributeSet): ViewGroup.LayoutParams {
        return LayoutParams(context, attrs)
    }

    private fun hasVisibleDrawer(): Boolean {
        return findVisibleDrawer() != null
    }

    private fun findVisibleDrawer(): View? {
        val childCount = childCount
        for (i in 0..childCount - 1) {
            val child = getChildAt(i)
            if (isDrawerView(child) && isDrawerVisible(child)) {
                return child
            }
        }
        return null
    }

    internal fun cancelChildViewTouch() {
        // Cancel child touches
        if (!mChildrenCanceledTouch) {
            val now = SystemClock.uptimeMillis()
            val cancelEvent = MotionEvent.obtain(now, now,
                    MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0)
            val childCount = childCount
            for (i in 0..childCount - 1) {
                getChildAt(i).dispatchTouchEvent(cancelEvent)
            }
            cancelEvent.recycle()
            mChildrenCanceledTouch = true
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && hasVisibleDrawer()) {
            KeyEventCompat.startTracking(event)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val visibleDrawer = findVisibleDrawer()
            if (visibleDrawer != null && getDrawerLockMode(visibleDrawer) == LOCK_MODE_UNLOCKED) {
                closeDrawers()
            }
            return visibleDrawer != null
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)

        if (ss.openDrawerGravity != Gravity.NO_GRAVITY) {
            val toOpen = findDrawerWithGravity(ss.openDrawerGravity)
            if (toOpen != null) {
                openDrawer(toOpen)
            }
        }

        setDrawerLockMode(ss.lockModeLeft, Gravity.LEFT)
        setDrawerLockMode(ss.lockModeRight, Gravity.RIGHT)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)

        val openDrawer = findOpenDrawer()
        if (openDrawer != null) {
            ss.openDrawerGravity = (openDrawer.layoutParams as LayoutParams).gravity
        }

        ss.lockModeLeft = mLockModeLeft
        ss.lockModeRight = mLockModeRight

        return ss
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        super.addView(child, index, params)

        val openDrawer = findOpenDrawer()
        if (openDrawer != null || isDrawerView(child)) {
            // A drawer is already open or the new view is a drawer, so the
            // new view should start out hidden.
            ViewCompat.setImportantForAccessibility(child,
                    ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS)
        } else {
            // Otherwise this is a content view and no drawer is open, so the
            // new view should start out visible.
            ViewCompat.setImportantForAccessibility(child,
                    ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES)
        }

        // We only need a delegate here if the framework doesn't understand
        // NO_HIDE_DESCENDANTS importance.
        if (!CAN_HIDE_DESCENDANTS) {
            ViewCompat.setAccessibilityDelegate(child, mChildAccessibilityDelegate)
        }
    }

    /**
     * State persisted across instances
     */
    protected class SavedState : View.BaseSavedState {
        internal var openDrawerGravity = Gravity.NO_GRAVITY
        internal var lockModeLeft = LOCK_MODE_UNLOCKED
        internal var lockModeRight = LOCK_MODE_UNLOCKED

        constructor(`in`: Parcel) : super(`in`) {
            openDrawerGravity = `in`.readInt()
        }

        constructor(superState: Parcelable) : super(superState) {
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(openDrawerGravity)
        }

        companion object {
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    private inner class ViewDragCallback(private val mAbsGravity: Int) : ViewDragHelper.Callback() {
        private var mDragger: ViewDragHelper? = null

        private val mPeekRunnable = { peekDrawer() }

        fun setDragger(dragger: ViewDragHelper) {
            mDragger = dragger
        }

        fun removeCallbacks() {
            this@DebugDrawerLayout.removeCallbacks(mPeekRunnable)
        }

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            // Only capture views where the gravity matches what we're looking for.
            // This lets us use two ViewDragHelpers, one for each side drawer.
            return isDrawerView(child) && checkDrawerViewAbsoluteGravity(child, mAbsGravity)
                    && getDrawerLockMode(child) == LOCK_MODE_UNLOCKED
        }

        override fun onViewDragStateChanged(state: Int) {
            updateDrawerState(mAbsGravity, state, mDragger!!.capturedView)
        }

        override fun onViewPositionChanged(changedView: View?, left: Int, top: Int, dx: Int, dy: Int) {
            val offset: Float
            val childWidth = changedView!!.width

            // This reverses the positioning shown in onLayout.
            if (checkDrawerViewAbsoluteGravity(changedView, Gravity.LEFT)) {
                offset = (childWidth + left).toFloat() / childWidth
            } else {
                val width = width
                offset = (width - left).toFloat() / childWidth
            }
            setDrawerViewOffset(changedView, offset)
            changedView.visibility = if (offset == 0f) View.INVISIBLE else View.VISIBLE
            invalidate()
        }

        override fun onViewCaptured(capturedChild: View?, activePointerId: Int) {
            val lp = capturedChild!!.layoutParams as LayoutParams
            lp.isPeeking = false

            closeOtherDrawer()
        }

        private fun closeOtherDrawer() {
            val otherGrav = if (mAbsGravity == Gravity.LEFT) Gravity.RIGHT else Gravity.LEFT
            val toClose = findDrawerWithGravity(otherGrav)
            if (toClose != null) {
                closeDrawer(toClose)
            }
        }

        override fun onViewReleased(releasedChild: View?, xvel: Float, yvel: Float) {
            // Offset is how open the drawer is, therefore left/right values
            // are reversed from one another.
            val offset = getDrawerViewOffset(releasedChild!!)
            val childWidth = releasedChild.width

            val left: Int
            if (checkDrawerViewAbsoluteGravity(releasedChild, Gravity.LEFT)) {
                left = if (xvel > 0 || xvel == 0f && offset > 0.5f) 0 else -childWidth
            } else {
                val width = width
                left = if (xvel < 0 || xvel == 0f && offset > 0.5f) width - childWidth else width
            }

            mDragger!!.settleCapturedViewAt(left, releasedChild.top)
            invalidate()
        }

        override fun onEdgeTouched(edgeFlags: Int, pointerId: Int) {
            postDelayed(mPeekRunnable, PEEK_DELAY.toLong())
        }

        private fun peekDrawer() {
            val toCapture: View?
            val childLeft: Int
            val peekDistance = mDragger!!.edgeSize
            val leftEdge = mAbsGravity == Gravity.LEFT
            if (leftEdge) {
                toCapture = findDrawerWithGravity(Gravity.LEFT)
                childLeft = (if (toCapture != null) -toCapture.width else 0) + peekDistance
            } else {
                toCapture = findDrawerWithGravity(Gravity.RIGHT)
                childLeft = width - peekDistance
            }
            // Only peek if it would mean making the drawer more visible and the drawer isn't locked
            if (toCapture != null && (leftEdge && toCapture.left < childLeft || !leftEdge && toCapture.left > childLeft) &&
                    getDrawerLockMode(toCapture) == LOCK_MODE_UNLOCKED) {
                val lp = toCapture.layoutParams as LayoutParams
                mDragger!!.smoothSlideViewTo(toCapture, childLeft, toCapture.top)
                lp.isPeeking = true
                invalidate()

                closeOtherDrawer()

                cancelChildViewTouch()
            }
        }

        override fun onEdgeLock(edgeFlags: Int): Boolean {
            if (ALLOW_EDGE_LOCK) {
                val drawer = findDrawerWithGravity(mAbsGravity)
                if (drawer != null && !isDrawerOpen(drawer)) {
                    closeDrawer(drawer)
                }
                return true
            }
            return false
        }

        override fun onEdgeDragStarted(edgeFlags: Int, pointerId: Int) {
            val toCapture: View?
            if (edgeFlags and ViewDragHelper.EDGE_LEFT == ViewDragHelper.EDGE_LEFT) {
                toCapture = findDrawerWithGravity(Gravity.LEFT)
            } else {
                toCapture = findDrawerWithGravity(Gravity.RIGHT)
            }

            if (toCapture != null && getDrawerLockMode(toCapture) == LOCK_MODE_UNLOCKED) {
                mDragger!!.captureChildView(toCapture, pointerId)
            }
        }

        override fun getViewHorizontalDragRange(child: View?): Int {
            return if (isDrawerView(child!!)) child.width else 0
        }

        override fun clampViewPositionHorizontal(child: View?, left: Int, dx: Int): Int {
            if (checkDrawerViewAbsoluteGravity(child!!, Gravity.LEFT)) {
                return Math.max(-child.width, Math.min(left, 0))
            } else {
                val width = width
                return Math.max(width - child.width, Math.min(left, width))
            }
        }

        override fun clampViewPositionVertical(child: View?, top: Int, dy: Int): Int {
            return child!!.top
        }
    }

    class LayoutParams : ViewGroup.MarginLayoutParams {

        var gravity = Gravity.NO_GRAVITY
        internal var onScreen: Float = 0.toFloat()
        internal var isPeeking: Boolean = false
        internal var knownOpen: Boolean = false

        constructor(c: Context, attrs: AttributeSet) : super(c, attrs) {

            val a = c.obtainStyledAttributes(attrs, LAYOUT_ATTRS)
            this.gravity = a.getInt(0, Gravity.NO_GRAVITY)
            a.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height) {
        }

        constructor(width: Int, height: Int, gravity: Int) : this(width, height) {
            this.gravity = gravity
        }

        constructor(source: LayoutParams) : super(source) {
            this.gravity = source.gravity
        }

        constructor(source: ViewGroup.LayoutParams) : super(source) {
        }

        constructor(source: ViewGroup.MarginLayoutParams) : super(source) {
        }
    }

    internal inner class AccessibilityDelegate : AccessibilityDelegateCompat() {
        private val mTmpRect = Rect()

        override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
            if (CAN_HIDE_DESCENDANTS) {
                super.onInitializeAccessibilityNodeInfo(host, info)
            } else {
                // Obtain a node for the host, then manually generate the list
                // of children to only include non-obscured views.
                val superNode = AccessibilityNodeInfoCompat.obtain(info)
                super.onInitializeAccessibilityNodeInfo(host, superNode)

                info.setSource(host)
                val parent = ViewCompat.getParentForAccessibility(host)
                if (parent is View) {
                    info.setParent(parent)
                }
                copyNodeInfoNoChildren(info, superNode)
                superNode.recycle()

                addChildrenForAccessibility(info, host as ViewGroup)
            }

            info.className = DebugDrawerLayout::class.java.name

            // This view reports itself as focusable so that it can intercept
            // the back button, but we should prevent this view from reporting
            // itself as focusable to accessibility services.
            info.isFocusable = false
            info.isFocused = false
        }

        override fun onInitializeAccessibilityEvent(host: View, event: AccessibilityEvent) {
            super.onInitializeAccessibilityEvent(host, event)

            event.className = DebugDrawerLayout::class.java.name
        }

        override fun dispatchPopulateAccessibilityEvent(host: View, event: AccessibilityEvent): Boolean {
            // Special case to handle window state change events. As far as
            // accessibility services are concerned, state changes from
            // DrawerLayout invalidate the entire contents of the screen (like
            // an Activity or Dialog) and they should announce the title of the
            // new content.
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                val eventText = event.text
                val visibleDrawer = findVisibleDrawer()
                if (visibleDrawer != null) {
                    val edgeGravity = getDrawerViewAbsoluteGravity(visibleDrawer)
                    val title = getDrawerTitle(edgeGravity)
                    if (title != null) {
                        eventText.add(title)
                    }
                }

                return true
            }

            return super.dispatchPopulateAccessibilityEvent(host, event)
        }

        override fun onRequestSendAccessibilityEvent(host: ViewGroup, child: View,
                                                     event: AccessibilityEvent): Boolean {
            if (CAN_HIDE_DESCENDANTS || includeChildForAccessibility(child)) {
                return super.onRequestSendAccessibilityEvent(host, child, event)
            }
            return false
        }

        private fun addChildrenForAccessibility(info: AccessibilityNodeInfoCompat, v: ViewGroup) {
            val childCount = v.childCount
            for (i in 0..childCount - 1) {
                val child = v.getChildAt(i)
                if (includeChildForAccessibility(child)) {
                    info.addChild(child)
                }
            }
        }

        /**
         * This should really be in AccessibilityNodeInfoCompat, but there unfortunately
         * seem to be a few elements that are not easily cloneable using the underlying API.
         * Leave it private here as it's not general-purpose useful.
         */
        private fun copyNodeInfoNoChildren(dest: AccessibilityNodeInfoCompat,
                                           src: AccessibilityNodeInfoCompat) {
            val rect = mTmpRect

            src.getBoundsInParent(rect)
            dest.setBoundsInParent(rect)

            src.getBoundsInScreen(rect)
            dest.setBoundsInScreen(rect)

            dest.isVisibleToUser = src.isVisibleToUser
            dest.packageName = src.packageName
            dest.className = src.className
            dest.contentDescription = src.contentDescription

            dest.isEnabled = src.isEnabled
            dest.isClickable = src.isClickable
            dest.isFocusable = src.isFocusable
            dest.isFocused = src.isFocused
            dest.isAccessibilityFocused = src.isAccessibilityFocused
            dest.isSelected = src.isSelected
            dest.isLongClickable = src.isLongClickable

            dest.addAction(src.actions)
        }
    }

    internal inner class ChildAccessibilityDelegate : AccessibilityDelegateCompat() {
        override fun onInitializeAccessibilityNodeInfo(child: View,
                                                       info: AccessibilityNodeInfoCompat) {
            super.onInitializeAccessibilityNodeInfo(child, info)

            if (!includeChildForAccessibility(child)) {
                // If we are ignoring the sub-tree rooted at the child,
                // break the connection to the rest of the node tree.
                // For details refer to includeChildForAccessibility.
                info.setParent(null)
            }
        }
    }

    companion object {
        private val TAG = "DrawerLayout"

        /**
         * Indicates that any drawers are in an idle, settled state. No animation is in progress.
         */
        const val STATE_IDLE = ViewDragHelper.STATE_IDLE

        /**
         * Indicates that a drawer is currently being dragged by the user.
         */
        const val STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING

        /**
         * Indicates that a drawer is in the process of settling to a final position.
         */
        const val STATE_SETTLING = ViewDragHelper.STATE_SETTLING

        /**
         * The drawer is unlocked.
         */
        const val LOCK_MODE_UNLOCKED = 0

        /**
         * The drawer is locked closed. The user may not open it, though
         * the app may open it programmatically.
         */
        const val LOCK_MODE_LOCKED_CLOSED = 1

        /**
         * The drawer is locked open. The user may not close it, though the app
         * may close it programmatically.
         */
        const val LOCK_MODE_LOCKED_OPEN = 2


        private const val MIN_DRAWER_MARGIN = 64 // dp

        private const val DEFAULT_SCRIM_COLOR = 0x99000000.toInt()

        /**
         * Length of time to delay before peeking the drawer.
         */
        private const val PEEK_DELAY = 160 // ms

        /**
         * Minimum velocity that will be detected as a fling
         */
        private const val MIN_FLING_VELOCITY = 400 // dips per second

        /**
         * Experimental feature.
         */
        private const val ALLOW_EDGE_LOCK = false

        private const val CHILDREN_DISALLOW_INTERCEPT = true

        private const val TOUCH_SLOP_SENSITIVITY = 1.0f

        private val LAYOUT_ATTRS = intArrayOf(android.R.attr.layout_gravity)

        /** Whether we can use NO_HIDE_DESCENDANTS accessibility importance.  */
        private val CAN_HIDE_DESCENDANTS = Build.VERSION.SDK_INT >= 19

        internal val IMPL: DrawerLayoutCompatImpl

        init {
            val version = Build.VERSION.SDK_INT
            if (version >= 21) {
                IMPL = DrawerLayoutCompatImplApi21()
            } else {
                IMPL = DrawerLayoutCompatImplBase()
            }
        }

        /**
         * Simple gravity to string - only supports LEFT and RIGHT for debugging output.

         * @param gravity Absolute gravity value
         * *
         * @return LEFT or RIGHT as appropriate, or a hex string
         */
        internal fun gravityToString(@EdgeGravity gravity: Int): String {
            if (gravity and Gravity.LEFT == Gravity.LEFT) {
                return "LEFT"
            }
            if (gravity and Gravity.RIGHT == Gravity.RIGHT) {
                return "RIGHT"
            }
            return Integer.toHexString(gravity)
        }

        private fun hasOpaqueBackground(v: View): Boolean {
            val bg = v.background
            if (bg != null) {
                return bg.opacity == PixelFormat.OPAQUE
            }
            return false
        }

        private fun includeChildForAccessibility(child: View): Boolean {
            // If the child is not important for accessibility we make
            // sure this hides the entire subtree rooted at it as the
            // IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDATS is not
            // supported on older platforms but we want to hide the entire
            // content and not opened drawers if a drawer is opened.
            return ViewCompat.getImportantForAccessibility(child) != ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS && ViewCompat.getImportantForAccessibility(child) != ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
    }
}
