package ru.ltst.u2020mvp.ui.debug

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import ru.ltst.u2020mvp.R
import timber.log.Timber
import java.util.*

class ContextualDebugActions(debugView: DebugView,
                             debugActions: Set<ContextualDebugActions.DebugAction<in View>>) :
        ViewGroup.OnHierarchyChangeListener {
    abstract class DebugAction<T : View> protected constructor(internal val viewClass: Class<T>) {

        /** Invoked when the action has been added as available to run.  */
        fun added() {
        }

        /** Invoked when the action has been removed as available to run.  */
        fun removed() {
        }

        /** Return true if action is applicable. Called each time the target view is added.  */
        fun enabled(): Boolean {
            return true
        }

        /** Human-readable action name. Displayed in debug drawer.  */
        abstract fun name(): String

        /** Perform this action using the specified view.  */
        abstract fun run(view: T)
    }

    private val buttonMap: MutableMap<DebugAction<in View>, View>
    private val actionMap: MutableMap<Class<in View>, MutableList<DebugAction<in View>>>

    private val drawerContext: Context
    private val contextualTitleView: View
    private val contextualListView: LinearLayout

    private var clickListener: View.OnClickListener? = null

    init {
        buttonMap = LinkedHashMap<DebugAction<in View>, View>()
        actionMap = LinkedHashMap<Class<in View>, MutableList<DebugAction<in View>>>()

        drawerContext = debugView.context
        contextualTitleView = debugView.contextualTitleView
        contextualListView = debugView.contextualListView

        for (debugAction in debugActions) {
            val cls = debugAction.viewClass
            Timber.d("Adding %s action for %s.", debugAction.javaClass.simpleName, cls.simpleName)

            var actionList: MutableList<DebugAction<in View>>? = actionMap[cls]
            if (actionList == null) {
                actionList = ArrayList<DebugAction<in View>>(2)
                actionMap.put(cls, actionList)
            }
            actionList.add(debugAction)
        }
    }

    fun setActionClickListener(clickListener: View.OnClickListener) {
        this.clickListener = clickListener
    }

    override fun onChildViewAdded(parent: View, child: View) {
        val actions = actionMap[child.javaClass]
        if (actions != null) {
            for (action in actions) {
                if (!action.enabled()) {
                    continue
                }
                Timber.d("Adding debug action \"%s\" for %s.", action.name(),
                        child.javaClass.simpleName)

                val button = createButton(action, child)
                buttonMap.put(action, button)
                contextualListView.addView(button)
                action.added()
            }
            updateContextualVisibility()
        }
    }

    override fun onChildViewRemoved(parent: View, child: View) {
        val actions = actionMap[child.javaClass]
        if (actions != null) {
            for (action in actions) {
                Timber.d("Removing debug action \"%s\" for %s.", action.name(),
                        child.javaClass.simpleName)
                val buttonView = buttonMap.remove(action)
                if (buttonView != null) {
                    contextualListView.removeView(buttonView)
                    action.removed()
                }
            }
            updateContextualVisibility()
        }
    }

    private fun createButton(action: DebugAction<in View>, child: View): Button {
        val button = LayoutInflater.from(drawerContext)
                .inflate(R.layout.debug_drawer_contextual_action, contextualListView, false) as Button
        button.text = action.name()
        button.setOnClickListener { view ->
            if (clickListener != null) {
                clickListener!!.onClick(view)
            }
            action.run(child)
        }
        return button
    }

    private fun updateContextualVisibility() {
        val visibility = if (contextualListView.childCount > 0) View.VISIBLE else View.GONE
        contextualTitleView.visibility = visibility
        contextualListView.visibility = visibility
    }
}
