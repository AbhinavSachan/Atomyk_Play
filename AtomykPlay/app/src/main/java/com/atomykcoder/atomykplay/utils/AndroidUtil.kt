package com.atomykcoder.atomykplay.utils

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.atomykcoder.atomykplay.R


object AndroidUtil {
    fun Drawable.toUnscaledBitmap(): Bitmap? {
        if (this is BitmapDrawable) {
            return this.bitmap
        }

        val bitmap = Bitmap.createBitmap(
            intrinsicWidth, intrinsicHeight, Bitmap.Config.RGB_565
        )

        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)

        return bitmap
    }

    fun pxToDp(resources: Resources, px: Int): Float {
        val density = resources.displayMetrics.density
        return px / density
    }

    fun setTheme(window: Window?, isDark: Boolean) {
        window?.setWindowAnimations(getAnimation(isDark))
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        window?.let { setSystemBarColor(it, isDark) }
    }

    private fun getAnimation(isDark: Boolean): Int {
        return if (isDark) {
            R.style.FadeAnimationDark
        } else {
            R.style.FadeAnimationLight
        }
    }

    private fun setSystemBarColor(window: Window, isDarkTheme: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = try {
                window.insetsController
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            controller?.apply {
                if (isDarkTheme) {
                    setSystemBarsAppearance(
                        0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                    setSystemBarsAppearance(
                        0, WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    )
                } else {
                    setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                    setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    )
                }
            }
        } else {
            if (!isDarkTheme) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }

    }

    fun setSystemDrawBehindBars(window: Window, isDarkTheme: Boolean, root: View) {
        // Alternate flags for Android API level 30 and above
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val controller = window.insetsController
                controller?.apply {
                    systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    // Set light or dark status bar based on theme
                    setSystemBarColor(window, isDarkTheme)
                    // Set window insets to draw behind status bar but not navigation bar
                    window.attributes.layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    window.let {
                        WindowCompat.setDecorFitsSystemWindows(
                            it, false
                        )
                    }/* Making the Navigation system bar not overlapping with the activity */
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
            }
        } catch (e: Exception) {
            Toast.makeText(window.context, "Unable to perform the task", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    fun Window.hideSystemUi(
        hideSystemUi: Boolean
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = insetsController
            controller?.apply {
                if (hideSystemUi) {
                    hide(WindowInsetsCompat.Type.statusBars())
                    statusBarColor = Color.TRANSPARENT

                    hide(WindowInsetsCompat.Type.navigationBars())
                    navigationBarColor = Color.TRANSPARENT
                } else {
                    show(WindowInsetsCompat.Type.statusBars())
                    statusBarColor = Color.TRANSPARENT

                    show(WindowInsetsCompat.Type.navigationBars())
                    navigationBarColor = context.getColor(R.color.player_bg)
                }
            }
        } else {

            // Hide the status bar
            if (hideSystemUi) {
                decorView.systemUiVisibility =
                    (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                statusBarColor = Color.TRANSPARENT
                navigationBarColor = Color.TRANSPARENT
            } else {
                statusBarColor = Color.TRANSPARENT
                navigationBarColor = context.getColor(R.color.player_bg)
            }
        }
    }

}