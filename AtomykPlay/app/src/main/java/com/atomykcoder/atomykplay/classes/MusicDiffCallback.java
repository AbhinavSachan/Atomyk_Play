package com.atomykcoder.atomykplay.classes;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil.Callback;

import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;

import java.util.ArrayList;
import java.util.Objects;

public class MusicDiffCallback extends Callback {
    private final ArrayList<MusicDataCapsule> oldMusicList;
    private final ArrayList<MusicDataCapsule> newMusicList;

    public MusicDiffCallback(ArrayList<MusicDataCapsule> oldMusicList, ArrayList<MusicDataCapsule> newMusicList) {
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
        return Objects.equals(oldMusicList.get(oldItemPosition).getsId(), newMusicList.get(newItemPosition).getsId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        MusicDataCapsule oldMusic = oldMusicList.get(oldItemPosition);
        MusicDataCapsule newMusic = newMusicList.get(newItemPosition);
        return oldMusic.getsName().equals(newMusic.getsName()) &&  oldMusic.getsLength().equals(newMusic.getsLength());
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
