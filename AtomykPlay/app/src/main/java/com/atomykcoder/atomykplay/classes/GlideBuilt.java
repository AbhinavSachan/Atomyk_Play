package com.atomykcoder.atomykplay.classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.atomykcoder.atomykplay.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.TransitionOptions;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

public class GlideBuilt {

    private final Context context;

    public GlideBuilt(Context _context) {
        context = _context;
    }

    public void glide(String uri, int placeholderImage, ImageView imageView, int image_measure) {
        try {
            Glide.with(context).load(uri).apply(new RequestOptions().placeholder(placeholderImage).error(R.drawable.ic_music_thumbnail))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .override(image_measure, image_measure)
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void glideBitmap(Bitmap newBitmap, int placeholderImage, ImageView imageView, int image_measure,boolean animate) {
        if (newBitmap != null){
            placeholderImage = -1;
        }
        DrawableTransitionOptions drawableTransitionOptions;
        if (animate){
            drawableTransitionOptions = DrawableTransitionOptions.withCrossFade(300);
        }else {
            drawableTransitionOptions = new DrawableTransitionOptions().dontTransition();
        }
        try {
            Glide.with(context).load(newBitmap).apply(new RequestOptions().placeholder(placeholderImage).error(R.drawable.ic_music_thumbnail))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .transition(drawableTransitionOptions)
                    .override(image_measure, image_measure)
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
