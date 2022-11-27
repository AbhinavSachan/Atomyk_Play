package com.atomykcoder.atomykplay.events;

public class UpdateMusicProgressEvent {
    public final int position;

    public UpdateMusicProgressEvent(int position) {
        this.position = position;
    }
}
