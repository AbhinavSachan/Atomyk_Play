package com.atomykcoder.atomykplay.viewModals;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class Playlist {
    private final String name;
//    private final String coverUri;
    private final Map<String, MusicDataCapsule> musicMap;

    public Playlist(String _name, Map<String, MusicDataCapsule> _musicMap) {
        name = _name;
        musicMap = _musicMap;
    }

    public Playlist(String _name) {
        name = _name;
        musicMap = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Map<String, MusicDataCapsule> getMusicList() {
        return musicMap;
    }

    public void addMusic(MusicDataCapsule music) {
        Log.i("info", "music added: " + music.getsId());
        musicMap.put(music.getsId(), music);
    }

    public void removeMusic(MusicDataCapsule music) {
        Log.i("info", "music removed");
        musicMap.remove(music.getsId());
    }


}
