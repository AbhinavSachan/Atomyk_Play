package com.atomykcoder.atomykplay.helperFunctions

import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly

object CustomMethods {
    @JvmStatic
    fun pickImage(
        intentActivityResultLauncher: ActivityResultLauncher<Intent?>,
        pickVisualMediaRequestActivityResultLauncher: ActivityResultLauncher<PickVisualMediaRequest?>
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intentActivityResultLauncher.launch(Intent(MediaStore.ACTION_PICK_IMAGES).setType("image/*"))
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R || Build.VERSION.SDK_INT == Build.VERSION_CODES.S || Build.VERSION.SDK_INT == Build.VERSION_CODES.S_V2) {
            pickVisualMediaRequestActivityResultLauncher.launch(
                PickVisualMediaRequest.Builder()
                    .setMediaType(ImageOnly)
                    .build()
            )
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intentActivityResultLauncher.launch(intent)
        }
    }
}