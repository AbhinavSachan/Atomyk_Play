package com.atomykcoder.atomykplay.fragments;

import static com.atomykcoder.atomykplay.activities.MainActivity.TAG_EDITOR_COVER;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.RemoteController;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
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
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.classes.GlideBuilt;
import com.atomykcoder.atomykplay.data.Music;
import com.atomykcoder.atomykplay.helperFunctions.MusicHelper;
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
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.datatype.Artwork;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TagEditorFragment extends Fragment {

    public ImageView coverImageView;
    private EditText editName, editArtist, editAlbum, editGenre;
    private ImageView pickImageView;
    private FloatingActionButton saveButton;
    private MainActivity mainActivity;
    private Music music;
    private ContentValues cv;
    private Uri imageUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tag_editor, container, false);
        String decodeMessage = (String) (getArguments() != null ? getArguments().getSerializable("currentMusic") : null);

        music = MusicHelper.decode(decodeMessage);


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
        mainActivity = (MainActivity) getContext();

        if (music != null) {
            editName.setText(music.getName());
            editArtist.setText(music.getArtist());
            editAlbum.setText(music.getAlbum());
            editGenre.setText(music.getGenre());
            final Bitmap[] image = {null};
            ExecutorService service1 = Executors.newSingleThreadExecutor();
            Handler handler = new Handler();
            service1.execute(() -> {

                //image decoder
                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(music.getPath());
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
                if (!Environment.isExternalStorageManager()) {
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
        } catch (Exception ex) {
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

    private void saveMusicChanges(Music music) {
        String newTitle = editName.getText().toString().trim();
        String newArtist = editArtist.getText().toString().trim();
        String newAlbum = editAlbum.getText().toString().trim();
        String newGenre = editGenre.getText().toString().trim();
        ExecutorService service = Executors.newSingleThreadExecutor();
        Handler handler = new Handler();
        service.execute(() -> {

            try {
                File musicFile = new File(music.getPath());
                AudioFile f = AudioFileIO.read(musicFile);
                Tag tag = f.getTag();


                tag.setField(FieldKey.TITLE, newTitle);
                tag.setField(FieldKey.ARTIST, newArtist);
                tag.setField(FieldKey.ALBUM, newAlbum);
                tag.setField(FieldKey.GENRE, newGenre);

                if (imageUri != null) {
                    File imageFile = new File(imageUri.toString());
                    if (imageFile.exists()) {
                        Artwork artwork = Artwork.createArtworkFromFile(imageFile);
                        tag.addField(artwork);
                        tag.setField(artwork);
                    }
                }

                f.setTag(tag);
                f.commit();

                handler.post(() -> {
                    Toast.makeText(getContext(), "Changes will be applied after restart", Toast.LENGTH_SHORT).show();
                    requireContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(musicFile)));
                    requireActivity().onBackPressed();
                });
            } catch (CannotReadException | InvalidAudioFrameException | ReadOnlyFileException | TagException | IOException | CannotWriteException e) {
                e.printStackTrace();
                handler.post(() -> {
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                });
            }
        });
        service.shutdown();

    }

    public void setImageUri(Uri album_uri) {
        imageUri = album_uri;
        GlideBuilt.glide(requireContext(), String.valueOf(imageUri), 0, coverImageView, 412);
    }
}