package com.atomykcoder.atomykplay.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;

import com.atomykcoder.atomykplay.R;
import com.google.android.material.card.MaterialCardView;

import de.hdodenhof.circleimageview.CircleImageView;

public class AboutFragment extends Fragment {

    private MaterialCardView akash_card, abhinav_card;
    private CircleImageView aks_img, abh_img;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar_about);
        aks_img = view.findViewById(R.id.akash_profile);
        abh_img = view.findViewById(R.id.abhinav_profile);
        akash_card = view.findViewById(R.id.akash_card);
        abhinav_card = view.findViewById(R.id.abhinav_card);

        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        Bitmap aks = BitmapFactory.decodeResource(getResources(),R.drawable.akash);
        Bitmap abh = BitmapFactory.decodeResource(getResources(),R.drawable.abhinav);
        aks_img.setImageBitmap(aks);
        abh_img.setImageBitmap(abh);

        akash_card.setCardBackgroundColor(getColor(aks));
        abhinav_card.setCardBackgroundColor(getColor(abh));

        return view;
    }
    private int getColor(Bitmap bitmap){
        Palette palette = Palette.from(bitmap).generate();
        return palette.getLightVibrantColor(getResources().getColor(R.color.secondary_bg,null));
    }
}