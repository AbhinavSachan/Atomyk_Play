package com.atomykcoder.atomykplay.events;

public class UpdateMusicImageEvent {
    public boolean shouldDisplayPlayImage = false;
    public UpdateMusicImageEvent(boolean _shouldDisplayPlayImage) {
        shouldDisplayPlayImage = _shouldDisplayPlayImage;
    }
}
