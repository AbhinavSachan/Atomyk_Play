package com.atomykcoder.atomykplay.helperFunctions;

import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;

import java.util.ArrayList;

public class PlaylistHelper {

    /**
     * check if main list items contains all playlist items or not
     *
     * @param _mainListItems main data list items
     * @param _playlistItems play list items
     * @return returns items to delete from play list items
     */
    public static ArrayList<MusicDataCapsule> compareList(
            ArrayList<MusicDataCapsule> _mainListItems,
            ArrayList<MusicDataCapsule> _playlistItems
    ) {
        ArrayList<MusicDataCapsule> itemsToDelete = new ArrayList<>();

        for (MusicDataCapsule music : _playlistItems) {
            if (!contains(music, _mainListItems)) {
                itemsToDelete.add(music);
            }
        }
        return itemsToDelete;
    }

    private static boolean contains(MusicDataCapsule item, ArrayList<MusicDataCapsule> dataList) {
        for (MusicDataCapsule music : dataList) {
            if (music.getsId().equals(item.getsId())) {
                return true;
            }
        }
        return false;
    }
}
