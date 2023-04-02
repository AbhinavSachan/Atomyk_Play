package com.atomykcoder.atomykplay.services;

import static com.atomykcoder.atomykplay.activities.MainActivity.BROADCAST_PLAY_NEW_MUSIC;
import static com.atomykcoder.atomykplay.activities.MainActivity.service_bound;
import static com.atomykcoder.atomykplay.activities.MainActivity.service_stopped;
import static com.atomykcoder.atomykplay.classes.ApplicationClass.CHANNEL_ID;

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
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
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
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.classes.ApplicationClass;
import com.atomykcoder.atomykplay.classes.PhoneStateCallback;
import com.atomykcoder.atomykplay.data.Music;
import com.atomykcoder.atomykplay.dataModels.LRCMap;
import com.atomykcoder.atomykplay.enums.PlaybackStatus;
import com.atomykcoder.atomykplay.events.PrepareRunnableEvent;
import com.atomykcoder.atomykplay.events.RemoveLyricsHandlerEvent;
import com.atomykcoder.atomykplay.events.SetMainLayoutEvent;
import com.atomykcoder.atomykplay.events.SetTimerText;
import com.atomykcoder.atomykplay.events.TimerFinished;
import com.atomykcoder.atomykplay.events.UpdateMusicImageEvent;
import com.atomykcoder.atomykplay.events.UpdateMusicProgressEvent;
import com.atomykcoder.atomykplay.fragments.BottomSheetPlayerFragment;
import com.atomykcoder.atomykplay.helperFunctions.Logger;
import com.atomykcoder.atomykplay.helperFunctions.MusicHelper;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    public static final int NOTIFICATION_ID = 874159;
    public static boolean is_playing = false;
    public static boolean ui_visible;
    //binder
    private final IBinder iBinder = new LocalBinder();
    private final Handler handler = new Handler();
    private Handler selfStopHandler;
    private Runnable selfStopRunnable;
    public MediaPlayer media_player;
    public Runnable seekBarRunnable;
    public Handler seekBarHandler;
    private boolean was_playing = false;
    //media session
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;
    //list of available music
    private ArrayList<Music> musicList;
    private int musicIndex = -1;
    private Music activeMusic = null;//object of currently playing audio
    private final CountDownTimer[] countDownTimer = new CountDownTimer[1];
    // phone call listener
    private PhoneStateListener phoneStateListener;
    //for above android 10 devices
    private PhoneStateCallback phoneStateCallback;
    private TelephonyManager telephonyManager;
    private AudioManager audioManager;
    //broadcast receivers
    //playing new song
    private StorageUtil storage;
    private StorageUtil.SettingsStorage settingsStorage;
    private NotificationManager notificationManager;
    private final BroadcastReceiver stopMusicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stoppedByNotification();
        }
    };
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
                if (settingsStorage != null) {
                    if (settingsStorage.loadAutoPlay()) {
                        if (state == 1) {
                            if (!is_playing) {
                                if (isMediaPlayerNotNull()) {
                                    resumeMedia();
                                } else {
                                    try {
                                        initiateMediaSession();
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                    initiateMediaPlayer();
                                }
                                Logger.normalLog("PluggedIn");
                            }
                        }
                    }
                }
            }
        }
    };
    private final BroadcastReceiver nextMusicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onNextReceived();

        }
    };
    private final BroadcastReceiver prevMusicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onPreviousReceived();

        }
    };
    private final BroadcastReceiver pausePlayMusicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onPlayPauseReceived();
        }
    };
    private final BroadcastReceiver playNewMusicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String encodedMessage = intent.getStringExtra("music");
            Music music = MusicHelper.decode(encodedMessage);

            if (music != null) {
                activeMusic = music;
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
    private int artworkDimension;
    private Bitmap defaultThumbnail;
    private MediaMetadataCompat defaultMetadata;
    private Notification initialNotification;
    private Notification musicNotification;
    private AudioFocusRequest audioFocusRequest;

    private void onNextReceived() {
        if (mediaSessionManager == null) {
            try {
                initiateMediaSession();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
        }
        skipToNext();
        Logger.normalLog("onNext");
    }

    private void onPreviousReceived() {
        if (mediaSessionManager == null) {
            try {
                initiateMediaSession();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
        }
        skipToPrevious();
        Logger.normalLog("onPrev");
    }

    private void onPlayPauseReceived() {
        if (!isMediaPlayerNotNull()) {
            try {
                initiateMediaSession();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            initiateMediaPlayer();
        } else {
            if (isMediaPlaying()) {
                pauseMedia();
            } else {
                resumeMedia();
            }
        }
        Logger.normalLog("onPlayPause");
    }

    /**
     * this function updates play icon according to media playback status
     */
    public void setIcon(PlaybackStatus playbackStatus) {
        if (musicList != null) {
            if (playbackStatus == PlaybackStatus.PLAYING) {
                EventBus.getDefault().post(new UpdateMusicImageEvent(false));
            } else if (playbackStatus == PlaybackStatus.PAUSED) {
                EventBus.getDefault().post(new UpdateMusicImageEvent(true));
            }
        }
    }

    private void registerPlayNewMusic() {
        IntentFilter filter = new IntentFilter(BROADCAST_PLAY_NEW_MUSIC);
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
    private void updateMetaData(Music activeMusic, Bitmap finalImage) {
        musicList = storage.loadQueueList();
        musicIndex = storage.loadMusicIndex();
        Bitmap thumbnail;
        MediaMetadataCompat metadata = defaultMetadata;

        if (activeMusic != null) {
            String dur = activeMusic.getDuration();
            String songName = activeMusic.getName();
            String artistName = activeMusic.getArtist();
            String album = activeMusic.getAlbum();

            thumbnail = finalImage != null ? ThumbnailUtils.extractThumbnail(
                    finalImage,
                    artworkDimension - (artworkDimension / 5),
                    artworkDimension - (artworkDimension / 5),
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT) : defaultThumbnail;

            metadata = new MediaMetadataCompat.Builder()
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, thumbnail)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artistName)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songName)
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, Long.parseLong(dur))
                    .build();
        }
        mediaSession.setMetadata(metadata);
    }

    /**
     * This function skips music to next index
     *
     * @param playbackStatus is music playing or not
     * @param playbackSpeed  to set the speed of seekbar in notification 1f for 1s/s and 0f to stopped
     */
    public void buildNotification(PlaybackStatus playbackStatus, float playbackSpeed) {

        if (!isMediaPlayerNotNull() || musicList == null || activeMusic == null) return;

        int notificationAction = R.drawable.ic_pause_for_noti;//needs to be initialized
        PendingIntent play_pauseAction = null;

        //build a new notification according to media player status
        if (playbackStatus == PlaybackStatus.PLAYING) {
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = R.drawable.ic_play_for_noti;
            play_pauseAction = playbackAction(0);
        }

        //building notification for player
        //set content
        //set control
        musicNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setShowWhen(false).setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .setColor(getResources().getColor(R.color.tertiary_bg, getTheme()))
                .setColorized(true)
                .setSmallIcon(R.drawable.ic_headset)
                //set content
                .setContentText(activeMusic.getArtist())
                .setContentTitle(activeMusic.getAlbum())
                .setContentInfo(activeMusic.getName())
                .setDeleteIntent(playbackAction(4))
                .setChannelId(CHANNEL_ID)
                //set control
                .addAction(R.drawable.ic_previous_for_noti, "Previous", playbackAction(3))
                .addAction(notificationAction, "Pause", play_pauseAction)
                .addAction(R.drawable.ic_next_for_noti, "next", playbackAction(2))
                .addAction(R.drawable.ic_close, "stop", playbackAction(4))
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_IMMUTABLE))
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_DEFAULT)
                .build();

        if (playbackStatus == PlaybackStatus.PLAYING) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PLAYING, getCurrentMediaPosition(), playbackSpeed)
                        .setActions(PlaybackStateCompat.ACTION_SEEK_TO).build());
            }
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PAUSED, getCurrentMediaPosition(), playbackSpeed)
                        .setActions(PlaybackStateCompat.ACTION_SEEK_TO).build());
            }
        }
        notificationManager.notify(NOTIFICATION_ID, musicNotification);
    }

    private void buildInitialNotification() {
        initialNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setColor(getResources().getColor(R.color.player_bg, getTheme()))
                .setColorized(true)
                .setSmallIcon(R.drawable.ic_headset)
                //set content
                .setContentText(getString(R.string.app_name) + " is running in background.")
                .setContentInfo("Running")
                .setChannelId(CHANNEL_ID)
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_IMMUTABLE))
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_DEFAULT)
                .build();

    }


    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
        switch (actionNumber) {
            case 0:
                //play
                playbackIntent.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackIntent, PendingIntent.FLAG_IMMUTABLE);

            case 1:
                //pause
                playbackIntent.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackIntent, PendingIntent.FLAG_IMMUTABLE);

            case 2:
                //next
                playbackIntent.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackIntent, PendingIntent.FLAG_IMMUTABLE);

            case 3:
                //previous
                playbackIntent.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackIntent, PendingIntent.FLAG_IMMUTABLE);

            case 4:
                //stop
                playbackIntent.setAction(ACTION_STOP);
                return PendingIntent.getService(this, actionNumber, playbackIntent, PendingIntent.FLAG_IMMUTABLE);

            default:
                break;
        }
        return null;
    }

    /**
     * This function sets up notification actions
     */
    private void handleNotificationActions(Intent playbackAction) {
        if (playbackAction == null ||
                playbackAction.getAction() == null) return;

        switch (playbackAction.getAction()) {
            case ACTION_PLAY:
                if (!isMediaPlayerNotNull())
                    onPlayPauseReceived();
                else
                    transportControls.play();
                break;
            case ACTION_PAUSE:
                if (!isMediaPlayerNotNull())
                    onPlayPauseReceived();
                else
                    transportControls.pause();
                break;
            case ACTION_NEXT:
                if (!isMediaPlayerNotNull())
                    onNextReceived();
                else
                    transportControls.skipToNext();
                break;
            case ACTION_PREVIOUS:
                if (!isMediaPlayerNotNull())
                    onPreviousReceived();
                else
                    transportControls.skipToPrevious();
                break;
            case ACTION_STOP:
                if (!isMediaPlayerNotNull())
                    stoppedByNotification();
                else
                    transportControls.stop();
                break;
        }
    }

    /**
     * this function checks if any phone calls are on going
     */
    private void callStateListener() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            phoneStateCallback = new CustomCallStateListener();
            telephonyManager.registerTelephonyCallback(ContextCompat.getMainExecutor(this), phoneStateCallback);
        } else {
            phoneStateListener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String phoneNumber) {
                    super.onCallStateChanged(state, phoneNumber);
                    takeActionOnCall(state);
                }
            };
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private class CustomCallStateListener extends PhoneStateCallback {
        @Override
        public void onCallStateChanged(int state) {
            takeActionOnCall(state);
        }
    }

    private void takeActionOnCall(int state) {
        switch (state) {
            case TelephonyManager.CALL_STATE_OFFHOOK:
            case TelephonyManager.CALL_STATE_RINGING:
                if (isMediaPlaying()) {
                    was_playing = true;
                    pauseMedia();
                    MainActivity.phone_ringing = true;
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                if (isMediaPlayerNotNull()) {
                    MainActivity.phone_ringing = false;
                    if (was_playing) {
                        if (!isMediaPlayerNotNull()) {
                            initiateMediaPlayer();
                            if (mediaSession == null) {
                                try {
                                    initiateMediaSession();
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            resumeMedia();
                        }
                        was_playing = false;
                    }
                }
                break;
        }
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
        Logger.normalLog("BindService");
        return iBinder;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        ExecutorService service = Executors.newFixedThreadPool(1);
        service.execute(() -> {
            //image decoder
            Bitmap[] image = {null};
            byte[] art = new byte[0];
            try (MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever()) {
                mediaMetadataRetriever.setDataSource(activeMusic.getPath());
                art = mediaMetadataRetriever.getEmbeddedPicture();
            } catch (IOException ignored) {
            }

            try {
                image[0] = BitmapFactory.decodeByteArray(art, 0, art.length);
            } catch (Exception ignored) {
            }

            handler.post(() -> {
                if (service_bound) {
                    EventBus.getDefault().post(new SetMainLayoutEvent(activeMusic, image[0]));
                    updateMetaData(activeMusic, image[0]);
                }
                resumeMedia();
            });
        });
        service.shutdown();
        service = null;

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        storage.clearMusicLastPos();
        loadList();

        if (musicIndex == -1 || musicList == null) return;

        switch (storage.loadRepeatStatus()) {
            case "no_repeat":
                if (musicIndex == musicList.size() - 1)
                    pauseMedia();
                else
                    skipToNext();
                break;
            case "repeat":
                skipToNext();
                break;
            case "repeat_one":
                playMedia();
                EventBus.getDefault().post(new PrepareRunnableEvent());
                break;
        }

        if (isMediaPlaying()) {
            setIcon(PlaybackStatus.PLAYING);
            buildNotification(PlaybackStatus.PLAYING, 1f);
        } else {
            setIcon(PlaybackStatus.PAUSED);
            buildNotification(PlaybackStatus.PAUSED, 0f);
        }

    }

    private void loadList() {
        if (storage != null) {
            musicList = storage.loadQueueList();
            musicIndex = storage.loadMusicIndex();
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    private void showToast(String s) {
        ((ApplicationClass) getApplication()).showToast(s);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                try {
                    stoppedByNotification();
                    setIcon(PlaybackStatus.PAUSED);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                showToast("MEDIA_ERROR_UNKNOWN" + extra);
                return true;
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                try {
                    stoppedByNotification();
                    setIcon(PlaybackStatus.PAUSED);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                showToast("MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK" + extra);
                return true;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                try {
                    //service is active send media with broadcast receiver
                    String encodedMessage = MusicHelper.encode(activeMusic);
                    Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEW_MUSIC);
                    broadcastIntent.putExtra("music", encodedMessage);
                    sendBroadcast(broadcastIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                showToast("MEDIA_ERROR_SERVER_DIED" + extra);
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
        if (!isMediaPlayerNotNull()) {
            Logger.normalLog("Media player null = " + isMediaPlayerNotNull());
            media_player = new MediaPlayer();
            //setup MediaPlayer event listeners
            media_player.setOnPreparedListener(this);
            media_player.setOnErrorListener(this);
            media_player.setOnBufferingUpdateListener(this);
            media_player.setOnSeekCompleteListener(this);
            media_player.setOnInfoListener(this);
            media_player.setOnCompletionListener(this);
        }

        //reset so that the media player is not pointing to another data source
        media_player.reset();
        media_player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            loadList();
            if (activeMusic != null) {
                if (musicList != null && !musicList.isEmpty()) {
                    media_player.setDataSource(activeMusic.getPath());
                    media_player.prepare();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * This function plays music from start
     */
    public void playMedia() {
        if (!requestAudioFocus())
            requestAudioFocus();

        if (MainActivity.phone_ringing) {
            showToast("Can't play while on call");
            return;
        }

        if (!isMediaPlayerNotNull()) return;

        if (!isMediaPlaying()) {
            media_player.start();
            is_playing = true;
            setIcon(PlaybackStatus.PLAYING);
            buildNotification(PlaybackStatus.PLAYING, 1f);
        }
    }

    /**
     * This function stops music to play something else
     */
    public void stopMedia() {
        if (!isMediaPlayerNotNull()) {
            return;
        }
        if (isMediaPlaying()) {
            media_player.stop();
            is_playing = false;
        }
    }

    /**
     * This function stops music and removes notification
     */
    public void stoppedByNotification() {
        if (isMediaPlayerNotNull() && isMediaPlaying()) {
            storage.saveMusicLastPos(getCurrentMediaPosition());
            media_player.pause();
            is_playing = false;
            setIcon(PlaybackStatus.PAUSED);
            buildNotification(PlaybackStatus.PAUSED, 0f);
        }

        if (notificationManager != null) {
            notificationManager.cancelAll();
        }

        service_stopped = true;
        stopSelf();
    }

    /**
     * This function resumes music
     */
    public void resumeMedia() {
        // request audio focus if false
        if (!requestAudioFocus())
            requestAudioFocus();

        //make toast if volume is off
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0f)
            showToast("Please turn the volume UP");

        // make toast and do early return if phone ringing
        if (MainActivity.phone_ringing) {
            showToast("Can't play while on call");
            return;
        }

        // initiate player and return early if media_player is null
        if (!isMediaPlayerNotNull()) {
            initiateMediaPlayer();
            return;
        }
        File file = null;
        if (activeMusic != null) {
            file = new File(activeMusic.getPath());
        }
        if (file != null) {
            if (!file.exists()) {
                skipToNext();
                return;
            }
        } else {
            skipToNext();
            return;
        }
        //if service is bound then set seekbar and load lyrics
        if (service_bound) {
            setSeekBar();
            LRCMap lrcMap = null;
            if (activeMusic != null) {
                lrcMap = storage.loadLyrics(activeMusic.getId());
            }
            if (lrcMap != null)
                EventBus.getDefault().post(new PrepareRunnableEvent());
        }

        // set is_playing to true
        is_playing = true;

        // set media_player seekbar to position and start media_player
        int position = storage.loadMusicLastPos();
        seekMediaTo(position);
        media_player.start();

        // set icon to playing and build notification
        setIcon(PlaybackStatus.PLAYING);
        buildNotification(PlaybackStatus.PLAYING, 1f);
        Logger.normalLog("Resume");
    }


    /**
     * This function pauses music
     */
    public void pauseMedia() {
        if (isMediaPlayerNotNull() && isMediaPlaying()) {
            if (service_bound) {
                if (seekBarHandler != null)
                    seekBarHandler.removeCallbacks(seekBarRunnable);
                EventBus.getDefault().post(new RemoveLyricsHandlerEvent());
            }
            storage.saveMusicLastPos(getCurrentMediaPosition());
            media_player.pause();

            is_playing = false;
            setIcon(PlaybackStatus.PAUSED);
            buildNotification(PlaybackStatus.PAUSED, 0f);
        }
    }

    /**
     * This function sends music position to main ui
     */
    public void setSeekBar() {
        seekBarHandler = new Handler(Looper.getMainLooper());
        if (isMediaPlayerNotNull()) {
            // if media_player hasn't finished the song yet then update progress
            if (getCurrentMediaPosition() <= media_player.getDuration()) {
                seekBarRunnable = () -> {
                    int position = 0;

                    try {
                        position = getCurrentMediaPosition();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }

                    EventBus.getDefault().post(new UpdateMusicProgressEvent(position));
                    seekBarHandler.postDelayed(seekBarRunnable, 300);
                };
                seekBarHandler.postDelayed(seekBarRunnable, 0);
            }
        }
    }

    public int getCurrentMediaPosition() {
        if (isMediaPlayerNotNull()) {
            return media_player.getCurrentPosition();
        }
        return 0;
    }

    public boolean isMediaPlaying() {
        if (isMediaPlayerNotNull()) {
            return media_player.isPlaying();
        }
        return false;
    }

    /**
     * This function skips music to previous index
     */
    public void seekMediaTo(int sec) {
        if (isMediaPlayerNotNull()) {
            media_player.seekTo(sec);
        }
    }

    public boolean isMediaPlayerNotNull() {
        return media_player != null;
    }

    public void skipToPrevious() {
        loadList();
        int lastPos = storage.loadMusicLastPos();

        storage.clearMusicLastPos();
        try {
            if (musicIndex == -1 || musicList == null) {
                return;
            }
            if (settingsStorage.loadOneClickSkip()) {
                setActiveMusicForPrevious();
            } else {
                if ((isMediaPlayerNotNull() && getCurrentMediaPosition() >= 3000) || lastPos >= 3000) {
                    activeMusic = musicList.get(musicIndex);
                    BottomSheetPlayerFragment.playing_same_song = true;
                } else {
                    setActiveMusicForPrevious();
                }
            }
        } catch (IndexOutOfBoundsException e) {
            showToast("Please try to play another song");
        }
        File file = null;
        if (activeMusic != null) {
            file = new File(activeMusic.getPath());
        }
        if (file != null) {
            if (!file.exists()) {
                storage.saveMusicIndex(musicIndex);
                skipToPrevious();
                return;
            }
        } else {
            storage.saveMusicIndex(musicIndex);
            skipToPrevious();
            return;
        }
        if (activeMusic != null) {
            storage.saveMusicIndex(musicIndex);
            stopMedia();
            initiateMediaPlayer();
            Logger.normalLog("Previous");
        }
    }

    private void setActiveMusicForPrevious() {
        if (musicIndex == 0) {
            musicIndex = musicList.size() - 1;
            activeMusic = musicList.get(musicIndex);
        } else {
            activeMusic = musicList.get(--musicIndex);
        }
        BottomSheetPlayerFragment.playing_same_song = false;
    }

    /**
     * This function skips music to next index
     */
    public void skipToNext() {
        loadList();
        storage.clearMusicLastPos();

        try {
            if (musicIndex != -1 && musicList != null)
                if (musicIndex == musicList.size() - 1) {
                    //if last in list
                    musicIndex = 0;
                    activeMusic = musicList.get(musicIndex);

                } else {
                    activeMusic = musicList.get(++musicIndex);
                }
            BottomSheetPlayerFragment.playing_same_song = false;
        } catch (IndexOutOfBoundsException e) {
            showToast("Please try to play another song");
        }
        File file = null;
        if (activeMusic != null) {
            file = new File(activeMusic.getPath());
        }
        if (file != null) {
            if (!file.exists()) {
                storage.saveMusicIndex(musicIndex);
                skipToNext();
                return;
            }
        } else {
            storage.saveMusicIndex(musicIndex);
            skipToNext();
            return;
        }
        if (activeMusic != null) {
            storage.saveMusicIndex(musicIndex);
            stopMedia();
            initiateMediaPlayer();
            Logger.normalLog("Next");
        }
    }

    public void setTimer(int progress) {

        //Sets The already Initialized countDownTimer to a new countDownTimer with given parameters
        countDownTimer[0] = new CountDownTimer((progress * 5L + 5) * 1000L * 60, 1000) {

            //Variables For storing seconds and minutes
            int seconds;
            int minutes;

            //Every Second Do Something
            //Update TextView Code Goes Here
            @Override
            public void onTick(long l) {

                //Storing Seconds and Minutes on Every Tick
                seconds = (int) (l / 1000) % 60;
                minutes = (int) ((l / (1000 * 60)) % 60);

                // Replace This with TextView.setText(View);
                String finalCDTimer = minutes + ":" + seconds;
                if (ui_visible) {
                    EventBus.getDefault().post(new SetTimerText(finalCDTimer));
                }
            }


            //Code After timer is Finished Goes Here
            @Override
            public void onFinish() {
                //Replace This pausePlayAudio() with just a pause Method.
                //Replaced it
                pauseMedia();
                if (ui_visible) {
                    EventBus.getDefault().post(new TimerFinished());
                    countDownTimer[0] = null;
                }
            }
        };
        // Start timer
        countDownTimer[0].start();
    }

    public void cancelTimer() {
        countDownTimer[0].cancel();
        EventBus.getDefault().post(new TimerFinished());
    }

    /**
     * this function sets up a media session
     */
    public void initiateMediaSession() throws RemoteException {
        Logger.normalLog("Media session null = " + (mediaSession == null));
        if (mediaSession != null) return;
        mediaSession = new MediaSessionCompat(getApplicationContext(), "MediaPlayerMediaSession");
        transportControls = mediaSession.getController().getTransportControls();
        mediaSession.setActive(true);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);
        Logger.normalLog("MediaSessionCreated");
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                String intentAction = mediaButtonEvent.getAction();

                if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
                    return super.onMediaButtonEvent(mediaButtonEvent);
                }

                KeyEvent event = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

                if (event == null) {
                    return super.onMediaButtonEvent(mediaButtonEvent);
                }

                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (event.getKeyCode()) {
                        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                            if (isMediaPlaying())
                                pauseMedia();
                            else
                                resumeMedia();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_NEXT:
                            onSkipToNext();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                            onSkipToPrevious();
                            break;
                    }
                    return true;
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
                    if (isMediaPlayerNotNull()) {
                        seekMediaTo(Math.toIntExact(pos));
                        if (isMediaPlaying()) {
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
                    if (!isMediaPlayerNotNull()) {
                        initiateMediaPlayer();
                        Logger.normalLog("FocusGainInitiated");
                        if (mediaSession == null) {
                            try {
                                initiateMediaSession();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        resumeMedia();
                        Logger.normalLog("FocusGainResumed");
                    }
                    was_playing = false;
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (isMediaPlaying()) {
                    pauseMedia();
                    Logger.normalLog("FocusTransientPaused");
                    was_playing = true;
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (settingsStorage.loadLowerVol()) {
                    if (isMediaPlaying()) {
                        media_player.setVolume(0.3f, 0.3f);
                        Logger.normalLog("FocusDuckLowerVol");
                    }
                }
                break;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Handle Intent action from MediaSession.TransportControls
        handleNotificationActions(intent);
        Logger.normalLog("ServiceStarted");
        if (selfStopHandler == null) {
            selfStopHandler = new Handler(Looper.getMainLooper());
        }
        if (storage == null) {
            storage = new StorageUtil(getApplicationContext());
        }
        if (settingsStorage == null) {
            settingsStorage = new StorageUtil.SettingsStorage(getApplicationContext());
        }
        if (mediaSessionManager == null) {
            mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        }
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        loadList();

        if (musicList != null)
            if (musicIndex != -1 && musicIndex < musicList.size()) {
                activeMusic = musicList.get(musicIndex);
            } else {
                stopSelf();
                return START_STICKY;
            }

        Bitmap defaultArtwork = BitmapFactory.decodeResource(MediaPlayerService.this.getApplicationContext()
                .getResources(), R.drawable.placeholder_art);


        artworkDimension =
                Math.min(defaultArtwork.getWidth(), defaultArtwork.getHeight());


        defaultThumbnail = ThumbnailUtils.extractThumbnail(defaultArtwork,
                artworkDimension - (artworkDimension / 5), artworkDimension - (artworkDimension / 5), ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

        if (defaultMetadata == null) {
            defaultMetadata = new MediaMetadataCompat.Builder()
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, defaultThumbnail)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "")
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "")
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "")
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
                    .build();
        }

        if (activeMusic != null) {
            if (musicNotification != null) {
                try {
                    startForeground(NOTIFICATION_ID, musicNotification);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                buildInitialNotification();
                try {
                    startForeground(NOTIFICATION_ID, initialNotification);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        //here we are stopping service after 5 minutes if app
        // is not running in background and player is not playing
        //this is a optional feature for user
        if (settingsStorage != null && settingsStorage.loadSelfStop()) {
            if (selfStopHandler != null && selfStopRunnable != null) {
                selfStopRunnable = () -> {

                    if (!is_playing && !ui_visible) {
                        stopSelf();
                    }
                    selfStopHandler.postDelayed(selfStopRunnable, 5 * 60 * 1000);
                };
                selfStopHandler.postDelayed(selfStopRunnable, 0);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeAudioFocus();
        service_stopped = true;
        selfStopHandler.removeCallbacks(selfStopRunnable);
        selfStopHandler = null;
        selfStopRunnable = null;
        if (isMediaPlayerNotNull()) {
            storage.saveMusicLastPos(getCurrentMediaPosition());
        }
        //disable phone state listener ♣
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (phoneStateCallback != null && telephonyManager != null) {
                telephonyManager.unregisterTelephonyCallback(phoneStateCallback);
            }
        } else {
            if (phoneStateListener != null && telephonyManager != null) {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            }
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
        Logger.normalLog("MediaPlayerReleased");
        if (isMediaPlayerNotNull()) {
            media_player.release();
        }
        if (mediaSession != null) {
            mediaSession.release();
        }
        media_player = null;
        mediaSession = null;

        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
            musicNotification = null;
        }
        stopForeground(true);
    }

    /**
     * this function requests focus to play the music
     *
     * @return result of request, true if granted
     */
    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            AudioFocusRequest.Builder builder = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN);
            builder.setOnAudioFocusChangeListener(this);
            audioFocusRequest = builder.build();
            result = audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void removeAudioFocus() {
        if (audioManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
            } else {
                audioManager.abandonAudioFocus(this);
            }
        }
    }

    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }
}