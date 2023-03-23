package com.atomykcoder.atomykplay.classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.atomykcoder.atomykplay.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class GlideBuilt {

    private final Context context;

    public GlideBuilt(Context _context) {
        context = _context;
    }

    public void glide(String uri, int placeholderImage, ImageView imageView, int image_measure) {
        try {
            Glide.with(context).load(uri).apply(new RequestOptions().placeholder(placeholderImage).error(R.drawable.ic_music_thumbnail))
                    .override(image_measure, image_measure)
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void glideBitmap(Bitmap bitmap, int placeholderImage, ImageView imageView, int image_measure) {
        try {
            Glide.with(context).load(bitmap).apply(new RequestOptions().placeholder(placeholderImage).error(R.drawable.ic_music_thumbnail))
                    .override(image_measure, image_measure)
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
