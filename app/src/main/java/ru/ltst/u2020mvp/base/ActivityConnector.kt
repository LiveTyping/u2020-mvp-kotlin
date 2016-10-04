package ru.ltst.u2020mvp.base

import java.lang.ref.WeakReference
import java.util.LinkedList
import java.util.WeakHashMap

open class ActivityConnector<AttachedObject> {
    private var attachedObjectRef: WeakReference<AttachedObject>? = null
    private val weakHashMap = WeakHashMap<AttachedObject, Any>()

    fun attach(`object`: AttachedObject) {
        val weakReference = WeakReference(`object`)
        weakHashMap.put(`object`, Any())
        attachedObjectRef = weakReference
    }

    fun detach(`object`: AttachedObject) {
        if (weakHashMap.remove(`object`) == null) {
            return
        }

        val it = weakHashMap.keys.iterator()
        if (it.hasNext()) {
            attachedObjectRef = WeakReference(it.next())
        } else {
            attachedObjectRef = null
        }
    }

    protected val attachedObject: AttachedObject?
        get() {
            if (attachedObjectRef == null)
                return null
            return attachedObjectRef!!.get()
        }
}
