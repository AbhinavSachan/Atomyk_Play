package com.atomykcoder.atomykplay.helperFunctions;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class Logger {
    /**
     * Tag is "DebugTag"
     * @param s
     */
    public static void normalLog(String s) {
        Log.d("DebugTag", s);
    }
}
