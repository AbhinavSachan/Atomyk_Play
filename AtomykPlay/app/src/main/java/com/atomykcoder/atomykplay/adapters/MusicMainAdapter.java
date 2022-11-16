package com.atomykcoder.atomykplay.adapters;

import static com.atomykcoder.atomykplay.function.MusicHelper.convertDuration;
import static com.atomykcoder.atomykplay.function.StorageUtil.no_shuffle;
import static com.atomykcoder.atomykplay.function.StorageUtil.shuffle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;
import com.atomykcoder.atomykplay.function.StorageUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.turingtechnologies.materialscrollbar.INameableAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusicMainAdapter extends RecyclerView.Adapter<MusicMainAdapter.MusicViewAdapter> implements INameableAdapter {
    Context context;
    ArrayList<MusicDataCapsule> musicArrayList;


    public MusicMainAdapter(Context context, ArrayList<MusicDataCapsule> musicArrayList) {
        this.context = context;
        this.musicArrayList = musicArrayList;
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
                    //sending broadcast to start the music from main activity
                    MainActivity mainActivity = (MainActivity) context;
                    storage.saveMusicList(musicArrayList);
                    //if shuffle button is already on it will shuffle it from start
                    if (storage.loadShuffle().equals(shuffle)) {
                        MusicDataCapsule activeMusic;
                        ArrayList<MusicDataCapsule> shuffleList = new ArrayList<>(musicArrayList);
                        //saving list in temp for restore function in player fragment
                        storage.saveTempMusicList(musicArrayList);

                        if (musicArrayList != null) {
                            if (position != -1 && position < musicArrayList.size()) {
                                activeMusic = musicArrayList.get(position);
                            } else {
                                activeMusic = musicArrayList.get(0);
                            }

                            ExecutorService service = Executors.newSingleThreadExecutor();
                            Handler handler = new Handler(Looper.getMainLooper());
                            service.execute(() -> {
                                //removing current item from list
                                shuffleList.remove(position);
                                //shuffling list
                                Collections.shuffle(shuffleList);
                                //adding the removed item in shuffled list on 0th index
                                shuffleList.add(0, activeMusic);
                                //saving list
                                storage.saveMusicList(shuffleList);
                                storage.saveMusicIndex(0);
                                // post-execute code here
                                handler.post(()->{
                                    mainActivity.playAudio();
                                    mainActivity.bottomSheetPlayerFragment.setAdapterInQueue();
                                });
                            });
                            service.shutdown();

                        }
                    } else if (storage.loadShuffle().equals(no_shuffle)) {
                        //Store serializable music list to sharedPreference
                        storage.saveMusicList(musicArrayList);
                        storage.saveMusicIndex(position);
                        mainActivity.playAudio();
                        mainActivity.bottomSheetPlayerFragment.setAdapterInQueue();
                    }
                } else {
                    Toast.makeText(context, "Song is unavailable", Toast.LENGTH_SHORT).show();
                    notifyItemRemoved(position);
                }

            }
        });


        //add bottom sheet functions in three dot click
        holder.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show();
            }
        });


        holder.nameText.setText(currentItem.getsName());
        holder.artistText.setText(currentItem.getsArtist());
        holder.durationText.setText(convertDuration(currentItem.getsLength()));
    }

    @Override
    public int getItemCount() {
        return musicArrayList.size();
    }

    @Override
    public Character getCharacterForElement(int element) {
        return musicArrayList.get(element).getsName().charAt(0);
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