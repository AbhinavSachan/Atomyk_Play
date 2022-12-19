package com.atomykcoder.atomykplay.fragments;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.atomykcoder.atomykplay.BuildConfig;
import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.classes.GlideBuilt;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.datatype.Artwork;
import org.jaudiotagger.tag.id3.ID3v24Tag;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TagEditorFragment extends Fragment {

    private static final int TAG_EDITOR_COVER = 6542;
    private EditText editName, editArtist, editAlbum, editGenre;
    private ImageView pickImageView;
    public ImageView coverImageView;
    private FloatingActionButton saveButton;
    private MusicDataCapsule music;
    private ContentValues cv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tag_editor, container, false);

         music = (MusicDataCapsule) (getArguments() != null ? getArguments().getSerializable("currentMusic") : null);

        Toolbar toolbar = view.findViewById(R.id.toolbar_tag_editor);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        editName = view.findViewById(R.id.edit_song_name_tag);
        editArtist = view.findViewById(R.id.edit_song_artist_tag);
        editAlbum = view.findViewById(R.id.edit_song_album_tag);
        editGenre = view.findViewById(R.id.edit_song_genre_tag);
        pickImageView = view.findViewById(R.id.pick_cover_tag);
        coverImageView = view.findViewById(R.id.song_image_view_tag);
        saveButton = view.findViewById(R.id.tag_editor_save_button);

        if (music != null) {
            editName.setText(music.getsName());
            editArtist.setText(music.getsArtist());
            editAlbum.setText(music.getsAlbum());
            editGenre.setText(music.getsGenre());
            final Bitmap[] image = {null};
            ExecutorService service1 = Executors.newSingleThreadExecutor();
            Handler handler = new Handler();
            service1.execute(() -> {

                //image decoder
                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(music.getsPath());
                byte[] art = mediaMetadataRetriever.getEmbeddedPicture();

                try {
                    image[0] = BitmapFactory.decodeByteArray(art, 0, art.length);
                } catch (Exception ignored) {
                }
                handler.post(() -> {
                    GlideBuilt.glideBitmap(requireContext(), image[0], R.drawable.ic_music, coverImageView, 412);
                });
            });
            service1.shutdown();
        }
        pickImageView.setOnClickListener(v -> {
            Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            requireActivity().startActivityForResult(gallery, TAG_EDITOR_COVER);
        });

        saveButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if(!Environment.isExternalStorageManager()) {
                    requestPermissionAndroid11AndAbove();
                } else {
                    saveMusicChanges(music);
                }
            } else {
                requestPermissionAndroidBelow11();
            }
        });

        return view;
    }


    @RequiresApi(api = Build.VERSION_CODES.R)
    private void requestPermissionAndroid11AndAbove() {
        try {
            Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
            startActivity(intent);
        } catch (Exception ex){
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivity(intent);
        }
    }

    private void requestPermissionAndroidBelow11() {
        Dexter.withContext(getContext())
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        saveMusicChanges(music);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    private void saveMusicChanges(MusicDataCapsule music) {
        String newTitle = editName.getText().toString().trim();
        String newArtist = editArtist.getText().toString().trim();
        String newAlbum = editAlbum.getText().toString().trim();
        String newGenre = editGenre.getText().toString().trim();

        try {
            AudioFile f = AudioFileIO.read(new File(music.getsPath()));
            Tag tag = f.getTag();

            Uri imageUri = (Uri) coverImageView.getTag();

            tag.setField(FieldKey.TITLE, newTitle);
            tag.setField(FieldKey.ARTIST, newArtist);
            tag.setField(FieldKey.ALBUM, newAlbum);
            tag.setField(FieldKey.GENRE, newGenre);

            if(imageUri != null) {
                File file = new File(imageUri.getPath().replace("/raw/", ""));
                Artwork artwork = Artwork.createArtworkFromFile(file);
                tag.addField(artwork);
                tag.setField(artwork);
            }
            f.commit();
            Toast.makeText(requireContext(), "Changes Made Successfully", Toast.LENGTH_SHORT).show();

        } catch (CannotReadException | InvalidAudioFrameException | ReadOnlyFileException | TagException | IOException | CannotWriteException e) {
            e.printStackTrace();
        }

    }

    private void print(String message) {
        System.out.println("message : " + message);
    }
}