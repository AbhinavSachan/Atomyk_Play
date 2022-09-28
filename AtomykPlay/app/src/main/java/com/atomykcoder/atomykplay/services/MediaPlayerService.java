package com.atomykcoder.atomykplay.services;

import static com.atomykcoder.atomykplay.ApplicationClass.CHANNEL_ID;
import static com.atomykcoder.atomykplay.MainActivity.service_bound;
import static com.atomykcoder.atomykplay.player.PlayerFragment.getEmbeddedImage;

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
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.atomykcoder.atomykplay.MainActivity;
import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.function.FetchMusic;
import com.atomykcoder.atomykplay.function.MusicDataCapsule;
import com.atomykcoder.atomykplay.function.PlaybackStatus;
import com.atomykcoder.atomykplay.function.StorageUtil;
import com.atomykcoder.atomykplay.player.PlayerFragment;

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
    public static final int NOTIFICATION_ID = 414141;
    public static boolean phone_ringing = false;
    //binder
    private final IBinder iBinder = new LocalBinder();
    public MediaPlayer media_player;
    public Runnable runnable;
    public Handler handler;
    //media session
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;
    //list of available music
    private ArrayList<MusicDataCapsule> musicList;
    private int musicIndex = -1;
    private MusicDataCapsule activeMusic = null;//object of currently playing audio
    //to pause when output device is unplugged
    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (media_player != null)
                pauseMedia();
        }
    };
    //
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private int resumePosition = -1;
    private AudioManager audioManager;
    //broadcast receivers
    //playing new song
    private final BroadcastReceiver playNewMusicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //get the new media index from SP

            //load data from SharedPref
            StorageUtil storage = new StorageUtil(getApplicationContext());
            musicList = storage.loadMusic();
            musicIndex = storage.loadMusicIndex();

            if (musicList != null)
                if (musicIndex != -1 && musicIndex < musicList.size()) {
                    activeMusic = musicList.get(musicIndex);
                } else {
                    stopSelf();
                }


            if (mediaSessionManager == null) {
                try {
                    initiateMediaSession();
                } catch (RemoteException e) {
                    e.printStackTrace();
                    stopSelf();
                }
            }


            musicIndex = new StorageUtil(getApplicationContext()).loadMusicIndex();
            if (musicIndex != -1 && musicIndex < musicList.size()) {
                activeMusic = musicList.get(musicIndex);
            } else {
                stopSelf();
            }
            stopMedia();
            initiateMediaPlayer();
            updateMetaData();
        }
    };
    private final BroadcastReceiver nextMusicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //get the new media index from SP
            //load data from SharedPref
            StorageUtil storage = new StorageUtil(getApplicationContext());
            musicList = storage.loadMusic();
            musicIndex = storage.loadMusicIndex();

            if (musicList != null)
                if (musicIndex != -1 && musicIndex < musicList.size()) {
                    activeMusic = musicList.get(musicIndex);
                } else {
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
            }

            skipToNext();
            updateMetaData();

        }
    };
    private final BroadcastReceiver prevMusicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                //load data from SharedPref
                StorageUtil storage = new StorageUtil(getApplicationContext());
                musicList = storage.loadMusic();
                musicIndex = storage.loadMusicIndex();

                if (musicList != null)
                    if (musicIndex != -1 && musicIndex < musicList.size()) {
                        activeMusic = musicList.get(musicIndex);
                    } else {
                        stopSelf();
                    }

            } catch (NullPointerException e) {
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
            }

            skipToPrevious();
            updateMetaData();

        }
    };
    private final BroadcastReceiver pausePlayMusicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            StorageUtil storage = new StorageUtil(getApplicationContext());

            try {
                //load data from SharedPref
                musicList = storage.loadMusic();
                musicIndex = storage.loadMusicIndex();

                if (musicList != null)
                    if (musicIndex != -1 && musicIndex < musicList.size()) {
                        activeMusic = musicList.get(musicIndex);
                    } else {
                        stopSelf();
                    }

            } catch (NullPointerException e) {
                stopSelf();
            }

            if (mediaSessionManager == null) {
                try {
                    initiateMediaSession();
                } catch (RemoteException e) {
                    e.printStackTrace();
                    stopSelf();
                }
            }

            if (media_player == null)
                initiateMediaPlayer();

            if (media_player != null)
                if (media_player.isPlaying()) {
                    pauseMedia();
                } else {
                    resumeMedia();
                    setSeekBar();
                }
        }
    };

    public void setIcon(PlaybackStatus playbackStatus) {
        ImageView miniImage = PlayerFragment.mini_pause;
        ImageView mainImage = PlayerFragment.playImg;
        if (musicList != null)
            if (playbackStatus == PlaybackStatus.PLAYING) {
                miniImage.setImageResource(R.drawable.ic_pause_mini);
                mainImage.setImageResource(R.drawable.ic_pause_main);
            } else if (playbackStatus == PlaybackStatus.PAUSED) {
                miniImage.setImageResource(R.drawable.ic_play_mini);
                mainImage.setImageResource(R.drawable.ic_play_main);
            }
    }

    private void registerPlayNewMusic() {
        IntentFilter filter = new IntentFilter(MainActivity.BROADCAST_PLAY_NEW_MUSIC);
        registerReceiver(playNewMusicReceiver, filter);
    }

    private void registerPausePlayMusic() {
        IntentFilter filter = new IntentFilter(MainActivity.BROADCAST_PAUSE_PLAY_MUSIC);
        registerReceiver(pausePlayMusicReceiver, filter);
    }

    private void registerPlayNextMusic() {
        IntentFilter filter = new IntentFilter(MainActivity.BROADCAST_PLAY_NEXT_MUSIC);
        registerReceiver(nextMusicReceiver, filter);
    }

    private void registerPlayPreviousMusic() {
        IntentFilter filter = new IntentFilter(MainActivity.BROADCAST_PLAY_PREVIOUS_MUSIC);
        registerReceiver(prevMusicReceiver, filter);
    }

    private void updateMetaData() {
        Bitmap albumArt = null;
        if (musicList != null)
            if (!musicList.get(musicIndex).getsAlbumUri().equals("")) {
                albumArt = getEmbeddedImage(musicList.get(musicIndex).getsPath());
            } else {
                albumArt = BitmapFactory.decodeResource(getResources(), R.drawable.ic_no_album);
            }
        if (musicList != null)
            mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, musicList.get(musicIndex).getsArtist())
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, musicList.get(musicIndex).getsAlbum())
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, musicList.get(musicIndex).getsName())
                    .build());
    }

    private void buildNotification(PlaybackStatus playbackStatus) {
        int notificationAction = R.drawable.ic_pause_for_noti;//needs to be initialized
        PendingIntent play_pauseAction = null;

        //build a new notification according to media player status
        if (playbackStatus == PlaybackStatus.PLAYING) {
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = R.drawable.ic_play_for_noti;
            play_pauseAction = playbackAction(0);

        }
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.no_song_image);
        Notification notificationBuilder = null;

        //building notification for player
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (musicList != null)
                notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setShowWhen(false).setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                .setMediaSession(mediaSession.getSessionToken())
                                .setShowActionsInCompactView(0, 1, 2))
                        .setColor(getResources().getColor(R.color.primary_bg, getTheme()))
                        .setColorized(true)
                        .setLargeIcon(largeIcon)
                        .setSmallIcon(R.drawable.ic_headset)
                        //set content
                        .setContentText(activeMusic.getsArtist())
                        .setContentTitle(activeMusic.getsName())
                        .setContentInfo(activeMusic.getsAlbum())
                        .setDeleteIntent(playbackAction(1))
                        .setChannelId(CHANNEL_ID)
                        //set control
                        .addAction(R.drawable.ic_previous_for_noti, "Previous", playbackAction(3))
                        .addAction(notificationAction, "Pause", play_pauseAction)
                        .addAction(R.drawable.ic_next_for_noti, "next", playbackAction(2))
                        .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE))
                        .setSilent(true)
                        .setOngoing(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build();
        }


        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.getNotificationChannel(CHANNEL_ID);
        }
        if (notificationBuilder != null)
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
            case 4:
                //previous
                playbackIntent.setAction(ACTION_STOP);
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
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                super.onCallStateChanged(state, phoneNumber);
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING: {
                        if (media_player != null) {
                            pauseMedia();
                            phone_ringing = true;
                        }
                    }

                    break;
                    case TelephonyManager.CALL_STATE_IDLE: {
                        if (media_player != null) {
                            phone_ringing = false;
                            resumeMedia();
                        }
                    }
                    break;
                }


            }
        };


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
        registerPausePlayMusic();
        registerPlayNextMusic();
        registerPlayPreviousMusic();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        new StorageUtil(getApplicationContext()).clearCacheMusicLastPos();
        buildNotification(PlaybackStatus.PAUSED);
        setIcon(PlaybackStatus.PAUSED);
        PlayerFragment.mini_progress.setProgress(0);
        PlayerFragment.mini_progress.setMax(0);
        stopMedia();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    //when output device is unplugged it will activate
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
        PlayerFragment.setMiniLayout();
        PlayerFragment.setMainPlayerLayout();
        if (service_bound)
            setSeekBar();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    private void initiateMediaPlayer() {
        media_player = new MediaPlayer();

        //setup MediaPlayer event listeners
        media_player.setOnPreparedListener(this);
        media_player.setOnErrorListener(this);
        media_player.setOnBufferingUpdateListener(this);
        media_player.setOnSeekCompleteListener(this);
        media_player.setOnInfoListener(this);
        media_player.setOnCompletionListener(this);

        //reset so that the media player is not pointing to another data source
        media_player.reset();
        media_player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            if (activeMusic != null) {
                media_player.setDataSource(activeMusic.getsPath());
                media_player.prepareAsync();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void playMedia() {
        Intent playerIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
        startService(playerIntent);
        if (!requestAudioFocus()) {
            requestAudioFocus();
        }
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0f){
            Toast.makeText(this, "Volume is OFF", Toast.LENGTH_SHORT).show();
        }
        if (media_player != null)
            if (!media_player.isPlaying()) {
                media_player.start();
                setIcon(PlaybackStatus.PLAYING);
                buildNotification(PlaybackStatus.PLAYING);
            }

    }

    public void stopMedia() {
        if (media_player == null) {
            return;
        }

        if (media_player.isPlaying()) {
            media_player.stop();
        }
        new StorageUtil(getApplicationContext()).clearCacheMusicLastPos();
        stopSelf();
    }

    public void stoppedByNotification() {
        if (media_player == null) {
            return;
        }
        if (media_player.isPlaying()) {
            new StorageUtil(getApplicationContext()).saveMusicLastPos(media_player.getCurrentPosition());
            media_player.pause();
            setIcon(PlaybackStatus.PAUSED);
        }
    }

    public void pauseMedia() {
        if (media_player.isPlaying()) {
            new StorageUtil(getApplicationContext()).saveMusicLastPos(media_player.getCurrentPosition());
            media_player.pause();
            setIcon(PlaybackStatus.PAUSED);
            buildNotification(PlaybackStatus.PAUSED);
        }
    }

    public void resumeMedia() {

        if (!requestAudioFocus()) {
            requestAudioFocus();
        }
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0f){
            Toast.makeText(this, "Volume is OFF", Toast.LENGTH_SHORT).show();
        }
        if (media_player != null) {
            if (!media_player.isPlaying()) {
                int position = new StorageUtil(getApplicationContext()).loadMusicLastPos();
                media_player.seekTo(position);
                media_player.start();
                new StorageUtil(getApplicationContext()).clearCacheMusicLastPos();
                setIcon(PlaybackStatus.PLAYING);
                buildNotification(PlaybackStatus.PLAYING);
            }
        } else {
            initiateMediaPlayer();
        }
    }

    //setting progress on player seekbar and mini progress bar
    public void setSeekBar() {
        if (media_player != null) {
            PlayerFragment.mini_progress.setMax(media_player.getDuration());
            PlayerFragment.seekBarMain.setMax(media_player.getDuration());
        }
        handler = new Handler(Looper.getMainLooper());

        if (media_player != null && media_player.getCurrentPosition() < media_player.getDuration()) {

            runnable = new Runnable() {
                @Override
                public void run() {
                    int position = 0;
                    if (media_player != null) {
                        try {
                            position = media_player.getCurrentPosition();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
                    PlayerFragment.mini_progress.setProgress(position);
                    PlayerFragment.seekBarMain.setProgress(position);
                    String cur = FetchMusic.convertDuration(String.valueOf(position));
                    PlayerFragment.curPosTv.setText(cur);
                    handler.postDelayed(runnable, 100);
                }
            };
            handler.postDelayed(runnable, 0);
        }
    }

    public void skipToPrevious() {
        new StorageUtil(getApplicationContext()).clearCacheMusicLastPos();

        if (musicList != null)
            if (musicIndex == 0) {
                musicIndex = musicList.size() - 1;
                activeMusic = musicList.get(musicIndex);
            } else {
                activeMusic = musicList.get(--musicIndex);
            }
        if (activeMusic != null) {
            //update stored index
            new StorageUtil(getApplicationContext()).saveMusicIndex(musicIndex);

            stopMedia();
            initiateMediaPlayer();
        }

    }

    public void skipToNext() {
        new StorageUtil(getApplicationContext()).clearCacheMusicLastPos();

        if (musicList != null)
            if (musicIndex == musicList.size() - 1) {
                //if last in list
                musicIndex = 0;
                activeMusic = musicList.get(musicIndex);
            } else {
                //get next in playlist
                activeMusic = musicList.get(++musicIndex);
            }
        if (activeMusic != null) {
            //update stored index
            new StorageUtil(getApplicationContext()).saveMusicIndex(musicIndex);

            stopMedia();
            initiateMediaPlayer();
        }
    }

    private void initiateMediaSession() throws RemoteException {
        if (mediaSessionManager != null) return; //manager already exists
        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        transportControls = mediaSession.getController().getTransportControls();
        mediaSession.setActive(true);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);

        updateMetaData();
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                skipToNext();
                updateMetaData();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
                updateMetaData();
            }

            @Override
            public void onStop() {
                super.onStop();
                stoppedByNotification();
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
                if (media_player == null) {
                    initiateMediaPlayer();
                } else if (!media_player.isPlaying()) {
                    playMedia();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (media_player != null)
                    if (media_player.isPlaying()) {
                        stopMedia();
                        buildNotification(PlaybackStatus.PAUSED);
                        setIcon(PlaybackStatus.PAUSED);
                    }
                media_player = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (media_player != null)
                    if (media_player.isPlaying()) {
                        pauseMedia();
                    }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (media_player != null)
                    if (media_player.isPlaying()) {
                        media_player.setVolume(0.3f, 0.3f);
                    }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //request audio focus
        if (!requestAudioFocus()) {
            stopSelf();
        }

        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent);
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        removeNotification();
        removeAudioFocus();
        new StorageUtil(getApplicationContext()).clearCacheMusicLastPos();
        if (media_player != null) {
            media_player.release();
        }
//        disable phone state listener ♣
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewMusicReceiver);
        unregisterReceiver(pausePlayMusicReceiver);
        unregisterReceiver(nextMusicReceiver);
        unregisterReceiver(prevMusicReceiver);
    }

    private void removeNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
        if (media_player != null) {
            media_player.release();
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
