package com.atomykcoder.atomykplay.classes;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.atomykcoder.atomykplay.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;

public class GlideBuilt {

    public static void glide(Context context, String uri,int placeholderImage, ImageView imageView, int image_measure){
        Glide.with(context).load(uri).apply(new RequestOptions().placeholder(placeholderImage))
                .override(image_measure, image_measure)
                .into(imageView);
    }
}
