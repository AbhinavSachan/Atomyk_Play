package com.atomykcoder.atomykplay.events;

import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;

import java.util.ArrayList;

public class SearchEvent {
    public ArrayList<MusicDataCapsule> dataList;
    public String query;

    public SearchEvent(String _query, ArrayList<MusicDataCapsule> _dataList) {
        query = _query;
        dataList = _dataList;
    }
}
