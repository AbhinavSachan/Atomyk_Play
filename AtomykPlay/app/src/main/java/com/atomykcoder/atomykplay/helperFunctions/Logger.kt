package com.atomykcoder.atomykplay.helperFunctions

import android.util.Log

object Logger {
    /**
     * Tag is "DebugTag"
     *
     * @param s
     */
    fun normalLog(s: String?) {
        Log.d("DebugTag", s!!)
    }
}
