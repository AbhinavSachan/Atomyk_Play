package com.atomykcoder.atomykplay.function;

import static com.atomykcoder.atomykplay.function.FetchMusic.convertDuration;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.io.File;
import java.util.ArrayList;

public class MusicQueueAdapter extends RecyclerView.Adapter<MusicQueueAdapter.MusicViewAdapter> {
    Context context;
    ArrayList<MusicDataCapsule> musicArrayList;

    public MusicQueueAdapter(Context context, ArrayList<MusicDataCapsule> musicArrayList) {
        this.context = context;
        this.musicArrayList = musicArrayList;
    }

    @NonNull
    @Override
    public MusicQueueAdapter.MusicViewAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.queue_music_item_layout, parent, false);
        return new MusicViewAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicQueueAdapter.MusicViewAdapter holder, @SuppressLint("RecyclerView") int position) {
        MusicDataCapsule currentItem = musicArrayList.get(position);

        try {
            Glide.with(context).load(currentItem.getsAlbumUri()).apply(new RequestOptions().placeholder(R.drawable.ic_no_album)
                    .override(75, 75)).into(holder.imageView);
        } catch (Exception ignored) {
        }
//playing song
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(currentItem.getsPath());
                if (file.exists()) {
                    //check is service active
                    StorageUtil storage = new StorageUtil(context);
                    //Store serializable music list to sharedPreference
                    storage.saveMusicIndex(position);
                    MainActivity mainActivity = (MainActivity) context;
                    if (PlayerFragment.queueSheetFragment != null)
                        if (PlayerFragment.queueSheetFragment.isShowing()) {
                            PlayerFragment.queueSheetFragment.dismiss();
                        }
                    mainActivity.playAudio();
                } else {
                    Toast.makeText(context, "Audio file is unavailable", Toast.LENGTH_SHORT).show();
                    notifyItemRemoved(position);
                }

            }
        });

        //add bottom sheet functions in three dot click
        holder.imageButton.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                return false;
            }
        });


        holder.nameText.setText(currentItem.getsName());
        String index = String.valueOf(position + 1);
        holder.musicIndex.setText(index);
        holder.artistText.setText(currentItem.getsArtist());
        holder.durationText.setText(convertDuration(currentItem.getsLength()));
    }

    @Override
    public int getItemCount() {
        return musicArrayList.size();
    }

    public static class MusicViewAdapter extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final ImageView imageButton;
        private final RelativeLayout cardView;
        private final TextView nameText, artistText, durationText, musicIndex;

        public MusicViewAdapter(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.song_album_cover_queue);
            imageButton = itemView.findViewById(R.id.more_option_i_btn_queue);
            cardView = itemView.findViewById(R.id.cv_song_play_queue);
            nameText = itemView.findViewById(R.id.song_name_queue);
            musicIndex = itemView.findViewById(R.id.song_index_num_queue);
            artistText = itemView.findViewById(R.id.song_artist_name_queue);
            durationText = itemView.findViewById(R.id.song_length_queue);
        }
    }
}
