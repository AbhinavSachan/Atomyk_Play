package com.atomykcoder.atomykplay.musicload;

public class MusicDataCapsule {
    String sName, sArtist, sCover, sLength;

    public MusicDataCapsule(String sName, String sArtist, String sCover, String sLength) {
        this.sName = sName;
        this.sArtist = sArtist;
        this.sCover = sCover;
        this.sLength = sLength;
    }
    public MusicDataCapsule(String sName, String sArtist, String sLength) {
        this.sName = sName;
        this.sArtist = sArtist;
        this.sLength = sLength;
    }

    public String getsName() {
        return sName;
    }

    public String getsArtist() {
        return sArtist;
    }

    public String getsCover() {
        return sCover;
    }

    public String getsLength() {
        return sLength;
    }
}
