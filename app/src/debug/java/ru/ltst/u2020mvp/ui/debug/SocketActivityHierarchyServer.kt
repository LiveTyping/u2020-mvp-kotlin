/*
 * Copyright (C) 2011 The Android Open Source Project
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

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewDebug
import okio.BufferedSink
import okio.BufferedSource
import okio.Okio
import ru.ltst.u2020mvp.base.mvp.ActivityHierarchyServer
import timber.log.Timber
import java.io.IOException
import java.io.OutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 *
 * This class can be used to enable the use of HierarchyViewer inside an
 * application. HierarchyViewer is an Android SDK tool that can be used
 * to inspect and debug the user interface of running applications. For
 * security reasons, HierarchyViewer does not work on production builds
 * (for instance phones bought in store.) By using this class, you can
 * make HierarchyViewer work on any device. You must be very careful
 * however to only enable HierarchyViewer when debugging your
 * application.
 *
 *
 *
 * To use this view server, your application must require the INTERNET
 * permission.
 */
class SocketActivityHierarchyServer : Runnable, ActivityHierarchyServer {

    private var mServer: ServerSocket? = null
    private val mPort: Int

    private var mThread: Thread? = null
    private var mThreadPool: ExecutorService? = null

    private val mListeners = CopyOnWriteArrayList<WindowListener>()

    private val mWindows = HashMap<View, String>()
    private val mWindowsLock = ReentrantReadWriteLock()

    private var mFocusedWindow: View? = null
    private val mFocusLock = ReentrantReadWriteLock()

    init {
        mPort = SocketActivityHierarchyServer.VIEW_SERVER_DEFAULT_PORT
    }

    /**
     * Starts the server.

     * @return True if the server was successfully created, or false if it already exists.
     * *
     * @throws java.io.IOException If the server cannot be created.
     */
    @Throws(IOException::class)
    fun start(): Boolean {
        if (mThread != null) {
            return false
        }

        mThread = Thread(this, "Local View Server [port=$mPort]")
        mThreadPool = Executors.newFixedThreadPool(VIEW_SERVER_MAX_CONNECTIONS)
        mThread!!.start()

        return true
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        var name = activity.title.toString()
        if (TextUtils.isEmpty(name)) {
            name = activity.javaClass.canonicalName +
                    "/0x" + Integer.toHexString(System.identityHashCode(activity))

        } else {
            name += " (" + activity.javaClass.canonicalName + ")"
        }
        mWindowsLock.writeLock().lock()
        try {
            mWindows.put(activity.window.decorView.rootView, name)
        } finally {
            mWindowsLock.writeLock().unlock()
        }
        fireWindowsChangedEvent()
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
        val view = activity.window.decorView
        mFocusLock.writeLock().lock()
        try {
            mFocusedWindow = view?.rootView
            if (mFocusedWindow != null) {
                mFocusedWindow!!.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) {
                    }

                    override fun onViewDetachedFromWindow(v: View) {
                        mFocusLock.writeLock().lock()
                        try {
                            if (v === mFocusedWindow) {
                                mFocusedWindow = null
                            }
                        } finally {
                            mFocusLock.writeLock().unlock()
                        }
                    }
                })
            }
        } finally {
            mFocusLock.writeLock().unlock()
        }
        fireFocusChangedEvent()
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        mWindowsLock.writeLock().lock()
        try {
            mWindows.remove(activity.window.decorView.rootView)
        } finally {
            mWindowsLock.writeLock().unlock()
        }
        fireWindowsChangedEvent()
    }

    override fun run() {
        try {
            mServer = ServerSocket(mPort, VIEW_SERVER_MAX_CONNECTIONS, InetAddress.getLocalHost())
        } catch (e: Exception) {
            Timber.w(e, "Starting ServerSocket error: ")
        }

        while (mServer != null && Thread.currentThread() === mThread) {
            // Any uncaught exception will crash the system process
            try {
                val client = mServer!!.accept()
                if (mThreadPool != null) {
                    mThreadPool!!.submit(ViewServerWorker(client))
                } else {
                    try {
                        client.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            } catch (e: Exception) {
                Timber.w(e, "Connection error: ")
            }

        }
    }

    private fun fireWindowsChangedEvent() {
        for (listener in mListeners) {
            listener.windowsChanged()
        }
    }

    private fun fireFocusChangedEvent() {
        for (listener in mListeners) {
            listener.focusChanged()
        }
    }

    private fun addWindowListener(listener: WindowListener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener)
        }
    }

    private fun removeWindowListener(listener: WindowListener) {
        mListeners.remove(listener)
    }

    private interface WindowListener {
        fun windowsChanged()

        fun focusChanged()
    }

    private class UncloseableOutputStream internal constructor(private val mStream: OutputStream) : OutputStream() {

        @Throws(IOException::class)
        override fun close() {
            // Don't close the stream
        }

        override fun equals(o: Any?): Boolean {
            return mStream == o
        }

        @Throws(IOException::class)
        override fun flush() {
            mStream.flush()
        }

        override fun hashCode(): Int {
            return mStream.hashCode()
        }

        override fun toString(): String {
            return mStream.toString()
        }

        @Throws(IOException::class)
        override fun write(buffer: ByteArray, offset: Int, count: Int) {
            mStream.write(buffer, offset, count)
        }

        @Throws(IOException::class)
        override fun write(buffer: ByteArray) {
            mStream.write(buffer)
        }

        @Throws(IOException::class)
        override fun write(oneByte: Int) {
            mStream.write(oneByte)
        }
    }

    private inner class ViewServerWorker(private val mClient: Socket?) : Runnable, WindowListener {
        private var mNeedWindowListUpdate: Boolean = false
        private var mNeedFocusedWindowUpdate: Boolean = false

        private val mLock = Object()

        init {
            mNeedWindowListUpdate = false
            mNeedFocusedWindowUpdate = false
        }

        override fun run() {
            var `in`: BufferedSource? = null
            try {
                `in` = Okio.buffer(Okio.source(mClient!!))

                val request = `in`!!.readUtf8Line()

                val command: String
                val parameters: String

                val index = request.indexOf(' ')
                if (index == -1) {
                    command = request
                    parameters = ""
                } else {
                    command = request.substring(0, index)
                    parameters = request.substring(index + 1)
                }

                val result: Boolean
                if (COMMAND_PROTOCOL_VERSION.equals(command, ignoreCase = true)) {
                    result = writeValue(mClient, VALUE_PROTOCOL_VERSION)
                } else if (COMMAND_SERVER_VERSION.equals(command, ignoreCase = true)) {
                    result = writeValue(mClient, VALUE_SERVER_VERSION)
                } else if (COMMAND_WINDOW_MANAGER_LIST.equals(command, ignoreCase = true)) {
                    result = listWindows(mClient)
                } else if (COMMAND_WINDOW_MANAGER_GET_FOCUS.equals(command, ignoreCase = true)) {
                    result = getFocusedWindow(mClient)
                } else if (COMMAND_WINDOW_MANAGER_AUTOLIST.equals(command, ignoreCase = true)) {
                    result = windowManagerAutolistLoop()
                } else {
                    result = windowCommand(mClient, command, parameters)
                }

                if (!result) {
                    Timber.w("An error occurred with the command: %s", command)
                }
            } catch (e: IOException) {
                Timber.w(e, "Connection error: ")
            } finally {
                if (`in` != null) {
                    try {
                        `in`.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
                if (mClient != null) {
                    try {
                        mClient.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }

        private fun windowCommand(client: Socket, command: String, parameters: String): Boolean {
            var parameters = parameters
            var success = true
            var out: BufferedSink? = null

            try {
                // Find the hash code of the window
                var index = parameters.indexOf(' ')
                if (index == -1) {
                    index = parameters.length
                }
                val code = parameters.substring(0, index)
                val hashCode = java.lang.Long.parseLong(code, 16).toInt()

                // Extract the command's parameter after the window description
                if (index < parameters.length) {
                    parameters = parameters.substring(index + 1)
                } else {
                    parameters = ""
                }

                val window = findWindow(hashCode) ?: return false

                // call stuff
                val dispatch = ViewDebug::class.java.getDeclaredMethod("dispatchCommand", View::class.java, String::class.java,
                        String::class.java, OutputStream::class.java)
                dispatch.isAccessible = true
                dispatch.invoke(null, window, command, parameters,
                        UncloseableOutputStream(client.outputStream))

                if (!client.isOutputShutdown) {
                    out = Okio.buffer(Okio.sink(client))
                    out!!.writeUtf8("DONE\n")
                    out.flush()
                }
            } catch (e: Exception) {
                Timber.w(e, "Could not send command %s with parameters %s", command, parameters)
                success = false
            } finally {
                if (out != null) {
                    try {
                        out.close()
                    } catch (e: IOException) {
                        success = false
                    }

                }
            }

            return success
        }

        private fun findWindow(hashCode: Int): View? {
            if (hashCode == -1) {
                var window: View? = null
                mWindowsLock.readLock().lock()
                try {
                    window = mFocusedWindow
                } finally {
                    mWindowsLock.readLock().unlock()
                }
                return window
            }

            mWindowsLock.readLock().lock()
            try {
                for ((key) in mWindows) {
                    if (System.identityHashCode(key) == hashCode) {
                        return key
                    }
                }
            } finally {
                mWindowsLock.readLock().unlock()
            }

            return null
        }

        private fun listWindows(client: Socket): Boolean {
            var result = true
            var out: BufferedSink? = null

            try {
                mWindowsLock.readLock().lock()

                out = Okio.buffer(Okio.sink(client))

                for ((key, value) in mWindows) {
                    out!!.writeHexadecimalUnsignedLong(System.identityHashCode(key).toLong())
                    out.writeByte(' '.toInt())
                    out.writeUtf8(value)
                    out.writeByte('\n'.toInt())
                }

                out!!.writeUtf8("DONE.\n")
                out.flush()
            } catch (e: Exception) {
                result = false
            } finally {
                mWindowsLock.readLock().unlock()

                if (out != null) {
                    try {
                        out.close()
                    } catch (e: IOException) {
                        result = false
                    }

                }
            }

            return result
        }

        private fun getFocusedWindow(client: Socket): Boolean {
            var result = true
            var focusName: String? = null

            var out: BufferedSink? = null
            try {
                out = Okio.buffer(Okio.sink(client))

                var focusedWindow: View? = null

                mFocusLock.readLock().lock()
                try {
                    focusedWindow = mFocusedWindow
                } finally {
                    mFocusLock.readLock().unlock()
                }

                if (focusedWindow != null) {
                    mWindowsLock.readLock().lock()
                    try {
                        focusName = mWindows[mFocusedWindow]
                    } finally {
                        mWindowsLock.readLock().unlock()
                    }

                    out!!.writeHexadecimalUnsignedLong(System.identityHashCode(focusedWindow).toLong())
                    out.writeByte(' '.toInt())
                    out.writeUtf8(focusName)
                }
                out!!.writeByte('\n'.toInt())
                out.flush()
            } catch (e: Exception) {
                result = false
            } finally {
                if (out != null) {
                    try {
                        out.close()
                    } catch (e: IOException) {
                        result = false
                    }

                }
            }

            return result
        }

        override fun windowsChanged() {
            synchronized(mLock) {
                mNeedWindowListUpdate = true
                mLock.notifyAll()
            }
        }

        override fun focusChanged() {
            synchronized(mLock) {
                mNeedFocusedWindowUpdate = true
                mLock.notifyAll()
            }
        }

        private fun windowManagerAutolistLoop(): Boolean {
            addWindowListener(this)
            var out: BufferedSink? = null
            try {
                out = Okio.buffer(Okio.sink(mClient!!))
                while (!Thread.interrupted()) {
                    var needWindowListUpdate = false
                    var needFocusedWindowUpdate = false
                    synchronized(mLock) {
                        while (!mNeedWindowListUpdate && !mNeedFocusedWindowUpdate) {
                            mLock.wait()
                        }
                        if (mNeedWindowListUpdate) {
                            mNeedWindowListUpdate = false
                            needWindowListUpdate = true
                        }
                        if (mNeedFocusedWindowUpdate) {
                            mNeedFocusedWindowUpdate = false
                            needFocusedWindowUpdate = true
                        }
                    }
                    if (needWindowListUpdate) {
                        out!!.writeUtf8("LIST UPDATE\n")
                        out.flush()
                    }
                    if (needFocusedWindowUpdate) {
                        out!!.writeUtf8("FOCUS UPDATE\n")
                        out.flush()
                    }
                }
            } catch (e: Exception) {
                Timber.w(e, "Connection error: ")
            } finally {
                if (out != null) {
                    try {
                        out.close()
                    } catch (e: IOException) {
                        // Ignore
                    }

                }
                removeWindowListener(this)
            }
            return true
        }
    }

    companion object {
        /**
         * The default port used to start view servers.
         */
        private val VIEW_SERVER_DEFAULT_PORT = 4939
        private val VIEW_SERVER_MAX_CONNECTIONS = 10

        private val VALUE_PROTOCOL_VERSION = "4"
        private val VALUE_SERVER_VERSION = "4"

        // Protocol commands
        // Returns the protocol version
        private val COMMAND_PROTOCOL_VERSION = "PROTOCOL"
        // Returns the server version
        private val COMMAND_SERVER_VERSION = "SERVER"
        // Lists all of the available windows in the system
        private val COMMAND_WINDOW_MANAGER_LIST = "LIST"
        // Keeps a connection open and notifies when the list of windows changes
        private val COMMAND_WINDOW_MANAGER_AUTOLIST = "AUTOLIST"
        // Returns the focused window
        private val COMMAND_WINDOW_MANAGER_GET_FOCUS = "GET_FOCUS"

        private fun writeValue(client: Socket, value: String): Boolean {
            var result: Boolean
            var out: BufferedSink? = null
            try {
                out = Okio.buffer(Okio.sink(client))
                out!!.writeUtf8(value)
                out.writeByte('\n'.toInt())
                out.flush()
                result = true
            } catch (e: Exception) {
                result = false
            } finally {
                if (out != null) {
                    try {
                        out.close()
                    } catch (e: IOException) {
                        result = false
                    }

                }
            }
            return result
        }
    }
}
/**
 * Creates a new ActivityHierarchyServer associated with the specified window manager on the
 * default local port. The server is not started by default.

 * @see .start
 */