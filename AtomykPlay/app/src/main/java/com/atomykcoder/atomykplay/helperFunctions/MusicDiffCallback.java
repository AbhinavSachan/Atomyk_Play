package com.atomykcoder.atomykplay.helperFunctions;

import androidx.recyclerview.widget.DiffUtil.Callback;

import com.atomykcoder.atomykplay.data.Music;

import java.util.ArrayList;
import java.util.Objects;

public class MusicDiffCallback extends Callback {

    private final ArrayList<Music> oldMusicList;
    private final ArrayList<Music> newMusicList;

    public MusicDiffCallback(ArrayList<Music> oldMusicList, ArrayList<Music> newMusicList) {
        this.oldMusicList = oldMusicList;
        this.newMusicList = newMusicList;
    }

    @Override
    public int getOldListSize() {
        return oldMusicList.size();
    }

    @Override
    public int getNewListSize() {
        return newMusicList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return Objects.equals(oldMusicList.get(oldItemPosition).getId(), newMusicList.get(newItemPosition).getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Music oldMusic = oldMusicList.get(oldItemPosition);
        Music newMusic = newMusicList.get(newItemPosition);
        return oldMusic.getName().equals(newMusic.getName())
                && oldMusic.getAlbum().equals(newMusic.getAlbum())
                && oldMusic.getDateAdded().equals(newMusic.getDateAdded())
                && oldMusic.getDuration().equals(newMusic.getDuration())
                && oldMusic.getArtist().equals(newMusic.getArtist());
    }

}
