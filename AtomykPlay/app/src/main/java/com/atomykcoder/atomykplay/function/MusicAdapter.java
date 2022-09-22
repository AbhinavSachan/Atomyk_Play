package com.atomykcoder.atomykplay.function;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.MainActivity;
import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.player.PlayerFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiFunction;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewAdapter>{
    Context context;
    ArrayList<MusicDataCapsule> musicData;



    public MusicAdapter(Context context, ArrayList<MusicDataCapsule> musicData) {
        this.context = context;
        this.musicData = musicData;
    }

    @NonNull
    @Override
    public MusicViewAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.music_item_layout, parent, false);
        return new MusicViewAdapter(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull MusicViewAdapter holder, @SuppressLint("RecyclerView") int position) {
        MusicDataCapsule currentItem = musicData.get(position);

        if (currentItem.getsAlbumUri() != null) {

            try {
                Glide.with(context).load(getAlbumArt(context,currentItem.getsAlbumUri())).apply(new RequestOptions()
                        .override(150, 150)).into(holder.imageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //playing song
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) context;

                mainActivity.playAudio(position);
            }
        });

        //add bottom sheet functions in three dot click
        holder.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show();
            }
        });


        String sName = currentItem.getsName()
                .replace("y2mate.com - ", "")
                .replace("&#039;", "'")
                .replace("%20", " ")
                .replace("_", " ")
                .replace("&amp;", ",");

        holder.nameText.setText(sName);
        holder.artistText.setText(currentItem.getsArtist());
        holder.durationText.setText(currentItem.getsLength());
    }

    private static Bitmap getAlbumArt(Context context, String uri) {
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();

        try {
            ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(Uri.parse(uri),"r");
            if (parcelFileDescriptor != null) {
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fileDescriptor,null,options);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return bm;
    }

    @Override
    public int getItemCount() {
        return musicData.size();
    }



    public static class MusicViewAdapter extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final ImageView imageButton;
        private final RelativeLayout cardView;
        private final TextView nameText, artistText, durationText;

        public MusicViewAdapter(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.song_album_cover);
            imageButton = itemView.findViewById(R.id.more_option_i_btn);
            cardView = itemView.findViewById(R.id.cv_song_play);
            nameText = itemView.findViewById(R.id.song_name);
            artistText = itemView.findViewById(R.id.song_artist_name);
            durationText = itemView.findViewById(R.id.song_length);

        }
    }
}
