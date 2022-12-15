package com.atomykcoder.atomykplay.helperFunctions;

import java.util.ArrayList;

public class CompareIDList {

    /**
     * check if main list items contains all playlist items or not
     *
     * @param _mainListIds main data list items
     * @param _playlistIds play list items
     * @return returns items to delete from play list items
     */
    public static ArrayList<String> compare(
            ArrayList<String> _mainListIds,
            ArrayList<String> _playlistIds
    ) {
        ArrayList<String> itemsToDelete = new ArrayList<>();

        for (String musicID : _playlistIds) {
            if (!contains(musicID, _mainListIds)) {
                itemsToDelete.add(musicID);
            }
        }
        return itemsToDelete;
    }

    private static boolean contains(String item, ArrayList<String> idList) {
        for (String musicID : idList) {
            if (musicID.equals(item)) {
                return true;
            }
        }
        return false;
    }
}
