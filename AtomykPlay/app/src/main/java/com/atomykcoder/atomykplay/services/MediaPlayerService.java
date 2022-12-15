package com.atomykcoder.atomykplay.services;

import static com.atomykcoder.atomykplay.activities.MainActivity.BROADCAST_PAUSE_PLAY_MUSIC;
import static com.atomykcoder.atomykplay.activities.MainActivity.BROADCAST_PLAY_NEXT_MUSIC;
import static com.atomykcoder.atomykplay.activities.MainActivity.BROADCAST_PLAY_PREVIOUS_MUSIC;
import static com.atomykcoder.atomykplay.activities.MainActivity.BROADCAST_STOP_MUSIC;
import static com.atomykcoder.atomykplay.activities.MainActivity.service_bound;
import static com.atomykcoder.atomykplay.activities.MainActivity.service_stopped;
import static com.atomykcoder.atomykplay.classes.ApplicationClass.CHANNEL_ID;

import android.annotation.SuppressLint;
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
import android.media.ThumbnailUtils;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.enums.PlaybackStatus;
import com.atomykcoder.atomykplay.events.PrepareRunnableEvent;
import com.atomykcoder.atomykplay.events.RemoveLyricsHandlerEvent;
import com.atomykcoder.atomykplay.events.SetMainLayoutEvent;
import com.atomykcoder.atomykplay.events.UpdateMusicImageEvent;
import com.atomykcoder.atomykplay.events.UpdateMusicProgressEvent;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
import com.atomykcoder.atomykplay.viewModals.LRCMap;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener,
        AudioManager.OnAudioFocusChangeListener {

    //♥♥☻☻
    //all public variables are in this format "public_variable"
    //all public and private static final string variables are in this format "PUBLIC_PRIVATE_STATIC_FINAL_STRING"
    //all private variables are in this format "privateVariable"
    //♥♥☻☻

    public static final String ACTION_PLAY = "com.atomykcoder.atomykplay.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.atomykcoder.atomykplay.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.atomykcoder.atomykplay.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.atomykcoder.atomykplay.ACTION_NEXT";
    public static final String ACTION_STOP = "com.atomykcoder.atomykplay.ACTION_STOP";

    //audio player notification ID
    public static final int NOTIFICATION_ID = 414141;
    public static boolean is_playing = false;
    public static boolean ui_visible = false;
    //binder
    private final IBinder iBinder = new LocalBinder();
    public MediaPlayer media_player;
    public Runnable seekBarRunnable;
    public Handler seekBarHandler;
    private boolean was_playing = false;
    //media session
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;
    //list of available music
    private ArrayList<String> musicIdList;
    private int musicIndex = -1;
    private MusicDataCapsule activeMusic = null;//object of currently playing audio
    //
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private AudioManager audioManager;
    //broadcast receivers
    //playing new song
    private StorageUtil storage;
    private final BroadcastReceiver stopMusicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stoppedByNotification();
        }
    };
    private StorageUtil.SettingsStorage settingsStorage;
    private NotificationManager notificationManager;
    //to pause when output device is unplugged
    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pauseMedia();
        }
    };//to play when output device is plugged
    private final BroadcastReceiver pluggedInDevice = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);

                if (settingsStorage.loadAutoPlay()) {
                    switch (state) {
                        case 0:
                            if (is_playing) {
                                pauseMedia();
                            }
                            break;
                        case 1:
                            if (!is_playing) {
                                if (media_player != null) {
                                    resumeMedia();
                                } else {
                                    try {
                                        initiateMediaSession();
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                    initiateMediaPlayer();
                                }
                            }
                            break;
                    }
                }
            }
        }
    };
    private final BroadcastReceiver nextMusicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mediaSessionManager == null) {
                try {
                    initiateMediaSession();
                } catch (RemoteException e) {
                    e.printStackTrace();
                    stopSelf();
                }
            }
            skipToNext();

        }
    };
    private final BroadcastReceiver prevMusicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mediaSessionManager == null) {
                try {
                    initiateMediaSession();
                } catch (RemoteException e) {
                    e.printStackTrace();
                    stopSelf();
                }
            }
            skipToPrevious();

        }
    };
    private final BroadcastReceiver pausePlayMusicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (media_player == null) {
                try {
                    initiateMediaSession();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                initiateMediaPlayer();
            } else {
                if (media_player.isPlaying()) {
                    pauseMedia();
                } else {
                    resumeMedia();
                }
            }
        }
    };
    private final BroadcastReceiver playNewMusicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String musicID =  intent.getStringExtra("music");

            Log.i("info", musicID);
            if (musicID != null) {
                activeMusic = storage.getItemFromInitialList(musicID);
                Log.i("info", activeMusic.getsName());
            } else {
                Log.i("info", "music is null");
            }

            if (mediaSessionManager == null) {
                try {
                    initiateMediaSession();
                } catch (RemoteException e) {
                    e.printStackTrace();
                    stopSelf();
                }
            }

            stopMedia();
            initiateMediaPlayer();
        }
    };

    /**
     * this function updates play icon according to media playback status
     */
    public void setIcon(PlaybackStatus playbackStatus) {
        if (musicIdList != null) {
            if (playbackStatus == PlaybackStatus.PLAYING) {
                EventBus.getDefault().post(new UpdateMusicImageEvent(false));
            } else if (playbackStatus == PlaybackStatus.PAUSED) {
                EventBus.getDefault().post(new UpdateMusicImageEvent(true));
            }
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

    private void registerStopMusic() {
        IntentFilter filter = new IntentFilter(MainActivity.BROADCAST_STOP_MUSIC);
        registerReceiver(stopMusicReceiver, filter);
    }

    private void registerPlayNextMusic() {
        IntentFilter filter = new IntentFilter(MainActivity.BROADCAST_PLAY_NEXT_MUSIC);
        registerReceiver(nextMusicReceiver, filter);
    }

    private void registerPlayPreviousMusic() {
        IntentFilter filter = new IntentFilter(MainActivity.BROADCAST_PLAY_PREVIOUS_MUSIC);
        registerReceiver(prevMusicReceiver, filter);
    }

    //when output device is unplugged it will activate
    private void registerBecomingNoisyReceiver() {
        IntentFilter i = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, i);
    }

    //when output device is plugged it will activate
    private void registerPluggedDeviceReceiver() {
        IntentFilter i = new IntentFilter(AudioManager.ACTION_HEADSET_PLUG);
        registerReceiver(pluggedInDevice, i);
    }

    /**
     * this function updates new music data in notification
     */
    private void updateMetaData(MusicDataCapsule activeMusic) {
        musicIdList = storage.loadQueueList();
        musicIndex = storage.loadMusicIndex();
        String songName = null;
        String artistName = null;
        String album = null;
        String albumUri = null;
        String dur = null;
        if (activeMusic != null) {
            dur = activeMusic.getsLength();
            songName = activeMusic.getsName();
            artistName = activeMusic.getsArtist();
            album = activeMusic.getsAlbum();

            albumUri = activeMusic.getsAlbumUri();
        }
        InputStream input;
        Bitmap artwork = null;
        Bitmap finalArtWork;
        try {
            if (albumUri != null) {
                input = getApplicationContext().getContentResolver().openInputStream(Uri.parse(albumUri));
                artwork = BitmapFactory.decodeStream(input);
                if (input != null) {
                    input.close();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (artwork != null) {
            int dimension = Math.min(artwork.getWidth(), artwork.getHeight());
            finalArtWork = ThumbnailUtils.extractThumbnail(artwork, dimension - (dimension / 5), dimension - (dimension / 5), ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        } else {
            artwork = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_art);
            int dimension = Math.min(artwork.getWidth(), artwork.getHeight());
            finalArtWork = ThumbnailUtils.extractThumbnail(artwork, dimension - (dimension / 5), dimension - (dimension / 5), ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        if (musicIdList != null)
            mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, finalArtWork)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artistName)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songName)
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, Long.parseLong(dur))
                    .build());
    }

    /**
     * This function skips music to next index
     *
     * @param playbackStatus is music playing or not
     * @param playbackSpeed  to set the speed of seekbar in notification 1f for 1s/s and 0f to stopped
     */
    @SuppressLint("NewApi")
    public void buildNotification(PlaybackStatus playbackStatus, float playbackSpeed) {
        int notificationAction = R.drawable.ic_pause_for_noti;//needs to be initialized
        PendingIntent play_pauseAction = null;

        //build a new notification according to media player status
        if (playbackStatus == PlaybackStatus.PLAYING) {
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = R.drawable.ic_play_for_noti;
            play_pauseAction = playbackAction(0);
        }

        Notification notificationBuilder = null;

        //building notification for player
        if (musicIdList != null)
            notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setShowWhen(false).setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(mediaSession.getSessionToken())
                            .setShowActionsInCompactView(0, 1, 2))
                    .setColor(getResources().getColor(R.color.tertiary_bg, getTheme()))
                    .setColorized(true)
                    .setSmallIcon(R.drawable.ic_headset)
                    //set content
                    .setContentText(activeMusic.getsArtist())
                    .setContentTitle(activeMusic.getsAlbum())
                    .setContentInfo(activeMusic.getsName())
                    .setDeleteIntent(playbackAction(4))
                    .setChannelId(CHANNEL_ID)
                    //set control
                    .addAction(R.drawable.ic_previous_for_noti, "Previous", playbackAction(3))
                    .addAction(notificationAction, "Pause", play_pauseAction)
                    .addAction(R.drawable.ic_next_for_noti, "next", playbackAction(2))
                    .addAction(R.drawable.ic_close, "stop", playbackAction(4))
                    .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_IMMUTABLE))
                    .setSilent(true)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_DEFAULT)
                    .build();

        if (media_player != null) {
            if (playbackStatus == PlaybackStatus.PLAYING) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                            .setState(PlaybackStateCompat.STATE_PLAYING, media_player.getCurrentPosition(), playbackSpeed)
                            .setActions(PlaybackStateCompat.ACTION_SEEK_TO).build());
                }
            } else if (playbackStatus == PlaybackStatus.PAUSED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                            .setState(PlaybackStateCompat.STATE_PAUSED, media_player.getCurrentPosition(), playbackSpeed)
                            .setActions(PlaybackStateCompat.ACTION_SEEK_TO).build());
                }
            }
        }
        if (notificationBuilder != null) {
            if (PlaybackStatus.PLAYING == playbackStatus) {
                startForeground(NOTIFICATION_ID, notificationBuilder);
            } else if (PlaybackStatus.PAUSED == playbackStatus) {
                stopForeground(false);
                notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder);
                //here we are stopping service after 5 minutes if app is not running in background and player is not playing
                if (settingsStorage.loadSelfStop()) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (!is_playing) {
                            if (!ui_visible) {
                                stopSelf();
                            }
                        }
                    }, 300000);

                }
            }
        }
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
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
                //stop
                playbackIntent.setAction(ACTION_STOP);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return PendingIntent.getService(this, actionNumber, playbackIntent, PendingIntent.FLAG_IMMUTABLE);
                }

            default:
                break;
        }
        return null;
    }

    /**
     * This function sets up notification actions
     */
    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null)
            return;

        String actionString = playbackAction.getAction();
        Intent playerIntent = new Intent(getApplicationContext(), MediaPlayerService.class);

        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            if (media_player == null) {
                if (!service_bound) {
                    startService(playerIntent);
                    new Handler().postDelayed(() -> {
                        //service is active send media with broadcast receiver
                        Intent broadcastIntent = new Intent(BROADCAST_PAUSE_PLAY_MUSIC);
                        sendBroadcast(broadcastIntent);
                    }, 0);
                } else {
                    //service is active send media with broadcast receiver
                    Intent broadcastIntent = new Intent(BROADCAST_PAUSE_PLAY_MUSIC);
                    sendBroadcast(broadcastIntent);
                }
            } else {
                transportControls.play();
            }
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            if (media_player == null) {
                if (!service_bound) {
                    startService(playerIntent);
                    new Handler().postDelayed(() -> {
                        //service is active send media with broadcast receiver
                        Intent broadcastIntent = new Intent(BROADCAST_PAUSE_PLAY_MUSIC);
                        sendBroadcast(broadcastIntent);
                    }, 0);
                } else {
                    //service is active send media with broadcast receiver
                    Intent broadcastIntent = new Intent(BROADCAST_PAUSE_PLAY_MUSIC);
                    sendBroadcast(broadcastIntent);
                }
            } else {
                transportControls.pause();
            }
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            if (media_player == null) {
                if (!service_bound) {
                    startService(playerIntent);
                    new Handler().postDelayed(() -> {
                        //service is active send media with broadcast receiver
                        Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEXT_MUSIC);
                        sendBroadcast(broadcastIntent);
                    }, 0);
                } else {
                    //service is active send media with broadcast receiver
                    Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEXT_MUSIC);
                    sendBroadcast(broadcastIntent);
                }
            } else {
                transportControls.skipToNext();
            }
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            if (media_player == null) {
                if (!service_bound) {
                    startService(playerIntent);
                    new Handler().postDelayed(() -> {
                        //service is active send media with broadcast receiver
                        Intent broadcastIntent = new Intent(BROADCAST_PLAY_PREVIOUS_MUSIC);
                        sendBroadcast(broadcastIntent);
                    }, 0);
                } else {
                    //service is active send media with broadcast receiver
                    Intent broadcastIntent = new Intent(BROADCAST_PLAY_PREVIOUS_MUSIC);
                    sendBroadcast(broadcastIntent);
                }
            } else {
                transportControls.skipToPrevious();
            }
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            if (media_player == null) {
                if (!service_bound) {
                    startService(playerIntent);
                    new Handler().postDelayed(() -> {
                        //service is active send media with broadcast receiver
                        Intent broadcastIntent = new Intent(BROADCAST_STOP_MUSIC);
                        sendBroadcast(broadcastIntent);
                    }, 0);
                } else {
                    //service is active send media with broadcast receiver
                    Intent broadcastIntent = new Intent(BROADCAST_STOP_MUSIC);
                    sendBroadcast(broadcastIntent);
                }
            } else {
                transportControls.stop();
            }
        }

    }

    @SuppressWarnings("deprecation")
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
                            if (media_player.isPlaying()) {
                                was_playing = true;
                                pauseMedia();
                            }
                            MainActivity.phone_ringing = true;
                        }
                    }
                    break;
                    case TelephonyManager.CALL_STATE_IDLE: {
                        if (media_player != null) {
                            MainActivity.phone_ringing = false;
                            if (was_playing) {
                                resumeMedia();
                                was_playing = false;
                            }
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
        registerPluggedDeviceReceiver();
        registerPlayNewMusic();
        registerPausePlayMusic();
        registerPlayNextMusic();
        registerPlayPreviousMusic();
        registerStopMusic();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        if (service_bound) {
            EventBus.getDefault().post(new SetMainLayoutEvent(activeMusic));
            updateMetaData(activeMusic);
        }
        resumeMedia();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        storage.clearMusicLastPos();
        musicIdList = storage.loadQueueList();
        musicIndex = storage.loadMusicIndex();

        if (storage.loadRepeatStatus().equals("no_repeat")) {
            if (musicIdList != null)
                if (musicIndex == musicIdList.size() - 1) {
                    pauseMedia();
                } else {
                    skipToNext();
                }
        } else if (storage.loadRepeatStatus().equals("repeat")) {
            skipToNext();
        } else if (storage.loadRepeatStatus().equals("repeat_one")) {
            playMedia();
            EventBus.getDefault().post(new PrepareRunnableEvent());
        }
        if (media_player.isPlaying()) {
            setIcon(PlaybackStatus.PLAYING);
            buildNotification(PlaybackStatus.PLAYING, 1f);
        } else {
            setIcon(PlaybackStatus.PAUSED);
            buildNotification(PlaybackStatus.PAUSED, 0f);
        }

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Toast.makeText(this, "MEDIA_ERROR_UNKNOWN" + extra, Toast.LENGTH_SHORT).show();
                return true;
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Toast.makeText(this, "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK" + extra, Toast.LENGTH_SHORT).show();
                return true;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Toast.makeText(this, "MEDIA_ERROR_SERVER_DIED" + extra, Toast.LENGTH_SHORT).show();
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
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

    /**
     * This function plays music from start
     */
    public void playMedia() {
        if (!requestAudioFocus()) {
            requestAudioFocus();
        }
        if (!MainActivity.phone_ringing) {
            if (media_player != null)
                if (!media_player.isPlaying()) {
                    media_player.start();

                    is_playing = true;
                    setIcon(PlaybackStatus.PLAYING);
                    buildNotification(PlaybackStatus.PLAYING, 1f);
                }
        } else {
            Toast.makeText(getApplicationContext(), "Can't play while on call", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * This function stops music to play something else
     */
    public void stopMedia() {
        if (media_player == null) {
            return;
        }
        if (media_player.isPlaying()) {
            media_player.stop();
            is_playing = false;
        }
    }

    /**
     * This function stops music and removes notification
     */
    public void stoppedByNotification() {
        if (media_player != null)
            if (media_player.isPlaying()) {
                storage.saveMusicLastPos(media_player.getCurrentPosition());
                media_player.pause();
                is_playing = false;
                setIcon(PlaybackStatus.PAUSED);
                buildNotification(PlaybackStatus.PAUSED,0f);
            }
        notificationManager.cancelAll();
        service_stopped = true;
        stopSelf();
    }

    /**
     * This function resumes music
     */
    public void resumeMedia() {
        if (!requestAudioFocus()) {
            requestAudioFocus();
        }
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0f) {
            Toast.makeText(getApplicationContext(), "Please turn the volume UP", Toast.LENGTH_SHORT).show();
        }
        if (!MainActivity.phone_ringing) {
            if (media_player != null) {
                if (!media_player.isPlaying()) {
                    int position = storage.loadMusicLastPos();
                    if (service_bound) {
                        setSeekBar();
                        LRCMap lrcMap = storage.loadLyrics(activeMusic.getsId());
                        if (lrcMap != null) {
                            EventBus.getDefault().post(new PrepareRunnableEvent());
                        }
                    }
                    is_playing = true;
                    media_player.seekTo(position);
                    media_player.start();
                    setIcon(PlaybackStatus.PLAYING);
                    buildNotification(PlaybackStatus.PLAYING, 1f);
                }
            } else {
                initiateMediaPlayer();
            }

        } else {
            Toast.makeText(getApplicationContext(), "Can't play while on call", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * This function pauses music
     */
    public void pauseMedia() {
        if (media_player != null) {
            if (media_player.isPlaying()) {
                if (service_bound) {
                    if (seekBarHandler != null) {
                        seekBarHandler.removeCallbacks(seekBarRunnable);
                    }
                    EventBus.getDefault().post(new RemoveLyricsHandlerEvent());
                }
                storage.saveMusicLastPos(media_player.getCurrentPosition());
                media_player.pause();

                is_playing = false;
                setIcon(PlaybackStatus.PAUSED);
                buildNotification(PlaybackStatus.PAUSED, 0f);
            }
        }
    }

    /**
     * This function sends music position to main ui
     */
    public void setSeekBar() {
        seekBarHandler = new Handler(Looper.getMainLooper());
        if (media_player != null) {
            if (media_player.getCurrentPosition() <= media_player.getDuration()) {
                seekBarRunnable = () -> {
                    int position = 0;
                    if (media_player != null) {
                        try {
                            position = media_player.getCurrentPosition();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
                    EventBus.getDefault().post(new UpdateMusicProgressEvent(position));
                    seekBarHandler.postDelayed(seekBarRunnable, 300);
                };
                seekBarHandler.postDelayed(seekBarRunnable, 0);
            }
        }
    }

    /**
     * This function skips music to previous index
     */
    public void skipToPrevious() {
        musicIdList = storage.loadQueueList();
        musicIndex = storage.loadMusicIndex();

        int lastPos = storage.loadMusicLastPos();
        storage.clearMusicLastPos();

        if (settingsStorage.loadOneClickSkip()) {
            if (musicIdList != null)
                if (musicIndex == 0) {
                    musicIndex = musicIdList.size() - 1;
                    activeMusic = storage.getItemFromInitialList(musicIdList.get(musicIndex));
                } else {
                    activeMusic = storage.getItemFromInitialList(musicIdList.get(--musicIndex));
                }
            if (activeMusic != null) {
                //update stored index
                storage.saveMusicIndex(musicIndex);

                stopMedia();
                initiateMediaPlayer();
            }
        } else {
            if (media_player != null) {
                if (media_player.getCurrentPosition() >= 3000) {
                    activeMusic = storage.getItemFromInitialList(musicIdList.get(musicIndex));
                    if (activeMusic != null) {
                        stopMedia();
                        initiateMediaPlayer();
                    }
                } else {
                    if (musicIdList != null)
                        if (musicIndex == 0) {
                            musicIndex = musicIdList.size() - 1;
                            activeMusic = storage.getItemFromInitialList(musicIdList.get(musicIndex));

                        } else {
                            activeMusic = storage.getItemFromInitialList(musicIdList.get(musicIndex));

                        }
                    if (activeMusic != null) {
                        //update stored index
                        storage.saveMusicIndex(musicIndex);

                        stopMedia();
                        initiateMediaPlayer();
                    }
                }
            } else {
                if (lastPos >= 3000) {
                    activeMusic = storage.getItemFromInitialList(musicIdList.get(musicIndex));
                    if (activeMusic != null) {
                        stopMedia();
                        initiateMediaPlayer();
                    }
                } else {
                    if (musicIdList != null)
                        if (musicIndex == 0) {
                            musicIndex = musicIdList.size() - 1;
                            activeMusic = storage.getItemFromInitialList(musicIdList.get(musicIndex));

                        } else {
                            activeMusic = storage.getItemFromInitialList(musicIdList.get(--musicIndex));

                        }
                    if (activeMusic != null) {
                        //update stored index
                        storage.saveMusicIndex(musicIndex);

                        stopMedia();
                        initiateMediaPlayer();
                    }
                }
            }
        }
    }

    /**
     * This function skips music to next index
     */
    public void skipToNext() {
        musicIdList = storage.loadQueueList();
        musicIndex = storage.loadMusicIndex();
        storage.clearMusicLastPos();

        if (musicIdList != null)
            if (musicIndex == musicIdList.size() - 1) {
                //if last in list
                musicIndex = 0;
                activeMusic = storage.getItemFromInitialList(musicIdList.get(musicIndex));

            } else {
                //get next in playlist
                activeMusic = storage.getItemFromInitialList(musicIdList.get(++musicIndex));

            }
        if (activeMusic != null) {
            //update stored index
            storage.saveMusicIndex(musicIndex);

            stopMedia();
            initiateMediaPlayer();
        }
    }

    /**
     * this function sets up a media session
     */
    public void initiateMediaSession() throws RemoteException {
        if (mediaSessionManager != null) return; //manager already exists
        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        transportControls = mediaSession.getController().getTransportControls();
        mediaSession.setActive(true);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);

//        updateMetaData(activeMusic);

        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                String intentAction = mediaButtonEvent.getAction();
                if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
                    KeyEvent event = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                    if (event != null) {
                        int action = event.getAction();
                        if (action == KeyEvent.ACTION_DOWN) {
                            if (event.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                                if (media_player != null) {
                                    if (media_player.isPlaying()) {
                                        pauseMedia();
                                    } else {
                                        resumeMedia();
                                    }
                                }
                                return true;
                            } else if (event.getKeyCode() == KeyEvent.KEYCODE_MEDIA_NEXT) {
                                onSkipToNext();
                                return true;
                            } else if (event.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
                                onSkipToPrevious();
                                return true;
                            }
                        }
                    }
                }
                return super.onMediaButtonEvent(mediaButtonEvent);
            }

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

            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
            }

            @Override
            public void onStop() {
                super.onStop();
                stoppedByNotification();
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                //clearing the storage before putting new value
                storage.clearMusicLastPos();
                //storing the current position of seekbar in storage so we can access it from services
                storage.saveMusicLastPos(Math.toIntExact(pos));

                //first checking setting the media seek to current position of seek bar and then setting all data in UI•
                if (service_bound) {
                    //removing handler so that we can seek without glitches handler will restart in setSeekBar() method☻
                    if (seekBarHandler != null)
                        seekBarHandler.removeCallbacks(seekBarRunnable);
                    if (media_player != null) {
                        media_player.seekTo(Math.toIntExact(pos));
                        if (media_player.isPlaying()) {
                            buildNotification(PlaybackStatus.PLAYING, 1f);
                        } else {
                            buildNotification(PlaybackStatus.PAUSED, 0f);
                        }
                        setSeekBar();
                    }
                }
            }

        });
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (was_playing) {
                    resumeMedia();
                    was_playing = false;
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (media_player != null)
                    if (media_player.isPlaying()) {
                        pauseMedia();
                        was_playing = true;
                    }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (settingsStorage.loadLowerVol()) {
                    if (media_player != null)
                        if (media_player.isPlaying()) {
                            media_player.setVolume(0.3f, 0.3f);
                        }
                }
                break;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent);

        storage = new StorageUtil(getApplicationContext());
        settingsStorage = new StorageUtil.SettingsStorage(getApplicationContext());
        musicIdList = storage.loadQueueList();
        musicIndex = storage.loadMusicIndex();

        if (musicIdList != null)
            if (musicIndex != -1 && musicIndex < musicIdList.size()) {
                activeMusic = storage.getItemFromInitialList(musicIdList.get(musicIndex));
            } else {
                stopSelf();
            }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeAudioFocus();
        service_stopped = true;
        if (media_player != null) {
            storage.saveMusicLastPos(media_player.getCurrentPosition());
        }
        //disable phone state listener ♣
        if (phoneStateListener != null && telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(pluggedInDevice);
        unregisterReceiver(playNewMusicReceiver);
        unregisterReceiver(pausePlayMusicReceiver);
        unregisterReceiver(nextMusicReceiver);
        unregisterReceiver(prevMusicReceiver);
        unregisterReceiver(stopMusicReceiver);

        releasePlayer();
    }

    private void releasePlayer() {
        if (media_player != null) {
            media_player.release();
        }
        if (mediaSession != null) {
            mediaSession.release();
        }
        media_player = null;
        mediaSession = null;

        stopForeground(true);
        stopSelf();
    }

    /**
     * this function requests focus to play the music
     *
     * @return result of request, true if granted
     */
    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    /**
     * this function removes focus from our app
     */
    private void removeAudioFocus() {
        if (audioManager != null) {
            audioManager.abandonAudioFocus(this);
        }
    }

    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }
}