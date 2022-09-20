package com.atomykcoder.atomykplay.services;

import static com.atomykcoder.atomykplay.ApplicationClass.CHANNEL_ID;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.atomykcoder.atomykplay.MainActivity;
import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.function.MusicDataCapsule;
import com.atomykcoder.atomykplay.function.PlaybackStatus;
import com.atomykcoder.atomykplay.function.StorageUtil;

import java.io.IOException;
import java.util.ArrayList;

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener, AudioManager.OnAudioFocusChangeListener {

    public static final String ACTION_PLAY = "com.atomykcoder.atomykplay.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.atomykcoder.atomykplay.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.atomykcoder.atomykplay.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.atomykcoder.atomykplay.ACTION_NEXT";
    public static final String ACTION_STOP = "com.atomykcoder.atomykplay.ACTION_STOP";
    //audio player notification ID
    public static final int NOTIFICATION_ID = 101;
    //binder
    private final IBinder iBinder = new LocalBinder();
    public MediaPlayer mediaPlayer;
    //media session
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;
    //list of available music
    private ArrayList<MusicDataCapsule> musicList;
    private int musicIndex = -1;
    private MusicDataCapsule activeMusic;//object of currently playing audio
    //broadcast receivers
    //playing new song
    private final BroadcastReceiver playNewMusic = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //get the new media index from SP
            musicIndex = new StorageUtil(getApplicationContext()).loadMusicIndex();
            if (musicIndex != -1 && musicIndex < musicList.size()) {
                activeMusic = musicList.get(musicIndex);
            } else {
                stopSelf();
            }
            stopMedia();
            mediaPlayer.reset();
            initiateMediaPlayer();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);

        }
    };
    //
    private boolean onGoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private int resumePosition;
    //to pause when output device is unplugged
    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };
    private AudioManager audioManager;

    private void registerPlayNewMusic() {
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAY_NEW_MUSIC);
        registerReceiver(playNewMusic, filter);
    }

    private void updateMetaData() {
        Bitmap albumArt;
        if (!musicList.get(musicIndex).getsAlbumUri().equals("")) {
            albumArt = getEmbeddedImage(musicList.get(musicIndex).getsPath());
        } else {
            albumArt = BitmapFactory.decodeResource(getResources(), R.drawable.ic_no_album);
        }
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, musicList.get(musicIndex).getsArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, musicList.get(musicIndex).getsAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, musicList.get(musicIndex).getsName())
                .build());
    }

    private Bitmap getEmbeddedImage(String songPath) {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(songPath);
        byte[] data = metadataRetriever.getEmbeddedPicture();
        if (data != null) {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        } else {
            return null;
        }
    }

    private void buildNotification(PlaybackStatus playbackStatus) {
        int notificationAction = R.drawable.ic_pause;//needs to be initialized
        PendingIntent play_pauseAction = null;

        //build a new notification according to media player status
        if (playbackStatus == PlaybackStatus.PLAYING) {
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = R.drawable.ic_play;
            play_pauseAction = playbackAction(0);

        }
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.i02_alert);

        Notification notificationBuilder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setShowWhen(false).setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(mediaSession.getSessionToken())
                            .setShowActionsInCompactView(0, 1, 2))
                    .setColor(getResources().getColor(R.color.primary_bg,getTheme()))
                    .setColorized(true)
                    .setLargeIcon(largeIcon)
                    .setSmallIcon(R.drawable.ic_headset)
                    //set content
                    .setContentText(activeMusic.getsArtist())
                    .setContentTitle(activeMusic.getsName())
                    .setContentInfo(activeMusic.getsAlbum())
                    //set control
                    .addAction(R.drawable.ic_previous, "Previous", playbackAction(3))
                    .addAction(notificationAction, "Pause", play_pauseAction)
                    .addAction(R.drawable.ic_next, "next", playbackAction(2))
                    .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE))
                    .setSilent(true)
                    .setPriority(NotificationCompat.PRIORITY_LOW).build();
        }

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, notificationBuilder);
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackIntent = new Intent(this, MediaPlayerService.class);
        switch (actionNumber) {
            case 0:
                //play
                playbackIntent.setAction(ACTION_PLAY);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return PendingIntent.getService(this, actionNumber, playbackIntent, PendingIntent.FLAG_IMMUTABLE);
                }
            case 1:
                //pause
                playbackIntent.setAction(ACTION_PAUSE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return PendingIntent.getService(this, actionNumber, playbackIntent, PendingIntent.FLAG_IMMUTABLE);
                }
            case 2:
                //next
                playbackIntent.setAction(ACTION_NEXT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return PendingIntent.getService(this, actionNumber, playbackIntent, PendingIntent.FLAG_IMMUTABLE);
                }
            case 3:
                //previous
                playbackIntent.setAction(ACTION_PREVIOUS);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return PendingIntent.getService(this, actionNumber, playbackIntent, PendingIntent.FLAG_IMMUTABLE);
                }
            default:
                break;
        }
        return null;
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;
        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }

    private void callStateListener() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new MyPhoneStateListener();
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        //manage incoming calls during playback
        callStateListener();
        //manage audio while output changes
        registerBecomingNoisyReceiver();
        //change audio on new click
        registerPlayNewMusic();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopMedia();
        stopSelf();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    private void registerBecomingNoisyReceiver() {
        IntentFilter i = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, i);

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA_ERROR_UNKNOWN" + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK" + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA_ERROR_SERVER_DIED" + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }


    private void initiateMediaPlayer() {
        mediaPlayer = new MediaPlayer();

        //setup MediaPlayer event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);

        //reset so that the media player is not pointing to another data source
        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(activeMusic.getsPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.prepareAsync();
    }

    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stopMedia() {
        if (mediaPlayer == null) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    private void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    private void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }


    private void skipToPrevious() {

        if (musicIndex == 0) {
            musicIndex = musicList.size() - 1;
            activeMusic = musicList.get(musicIndex);
        } else {
            activeMusic = musicList.get(--musicIndex);
        }
        //update stored index
        new StorageUtil(getApplicationContext()).storeMusicIndex(musicIndex);

        stopMedia();
        //reset player
        mediaPlayer.reset();
        initiateMediaPlayer();

    }

    private void skipToNext() {
        if (musicIndex == musicList.size() - 1) {
            //if last in list
            musicIndex = 0;
            activeMusic = musicList.get(musicIndex);
        } else {
            //get next in playlist
            activeMusic = musicList.get(++musicIndex);
        }
        //update stored index
        new StorageUtil(getApplicationContext()).storeMusicIndex(musicIndex);

        stopMedia();
        //reset player
        mediaPlayer.reset();
        initiateMediaPlayer();

    }


    private void initiateMediaSession() throws RemoteException {
        if (mediaSessionManager != null) return; //manager already exists
        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        transportControls = mediaSession.getController().getTransportControls();
        mediaSession.setActive(true);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        updateMetaData();
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
                buildNotification(PlaybackStatus.PAUSED);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                skipToNext();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                stopSelf();
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
            }

        });
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mediaPlayer == null) {
                    initiateMediaPlayer();
                } else if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    resumePosition = mediaPlayer.getCurrentPosition();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.setVolume(0.3f, 0.3f);
                }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            //load data from SharedPref
            StorageUtil storage = new StorageUtil(getApplicationContext());
            musicList = storage.loadMusic();
            musicIndex = storage.loadMusicIndex();

            if (musicIndex != -1 && musicIndex < musicList.size()) {
                activeMusic = musicList.get(musicIndex);
            } else {
                stopSelf();
            }

        } catch (NullPointerException e) {
            stopSelf();
        }
        //request audio focus
        if (!requestAudioFocus()) {
            stopSelf();
        }


        if (mediaSessionManager == null) {
            try {
                initiateMediaSession();
                initiateMediaPlayer();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            buildNotification(PlaybackStatus.PLAYING);
        }

        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
            stopSelf();
        }
        removeAudioFocus();
//        disable phone state listener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        removeNotification();
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewMusic);
    }

    private void removeNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
        if (mediaPlayer != null){
            mediaPlayer.release();
        }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void removeAudioFocus() {
        audioManager.abandonAudioFocus(this);
    }

    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }
}
