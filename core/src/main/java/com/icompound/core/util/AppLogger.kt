package com.icompound.core.util

import android.util.Log
import com.icompound.core.BuildConfig

object AppLogger {
    private const val TAG = "ICompound_APP"
    var isDebuggable = BuildConfig.DEBUG

    fun d(message: String) {
        if (isDebuggable) {
            Log.d(TAG, message)
        }
    }

    fun i(message: String) {
        if (isDebuggable) {
            Log.i(TAG, message)
        }
    }

    fun e(
        message: String,
        throwable: Throwable? = null,
    ) {
        if (isDebuggable) {
            Log.e(TAG, message, throwable)
        }
    }

    fun w(message: String) {
        if (isDebuggable) {
            Log.w(TAG, message)
        }
    }
}
