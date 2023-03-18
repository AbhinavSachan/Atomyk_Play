package com.atomykcoder.atomykplay.helperFunctions;

import android.content.Intent;
import android.os.Build;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

public class CustomMethods {

    public static void pickImage(ActivityResultLauncher<Intent> intentActivityResultLauncher, ActivityResultLauncher<PickVisualMediaRequest> pickVisualMediaRequestActivityResultLauncher) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intentActivityResultLauncher.launch(new Intent(MediaStore.ACTION_PICK_IMAGES).setType("image/*"));
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R || Build.VERSION.SDK_INT == Build.VERSION_CODES.S || Build.VERSION.SDK_INT == Build.VERSION_CODES.S_V2) {
            pickVisualMediaRequestActivityResultLauncher.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intentActivityResultLauncher.launch(intent);
        }
    }
}
