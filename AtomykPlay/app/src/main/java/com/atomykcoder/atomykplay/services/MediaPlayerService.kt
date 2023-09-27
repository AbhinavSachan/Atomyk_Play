package com.atomykcoder.atomykplay.services

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothManager
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.*
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer.*
import android.media.session.MediaSessionManager
import android.os.*
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.KeyEvent
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.atomykcoder.atomykplay.ApplicationClass
import com.atomykcoder.atomykplay.BuildConfig
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.classes.PhoneStateCallback
import com.atomykcoder.atomykplay.constants.BroadcastStrings.BROADCAST_PAUSE_PLAY_MUSIC
import com.atomykcoder.atomykplay.constants.BroadcastStrings.BROADCAST_PLAY_NEW_MUSIC
import com.atomykcoder.atomykplay.constants.BroadcastStrings.BROADCAST_PLAY_NEXT_MUSIC
import com.atomykcoder.atomykplay.constants.BroadcastStrings.BROADCAST_PLAY_PREVIOUS_MUSIC
import com.atomykcoder.atomykplay.constants.BroadcastStrings.BROADCAST_STOP_MUSIC
import com.atomykcoder.atomykplay.constants.RepeatModes
import com.atomykcoder.atomykplay.data.Music
import com.atomykcoder.atomykplay.enums.PlaybackStatus
import com.atomykcoder.atomykplay.events.*
import com.atomykcoder.atomykplay.fragments.BottomSheetPlayerFragment
import com.atomykcoder.atomykplay.helperFunctions.AudioFileCover
import com.atomykcoder.atomykplay.helperFunctions.GlideApp
import com.atomykcoder.atomykplay.helperFunctions.Logger
import com.atomykcoder.atomykplay.helperFunctions.MusicHelper
import com.atomykcoder.atomykplay.models.LRCMap
import com.atomykcoder.atomykplay.ui.MainActivity
import com.atomykcoder.atomykplay.utils.AndroidUtil.toUnscaledBitmap
import com.atomykcoder.atomykplay.utils.MusicEnhancerUtil
import com.atomykcoder.atomykplay.utils.StorageUtil
import com.atomykcoder.atomykplay.utils.StorageUtil.SettingsStorage
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.IOException

class MediaPlayerService : MediaBrowserServiceCompat(), OnCompletionListener,
    OnBufferingUpdateListener, OnErrorListener, OnInfoListener, OnPreparedListener,
    OnSeekCompleteListener, OnAudioFocusChangeListener {
    private var mPackageValidator: PackageValidator? = null

    //binder
    private val iBinder: IBinder = LocalBinder()
    private val handler = Handler(Looper.getMainLooper())
    private val countDownTimer = arrayOfNulls<CountDownTimer>(1)
    private var mediaPlayer: MediaPlayer? = null
    private val coroutineScope by lazy {
        CoroutineScope(Dispatchers.IO)
    }

    @JvmField
    var seekBarRunnable: Runnable? = null

    @JvmField
    var seekBarHandler: Handler? = null
    private val selfStopHandler: Handler by lazy { Handler(Looper.getMainLooper()) }
    private var selfStopRunnable: Runnable? = null
    private var wasPlaying = false

    private var musicEnhancerUtil: MusicEnhancerUtil? = null

    //media session
    private val mediaSessionManager: MediaSessionManager by lazy {
        getSystemService(
            MEDIA_SESSION_SERVICE
        ) as MediaSessionManager
    }
    private var mediaSession: MediaSessionCompat? = null
    private var transportControls: MediaControllerCompat.TransportControls? = null

    //list of available music
    private var musicList: ArrayList<Music>? = null
    private var musicIndex = -1
    private var activeMusic: Music? = null //object of currently playing audio

    // phone call listener
    private var phoneStateListener: PhoneStateListener? = null

    //for above android 10 devices
    private var phoneStateCallback: PhoneStateCallback? = null
    private val telephonyManager: TelephonyManager by lazy { getSystemService(TELEPHONY_SERVICE) as TelephonyManager }
    private val audioManager: AudioManager by lazy { getSystemService(AUDIO_SERVICE) as AudioManager }

    //broadcast receivers
    //playing new song
    private val storage: StorageUtil by lazy { StorageUtil(applicationContext) }
    private val settingsStorage: SettingsStorage by lazy { SettingsStorage(applicationContext) }
    private val notificationManager: NotificationManager by lazy {
        getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager
    }
    private var artworkDimension = 0
    private var defaultThumbnail: Bitmap? = null
    private var defaultMetadata: MediaMetadataCompat? = null
    private var initialNotification: Notification? = null
    private var musicNotification: Notification? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    private val stopMusicReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            stoppedByNotification()
        }
    }

    //to pause when output device is unplugged
    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            pauseMedia()
        }
    } //to play when output device is plugged
    private val nextMusicReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            onNextReceived()
        }
    }
    private val prevMusicReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            onPreviousReceived()
        }
    }
    private val pluggedInDevice = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_HEADSET_PLUG) {
                val state = intent.getIntExtra("state", -1)
                if (state == 1) {
                    if (settingsStorage.loadAutoPlay()) {
                        if (!is_playing) {
                            if (isMediaPlayerNotNull) {
                                resumeMedia(true)
                            } else {
                                try {
                                    initiateMediaSession()
                                } catch (e: RemoteException) {
                                    e.printStackTrace()
                                }
                                initiateMediaPlayer()
                            }
                        }
                    }
                }

            }
        }
    }

    private val isBluetoothHeadsetConnected: Boolean
        get() {
            val bluetoothManager =
                applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val mBluetoothAdapter = bluetoothManager.adapter
//        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ApplicationClass.instance.showToast("Please Allow Bluetooth Permission")
                return false
            }
            return (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled && mBluetoothAdapter.getProfileConnectionState(
                BluetoothHeadset.HEADSET
            ) == BluetoothAdapter.STATE_CONNECTED)
        }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            Logger.normalLog("Action $action received")
            when (action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    Toast.makeText(context, "Bluetooth Connected", Toast.LENGTH_SHORT).show()
                    bluetoothConnected()
                }

                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    Toast.makeText(context, "Bluetooth Disconnected", Toast.LENGTH_SHORT).show()
                    bluetoothDisconnected()
                }
            }
        }
    }
    private val pausePlayMusicReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            onPlayPauseReceived()
        }
    }
    private val playNewMusicReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val encodedMessage = intent.getStringExtra("music")
            val music = MusicHelper.decode(encodedMessage)
            if (music != null) {
                activeMusic = music
            }
            try {
                initiateMediaSession()
            } catch (e: RemoteException) {
                e.printStackTrace()
                stopSelf()
            }
            stopMedia()
            initiateMediaPlayer()
        }
    }

    private fun onNextReceived() {
        try {
            initiateMediaSession()
        } catch (e: RemoteException) {
            stopSelf()
        }
        skipToNext()
    }

    private fun onPreviousReceived() {
        try {
            initiateMediaSession()
        } catch (e: RemoteException) {
            stopSelf()
        }
        skipToPrevious()
    }

    private fun onPlayPauseReceived() {
        if (!isMediaPlayerNotNull) {
            try {
                initiateMediaSession()
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            initiateMediaPlayer()
        } else {
            if (isMediaPlaying) {
                pauseMedia()
            } else {
                resumeMedia(true)
            }
        }
    }

    /**
     * this function updates play icon according to media playback status
     */
    fun setIcon(playbackStatus: PlaybackStatus) {
        if (musicList != null) {
            if (playbackStatus == PlaybackStatus.PLAYING) {
                EventBus.getDefault().post(UpdateMusicImageEvent(false))
            } else if (playbackStatus == PlaybackStatus.PAUSED) {
                EventBus.getDefault().post(UpdateMusicImageEvent(true))
            }
        }
    }

    private fun registerPlayNewMusic() {
        val filter = IntentFilter(BROADCAST_PLAY_NEW_MUSIC)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(playNewMusicReceiver, filter)
        } else {
            registerReceiver(playNewMusicReceiver, filter, Context.RECEIVER_EXPORTED)
        }
    }

    private fun registerPausePlayMusic() {
        val filter = IntentFilter(BROADCAST_PAUSE_PLAY_MUSIC)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(pausePlayMusicReceiver, filter)
        } else {
            registerReceiver(pausePlayMusicReceiver, filter, Context.RECEIVER_EXPORTED)
        }
    }

    private fun registerStopMusic() {
        val filter = IntentFilter(BROADCAST_STOP_MUSIC)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stopMusicReceiver, filter)
        } else {
            registerReceiver(stopMusicReceiver, filter, Context.RECEIVER_EXPORTED)
        }
    }

    private fun registerPlayNextMusic() {
        val filter = IntentFilter(BROADCAST_PLAY_NEXT_MUSIC)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(nextMusicReceiver, filter)
        } else {
            registerReceiver(nextMusicReceiver, filter, Context.RECEIVER_EXPORTED)
        }
    }

    private fun registerPlayPreviousMusic() {
        val filter = IntentFilter(BROADCAST_PLAY_PREVIOUS_MUSIC)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(prevMusicReceiver, filter)
        } else {
            registerReceiver(prevMusicReceiver, filter, Context.RECEIVER_EXPORTED)
        }
    }

    //when output device is unplugged it will activate
    private fun registerBecomingNoisyReceiver() {
        val i = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(becomingNoisyReceiver, i)
        } else {
            registerReceiver(becomingNoisyReceiver, i, Context.RECEIVER_EXPORTED)
        }
    }

    //when output device is plugged it will activate
    private fun registerPluggedDeviceReceiver() {
        val i = IntentFilter(AudioManager.ACTION_HEADSET_PLUG)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(pluggedInDevice, i)
        } else {
            registerReceiver(pluggedInDevice, i, Context.RECEIVER_EXPORTED)
        }
    }

    private fun registerBluetoothReceiver() {
        val i = IntentFilter()
        i.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        i.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(bluetoothReceiver, i)
        } else {
            registerReceiver(bluetoothReceiver, i, Context.RECEIVER_EXPORTED)
        }
    }

    /**
     * this function updates new music data in notification
     */
    private fun updateMetaData(activeMusic: Music?, finalImage: Bitmap?) {
        if (mediaSession == null) {
            try {
                initiateMediaSession()
            } catch (ignored: RemoteException) {
            }
        }
        coroutineScope.launch {
            loadList()
            val thumbnail: Bitmap
            var metadata = defaultMetadata
            if (activeMusic != null) {
                val dur = activeMusic.duration
                val songName = activeMusic.name
                val artistName = activeMusic.artist
                val album = activeMusic.album
                thumbnail = if (finalImage != null) {
                    val artworkDimension = finalImage.width.coerceAtMost(finalImage.height)
                    ThumbnailUtils.extractThumbnail(
                        finalImage,
                        artworkDimension - artworkDimension / 5,
                        artworkDimension - artworkDimension / 5
                    )
                } else {
                    defaultThumbnail!!
                }

                metadata = MediaMetadataCompat.Builder()
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, thumbnail)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artistName)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songName)
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, dur.toLong()).build()
            }
            val finalMetaData = metadata
            handler.post {
                mediaSession!!.setMetadata(finalMetaData)
                buildNotification(PlaybackStatus.PLAYING, 1f)
            }
        }

    }

    /**
     * This function skips music to next index
     *
     * @param playbackStatus is music playing or not
     * @param playbackSpeed  to set the speed of seekbar in notification 1f for 1s/s and 0f to stopped
     */
    fun buildNotification(playbackStatus: PlaybackStatus, playbackSpeed: Float) {
        if (!isMediaPlayerNotNull || activeMusic == null) return
        var notificationAction = R.drawable.ic_pause_for_noti //needs to be initialized
        var playPauseAction: PendingIntent? = null

        //build a new notification according to media player status
        if (playbackStatus == PlaybackStatus.PLAYING) {
            playPauseAction = playbackAction(1)
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = R.drawable.ic_play_for_noti
            playPauseAction = playbackAction(0)
        }
        val prevAction: NotificationCompat.Action
        val stopAction: NotificationCompat.Action
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            if (isMediaPlaying) {
                prevAction = NotificationCompat.Action.Builder(
                    R.drawable.ic_previous_for_noti, "Previous", playbackAction(3)
                ).build()
                stopAction = NotificationCompat.Action.Builder(
                    R.drawable.ic_close, "Stop", playbackAction(4)
                ).build()
            } else {
                prevAction = NotificationCompat.Action.Builder(
                    R.drawable.ic_close, "Stop", playbackAction(4)
                ).build()
                stopAction = NotificationCompat.Action.Builder(
                    R.drawable.ic_previous_for_noti, "Previous", playbackAction(3)
                ).build()
            }
        } else {
            prevAction = NotificationCompat.Action.Builder(
                R.drawable.ic_previous_for_noti, "Previous", playbackAction(3)
            ).build()
            stopAction =
                NotificationCompat.Action.Builder(R.drawable.ic_close, "Stop", playbackAction(4))
                    .build()
        }


        //building notification for player
        //set content
        musicNotification =
            NotificationCompat.Builder(this, ApplicationClass.CHANNEL_ID).setShowWhen(false)
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession!!.sessionToken)
                        .setShowActionsInCompactView(0, 1, 2)

                ).setColor(resources.getColor(R.color.tertiary_bg, theme)).setColorized(true)
                .setSmallIcon(R.drawable.ic_headset) //set content
                .setSubText(activeMusic!!.artist).setContentTitle(activeMusic!!.name)
                .setDeleteIntent(playbackAction(4))
                .setChannelId(ApplicationClass.CHANNEL_ID) //set control
                .addAction(prevAction).addAction(notificationAction, "Pause", playPauseAction)
                .addAction(R.drawable.ic_next_for_noti, "Next", playbackAction(2))
                .addAction(stopAction)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(
                    PendingIntent.getActivity(
                        this,
                        0,
                        Intent(applicationContext, MainActivity::class.java),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                ).setSilent(true).setPriority(NotificationCompat.PRIORITY_MAX)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .build()
        if (playbackStatus == PlaybackStatus.PLAYING) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mediaSession!!.setPlaybackState(
                    PlaybackStateCompat.Builder().setState(
                        PlaybackStateCompat.STATE_PLAYING,
                        currentMediaPosition.toLong(),
                        playbackSpeed
                    ).setActions(
                        PlaybackStateCompat.ACTION_SEEK_TO
                                or PlaybackStateCompat.ACTION_PLAY_PAUSE
                                or PlaybackStateCompat.ACTION_PLAY
                                or PlaybackStateCompat.ACTION_PAUSE
                                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    )
                        .build()
                )
            }
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mediaSession!!.setPlaybackState(
                    PlaybackStateCompat.Builder().setState(
                        PlaybackStateCompat.STATE_PAUSED,
                        currentMediaPosition.toLong(),
                        playbackSpeed
                    ).setActions(
                        PlaybackStateCompat.ACTION_SEEK_TO
                                or PlaybackStateCompat.ACTION_PLAY_PAUSE
                                or PlaybackStateCompat.ACTION_PLAY
                                or PlaybackStateCompat.ACTION_PAUSE
                                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    ).build()
                )
            }
        }
        notificationManager.notify(NOTIFICATION_ID, musicNotification)
    }

    private fun buildInitialNotification() {
        initialNotification = NotificationCompat.Builder(this, ApplicationClass.CHANNEL_ID)
            .setColor(resources.getColor(R.color.player_bg, theme)).setColorized(true)
            .setSmallIcon(R.drawable.ic_headset) //set content
            .setContentText(getString(R.string.app_name) + " is running in background.")
            .setContentInfo("Running").setChannelId(ApplicationClass.CHANNEL_ID).setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(applicationContext, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            ).setSilent(true).setPriority(NotificationCompat.PRIORITY_LOW)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_DEFAULT).build()
    }

    private fun playbackAction(actionNumber: Int): PendingIntent? {
        val playbackIntent = Intent(applicationContext, MediaPlayerService::class.java)
        when (actionNumber) {
            0 -> {
                //play
                playbackIntent.action = ACTION_PLAY
                return PendingIntent.getService(
                    this, actionNumber, playbackIntent, PendingIntent.FLAG_IMMUTABLE
                )
            }

            1 -> {
                //pause
                playbackIntent.action = ACTION_PAUSE
                return PendingIntent.getService(
                    this, actionNumber, playbackIntent, PendingIntent.FLAG_IMMUTABLE
                )
            }

            2 -> {
                //next
                playbackIntent.action = ACTION_NEXT
                return PendingIntent.getService(
                    this, actionNumber, playbackIntent, PendingIntent.FLAG_IMMUTABLE
                )
            }

            3 -> {
                //previous
                playbackIntent.action = ACTION_PREVIOUS
                return PendingIntent.getService(
                    this, actionNumber, playbackIntent, PendingIntent.FLAG_IMMUTABLE
                )
            }

            4 -> {
                //stop
                playbackIntent.action = ACTION_STOP
                return PendingIntent.getService(
                    this, actionNumber, playbackIntent, PendingIntent.FLAG_IMMUTABLE
                )
            }

            else -> {}
        }
        return null
    }

    /**
     * This function sets up notification actions
     */
    private fun handleNotificationActions(playbackAction: Intent?) {
        if (playbackAction == null || playbackAction.action == null) return
        when (playbackAction.action) {
            ACTION_PLAY -> if (!isMediaPlayerNotNull) onPlayPauseReceived() else transportControls!!.play()
            ACTION_PAUSE -> if (!isMediaPlayerNotNull) onPlayPauseReceived() else transportControls!!.pause()
            ACTION_NEXT -> if (!isMediaPlayerNotNull) onNextReceived() else transportControls!!.skipToNext()
            ACTION_PREVIOUS -> if (!isMediaPlayerNotNull) onPreviousReceived() else transportControls!!.skipToPrevious()
            ACTION_STOP -> if (!isMediaPlayerNotNull) stoppedByNotification() else transportControls!!.stop()
        }
    }

    /**
     * this function checks if any phone calls are on going
     */
    private fun callStateListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            phoneStateCallback = CustomCallStateListener()
            telephonyManager.registerTelephonyCallback(
                ContextCompat.getMainExecutor(this), phoneStateCallback!!
            )
        } else {
            phoneStateListener = object : PhoneStateListener() {
                @Deprecated("Deprecated in Java")
                override fun onCallStateChanged(state: Int, phoneNumber: String) {
                    super.onCallStateChanged(state, phoneNumber)
                    takeActionOnCall(state)
                }
            }
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    private fun takeActionOnCall(state: Int) {
        when (state) {
            TelephonyManager.CALL_STATE_OFFHOOK, TelephonyManager.CALL_STATE_RINGING -> {
                if (isMediaPlaying) {
                    wasPlaying = true
                }
                pauseMedia()
                MainActivity.phone_ringing = true
            }

            TelephonyManager.CALL_STATE_IDLE -> {
                MainActivity.phone_ringing = false
                if (wasPlaying) {
                    resumeMedia(true)
                    wasPlaying = false
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        //manage incoming calls during playback
        callStateListener()

        //manage audio while output changes
        registerBecomingNoisyReceiver()
        registerPluggedDeviceReceiver()
        registerPlayNewMusic()
        registerPausePlayMusic()
        registerPlayNextMusic()
        registerPlayPreviousMusic()
        registerStopMusic()
        registerBluetoothReceiver()
        mPackageValidator =
            PackageValidator(applicationContext, R.xml.allowed_media_browser_callers)

        loadList()

        if (musicIndex != -1 && musicIndex < musicList!!.size) {
            activeMusic = musicList!![musicIndex]
        }

        val defaultArtwork = BitmapFactory.decodeResource(
            this@MediaPlayerService.applicationContext.resources, R.drawable.ic_music_notification
        )

        artworkDimension = defaultArtwork.width.coerceAtMost(defaultArtwork.height)
        defaultThumbnail = ThumbnailUtils.extractThumbnail(
            defaultArtwork,
            artworkDimension - artworkDimension / 5,
            artworkDimension - artworkDimension / 5,
            ThumbnailUtils.OPTIONS_RECYCLE_INPUT
        )

        if (defaultMetadata == null) {
            defaultMetadata = MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, defaultThumbnail)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "")
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0).build()
        }
        if (musicNotification != null) {
            try {
                startForeground(NOTIFICATION_ID, musicNotification)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            buildInitialNotification()
            try {
                startForeground(NOTIFICATION_ID, initialNotification)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (settingsStorage.loadAutoPlayBt()){
            if (isBluetoothHeadsetConnected){
                try {
                    initiateMediaSession()
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
                initiateMediaPlayer()
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return iBinder
    }

    val audioSessionId: Int?
        get() = mediaPlayer?.audioSessionId

    override fun onPrepared(mp: MediaPlayer) {
        EventBus.getDefault().post(SetMainLayoutEvent(activeMusic))
        resumeMedia(false)
        var finalImage: Bitmap?
        coroutineScope.launch {
            GlideApp.with(applicationContext).load(activeMusic?.path?.let { AudioFileCover(it) })
                .override(512).into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        finalImage = resource.toUnscaledBitmap()
                        if (MainActivity.service_bound) {
                            EventBus.getDefault()
                                .post(SetImageInMainPlayer(finalImage, activeMusic))
                        }
                        updateMetaData(activeMusic, finalImage)
                        finalImage = null
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        if (MainActivity.service_bound) {
                            EventBus.getDefault().post(SetImageInMainPlayer(null, activeMusic))
                        }
                        updateMetaData(activeMusic, null)
                        finalImage = null
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        if (MainActivity.service_bound) {
                            EventBus.getDefault().post(SetImageInMainPlayer(null, activeMusic))
                        }
                        updateMetaData(activeMusic, null)
                        finalImage = null
                    }
                })
        }

    }


    override fun onCompletion(mp: MediaPlayer) {
        storage.clearMusicLastPos()
        if (musicIndex == -1 || musicList == null) {
            loadList()
        }
        when (storage.loadRepeatStatus()) {
            RepeatModes.REPEAT_MODE_NONE -> if (musicIndex == musicList!!.size - 1) pauseMedia() else skipToNext()
            RepeatModes.REPEAT_MODE_ALL -> skipToNext()
            RepeatModes.REPEAT_MODE_ONE -> {
                playMedia()
                EventBus.getDefault().post(PrepareRunnableEvent())
            }
        }
        if (isMediaPlaying) {
            setIcon(PlaybackStatus.PLAYING)
            buildNotification(PlaybackStatus.PLAYING, 1f)
        } else {
            setIcon(PlaybackStatus.PAUSED)
            buildNotification(PlaybackStatus.PAUSED, 0f)
        }
    }

    private fun loadList() {
        musicList = storage.loadQueueList()
        musicIndex = storage.loadMusicIndex()
    }

    override fun onBufferingUpdate(mp: MediaPlayer, percent: Int) {}
    private fun showToast(s: String) {
        ApplicationClass.instance.showToast(s)
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        when (what) {
            MEDIA_ERROR_UNKNOWN -> {
                try {
                    stoppedByNotification()
                    setIcon(PlaybackStatus.PAUSED)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                showToast("MEDIA_ERROR_UNKNOWN$extra")
                return true
            }

            MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> {
                try {
                    stoppedByNotification()
                    setIcon(PlaybackStatus.PAUSED)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                showToast("MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK$extra")
                return true
            }

            MEDIA_ERROR_SERVER_DIED -> {
                try {
                    //service is active send media with broadcast receiver
                    val encodedMessage = MusicHelper.encode(activeMusic)
                    val broadcastIntent = Intent(BROADCAST_PLAY_NEW_MUSIC)
                    broadcastIntent.putExtra("music", encodedMessage)
                    sendBroadcast(broadcastIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                showToast("MEDIA_ERROR_SERVER_DIED$extra")
                return true
            }

            else -> {}
        }
        return false
    }

    override fun onInfo(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        return false
    }


    override fun onSeekComplete(mp: MediaPlayer) {}
    private fun initiateMediaPlayer() {
        if (!isMediaPlayerNotNull) {
            mediaPlayer = MediaPlayer()
            //setup MediaPlayer event listeners
            mediaPlayer!!.setOnPreparedListener(this)
            mediaPlayer!!.setOnErrorListener(this)
            mediaPlayer!!.setOnBufferingUpdateListener(this)
            mediaPlayer!!.setOnSeekCompleteListener(this)
            mediaPlayer!!.setOnInfoListener(this)
            mediaPlayer!!.setOnCompletionListener(this)

            val audioAttributes = AudioAttributes.Builder()
            audioAttributes.setLegacyStreamType(AudioManager.STREAM_MUSIC)
            audioAttributes.setUsage(AudioAttributes.USAGE_MEDIA)
            audioAttributes.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            mediaPlayer!!.setAudioAttributes(audioAttributes.build())
            openEqualizer(settingsStorage.loadEnhanceAudio())
        }

        //reset so that the media player is not pointing to another data source
        mediaPlayer?.reset()
        try {
            activeMusic?.let {
                musicList ?: loadList()
                mediaPlayer?.setDataSource(it.path)
                mediaPlayer?.prepare()
            }
        } catch (ignored: IOException) {
        }
    }

    fun openEqualizer(shouldEnhance: Boolean) {
        if (shouldEnhance) {
            initiateEqualizerUtil().run {
                setBassLevel(settingsStorage.loadBassLevel())
                setVirtualizerStrength(settingsStorage.loadVirLevel())
            }
        }
    }

    private fun initiateEqualizerUtil() {
        val id = audioSessionId ?: return
        musicEnhancerUtil = MusicEnhancerUtil(id)

        musicEnhancerUtil?.enableEffects(true)
    }

    fun disableEqualizerUtil() {
        musicEnhancerUtil?.enableEffects(false)
        musicEnhancerUtil?.releaseEqualizer()
        musicEnhancerUtil = null
    }

    fun setBassLevel(level: Int) {
        musicEnhancerUtil?.setBassBandLevel(level)
    }

    fun setVirtualizerStrength(strength: Int) {
        musicEnhancerUtil?.setVirtualizerStrength(strength)
    }

    /**
     * This function plays music from start
     */
    private fun playMedia() {
        if (!requestAudioFocus()) return
        if (MainActivity.phone_ringing) {
            showToast("Can't play while on call")
            return
        }
        if (!isMediaPlayerNotNull) return
        if (!isMediaPlaying) {
            mediaPlayer!!.start()
            is_playing = true
            setIcon(PlaybackStatus.PLAYING)
            buildNotification(PlaybackStatus.PLAYING, 1f)
        }
    }

    /**
     * This function stops music to play something else
     */
    fun stopMedia() {
        if (!isMediaPlayerNotNull) {
            return
        }
        if (isMediaPlaying) {
            mediaPlayer!!.stop()
            is_playing = false
        }
    }

    /**
     * This function stops music and removes notification
     */
    fun stoppedByNotification() {
        if (isMediaPlayerNotNull && isMediaPlaying) {
            storage.saveMusicLastPos(currentMediaPosition)
            mediaPlayer!!.pause()
            is_playing = false
            setIcon(PlaybackStatus.PAUSED)
            buildNotification(PlaybackStatus.PAUSED, 0f)
        }
        if (initialNotification == null) {
            buildInitialNotification()
        }
        notificationManager.notify(NOTIFICATION_ID, initialNotification)
        stopSelf()
    }

    /**
     * This function resumes music
     */
    fun resumeMedia(showNotification: Boolean) {
        // request audio focus if false
        if (!requestAudioFocus()) return

        //make toast if volume is off
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() == 0f) {
            showToast("Please turn the volume UP")
        }

        // make toast and do early return if phone ringing
        if (MainActivity.phone_ringing) {
            showToast("Can't play while on call")
            return
        }

        // initiate player and return early if media_player is null
        if (!isMediaPlayerNotNull) {
            initiateMediaPlayer()
            return
        }
        var file: File? = null
        if (activeMusic != null) {
            file = File(activeMusic!!.path)
        }
        if (file != null) {
            if (!file.exists()) {
                skipToNext()
                return
            }
        } else {
            skipToNext()
            return
        }
        //if service is bound then set seekbar and load lyrics
        if (MainActivity.service_bound) {
            setSeekBar()
            var lrcMap: LRCMap? = null
            if (activeMusic != null) {
                lrcMap = storage.loadLyrics(activeMusic!!.id)
            }
            if (lrcMap != null) EventBus.getDefault().post(PrepareRunnableEvent())
        }

        // set is_playing to true
        is_playing = true

        // set media_player seekbar to position and start media_player
        val position = storage.loadMusicLastPos()
        seekMediaTo(position)
        mediaPlayer!!.start()

        // set icon to playing and build notification
        setIcon(PlaybackStatus.PLAYING)
        if (showNotification) {
            buildNotification(PlaybackStatus.PLAYING, 1f)
        }
    }

    /**
     * This function pauses music
     */
    fun pauseMedia() {
        if (isMediaPlayerNotNull && isMediaPlaying) {
            if (MainActivity.service_bound) {
                if (seekBarHandler != null) seekBarHandler!!.removeCallbacks(seekBarRunnable!!)
                EventBus.getDefault().post(RemoveLyricsHandlerEvent())
            }
            storage.saveMusicLastPos(currentMediaPosition)
            mediaPlayer!!.pause()
            is_playing = false
            setIcon(PlaybackStatus.PAUSED)
            buildNotification(PlaybackStatus.PAUSED, 0f)
        }
    }

    /**
     * This function sends music position to main ui
     */
    fun setSeekBar() {
        seekBarHandler = Handler(Looper.getMainLooper())
        if (isMediaPlayerNotNull) {
            // if media_player hasn't finished the song yet then update progress
            if (currentMediaPosition <= mediaPlayer!!.duration) {
                seekBarRunnable = Runnable {
                    var position = 0
                    try {
                        position = currentMediaPosition
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()
                    }
                    EventBus.getDefault().post(UpdateMusicProgressEvent(position))
                    seekBarHandler!!.postDelayed(seekBarRunnable!!, 300)
                }
                seekBarHandler!!.postDelayed(seekBarRunnable!!, 0)
            }
        }
    }

    val currentMediaPosition: Int
        get() = if (isMediaPlayerNotNull) {
            mediaPlayer!!.currentPosition
        } else 0

    val isMediaPlaying: Boolean
        get() = if (isMediaPlayerNotNull) {
            mediaPlayer!!.isPlaying
        } else false


    /**
     * This function skips music to previous index
     */
    fun seekMediaTo(sec: Int) {
        if (isMediaPlayerNotNull) {
            mediaPlayer!!.seekTo(sec)
        }
    }

    val isMediaPlayerNotNull: Boolean
        get() = mediaPlayer != null

    fun skipToPrevious() {
        loadList()
        val lastPos = storage.loadMusicLastPos()
        storage.clearMusicLastPos()
        try {
            if (musicIndex == -1 || musicList == null) {
                return
            }
            if (settingsStorage.loadOneClickSkip()) {
                setActiveMusicForPrevious()
            } else {
                if (isMediaPlayerNotNull && currentMediaPosition >= 3000 || lastPos >= 3000) {
                    activeMusic = musicList!![musicIndex]
                    BottomSheetPlayerFragment.playing_same_song = true
                } else {
                    setActiveMusicForPrevious()
                }
            }
        } catch (e: IndexOutOfBoundsException) {
            showToast("Please try to play another song")
        }
        var file: File? = null
        if (activeMusic != null) {
            file = File(activeMusic!!.path)
        }
        if (file != null) {
            if (!file.exists()) {
                storage.saveMusicIndex(musicIndex)
                skipToPrevious()
                return
            }
        } else {
            storage.saveMusicIndex(musicIndex)
            skipToPrevious()
            return
        }
        if (activeMusic != null) {
            storage.saveMusicIndex(musicIndex)
            stopMedia()
            initiateMediaPlayer()
        }
    }

    private fun setActiveMusicForPrevious() {
        if (musicIndex == 0) {
            musicIndex = musicList!!.size - 1
            activeMusic = musicList!![musicIndex]
        } else {
            activeMusic = musicList!![--musicIndex]
        }
        BottomSheetPlayerFragment.playing_same_song = false
    }

    /**
     * This function skips music to next index
     */
    fun skipToNext() {
        loadList()
        storage.clearMusicLastPos()
        try {
            if (musicIndex != -1 && musicList != null) if (musicIndex == musicList!!.size - 1) {
                //if last in list
                musicIndex = 0
                activeMusic = musicList!![musicIndex]
            } else {
                activeMusic = musicList!![++musicIndex]
            }
            BottomSheetPlayerFragment.playing_same_song = false
        } catch (e: IndexOutOfBoundsException) {
            showToast("Please try to play another song")
        }
        var file: File? = null
        if (activeMusic != null) {
            file = File(activeMusic!!.path)
        }
        if (file != null) {
            if (!file.exists()) {
                storage.saveMusicIndex(musicIndex)
                skipToNext()
                return
            }
        } else {
            storage.saveMusicIndex(musicIndex)
            skipToNext()
            return
        }
        if (activeMusic != null) {
            storage.saveMusicIndex(musicIndex)
            stopMedia()
            initiateMediaPlayer()
        }
    }

    fun setTimer(progress: Int) {

        //Sets The already Initialized countDownTimer to a new countDownTimer with given parameters
        countDownTimer[0] = object : CountDownTimer((progress * 5L + 5) * 1000L * 60, 1000) {
            //Variables For storing seconds and minutes
            var seconds = 0
            var minutes = 0

            //Every Second Do Something
            //Update TextView Code Goes Here
            override fun onTick(l: Long) {

                //Storing Seconds and Minutes on Every Tick
                seconds = (l / 1000).toInt() % 60
                minutes = (l / (1000 * 60) % 60).toInt()

                // Replace This with TextView.setText(View);
                val finalCDTimer = "$minutes:$seconds"
                EventBus.getDefault().post(SetTimerText(finalCDTimer))
            }

            //Code After timer is Finished Goes Here
            override fun onFinish() {
                //Replace This pausePlayAudio() with just a pause Method.
                //Replaced it
                pauseMedia()
                EventBus.getDefault().post(TimerFinished())
                countDownTimer[0] = null
            }
        }
        // Start timer
        countDownTimer[0]?.start()
    }

    fun cancelTimer() {
        countDownTimer[0]?.apply {
            cancel()
            EventBus.getDefault().post(TimerFinished())
        }
    }

    /**
     * this function sets up a media session
     */
    @Throws(RemoteException::class)
    fun initiateMediaSession() {
        if (mediaSession != null) return
        handler.post {
            mediaSession = MediaSessionCompat(this, BuildConfig.APPLICATION_ID)
            transportControls = mediaSession!!.controller.transportControls
            mediaSession!!.isActive = true
            mediaSession!!.setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS)
            mediaSession!!.setCallback(object : MediaSessionCompat.Callback() {
                override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
                    val intentAction = mediaButtonEvent.action
                    if (Intent.ACTION_MEDIA_BUTTON != intentAction) {
                        return super.onMediaButtonEvent(mediaButtonEvent)
                    }
                    val event = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        mediaButtonEvent.getParcelableExtra(
                            Intent.EXTRA_KEY_EVENT, KeyEvent::class.java
                        ) ?: return super.onMediaButtonEvent(mediaButtonEvent)
                    } else {
                        mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                            ?: return super.onMediaButtonEvent(mediaButtonEvent)
                    }
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        when (event.keyCode) {
                            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                                Logger.normalLog("PLAY_PAUSE")
                                if (isMediaPlaying) pauseMedia() else resumeMedia(true)
                            }

                            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                                Logger.normalLog("PLAY")
                                if (!isMediaPlaying) resumeMedia(true)
                            }

                            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                                Logger.normalLog("PAUSE")
                                if (isMediaPlaying) pauseMedia()
                            }

                            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                                Logger.normalLog("NEXT")
                                onSkipToNext()
                            }

                            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                                Logger.normalLog("PREVIOUS")
                                onSkipToPrevious()
                            }
                        }
                        return true
                    }
                    return super.onMediaButtonEvent(mediaButtonEvent)
                }

                override fun onPlay() {
                    super.onPlay()
                    resumeMedia(true)
                }

                override fun onPause() {
                    super.onPause()
                    pauseMedia()
                }

                override fun onSkipToNext() {
                    super.onSkipToNext()
                    skipToNext()
                }

                override fun onSkipToPrevious() {
                    super.onSkipToPrevious()
                    skipToPrevious()
                }

                override fun onStop() {
                    super.onStop()
                    stoppedByNotification()
                }

                override fun onSeekTo(pos: Long) {
                    super.onSeekTo(pos)
                    try {//clearing the storage before putting new value
                        storage.clearMusicLastPos()
                        //storing the current position of seekbar in storage so we can access it from services
                        storage.saveMusicLastPos(Math.toIntExact(pos))

                        //first checking setting the media seek to current position of seek bar and then setting all data in UI•
                        if (MainActivity.service_bound) {
                            //removing handler so that we can seek without glitches handler will restart in setSeekBar() method☻
                            seekBarRunnable?.let {
                                seekBarHandler?.removeCallbacks(it)
                            }
                            if (isMediaPlayerNotNull) {
                                seekMediaTo(Math.toIntExact(pos))
                                if (isMediaPlaying) {
                                    buildNotification(PlaybackStatus.PLAYING, 1f)
                                } else {
                                    buildNotification(PlaybackStatus.PAUSED, 0f)
                                }
                                setSeekBar()
                            }
                        }
                    } catch (_: Exception) {
                    }
                }
            })
        }
    }

    override fun onAudioFocusChange(focusState: Int) {
        when (focusState) {
            AudioManager.AUDIOFOCUS_GAIN -> if (wasPlaying) {
                if (isMediaPlayerNotNull) {
                    resumeMedia(true)
                }
                wasPlaying = false
            }

            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> if (isMediaPlaying) {
                pauseMedia()
                wasPlaying = true
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> if (settingsStorage.loadLowerVol()) {
                if (isMediaPlaying) {
                    mediaPlayer!!.setVolume(0.3f, 0.3f)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //Handle Intent action from MediaSession.TransportControls
        if (intent != null) {
            handleNotificationActions(intent)
            return START_STICKY
        }
        return super.onStartCommand(null, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        //here we are stopping service after 5 minutes if app
        // is not running in background and player is not playing
        //this is a optional feature for user
        if (settingsStorage.loadSelfStop()) {
            if (selfStopRunnable != null) {
                selfStopRunnable = Runnable {
                    if (!is_playing && !ui_visible) {
                        stopSelf()
                    }
                    selfStopHandler.postDelayed(selfStopRunnable!!, (5 * 60 * 1000).toLong())
                }
                selfStopHandler.postDelayed(selfStopRunnable!!, 0)
            }
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?,
    ): BrowserRoot {
        // Check origin to ensure we're not allowing any arbitrary app to browse app contents
        return if (!mPackageValidator!!.isKnownCaller(clientPackageName, clientUid)) {
            // Request from an untrusted package: return an empty browser root
            BrowserRoot("__EMPTY_ROOT__", null)
        } else {
            /**
             * By default return the browsable root. Treat the EXTRA_RECENT flag as a special case
             * and return the recent root instead.
             */
            val isRecentRequest = rootHints?.getBoolean(BrowserRoot.EXTRA_RECENT) ?: false
            val browserRootPath = if (isRecentRequest) "__RECENT__" else "__ROOT__"
            BrowserRoot(browserRootPath, null)
        }
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>,
    ) {
    }

    override fun onUnbind(intent: Intent): Boolean {
        return isMediaPlaying
    }

    override fun onRebind(intent: Intent) {
        super.onRebind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaService()
    }

    private fun releaseMediaService() {
        removeAudioFocus()
        finalizeThings()
        nullifyResources()
        unregisterReceivers()
    }

    private fun finalizeThings() {
        MainActivity.service_stopped = true

        cancelTimer()
        if (isMediaPlayerNotNull) {
            storage.saveMusicLastPos(currentMediaPosition)
        }
        //disable phone state listener ♣
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (phoneStateCallback != null) {
                telephonyManager.unregisterTelephonyCallback(phoneStateCallback!!)
            }
        } else {
            if (phoneStateListener != null) {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
            }
        }
    }

    private fun unregisterReceivers() {
        unregisterReceiver(becomingNoisyReceiver)
        unregisterReceiver(pluggedInDevice)
        unregisterReceiver(playNewMusicReceiver)
        unregisterReceiver(pausePlayMusicReceiver)
        unregisterReceiver(nextMusicReceiver)
        unregisterReceiver(prevMusicReceiver)
        unregisterReceiver(stopMusicReceiver)
        unregisterReceiver(bluetoothReceiver)
    }

    private fun nullifyResources() {
        selfStopHandler.removeCallbacksAndMessages(null)
        handler.removeCallbacksAndMessages(null)
        defaultMetadata = null
        defaultThumbnail = null
        mPackageValidator = null
        seekBarRunnable = null
        seekBarHandler = null
        selfStopRunnable = null
        transportControls = null
        musicList?.clear()
        musicList = null
        phoneStateListener = null
        phoneStateCallback = null
        audioFocusRequest = null
        disableEqualizerUtil()
        nullifyPlayer()
    }

    private fun nullifyPlayer() {
        mediaPlayer?.release()
        mediaSession?.release()

        mediaPlayer = null
        mediaSession = null
        notificationManager.cancel(NOTIFICATION_ID)

        stopForeground(STOP_FOREGROUND_REMOVE)
    }


    /**
     * this function requests focus to play the music
     *
     * @return result of request, true if granted
     */
    private fun requestAudioFocus(): Boolean {
        val result: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val builder = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            builder.setOnAudioFocusChangeListener(this)
            audioFocusRequest = builder.build()
            result = audioManager.requestAudioFocus(audioFocusRequest!!)
        } else {
            result = audioManager.requestAudioFocus(
                this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN
            )
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun removeAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            audioManager.abandonAudioFocus(this)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private inner class CustomCallStateListener : PhoneStateCallback() {
        override fun onCallStateChanged(state: Int) {
            takeActionOnCall(state)
        }
    }

    inner class LocalBinder : Binder() {
        val service: MediaPlayerService
            get() = this@MediaPlayerService
    }

    companion object {
        //♥♥☻☻
        //all public variables are in this format "public_variable"
        //all public and private static final string variables are in this format "PUBLIC_PRIVATE_STATIC_FINAL_STRING"
        //all private variables are in this format "privateVariable"
        //♥♥☻☻
        const val ACTION_PLAY = "com.atomykcoder.atomykplay.ACTION_PLAY"
        const val ACTION_PAUSE = "com.atomykcoder.atomykplay.ACTION_PAUSE"
        const val ACTION_PREVIOUS = "com.atomykcoder.atomykplay.ACTION_PREVIOUS"
        const val ACTION_NEXT = "com.atomykcoder.atomykplay.ACTION_NEXT"
        const val ACTION_STOP = "com.atomykcoder.atomykplay.ACTION_STOP"

        //audio player notification ID
        const val NOTIFICATION_ID = 874159

        @JvmField
        var is_playing = false

        @JvmField
        var ui_visible = false
    }

    fun bluetoothConnected() {
        if (settingsStorage.loadAutoPlayBt()) {
            if (!is_playing) {
                if (isMediaPlayerNotNull) {
                    resumeMedia(true)
                } else {
                    try {
                        initiateMediaSession()
                    } catch (e: RemoteException) {
                        e.printStackTrace()
                    }
                    initiateMediaPlayer()
                }
            }
        }
    }

    fun bluetoothDisconnected() {
        pauseMedia()
    }
}
