package com.atomykcoder.atomykplay.utils

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.atomykcoder.atomykplay.helperFunctions.Logger

class AndroidUtil {
    companion object {

        fun setSystemDrawBehindBars(
            window: Window,
            isDarkTheme: Boolean,
            root: View,
            statusBarColor: Int,
            navigationBarColor: Int,
            hideStatusBar: Boolean,
            hideNavigationBar: Boolean
        ) {
            // Alternate flags for Android API level 30 and above
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val controller = window.insetsController
                    controller?.let {
                        // Hide the status bar and enable transparent system bars
                        if (hideStatusBar) {
                            it.hide(WindowInsetsCompat.Type.statusBars())
                            window.statusBarColor = Color.TRANSPARENT
                        } else {
                            it.show(WindowInsetsCompat.Type.statusBars())
                            window.statusBarColor = statusBarColor
                        }
                        if (hideNavigationBar) {
                            it.hide(WindowInsetsCompat.Type.navigationBars())
                            window.navigationBarColor = Color.TRANSPARENT
                        } else {
                            it.show(WindowInsetsCompat.Type.navigationBars())
                            window.navigationBarColor = navigationBarColor
                        }
                        it.systemBarsBehavior =
                            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        // Set light or dark status bar based on theme
                        if (!isDarkTheme) {
                            it.setSystemBarsAppearance(
                                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                            )
                        }
                        // Set window insets to draw behind status bar but not navigation bar
                        window.attributes.layoutInDisplayCutoutMode =
                            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                        WindowCompat.setDecorFitsSystemWindows(window, false)
                        /* Making the Navigation system bar not overlapping with the activity */
                        // Root ViewGroup of my activity
                        ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->

                            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

                            // Apply the insets as a margin to the view. Here the system is setting
                            // only the bottom, left, and right dimensions, but apply whichever insets are
                            // appropriate to your layout. You can also update the view padding
                            // if that's more appropriate.

                            view.layoutParams = (view.layoutParams as FrameLayout.LayoutParams).apply {
                                leftMargin = insets.left
                                bottomMargin = insets.bottom
                                rightMargin = insets.right
                            }

                            // Return CONSUMED if you don't want want the window insets to keep being
                            // passed down to descendant views.
                            WindowInsetsCompat.CONSUMED
                        }
                    }
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    if (isDarkTheme) {
                        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    } else {
                        window.decorView.systemUiVisibility =
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    }

                    // Hide the status bar
                    if (hideStatusBar) {
                        window.decorView.systemUiVisibility = (window.decorView.systemUiVisibility
                                or View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                        window.statusBarColor = Color.TRANSPARENT

                    } else {
                        window.statusBarColor = statusBarColor

                    }
                    // Hide the navigation bar
                    if (hideNavigationBar) {
                        window.decorView.systemUiVisibility = (window.decorView.systemUiVisibility
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                        window.navigationBarColor = Color.TRANSPARENT

                    } else {
                        window.navigationBarColor = statusBarColor

                    }

                }
            } catch (e: Exception) {
                Toast.makeText(window.context,"Unable to perform the task",Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}