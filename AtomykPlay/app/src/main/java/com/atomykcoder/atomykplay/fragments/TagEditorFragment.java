package com.atomykcoder.atomykplay.fragments;

import static com.atomykcoder.atomykplay.helperFunctions.CustomMethods.pickImage;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.atomykcoder.atomykplay.BuildConfig;
import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.classes.ApplicationClass;
import com.atomykcoder.atomykplay.classes.GlideBuilt;
import com.atomykcoder.atomykplay.data.Music;
import com.atomykcoder.atomykplay.helperFunctions.Logger;
import com.atomykcoder.atomykplay.helperFunctions.MusicHelper;
import com.atomykcoder.atomykplay.repository.LoadingStatus;
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
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.framebody.FrameBodyAPIC;
import org.jaudiotagger.tag.id3.framebody.FrameBodyUSLT;
import org.jaudiotagger.tag.id3.valuepair.TextEncoding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TagEditorFragment extends Fragment {

    private final MutableLiveData<LoadingStatus> loadingStatus = new MutableLiveData<>();
    public ImageView coverImageView;
    ExecutorService service = Executors.newFixedThreadPool(1);
    Handler handler = new Handler();
    GlideBuilt glideBuilt;
    private EditText editName, editArtist, editAlbum,editYear, editGenre;
    private Music music;
    private Uri imageUri;
    // Registers a photo picker activity launcher in single-select mode.
    private final ActivityResultLauncher<PickVisualMediaRequest> mediaPicker = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            setImageUri(uri);
        }
    });
    private final ActivityResultLauncher<Intent> pickIntent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            if (result.getData() != null) {
                setImageUri(result.getData().getData());
            }
        }
    });
    private Uri musicUri;
    private View progressIndicator;

    @Override
    public void onDestroy() {
        super.onDestroy();
        service.shutdown();
        progressIndicator = null;
        coverImageView = null;
        editName = null;
        editArtist = null;
        editGenre = null;
        editAlbum = null;
        editYear = null;
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            cursor.moveToFirst();
            int column_index = cursor.getColumnIndex(proj[0]);
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public LiveData<LoadingStatus> getLoadingStatus() {
        return loadingStatus;
    }

    public void setLoadingStatus(LoadingStatus status) {
        loadingStatus.setValue(status);
    }

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
        progressIndicator = view.findViewById(R.id.progress_bar_tag);
        editArtist = view.findViewById(R.id.edit_song_artist_tag);
        editAlbum = view.findViewById(R.id.edit_song_album_tag);
        editYear = view.findViewById(R.id.edit_song_year_tag);
        editGenre = view.findViewById(R.id.edit_song_genre_tag);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            editGenre.setVisibility(View.GONE);
        }
        ImageView pickImageView = view.findViewById(R.id.pick_cover_tag);
        coverImageView = view.findViewById(R.id.song_image_view_tag);
        FloatingActionButton saveButton = view.findViewById(R.id.tag_editor_save_button);
        glideBuilt = new GlideBuilt(requireContext());

        if (music != null) {
            editName.setText(music.getName());
            editArtist.setText(music.getArtist());
            editAlbum.setText(music.getAlbum());
            editYear.setText(music.getYear());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                editGenre.setText(music.getGenre());
            }
            final Bitmap[] image = {null};


            service.execute(() -> {

                //image decoder
                byte[] art = new byte[0];
                try (MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever()) {
                    mediaMetadataRetriever.setDataSource(music.getPath());
                    art = mediaMetadataRetriever.getEmbeddedPicture();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    image[0] = BitmapFactory.decodeByteArray(art, 0, art.length);
                } catch (Exception ignored) {
                }
                handler.post(() -> {
                    glideBuilt.glideBitmap(image[0], R.drawable.ic_music, coverImageView, 412, false);
                });
            });
        }
        pickImageView.setOnClickListener(v -> pickImage(pickIntent, mediaPicker));

        saveButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    requestPermissionAndroid11AndAbove();
                } else {
                    saveMusicChanges(music);
                }
            } else {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionAndroidBelow11();
                } else {
                    saveMusicChanges(music);
                }
            }
        });

        setLoader();

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
        Dexter.withContext(requireContext())
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            saveMusicChanges(music);
                        } else {
                            showToast("Permissions denied!");
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    private void showToast(String s) {
        ((ApplicationClass) requireActivity().getApplication()).showToast(s);
    }

    private void saveMusicChanges(Music music) {
        String newTitle = editName.getText().toString().trim();
        String newArtist = editArtist.getText().toString().trim();
        String newAlbum = editAlbum.getText().toString().trim();
        String newYear = editYear.getText().toString().trim();
        String newGenre = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            newGenre = editGenre.getText().toString().trim();
        }
        setLoadingStatus(LoadingStatus.LOADING);
        String finalNewGenre = newGenre;
        service.execute(() -> {
            try {

                File musicFile = new File(music.getPath());
                AudioFile f = AudioFileIO.read(musicFile);
                Tag tag = f.getTag();

                tag.setField(FieldKey.TITLE, newTitle);
                tag.setField(FieldKey.ARTIST, newArtist);
                tag.setField(FieldKey.ALBUM, newAlbum);
                tag.setField(FieldKey.YEAR, newYear);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    tag.setField(FieldKey.GENRE, finalNewGenre);
                }

                if (imageUri != null) {
                    String filePath = getRealPathFromURI(requireContext(), imageUri);
                    File imageFile = new File(filePath);

                    Artwork artwork = Artwork.createArtworkFromFile(imageFile);
                    tag.setField(artwork);
                }

                f.setTag(tag);
                f.commit();

                handler.post(() -> {
                    musicUri = Uri.fromFile(musicFile);
                    setLoadingStatus(LoadingStatus.SUCCESS);
                });
            } catch (CannotReadException | InvalidAudioFrameException | ReadOnlyFileException |
                     TagException | IOException | CannotWriteException e) {
                Logger.normalLog(e.toString());
                handler.post(() -> {
                    setLoadingStatus(LoadingStatus.FAILURE);
                });
            }
        });

    }

    private void setLoader() {
        getLoadingStatus().observe(requireActivity(), it -> {
            switch (it) {
                case LOADING:
                    progressIndicator.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    showToast("Change's will be applied after restart");
                    requireContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, musicUri));
                    progressIndicator.setVisibility(View.GONE);
                    requireActivity().onBackPressed();
                    break;
                case FAILURE:
                    showToast("Something went wrong");
                    progressIndicator.setVisibility(View.GONE);
                    requireActivity().onBackPressed();
                    break;
            }
        });
    }

    private void setImageUri(Uri album_uri) {
        imageUri = album_uri;
        glideBuilt.glide(String.valueOf(imageUri), 0, coverImageView, 512);
    }
}