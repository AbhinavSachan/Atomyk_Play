package com.atomykcoder.atomykplay.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;

import com.atomykcoder.atomykplay.BuildConfig;
import com.atomykcoder.atomykplay.R;
import com.google.android.material.card.MaterialCardView;

import de.hdodenhof.circleimageview.CircleImageView;

public class AboutFragment extends Fragment {

    private final Uri flatIconUri = Uri.parse("https://www.flaticon.com/");
    private final Uri svgRepoUri = Uri.parse("https://www.svgrepo.com/");
    private final Uri lottieFilesUri = Uri.parse("https://lottiefiles.com/");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar_about);
        CircleImageView abh_img = view.findViewById(R.id.abhinav_profile);
        MaterialCardView abhinav_card = view.findViewById(R.id.abhinav_card);

        TextView flatIcon = view.findViewById(R.id.flat_icon);
        TextView svgRepo = view.findViewById(R.id.svg_repo);
        TextView lottie = view.findViewById(R.id.lottie_files);
        TextView verTv = view.findViewById(R.id.version_name);
        TextView rateUs = view.findViewById(R.id.rate_us);
        TextView changelog = view.findViewById(R.id.changelog);
        TextView openLicense = view.findViewById(R.id.open_license);


        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        Bitmap aks = BitmapFactory.decodeResource(getResources(), R.drawable.akash);
        Bitmap abh = BitmapFactory.decodeResource(getResources(), R.drawable.abhinav);
        abh_img.setImageBitmap(abh);

        abhinav_card.setCardBackgroundColor(getColor(abh));

        flatIcon.setOnClickListener(v -> openLinkInChrome(flatIconUri));
        svgRepo.setOnClickListener(v -> openLinkInChrome(svgRepoUri));
        lottie.setOnClickListener(v -> openLinkInChrome(lottieFilesUri));
        rateUs.setOnClickListener(v -> rateUs());
        changelog.setOnClickListener(v -> changes());
        openLicense.setOnClickListener(v -> changes());
        verTv.setText(BuildConfig.VERSION_NAME);
        return view;
    }

    private void changes() {
        Toast.makeText(requireContext(), "Coming soon!", Toast.LENGTH_SHORT).show();
    }

    private int getColor(Bitmap bitmap) {
        Palette palette = Palette.from(bitmap).generate();
        return palette.getLightVibrantColor(getResources().getColor(R.color.secondary_bg, null));
    }

    private void openLinkInChrome(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    private void rateUs() {
        Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + requireContext().getPackageName()));
        startActivity(rateIntent);
    }
}