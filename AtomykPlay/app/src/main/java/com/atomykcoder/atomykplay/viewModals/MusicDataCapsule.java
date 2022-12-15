package com.atomykcoder.atomykplay.viewModals;

import java.io.Serializable;

public class MusicDataCapsule implements Serializable {
    String sName;
    String sArtist;
    String sAlbum;
    String sAlbumUri;
    String sDuration;
    String sPath;
    String sBitrate;
    String sMimeType;
    String sSize;
    String sGenre;
    String sId;
    String sDateAdded;

    public MusicDataCapsule(String sName, String sArtist, String sAlbum, String sAlbumUri, String sDuration, String sPath, String sBitrate, String sMimeType, String sSize, String sGenre, String sId, String sDateAdded) {
        this.sName = sName;
        this.sArtist = sArtist;
        this.sAlbum = sAlbum;
        this.sAlbumUri = sAlbumUri;
        this.sDuration = sDuration;
        this.sPath = sPath;
        this.sBitrate = sBitrate;
        this.sMimeType = sMimeType;
        this.sSize = sSize;
        this.sGenre = sGenre;
        this.sId = sId;
        this.sDateAdded = sDateAdded;
    }

    public String getsBitrate() {
        return sBitrate;
    }

    public String getsMimeType() {
        return sMimeType;
    }

    public String getsSize() {
        return sSize;
    }

    public String getsGenre() {
        return sGenre;
    }

    public String getsName() {
        return sName;
    }

    public String getsArtist() {
        return sArtist;
    }

    public String getsAlbum() {
        return sAlbum;
    }

    public String getsAlbumUri() {
        return sAlbumUri;
    }

    public String getsDuration() {
        return sDuration;
    }

    public String getsPath() {
        return sPath;
    }

    public String getsId() {
        return sId;
    }

    public String getsDateAdded() {
        return sDateAdded;
    }
}
