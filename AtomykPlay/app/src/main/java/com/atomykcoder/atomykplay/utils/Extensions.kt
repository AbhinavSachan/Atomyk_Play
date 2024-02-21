package com.atomykcoder.atomykplay.utils

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.atomykcoder.atomykplay.R

fun AppCompatActivity.showToast(msg: String? = null) {
    Toast.makeText(this, msg ?: this.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT)
        .show()
}

fun Fragment.showToast(msg: String? = null) {
    Toast.makeText(
        this.context,
        msg ?: this.context?.getString(R.string.something_went_wrong),
        Toast.LENGTH_SHORT
    ).show()
}

fun Context.showToast(msg: String? = null) {
    Toast.makeText(
        this, msg ?: this.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT
    ).show()
}