package com.atomykcoder.atomykplay.classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

public class GlideBuilt {

    public static void glide(Context context, String uri, int placeholderImage, ImageView imageView, int image_measure) {
        Glide.with(context).load(uri).apply(new RequestOptions().placeholder(placeholderImage))
                .override(image_measure, image_measure)
                .into(imageView);
    }

    public static void glideBitmap(Context context, Bitmap bitmap, int placeholderImage, ImageView imageView, int image_measure) {
        Glide.with(context).load(bitmap).apply(new RequestOptions().placeholder(placeholderImage))
                .override(image_measure, image_measure)
                .into(imageView);
    }
}
