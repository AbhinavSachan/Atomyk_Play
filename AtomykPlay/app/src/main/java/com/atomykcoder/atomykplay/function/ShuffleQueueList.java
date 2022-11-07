package com.atomykcoder.atomykplay.function;

import java.util.ArrayList;
import java.util.Collections;

public class ShuffleQueueList {

    public void shuffle(ArrayList<MusicDataCapsule> musicList, MusicDataCapsule activeMusic, int musicIndex, StorageUtil storageUtil) {

        //removing current item from list
        musicList.remove(musicIndex);
        //shuffling list
        Collections.shuffle(musicList);
        //adding the removed item in shuffled list on 0th index
        musicList.add(0, activeMusic);
        //saving list
        storageUtil.saveMusicList(musicList);
        //saving index
        storageUtil.saveMusicIndex(0);

    }

}
