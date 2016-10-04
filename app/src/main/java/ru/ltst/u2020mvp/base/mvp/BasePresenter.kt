package ru.ltst.u2020mvp.base.mvp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable

import java.lang.ref.WeakReference
import java.util.ArrayList

abstract class BasePresenter<V : BaseView> {


    private var view: WeakReference<V>? = null

    /**
     * Load has been called for the current [.view].
     */

    private var loaded: Boolean = false

    private var onActivityResult: OnActivityResult? = null


    fun takeView(view: V?) {
        if (view == null) throw NullPointerException("new view must not be null")

        if (this.view != null) dropView(this.view!!.get())

        this.view = WeakReference(view)
        if (!loaded) {
            loaded = true
            onLoad(onActivityResult)
            onActivityResult = null
        }
    }

    fun dropView(view: V?) {
        if (view == null) throw NullPointerException("dropped view must not be null")
        loaded = false
        this.view = null
        onDestroy()
    }

    fun hasView(): Boolean {
        return view != null
    }

    fun getView(): V {
        if (view == null) throw NullPointerException("getView called when view is null. Ensure takeView(View view) is called first.")
        return view!!.get()
    }

    protected open fun onLoad(onActivityResult: OnActivityResult?) {
        if (onActivityResult != null) {
            onResult(onActivityResult)
        }
    }

    protected open fun onDestroy() {
    }

    open fun onRestore(savedInstanceState: Bundle) {
        if (savedInstanceState.containsKey(BF_ON_RESULT_OBJECT)) {
            onActivityResult = savedInstanceState.getParcelable<OnActivityResult>(BF_ON_RESULT_OBJECT)
        }
    }

    protected fun onResult(onActivityResult: OnActivityResult) {
    }

    open fun onSave(outState: Bundle) {
        if (onActivityResult != null) {
            outState.putParcelable(BF_ON_RESULT_OBJECT, onActivityResult)
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        val integers = requestCodes()
        if (resultCode == Activity.RESULT_OK && integers.contains(requestCode)) {
            onActivityResult = OnActivityResult(requestCode, resultCode, data)
            if (hasView()) {
                onResult(onActivityResult!!)
                onActivityResult = null
            }
        }
    }

    protected fun requestCodes(): ArrayList<Int> {
        return ArrayList()
    }

    open fun onNetworkConnectionStateChanged(isConnected: Boolean) {
    }

    class OnActivityResult : Parcelable {
        var requestCode: Int = 0
            private set
        private var resultCode: Int = 0
        var data: Intent? = null
            private set

        constructor(requestCode: Int, resultCode: Int, data: Intent) {
            this.requestCode = requestCode
            this.resultCode = resultCode
            this.data = data
        }

        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false

            val that = o as OnActivityResult?

            if (requestCode != that!!.requestCode) return false
            if (resultCode != that.resultCode) return false
            return if (data != null) data == that.data else that.data == null

        }

        override fun hashCode(): Int {
            var result = requestCode
            result = 31 * result + resultCode
            result = 31 * result + if (data != null) data!!.hashCode() else 0
            return result
        }


        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeInt(this.requestCode)
            dest.writeInt(this.resultCode)
            dest.writeParcelable(this.data, flags)
        }

        protected constructor(`in`: Parcel) {
            this.requestCode = `in`.readInt()
            this.resultCode = `in`.readInt()
            this.data = `in`.readParcelable<Intent>(Intent::class.java.classLoader)
        }

        companion object {
            @JvmStatic
            val CREATOR: Parcelable.Creator<OnActivityResult> = object : Parcelable.Creator<OnActivityResult> {
                override fun createFromParcel(source: Parcel): OnActivityResult {
                    return OnActivityResult(source)
                }

                override fun newArray(size: Int): Array<OnActivityResult?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    companion object {
        private val BF_ON_RESULT_OBJECT = "BasePresenter.on.result.object"
    }
}
