package com.atomykcoder.atomykplay.function;

public class MusicDataCapsule {
    String sName, sArtist, sAlbum, sAlbumUri, sLength, sPath;

    public MusicDataCapsule(String sName, String sArtist, String sAlbum, String sAlbumUri, String sLength, String sPath) {
        this.sName = sName;
        this.sArtist = sArtist;
        this.sAlbum = sAlbum;
        this.sAlbumUri = sAlbumUri;
        this.sLength = sLength;
        this.sPath = sPath;
    }

    public String getsAlbum() {
        return sAlbum;
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
