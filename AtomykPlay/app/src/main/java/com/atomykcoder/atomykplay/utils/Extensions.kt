package com.atomykcoder.atomykplay.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
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

fun Context.getBitmapFromAlbumUri(albumUri: String): Bitmap? {
    try {
        val inputStream = contentResolver.openInputStream(albumUri.toUri())
        if (inputStream != null) {
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            val img = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()
            return img
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun Array<Uri?>?.toNonNullArray(): Array<Uri> {
    return this?.filterNotNull()?.toTypedArray() ?: emptyArray()
}
