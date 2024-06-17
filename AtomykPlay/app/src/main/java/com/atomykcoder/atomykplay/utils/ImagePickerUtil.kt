package com.atomykcoder.atomykplay.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity

object ImagePickerUtil {
    fun pickImage(
        intentActivityResultLauncher: ActivityResultLauncher<Intent?>,
        pickVisualMediaRequestActivityResultLauncher: ActivityResultLauncher<PickVisualMediaRequest?>
    ) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                intentActivityResultLauncher.launch(Intent(MediaStore.ACTION_PICK_IMAGES).setType("image/*"))
            }

            Build.VERSION.SDK_INT in Build.VERSION_CODES.R..Build.VERSION_CODES.S_V2 -> {
                pickVisualMediaRequestActivityResultLauncher.launch(
                    PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly).build()
                )
            }

            else -> {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intentActivityResultLauncher.launch(intent)
            }
        }
    }

    fun registerMediaPicker(
        activity: FragmentActivity, callback: (Uri) -> Unit
    ): ActivityResultLauncher<PickVisualMediaRequest?> {
        return activity.registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            uri?.let { callback(it) }
        }
    }

    fun registerPickIntent(
        activity: FragmentActivity, callback: (Uri) -> Unit
    ): ActivityResultLauncher<Intent?> {
        return activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    callback(uri)
                }
            }
        }
    }
}
