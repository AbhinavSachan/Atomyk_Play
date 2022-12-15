package com.atomykcoder.atomykplay.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;

public class TagEditorFragment extends Fragment {

    public static final int PICK_IMAGE_COVER = 6542;
    private MainActivity mainActivity;
    private EditText editName, editArtist, editAlbum, editGenre;
    private ImageView imageViewShow, imageViewPick;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tag_editor, container, false);

        MusicDataCapsule music = (MusicDataCapsule) (getArguments() != null ? getArguments().getSerializable("currentMusic") : null);

        mainActivity = (MainActivity) requireContext();
        Toolbar toolbar = view.findViewById(R.id.toolbar_tag_editor);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        editName = view.findViewById(R.id.edit_song_name_tag);
        editArtist = view.findViewById(R.id.edit_song_artist_tag);
        editAlbum = view.findViewById(R.id.edit_song_album_tag);
        editGenre = view.findViewById(R.id.edit_song_genre_tag);
        imageViewPick = view.findViewById(R.id.pick_cover_tag);
        imageViewShow = view.findViewById(R.id.song_image_view_tag);

        if (music != null) {
            editName.setText(music.getsName());
            editArtist.setText(music.getsArtist());
            editAlbum.setText(music.getsAlbum());
            editGenre.setText(music.getsGenre());
        }
        imageViewPick.setOnClickListener(v -> {
            Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            requireActivity().startActivityForResult(gallery, PICK_IMAGE_COVER);
        });

        return view;
    }

}