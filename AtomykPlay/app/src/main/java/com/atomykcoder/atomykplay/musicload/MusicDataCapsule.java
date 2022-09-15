package com.atomykcoder.atomykplay.musicload;

public class MusicDataCapsule {
    String sName, sArtist, sAlbumUri, sLength, sPath;

    public MusicDataCapsule(String sName, String sArtist, String sAlbumUri, String sLength, String sPath) {
        this.sName = sName;
        this.sArtist = sArtist;
        this.sAlbumUri = sAlbumUri;
        this.sLength = sLength;
        this.sPath = sPath;
    }


    public String getsPath() {
        return sPath;
    }

    public String getsName() {
        return sName;
    }

    public String getsArtist() {
        return sArtist;
    }

    public String getsAlbumUri() {
        return sAlbumUri;
    }

    public String getsLength() {
        return sLength;
    }
}
