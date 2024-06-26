package com.atomykcoder.atomykplay.ui

import android.os.Build
import androidx.appcompat.app.AppCompatActivity

private typealias BackPressCallback = () -> Boolean

@Suppress("DEPRECATION")
open class BackPressCompatActivity : AppCompatActivity() {
    private var listener: BackPressCallback? = null

    /**
     * A custom back press listener to support predictive back navigation
     * introduced in Android 13.
     *
     * If return true, activity will finish otherwise no action taken.
     */
    fun setOnBackPressListener(block: BackPressCallback) {
        listener = block

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(1000) {
                if (block()) {
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    final override fun onBackPressed() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (listener?.invoke() == false) {
                return
            }
        }
        super.onBackPressed()
    }
}