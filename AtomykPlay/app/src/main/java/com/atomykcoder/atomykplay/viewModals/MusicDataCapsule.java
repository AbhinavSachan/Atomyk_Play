package com.atomykcoder.atomykplay.viewModals;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

@Parcel
public class MusicDataCapsule {
    String sName;
    String sArtist;
    String sAlbum;
    String sAlbumUri;
    String sLength;
    String sPath;
    String sBitrate;
    String sMimeType;
    String sSize;
    String sGenre;

    @ParcelConstructor
    public MusicDataCapsule(String sName, String sArtist, String sAlbum, String sAlbumUri, String sLength, String sPath, String sBitrate, String sMimeType, String sSize, String sGenre) {
        this.sName = sName;
        this.sArtist = sArtist;
        this.sAlbum = sAlbum;
        this.sAlbumUri = sAlbumUri;
        this.sLength = sLength;
        this.sPath = sPath;
        this.sBitrate = sBitrate;
        this.sMimeType = sMimeType;
        this.sSize = sSize;
        this.sGenre = sGenre;
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

    public String getsLength() {
        return sLength;
    }

    public String getsPath() {
        return sPath;
    }

}
