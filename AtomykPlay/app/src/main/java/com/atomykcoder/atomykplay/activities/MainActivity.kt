package com.atomykcoder.atomykplay.activities

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.RecoverableSecurityException
import android.content.*
import android.content.pm.PackageManager
import android.graphics.*
import android.media.MediaMetadataRetriever
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Size
import android.view.*
import android.widget.*
import androidx.activity.result.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.adapters.FoundLyricsAdapter
import com.atomykcoder.atomykplay.adapters.MusicMainAdapter
import com.atomykcoder.atomykplay.adapters.PlaylistDialogAdapter
import com.atomykcoder.atomykplay.classes.GlideBuilt
import com.atomykcoder.atomykplay.classes.PhoneStateCallback
import com.atomykcoder.atomykplay.customScripts.CustomBottomSheet
import com.atomykcoder.atomykplay.customScripts.LinearLayoutManagerWrapper
import com.atomykcoder.atomykplay.data.Music
import com.atomykcoder.atomykplay.dataModels.Playlist
import com.atomykcoder.atomykplay.enums.OptionSheetEnum
import com.atomykcoder.atomykplay.events.PrepareRunnableEvent
import com.atomykcoder.atomykplay.events.RemoveFromFavoriteEvent
import com.atomykcoder.atomykplay.events.RemoveFromPlaylistEvent
import com.atomykcoder.atomykplay.events.RemoveLyricsHandlerEvent
import com.atomykcoder.atomykplay.fragments.*
import com.atomykcoder.atomykplay.helperFunctions.CustomMethods.pickImage
import com.atomykcoder.atomykplay.helperFunctions.MusicHelper
import com.atomykcoder.atomykplay.repository.LoadingStatus
import com.atomykcoder.atomykplay.repository.MusicRepo
import com.atomykcoder.atomykplay.repository.MusicRepo.Companion.instance
import com.atomykcoder.atomykplay.services.MediaPlayerService
import com.atomykcoder.atomykplay.services.MediaPlayerService.LocalBinder
import com.atomykcoder.atomykplay.utils.MusicUtil
import com.atomykcoder.atomykplay.utils.StorageUtil
import com.atomykcoder.atomykplay.utils.StorageUtil.SettingsStorage
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity(), View.OnClickListener,
    NavigationView.OnNavigationItemSelectedListener {
    var service_connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocalBinder
            media_player_service = binder.service
            service_bound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            service_bound = false
            media_player_service = null
        }
    }
    var mainPlayerSheetBehavior: CustomBottomSheet<View?>? = null

    @JvmField
    var bottomSheetPlayerFragment: BottomSheetPlayerFragment? = null
    private var lastAddedFragment: LastAddedFragment? = null
    private var lyricsListBehavior: BottomSheetBehavior<View?>? = null
    private var optionSheetBehavior: BottomSheetBehavior<View?>? = null
    private var donationSheetBehavior: BottomSheetBehavior<View?>? = null
    private var detailsSheetBehavior: BottomSheetBehavior<View?>? = null
    private var plSheetBehavior: BottomSheetBehavior<View?>? = null

    @JvmField
    var addToPlDialog: AlertDialog? = null
    var plSheet: View? = null
    var player_bottom_sheet: View? = null
    var plItemSelected: Playlist? = null
    var selectedItem: Music? = null
    var isChecking = false
    private var glideBuilt: GlideBuilt? = null
    private var shadowLyrFound: View? = null
    private var shadowOuterSheet: View? = null
    private var shadowOuterSheet2: View? = null
    private var shadowMain: View? = null
    private var anchoredShadow: View? = null
    private var playlist_image_View: ImageView? = null
    private var navCover: ImageView? = null
    private var playListImageUri: Uri? = null

    // Registers a photo picker activity launcher in single-select mode.
    private val mediaPickerForPLCover =
        registerForActivityResult<PickVisualMediaRequest?, Uri>(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                playListImageUri = uri
                playlist_image_View!!.setImageURI(playListImageUri)
            }
        }
    private val pickIntentForPLCover =
        registerForActivityResult<Intent?, ActivityResult>(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                if (result.data != null) {
                    playListImageUri = result.data!!.data
                    playlist_image_View!!.setImageURI(playListImageUri)
                }
            }
        }
    private var musicMainAdapter: MusicMainAdapter? = null
    private var linearLayout: LinearLayout? = null
    private var musicRecyclerView: RecyclerView? = null
    private var telephonyManager: TelephonyManager? = null
    private var phoneStateListener: PhoneStateListener? = null
    private var phoneStateCallback: PhoneStateCallback? = null
    private var progressBar: ProgressBar? = null
    private var storageUtil: StorageUtil? = null
    private var drawer: DrawerLayout? = null
    private val detailsSheetCallback: BottomSheetCallback = object : BottomSheetCallback() {
        @SuppressLint("SwitchIntDef")
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    drawer!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    shadowOuterSheet2!!.isClickable = true
                    shadowOuterSheet2!!.isFocusable = true
                    shadowOuterSheet2!!.alpha = 0.45f
                    detailsSheet!!.elevation = 20f
                }
                BottomSheetBehavior.STATE_EXPANDED -> {
                    shadowOuterSheet2!!.isClickable = true
                    shadowOuterSheet2!!.isFocusable = true
                    drawer!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    shadowOuterSheet2!!.alpha = 1f
                    detailsSheet!!.elevation = 20f
                }
                BottomSheetBehavior.STATE_HIDDEN -> {
                    drawer!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    shadowOuterSheet2!!.isClickable = false
                    shadowOuterSheet2!!.isFocusable = false
                    detailsSheet!!.elevation = 0f
                }
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            shadowOuterSheet2!!.alpha = 0.45f + slideOffset
        }
    }
    private val donationCallback: BottomSheetCallback = object : BottomSheetCallback() {
        @SuppressLint("SwitchIntDef")
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    drawer!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    shadowOuterSheet!!.isClickable = true
                    shadowOuterSheet!!.isFocusable = true
                    shadowOuterSheet!!.alpha = 0.6f
                    donationSheet!!.elevation = 16f
                }
                BottomSheetBehavior.STATE_EXPANDED -> {
                    shadowOuterSheet!!.isClickable = true
                    shadowOuterSheet!!.isFocusable = true
                    drawer!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    shadowOuterSheet!!.alpha = 1f
                    donationSheet!!.elevation = 16f
                }
                BottomSheetBehavior.STATE_HIDDEN -> {
                    drawer!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    shadowOuterSheet!!.isClickable = false
                    shadowOuterSheet!!.isFocusable = false
                    donationSheet!!.elevation = 0f
                }
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            shadowOuterSheet!!.alpha = 0.6f + slideOffset
        }
    }
    private val plSheetCallback: BottomSheetCallback = object : BottomSheetCallback() {
        @SuppressLint("SwitchIntDef")
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    drawer!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    shadowOuterSheet!!.isClickable = true
                    shadowOuterSheet!!.isFocusable = true
                    shadowOuterSheet!!.alpha = 0.6f
                    plSheet!!.elevation = 18f
                }
                BottomSheetBehavior.STATE_EXPANDED -> {
                    shadowOuterSheet!!.isClickable = true
                    shadowOuterSheet!!.isFocusable = true
                    drawer!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    plSheet!!.elevation = 18f
                    shadowOuterSheet!!.alpha = 1f
                }
                BottomSheetBehavior.STATE_HIDDEN -> {
                    drawer!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    shadowOuterSheet!!.isClickable = false
                    shadowOuterSheet!!.isFocusable = false
                    plSheet!!.elevation = 0f
                }
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            shadowOuterSheet!!.alpha = 0.6f + slideOffset
        }
    }
    private var navigationView: NavigationView? = null
    private var tempThemeColor = 0
    private var navSongName: TextView? = null
    private var navArtistName: TextView? = null
    var mainPlayerSheetCallback: BottomSheetCallback = object : BottomSheetCallback() {
        @SuppressLint("SwitchIntDef")
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    val miniPlayer = bottomSheetPlayerFragment?.mini_play_view
                    val mainPlayer = bottomSheetPlayerFragment?.player_layout
                    if (miniPlayer == null || mainPlayer == null) {
                        return
                    }
                    mainPlayer.visibility = View.INVISIBLE
                    miniPlayer.visibility = View.VISIBLE
                    drawer!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    miniPlayer.alpha = 1f
                    shadowMain!!.alpha = 0f
                    anchoredShadow!!.alpha = 1f
                    mainPlayer.alpha = 0f
                    anchoredShadow!!.elevation = 10f
                    player_bottom_sheet!!.elevation = 12f
                    changeNavigationColor(
                        tempThemeColor,
                        resources.getColor(R.color.player_bg, null)
                    )
                    tempThemeColor = resources.getColor(R.color.player_bg, null)
                }
                BottomSheetBehavior.STATE_EXPANDED -> {
                    val miniPlayer = bottomSheetPlayerFragment?.mini_play_view
                    val mainPlayer = bottomSheetPlayerFragment?.player_layout
                    if (miniPlayer == null || mainPlayer == null) {
                        return
                    }
                    miniPlayer.visibility = View.INVISIBLE
                    mainPlayer.visibility = View.VISIBLE
                    drawer!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    miniPlayer.alpha = 0f
                    mainPlayer.alpha = 1f
                    anchoredShadow!!.alpha = 1f
                    shadowMain!!.alpha = 1f
                    anchoredShadow!!.elevation = 10f
                    player_bottom_sheet!!.elevation = 12f
                    bottomSheetPlayerFragment?.themeColor?.observe(this@MainActivity) { it: Int ->
                        if (mainPlayerSheetBehavior!!.state == BottomSheetBehavior.STATE_EXPANDED) {
                            changeNavigationColor(tempThemeColor, it)
                            tempThemeColor = it
                        }
                    }
                }
                BottomSheetBehavior.STATE_HIDDEN -> {
                    anchoredShadow!!.alpha = 0f
                    drawer!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    player_bottom_sheet!!.elevation = 0f
                    anchoredShadow!!.elevation = 0f
                    clearStorage()
                    bottomSheetPlayerFragment?.queueAdapter?.clearList()
                    bottomSheetPlayerFragment?.resetMainPlayerLayout()
                    resetDataInNavigation()
                    stopMusic()
                }
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            val miniPlayer = bottomSheetPlayerFragment?.mini_play_view
            val mainPlayer = bottomSheetPlayerFragment?.player_layout
            if (miniPlayer == null || mainPlayer == null) {
                return
            }
            miniPlayer.visibility = View.VISIBLE
            mainPlayer.visibility = View.VISIBLE
            miniPlayer.alpha = 1 - slideOffset * 35
            mainPlayer.alpha = 0 + slideOffset
            shadowMain!!.alpha = 0 + slideOffset
        }
    }
    private var deleteBtn: View? = null
    private var optionSheet: View? = null
    private var detailsSheet: View? = null
    private var donationSheet: View? = null
    private val optionCallback: BottomSheetCallback = object : BottomSheetCallback() {
        @SuppressLint("SwitchIntDef")
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    drawer!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    shadowOuterSheet!!.isClickable = true
                    shadowOuterSheet!!.isFocusable = true
                    shadowOuterSheet!!.alpha = 0.7f
                    optionSheet!!.elevation = 18f
                }
                BottomSheetBehavior.STATE_EXPANDED -> {
                    shadowOuterSheet!!.isClickable = true
                    shadowOuterSheet!!.isFocusable = true
                    drawer!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    shadowOuterSheet!!.alpha = 1f
                    optionSheet!!.elevation = 18f
                }
                BottomSheetBehavior.STATE_HIDDEN -> {
                    drawer!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    shadowOuterSheet!!.isClickable = false
                    shadowOuterSheet!!.isFocusable = false
                    optionSheet!!.elevation = 0f
                }
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            shadowOuterSheet!!.alpha = 0.7f + slideOffset
        }
    }
    private var removeFromList: View? = null
    private var optionCover: ImageView? = null
    private var plOptionCover: ImageView? = null
    private var addToFav: ImageView? = null
    private var optionName: TextView? = null
    private var optionArtist: TextView? = null
    private var plOptionName: TextView? = null
    private var optionPlCount: TextView? = null
    private var playlistDialogAdapter: PlaylistDialogAdapter? = null
    private var playlistArrayList: ArrayList<Playlist>? = null
    private var plDialogRecyclerView: RecyclerView? = null
    private var playlistFragment: PlaylistsFragment? = null
    private var searchFragment: SearchFragment? = null
    private var ringtoneDialog: AlertDialog? = null
    private var pl_name: String? = null

    // Registers a photo picker activity launcher in single-select mode.
    private val mediaPickerForPLCoverChange =
        registerForActivityResult<PickVisualMediaRequest?, Uri>(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                val coverImageUri = uri.toString()
                storageUtil?.replacePlaylist(
                    storageUtil!!.loadPlaylist(pl_name),
                    pl_name,
                    coverImageUri
                )
                playlistFragment?.playlistAdapter?.updateView(storageUtil!!.allPlaylist)
            }
        }
    private val pickIntentForPLCoverChange =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                if (result.data != null) {
                    val coverImageUri = result.data!!.data.toString()
                    storageUtil?.replacePlaylist(
                        storageUtil!!.loadPlaylist(pl_name),
                        pl_name,
                        coverImageUri
                    )
                    playlistFragment?.playlistAdapter?.updateView(storageUtil!!.allPlaylist)
                }
            }
        }
    private var optionTag: OptionSheetEnum? = null
    private var songPathTv: TextView? = null
    private var songNameTv: TextView? = null
    private var songArtistTv: TextView? = null
    private var songSizeTv: TextView? = null
    private var songGenreTv: TextView? = null
    private var songBitrateTv: TextView? = null
    private var songAlbumTv: TextView? = null
    private var handler: Handler? = null

    // Create a new ActivityResultLauncher to handle the delete request result
    private val deleteMusicRequestLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                // Delete request was successful
                updateAdaptersForRemovedItem()
            } else {
                // Delete request failed
                showToast("Failed to delete this song")
            }
        }
    private var musicRepo: MusicRepo? = null
    private var lyricsSheet: View? = null
    private val lrcFoundCallback: BottomSheetCallback = object : BottomSheetCallback() {
        @SuppressLint("SwitchIntDef")
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    shadowLyrFound!!.alpha = 0.2f
                    lyricsSheet!!.elevation = 4f
                }
                BottomSheetBehavior.STATE_EXPANDED -> {
                    shadowLyrFound!!.alpha = 1f
                    lyricsSheet!!.elevation = 4f
                }
                BottomSheetBehavior.STATE_HIDDEN -> lyricsSheet!!.elevation = 0f
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            shadowLyrFound!!.alpha = 0.2f + slideOffset
        }
    }
    private var lastClickTime: Long = 0
    private var executorService: ExecutorService? = null
    private var noPl_tv: TextView? = null
    fun clearStorage() {
        storageUtil!!.clearMusicLastPos()
        storageUtil!!.clearMusicIndex()
        storageUtil!!.clearQueueList()
        storageUtil!!.clearTempMusicList()
    }

    private fun checkForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                is_granted = false
                showRequestDialog()
            } else {
                is_granted = true
                setUpServiceAndScanner()
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                is_granted = false
                showRequestDialog()
            } else {
                is_granted = true
                setUpServiceAndScanner()
            }
        }
    }

    private fun showRequestDialog() {
        val builder = MaterialAlertDialogBuilder(this)
        val customLayout = layoutInflater.inflate(R.layout.permission_request_dialog_layout, null)
        builder.setView(customLayout)
        builder.setCancelable(true)
        builder.setPositiveButton("Allow") { _: DialogInterface?, _: Int ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestReadAudioAndPhonePermissionAbove12()
            } else {
                requestReadStorageAndPhonePermissionBelow12()
            }
        }
        builder.setOnCancelListener { dialog: DialogInterface? -> finish() }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun setDetailsMenuButtons() {
        songPathTv = findViewById(R.id.file_path_detail)
        songNameTv = findViewById(R.id.file_name_detail)
        songArtistTv = findViewById(R.id.file_artist_detail)
        songSizeTv = findViewById(R.id.file_size_detail)
        songGenreTv = findViewById(R.id.file_genre_detail)
        songBitrateTv = findViewById(R.id.file_bitrate_detail)
        songAlbumTv = findViewById(R.id.file_album_detail)
    }

    override fun onStart() {
        super.onStart()
        MediaPlayerService.ui_visible = true
        if (is_granted) {
            if (musicMainAdapter == null) {
                setUpMusicScanner()
            }
        }
        if (service_bound) {
            if (MediaPlayerService.is_playing) {
                if (media_player_service != null) {
                    media_player_service!!.setSeekBar()
                }
                EventBus.getDefault().post(PrepareRunnableEvent())
            }
        }
    }

    override fun onStop() {
        super.onStop()
        MediaPlayerService.ui_visible = false
        if (service_bound) {
            media_player_service?.seekBarRunnable?.let {
                media_player_service?.seekBarHandler?.removeCallbacks(it)
            }
            EventBus.getDefault().post(RemoveLyricsHandlerEvent())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MediaPlayerService.ui_visible = false
        executorService?.shutdown()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            phoneStateCallback?.let { telephonyManager?.unregisterTelephonyCallback(it) }
        } else {
            phoneStateListener?.let {
                telephonyManager?.listen(
                    it,
                    PhoneStateListener.LISTEN_NONE
                )
            }
        }
        if (service_bound) {
            unbindService(service_connection)
            stopMusicService()
        }
        playlistFragment = null
        searchFragment = null
        lastAddedFragment = null
        bottomSheetPlayerFragment = null

    }

    fun changeNavigationColor(animateFrom: Int, animateTo: Int) {
        val window = window
        val colorAnimation = ValueAnimator.ofArgb(animateFrom, animateTo)
        colorAnimation.duration = 100
        colorAnimation.addUpdateListener { animator: ValueAnimator ->
            window.navigationBarColor = animator.animatedValue as Int
        }
        colorAnimation.start()
    }

    private fun stopMusicService() {
        if (media_player_service != null) {
            if (!media_player_service!!.isMediaPlaying) {
                stopService(Intent(this@MainActivity, MediaPlayerService::class.java))
                service_stopped = true
            }
        }
    }

    /**
     * this function starts service and binds to MainActivity
     */
    private fun bindService() {
        val playerIntent = Intent(this@MainActivity, MediaPlayerService::class.java)
        this@MainActivity.bindService(playerIntent, service_connection, BIND_AUTO_CREATE)
    }

    private fun startService() {
        val playerIntent = Intent(this@MainActivity, MediaPlayerService::class.java)
        service_stopped = try {
            startService(playerIntent)
            false
        } catch (e: Exception) {
            true
        }
    }

    private fun setUpPlOptionMenuButtons() {
        val addPlayNextPlBtn = findViewById<View>(R.id.add_play_next_pl_option)
        val addToQueuePlBtn = findViewById<View>(R.id.add_to_queue_pl_option)
        val nameEditorBtnPl = findViewById<View>(R.id.rename_pl_option)
        val chooseCoverPl = findViewById<View>(R.id.choose_cover_option)
        val deletePlBtn = findViewById<View>(R.id.delete_pl_option)
        plOptionCover = findViewById(R.id.playlist_cover_option)
        plOptionName = findViewById(R.id.playlist_name_option)
        optionPlCount = findViewById(R.id.playlist_count_name_option)
        addPlayNextPlBtn.setOnClickListener(this)
        addToQueuePlBtn.setOnClickListener(this)
        nameEditorBtnPl.setOnClickListener(this)
        chooseCoverPl.setOnClickListener(this)
        deletePlBtn.setOnClickListener(this)
    }

    private fun playShuffleSong() {
        val list: ArrayList<Music>? = storageUtil?.loadInitialList()
        if (list == null) {
            showToast("No Songs")
            return
        }
        if (list.isEmpty()) {
            showToast("No Songs")
            return
        }
        playRandomSong(list)
    }

    private fun setLastAddFragment() {
        val fragmentManager = supportFragmentManager
        lastAddedFragment =
            fragmentManager.findFragmentByTag(LAST_ADDED_FRAGMENT_TAG) as LastAddedFragment?
        val transaction = fragmentManager.beginTransaction()
        if (lastAddedFragment == null) {
            lastAddedFragment = LastAddedFragment()
            lastAddedFragment!!.enterTransition = TransitionInflater.from(this)
                .inflateTransition(android.R.transition.slide_bottom)
            transaction.replace(R.id.sec_container, lastAddedFragment!!, LAST_ADDED_FRAGMENT_TAG)
                .addToBackStack(null).commit()
            navigationView!!.setCheckedItem(R.id.navigation_last_added)
        }
    }

    private fun setPlaylistFragment() {
        val fragmentManager = supportFragmentManager
        val fragment3 = fragmentManager.findFragmentByTag(PLAYLISTS_FRAGMENT_TAG)
        val transaction = fragmentManager.beginTransaction()
        playlistFragment = PlaylistsFragment()
        if (fragment3 == null) {
            playlistFragment!!.enterTransition = TransitionInflater.from(this)
                .inflateTransition(android.R.transition.slide_right)
            transaction.replace(R.id.sec_container, playlistFragment!!, PLAYLISTS_FRAGMENT_TAG)
                .addToBackStack(null).commit()
            navigationView!!.setCheckedItem(R.id.navigation_playlist)
        }
    }

    private fun setUpOptionMenuButtons() {
        //option menu buttons
        val addPlayNextBtn = findViewById<View>(R.id.add_play_next_option)
        val addToQueueBtn = findViewById<View>(R.id.add_to_queue_option)
        val addToPlaylist = findViewById<View>(R.id.add_to_playlist_option)
        val setAsRingBtn = findViewById<View>(R.id.set_ringtone_option)
        val tagEditorBtn = findViewById<View>(R.id.tagEditor_option)
        val addLyricsBtn = findViewById<View>(R.id.addLyrics_option)
        val detailsBtn = findViewById<View>(R.id.details_option)
        val shareBtn = findViewById<View>(R.id.share_music_option)
        deleteBtn = findViewById(R.id.delete_music_option)
        optionCover = findViewById(R.id.song_album_cover_option)
        optionArtist = findViewById(R.id.song_artist_name_option)
        optionName = findViewById(R.id.song_name_option)
        addToFav = findViewById(R.id.add_to_favourites_option)
        removeFromList = findViewById(R.id.remove_music_option)
        addPlayNextBtn.setOnClickListener(this)
        addToQueueBtn.setOnClickListener(this)
        addToPlaylist.setOnClickListener(this)
        setAsRingBtn.setOnClickListener(this)
        tagEditorBtn.setOnClickListener(this)
        addLyricsBtn.setOnClickListener(this)
        detailsBtn.setOnClickListener(this)
        shareBtn.setOnClickListener(this)
        deleteBtn?.setOnClickListener(this)
        addToFav?.setOnClickListener(this)
        removeFromList?.setOnClickListener(this)
    }

    private fun setBottomSheetProperties(
        sheet: BottomSheetBehavior<View?>,
        peekHeight: Int,
        skipCollapse: Boolean,
    ) {
        sheet.isHideable = true
        sheet.peekHeight = peekHeight
        sheet.skipCollapsed = skipCollapse
        sheet.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun setBottomSheets() {
        val height = getDisplaySize().height

        val mainPlayerPeekHeight = (height / 10.6f).toInt()
        mainPlayerSheetBehavior!!.isHideable = true
        mainPlayerSheetBehavior!!.peekHeight = mainPlayerPeekHeight
        if (music != null) {
            mainPlayerSheetBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED)
        } else {
            anchoredShadow!!.alpha = 0f
            mainPlayerSheetBehavior!!.setState(BottomSheetBehavior.STATE_HIDDEN)
        }
        val optionPeekHeight = (height / 1.3f).toInt()
        optionSheetBehavior = BottomSheetBehavior.from(optionSheet!!)
        setBottomSheetProperties(optionSheetBehavior!!, optionPeekHeight, true)

        val detailsPeekHeight = (height / 1.95f).toInt()
        detailsSheetBehavior = BottomSheetBehavior.from(detailsSheet!!)
        setBottomSheetProperties(detailsSheetBehavior!!, detailsPeekHeight, true)

        val lyricFoundPeekHeight = (height / 3.5f).toInt()
        lyricsSheet = findViewById(R.id.found_lyrics_fragments)
        lyricsListBehavior = BottomSheetBehavior.from(lyricsSheet!!)
        setBottomSheetProperties(lyricsListBehavior!!, lyricFoundPeekHeight, false)

        val donationPeekHeight = (height / 1.4f).toInt()
        donationSheetBehavior = BottomSheetBehavior.from(donationSheet!!)
        setBottomSheetProperties(donationSheetBehavior!!, donationPeekHeight, true)

        val plOptionPeekHeight = (height / 1.8f).toInt()
        plSheetBehavior = BottomSheetBehavior.from(plSheet!!)
        setBottomSheetProperties(plSheetBehavior!!, plOptionPeekHeight, true)
    }

    private fun getDisplaySize(): Size {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics1 = windowManager.currentWindowMetrics
            // Gets all excluding insets
            val windowInsets = metrics1.windowInsets
            val insets: Insets =
                windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.statusBars())

            val insetsWidth: Int = insets.right + insets.left
            val insetsHeight: Int = insets.top + insets.bottom


            // Legacy size that Display#getSize reports
            val bounds = metrics1.bounds
            return Size(
                bounds.width() - insetsWidth,
                bounds.height() - insetsHeight
            )
        } else {
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            return Size(
                metrics.widthPixels,
                metrics.heightPixels
            )
        }
    }

    private fun closeSheetWhenClickOutSide(
        sheetBehavior: BottomSheetBehavior<View?>?,
        sheet: View?,
        event: MotionEvent,
    ) {
        if (sheetBehavior!!.state == BottomSheetBehavior.STATE_COLLAPSED) {
            val outRect = Rect()
            sheet!!.getGlobalVisibleRect(outRect)
            if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun resetDataInNavigation() {
        navSongName!!.text = "Song Name"
        navArtistName!!.text = "Artist"
        glideBuilt!!.glide(null, R.drawable.ic_music, navCover, 512)
    }

    fun setDataInNavigation(song_name: String?, artist_name: String?, album_uri: Bitmap?) {
        navSongName!!.text = song_name
        navArtistName!!.text = artist_name
        glideBuilt!!.glideBitmap(album_uri, R.drawable.ic_music, navCover, 512, false)
    }

    private val music: Music?
        get() {
            val musicList = storageUtil?.loadQueueList()
            var activeMusic: Music? = null
            val musicIndex: Int = storageUtil!!.loadMusicIndex()
            if (musicList.isNotNullAndNotEmpty()) {
                activeMusic = if (musicIndex != -1 && musicIndex < musicList!!.size) {
                    musicList[musicIndex]
                } else {
                    musicList!![0]
                }
            }
            return activeMusic
        }

    private fun <T> Collection<T>?.isNotNullAndNotEmpty(): Boolean {
        return this != null && this.isNotEmpty()
    }

    fun openBottomPlayer() {
        if (mainPlayerSheetBehavior!!.state == BottomSheetBehavior.STATE_HIDDEN) {
            mainPlayerSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun setLyricListAdapter(bundle: Bundle) {
        val titles = bundle.getStringArrayList("titles")
        val sampleLyrics = bundle.getStringArrayList("sampleLyrics")
        val urls = bundle.getStringArrayList("urls")
        val recyclerView = findViewById<RecyclerView>(R.id.found_lyrics_recycler_view)
        val layoutManager = LinearLayoutManager(this)
        val adapter = FoundLyricsAdapter(titles, sampleLyrics, urls, this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
    }

    fun stopMusic() {
        if (!service_stopped) {
            //service is active send media with broadcast receiver
            val broadcastIntent = Intent(BROADCAST_STOP_MUSIC)
            sendBroadcast(broadcastIntent)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("serviceState", service_bound)
        super.onSaveInstanceState(outState)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        val fragment1 = fragmentManager.findFragmentByTag(SETTINGS_FRAGMENT_TAG)
        val fragment2 = fragmentManager.findFragmentByTag(PLAYLISTS_FRAGMENT_TAG)
        val fragment3 = fragmentManager.findFragmentByTag(ABOUT_FRAGMENT_TAG)
        val fragment4 = fragmentManager.findFragmentByTag(LAST_ADDED_FRAGMENT_TAG)
        val fragment5 = fragmentManager.findFragmentByTag(FAVORITE_FRAGMENT_TAG)
        val fragment6 = fragmentManager.findFragmentByTag(TAG_EDITOR_FRAGMENT_TAG)
        val fragment7 = fragmentManager.findFragmentByTag(SEARCH_FRAGMENT_TAG)
        val fragment8 = fragmentManager.findFragmentByTag(ADD_LYRICS_FRAGMENT_TAG)
        val fragment9 = fragmentManager.findFragmentByTag(OPEN_PLAYLIST_FRAGMENT_TAG)
        if (mainPlayerSheetBehavior!!.state == BottomSheetBehavior.STATE_COLLAPSED || mainPlayerSheetBehavior!!.state == BottomSheetBehavior.STATE_HIDDEN) {
            if (drawer!!.isDrawerOpen(GravityCompat.START)) {
                drawer!!.closeDrawer(GravityCompat.START)
            } else {
                if (lyricsListBehavior!!.state == BottomSheetBehavior.STATE_COLLAPSED || lyricsListBehavior!!.state == BottomSheetBehavior.STATE_EXPANDED) {
                    lyricsListBehavior!!.state = BottomSheetBehavior.STATE_HIDDEN
                } else {
                    if (optionSheetBehavior!!.state == BottomSheetBehavior.STATE_COLLAPSED || optionSheetBehavior!!.state == BottomSheetBehavior.STATE_EXPANDED) {
                        optionSheetBehavior!!.state = BottomSheetBehavior.STATE_HIDDEN
                    } else {
                        if (fragment1 != null || fragment2 != null || fragment3 != null || fragment4 != null) {
                            navigationView!!.setCheckedItem(R.id.navigation_home)
                        }
                        if (MediaPlayerService.is_playing) {
                            if (fragment1 != null || fragment2 != null || fragment3 != null || fragment4 != null || fragment5 != null || fragment6 != null || fragment7 != null || fragment8 != null || fragment9 != null) {
                                fragmentManager.popBackStackImmediate()
                            } else {
                                moveTaskToBack(false)
                            }
                        } else {
                            super.onBackPressed()
                        }
                    }
                }
            }
        } else {
            if (bottomSheetPlayerFragment!!.queueSheetBehaviour?.state == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetPlayerFragment!!.queueSheetBehaviour?.setState(BottomSheetBehavior.STATE_HIDDEN)
            } else {
                if (mainPlayerSheetBehavior!!.state != BottomSheetBehavior.STATE_HIDDEN) {
                    mainPlayerSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        service_bound = savedInstanceState.getBoolean("serviceState")
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        //this function will collapse the option bottom sheet if we click outside of sheet
        if (event.action == MotionEvent.ACTION_DOWN) {
            closeSheetWhenClickOutSide(optionSheetBehavior, optionSheet, event)
            closeSheetWhenClickOutSide(detailsSheetBehavior, detailsSheet, event)
            closeSheetWhenClickOutSide(donationSheetBehavior, donationSheet, event)
            closeSheetWhenClickOutSide(plSheetBehavior, plSheet, event)
        }
        return super.dispatchTouchEvent(event)
    }

    fun setBottomSheetState() {
        lyricsListBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    /**
     * play from start
     */
    fun playAudio(music: Music?) {
        //starting service if its not started yet otherwise it will send broadcast msg to service
        storageUtil?.clearMusicLastPos()
        val encodedMessage = MusicHelper.encode(music)
        if (!phone_ringing) {
            if (service_stopped) {
                startService()
                handler?.postDelayed({

                    //service is active send media with broadcast receiver
                    val broadcastIntent = Intent(BROADCAST_PLAY_NEW_MUSIC)
                    broadcastIntent.putExtra("music", encodedMessage)
                    sendBroadcast(broadcastIntent)
                }, 0)
            } else {
                //service is active send media with broadcast receiver
                val broadcastIntent = Intent(BROADCAST_PLAY_NEW_MUSIC)
                broadcastIntent.putExtra("music", encodedMessage)
                sendBroadcast(broadcastIntent)
            }
        } else {
            showToast("Can't play while on call")
        }
    }

    private fun playRandomSong(songs: ArrayList<Music>) {
        if (shouldIgnoreClick()) {
            return
        }
        storageUtil?.saveShuffle(true)
        storageUtil?.saveTempMusicList(songs)
        /*
         * Plays a random song from the given list of songs by sending a broadcast message
         */
        val rand = Random()
        val music = songs[rand.nextInt(songs.size)]
        //removing current item from list
        songs.remove(music)

        //shuffling list
        songs.shuffle()

        //adding the removed item in shuffled list on 0th index
        songs.add(0, music)

        //saving list
        storageUtil!!.saveQueueList(songs)
        storageUtil!!.saveMusicIndex(0)
        bottomSheetPlayerFragment!!.updateQueueAdapter(songs)
        playAudio(music)
        openBottomPlayer()
    }

    /**
     * play or pause
     */
    fun pausePlayAudio() {
        if (!phone_ringing) {
            if (service_stopped) {
                startService()
                handler!!.postDelayed({

                    //service is active send media with broadcast receiver
                    val broadcastIntent = Intent(BROADCAST_PAUSE_PLAY_MUSIC)
                    sendBroadcast(broadcastIntent)
                }, 0)
            } else {
                //service is active send media with broadcast receiver
                val broadcastIntent = Intent(BROADCAST_PAUSE_PLAY_MUSIC)
                sendBroadcast(broadcastIntent)
            }
        } else {
            showToast("Can't play while on call")
        }
    }

    private fun shouldIgnoreClick(): Boolean {
        val delay: Long = 500
        if (SystemClock.elapsedRealtime() < lastClickTime + delay) {
            return true
        }
        lastClickTime = SystemClock.elapsedRealtime()
        return false
    }

    /**
     * next music
     */
    fun playNextAudio() {
        if (shouldIgnoreClick()) {
            return
        }
        if (!phone_ringing) {
            if (service_stopped) {
                startService()
                handler!!.postDelayed({

                    //service is active send media with broadcast receiver
                    val broadcastIntent = Intent(BROADCAST_PLAY_NEXT_MUSIC)
                    sendBroadcast(broadcastIntent)
                }, 0)
            } else {
                //service is active send media with broadcast receiver
                val broadcastIntent = Intent(BROADCAST_PLAY_NEXT_MUSIC)
                sendBroadcast(broadcastIntent)
            }
        } else {
            showToast("Can't play while on call")
        }
    }

    /**
     * previous music
     */
    fun playPreviousAudio() {
        if (shouldIgnoreClick()) {
            return
        }
        if (!phone_ringing) {
            if (service_stopped) {
                startService()
                handler!!.postDelayed({

                    //service is active send media with broadcast receiver
                    val broadcastIntent = Intent(BROADCAST_PLAY_PREVIOUS_MUSIC)
                    sendBroadcast(broadcastIntent)
                }, 0)
            } else {
                //service is active send media with broadcast receiver
                val broadcastIntent = Intent(BROADCAST_PLAY_PREVIOUS_MUSIC)
                sendBroadcast(broadcastIntent)
            }
        } else {
            showToast("Can't play while on call")
        }
    }

    /**
     * this function sets up player bottom sheet
     */
    private fun setFragmentInSlider() {
        bottomSheetPlayerFragment = BottomSheetPlayerFragment()
        val mainPlayerManager = supportFragmentManager
        val transaction = mainPlayerManager.beginTransaction()
        transaction.replace(R.id.player_main_container, bottomSheetPlayerFragment!!)
        transaction.commit()
    }

    private fun setSearchFragment() {
        searchFragment = SearchFragment()
        replaceFragment(
            R.id.sec_container,
            searchFragment!!,
            android.R.transition.fade,
            SEARCH_FRAGMENT_TAG
        )
    }

    private fun setRingtone(music: Music?) {
        val canWrite = Settings.System.canWrite(this)
        if (canWrite) {
            val newUri = Uri.fromFile(File(music!!.path))
            val builder = MaterialAlertDialogBuilder(this@MainActivity)
            builder.setTitle("Set as ringtone")
            builder.setMessage("Name - ${music.name}")
            builder.setCancelable(true)
            builder.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
                RingtoneManager.setActualDefaultRingtoneUri(
                    this@MainActivity,
                    RingtoneManager.TYPE_RINGTONE,
                    newUri
                )
                showToast("Ringtone set successfully")
            }
            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

            ringtoneDialog = builder.create()
            ringtoneDialog?.show()
        } else {
            requestWriteSettingsDialog()
        }
    }

    private fun requestWriteSettingsDialog() {
        val builder = MaterialAlertDialogBuilder(this@MainActivity)
        builder.setTitle("Permission request")
        builder.setMessage("For this function to work properly it needs WRITE_SETTINGS Permission.")
        builder.setCancelable(true)
        builder.setPositiveButton("Allow") { _, _ ->
            requestWriteSettingsPermission()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun requestWriteSettingsPermission() {
        val intent = Intent()
        intent.action = Settings.ACTION_MANAGE_WRITE_SETTINGS
        val uri = Uri.fromParts("package", applicationContext.packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    fun openOptionMenu(music: Music, tag: OptionSheetEnum) {
        selectedItem = music
        optionTag = tag
        removeFromList!!.visibility = View.GONE
        deleteBtn!!.visibility = View.GONE
        if (!storageUtil!!.checkFavourite(music)) {
            addToFav!!.setImageResource(R.drawable.ic_favorite_border)
        } else if (storageUtil!!.checkFavourite(music)) {
            addToFav!!.setImageResource(R.drawable.ic_favorite)
        }
        when (tag) {
            OptionSheetEnum.OPEN_PLAYLIST -> {
                removeFromList!!.visibility = View.VISIBLE
                deleteBtn!!.visibility = View.GONE
            }
            OptionSheetEnum.FAVORITE_LIST -> {}
            OptionSheetEnum.MAIN_LIST -> {
                removeFromList!!.visibility = View.GONE
                deleteBtn!!.visibility = View.VISIBLE
            }
        }
        executorService?.execute {
            var image: Bitmap? = null
            //image decoder
            try {
                MediaMetadataRetriever().use { mediaMetadataRetriever ->
                    mediaMetadataRetriever.setDataSource(music.path)
                    val art = mediaMetadataRetriever.embeddedPicture
                    try {
                        image = BitmapFactory.decodeByteArray(art, 0, art!!.size)
                    } catch (ignored: Exception) {
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val finalImage = image
            runOnUiThread {
                optionSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
                optionName!!.text = music.name
                optionArtist!!.text = music.artist
                glideBuilt!!.glideBitmap(finalImage, R.drawable.ic_music, optionCover, 128, false)
            }
        }
    }

    fun openPlOptionMenu(currentItem: Playlist) {
        plItemSelected = currentItem
        val count = currentItem.musicList.size.toString() + " Songs"
        plSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
        plOptionName!!.text = currentItem.name
        optionPlCount!!.text = count
        glideBuilt!!.glide(currentItem.coverUri, R.drawable.ic_music_list, plOptionCover, 128)
    }

    private fun deleteFromDevice(musics: List<Music?>) {
        val contentResolver = contentResolver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val uris: MutableList<Uri> = ArrayList()
            for (music in musics) {
                val contentUri = getContentUri(music)
                if (contentUri != null) {
                    uris.add(contentUri)
                }
            }
            // for android 11 and above
            if (uris.isNotEmpty()) {
                // Create a PendingIntent for the delete request
                val pendingIntent = MediaStore.createDeleteRequest(contentResolver, uris)

                // Create an IntentSenderRequest for the delete request
                val request = IntentSenderRequest.Builder(pendingIntent.intentSender).build()

                // Launch the delete request using the ActivityResultLauncher
                deleteMusicRequestLauncher.launch(request)
            }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            // for android 10
            for (music in musics) {
                try {
                    deleteItemWithContentResolver(music!!, contentResolver)
                } // for android 10 we must catch a recoverable security exception
                catch (e: RecoverableSecurityException) {
                    val intent = e.userAction.actionIntent.intentSender
                    // Create an IntentSenderRequest for the delete request
                    val request = IntentSenderRequest.Builder(intent).build()
                    // Launch the delete request using the ActivityResultLauncher
                    deleteMusicRequestLauncher.launch(request)
                }
            }
        } else {
            // for older devices
            for (music in musics) {
                try {
                    deleteItemWithContentResolver(music!!, contentResolver)
                } catch (e: Exception) {
                    showToast("Failed to delete this song")
                }
            }
        }
    }

    private fun deleteItemWithContentResolver(music: Music, contentResolver: ContentResolver) {
        val builder = MaterialAlertDialogBuilder(this)
        val customLayout = layoutInflater.inflate(R.layout.delete_layout, null)
        builder.setView(customLayout)
        builder.setCancelable(false)
        val imageView = customLayout.findViewById<ImageView>(R.id.cover_image)
        glideBuilt!!.glide(music.albumUri, R.drawable.ic_music_thumbnail, imageView, 512)
        builder.setPositiveButton("Allow") { dialog: DialogInterface, i: Int ->
            if (ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestWriteStoragePermissionBelow11()
            } else {
                val contentUri = getContentUri(music)
                val selectionArgs = arrayOf(music.id)
                if (contentUri != null) {
                    contentResolver.delete(
                        contentUri,
                        MediaStore.Audio.Media._ID + "=?",
                        selectionArgs
                    )
                    updateAdaptersForRemovedItem()
                }
                dialog.cancel()
            }
        }
        builder.setNegativeButton("Deny", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    fun updateAdaptersForRemovedItem() {
        musicMainAdapter!!.removeItem(selectedItem!!)
        if (lastAddedFragment != null && lastAddedFragment!!.adapter != null) {
            lastAddedFragment!!.adapter!!.removeItem(selectedItem!!)
        }
        if (searchFragment != null && searchFragment!!.adapter != null) {
            searchFragment!!.adapter!!.removeItem(selectedItem!!)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsStorage = SettingsStorage(this)
        val switch1 = settingsStorage.loadIsThemeDark()
        if (!switch1) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        if (!settingsStorage.loadIsThemeDark()) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.activity_main)

        //initializations
        MediaPlayerService.ui_visible = true
        storageUtil = StorageUtil(this@MainActivity)
        executorService = Executors.newFixedThreadPool(10)
        handler = Handler(Looper.getMainLooper())
        musicRepo = instance
        glideBuilt = GlideBuilt(this)
        tempThemeColor = resources.getColor(R.color.player_bg, null)
        linearLayout = findViewById(R.id.song_not_found_layout)
        musicRecyclerView = findViewById(R.id.music_recycler)
        player_bottom_sheet = findViewById(R.id.player_main_container)
        progressBar = findViewById(R.id.progress_bar_main_activity)
        shadowMain = findViewById(R.id.shadow_main)
        shadowLyrFound = findViewById(R.id.shadow_lyrics_found)
        shadowOuterSheet = findViewById(R.id.outer_sheet_shadow)
        shadowOuterSheet2 = findViewById(R.id.outer_sheet_details_shadow)
        optionSheet = findViewById(R.id.option_bottom_sheet)
        donationSheet = findViewById(R.id.donation_bottom_sheet)
        plSheet = findViewById(R.id.pl_option_bottom_sheet)
        detailsSheet = findViewById(R.id.file_details_sheet)
        anchoredShadow = findViewById(R.id.anchored_player_shadow)
        mainPlayerSheetBehavior =
            BottomSheetBehavior.from(player_bottom_sheet!!) as CustomBottomSheet<View?>
        val openDrawer = findViewById<ImageView>(R.id.open_drawer_btn)
        val searchBar = findViewById<MaterialCardView>(R.id.searchBar_card)
        val plCard = findViewById<View>(R.id.playlist_card_view_ma)
        val lastAddCard = findViewById<View>(R.id.last_added_card_view)
        val shuffleCard = findViewById<View>(R.id.shuffle_play_card_view)
        navigationView = findViewById(R.id.navigation_drawer)
        drawer = findViewById(R.id.drawer_layout)
        val headerView = navigationView!!.getHeaderView(0)
        val navDetailLayout = headerView.findViewById<View>(R.id.nav_details_layout)
        navCover = headerView.findViewById(R.id.nav_cover_img)
        navSongName = headerView.findViewById(R.id.nav_song_name)
        navArtistName = headerView.findViewById(R.id.nav_song_artist)
        navDetailLayout.setOnClickListener { v: View? ->
            drawer!!.closeDrawer(GravityCompat.START)
            if (music != null) {
                mainPlayerSheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        navigationView!!.setNavigationItemSelectedListener(this)
        openDrawer.setOnClickListener {
            if (!drawer!!.isDrawerOpen(GravityCompat.START)) {
                drawer!!.openDrawer(GravityCompat.START)
            }
        }
        searchBar.setOnClickListener { setSearchFragment() }
        plCard.setOnClickListener { setPlaylistFragment() }
        lastAddCard.setOnClickListener { setLastAddFragment() }
        shuffleCard.setOnClickListener { playShuffleSong() }

        //Checking storage & other permissions
        checkForPermission()
        setFragmentInSlider()
        setBottomSheets()
        setUpOptionMenuButtons()
        setDetailsMenuButtons()
        setUpPlOptionMenuButtons()
        lyricsListBehavior!!.addBottomSheetCallback(lrcFoundCallback)
        optionSheetBehavior!!.addBottomSheetCallback(optionCallback)
        detailsSheetBehavior!!.addBottomSheetCallback(detailsSheetCallback)
        plSheetBehavior!!.addBottomSheetCallback(plSheetCallback)
        donationSheetBehavior!!.addBottomSheetCallback(donationCallback)
        mainPlayerSheetBehavior!!.addBottomSheetCallback(mainPlayerSheetCallback)
        if (savedInstanceState == null) {
            navigationView!!.setCheckedItem(R.id.navigation_home)
        }
        if (is_granted) {
            if (!isChecking) {
                checkForUpdateList(false)
            }
        }
    }

    private fun addToNextPlay(music: Music?) {
        bottomSheetPlayerFragment!!.queueAdapter!!.updateItemInserted(music)
    }

    private fun addToQueue(music: Music?) {
        bottomSheetPlayerFragment!!.queueAdapter!!.updateItemInsertedLast(music)
    }

    private fun closeOptionSheet() {
        optionSheetBehavior!!.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun showToast(s: String?) {
        Toast.makeText(applicationContext, s, Toast.LENGTH_SHORT).show()
    }

    private fun callStateListener() {
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            phoneStateCallback = CustomCallStateListener()
            telephonyManager!!.registerTelephonyCallback(
                ContextCompat.getMainExecutor(this),
                phoneStateCallback!!
            )
        } else {
            phoneStateListener = object : PhoneStateListener() {
                @Deprecated("Deprecated in Java")
                override fun onCallStateChanged(state: Int, phoneNumber: String) {
                    super.onCallStateChanged(state, phoneNumber)
                    takeActionOnCall(state)
                }
            }
            telephonyManager!!.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    private fun takeActionOnCall(state: Int) {
        when (state) {
            TelephonyManager.CALL_STATE_OFFHOOK, TelephonyManager.CALL_STATE_RINGING -> {
                phone_ringing = true
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                phone_ringing = false
            }
        }
    }

    //Checks whether user granted permissions for external storage or not
    //if not then shows dialogue to grant permissions
    private fun requestReadStorageAndPhonePermissionBelow12() {
        Dexter.withContext(this@MainActivity).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
                if (multiplePermissionsReport.areAllPermissionsGranted()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        requestForegroundServicePermissionAbove8()
                    }
                    is_granted = true
                    setUpServiceAndScanner()
                } else {
                    is_granted = false
                    showToast("Permissions denied!")
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                list: List<PermissionRequest>,
                permissionToken: PermissionToken,
            ) {
                is_granted = false
                permissionToken.continuePermissionRequest()
            }
        }).check()
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private fun requestReadAudioAndPhonePermissionAbove12() {
        Dexter.withContext(this@MainActivity).withPermissions(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_PHONE_STATE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
                if (multiplePermissionsReport.areAllPermissionsGranted()) {
                    requestForegroundServicePermissionAbove8()
                    is_granted = true
                    setUpServiceAndScanner()
                } else {
                    is_granted = false
                    showToast("Permissions denied!")
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                list: List<PermissionRequest>,
                permissionToken: PermissionToken,
            ) {
                is_granted = false
                permissionToken.continuePermissionRequest()
            }
        }).check()
    }

    private fun requestWriteStoragePermissionBelow11() {
        Dexter.withContext(this@MainActivity)
            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse) {}
                override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse) {
                    showToast("Permission denied!")
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissionRequest: PermissionRequest,
                    permissionToken: PermissionToken,
                ) {
                    permissionToken.continuePermissionRequest()
                }
            }).check()
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private fun requestForegroundServicePermissionAbove8() {
        Dexter.withContext(this@MainActivity).withPermission(Manifest.permission.FOREGROUND_SERVICE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse) {}
                override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse) {}
                override fun onPermissionRationaleShouldBeShown(
                    permissionRequest: PermissionRequest,
                    permissionToken: PermissionToken,
                ) {
                    permissionToken.continuePermissionRequest()
                }
            }).check()
    }

    private fun setUpServiceAndScanner() {
        setUpMusicScanner()
        callStateListener()
        bindService()
        startService()
    }

    fun checkForUpdateList(enableLoading: Boolean) {
        // do in background code here
        val service = Executors.newFixedThreadPool(1)
        if (enableLoading) {
            setObserver()
        }
        isChecking = true
        service.execute {
            musicRepo!!.fetchMusic(this).thenAccept {
                setMusicAdapter(musicRepo!!.initialMusicList)
                isChecking = false
            }.exceptionally {
                showToast(it.message)
                isChecking = false
                null
            }
        }
        service.shutdown()
    }

    private fun setObserver() {
        musicRepo!!.getStatus().observe(this) {
            when (it) {
                LoadingStatus.LOADING -> {
                    progressBar!!.visibility = View.VISIBLE
                    linearLayout!!.visibility = View.GONE
                }
                LoadingStatus.SUCCESS -> {
                    val list: ArrayList<Music> = musicRepo!!.initialMusicList
                    storageUtil!!.saveInitialList(list)
                    if (list.isEmpty()) {
                        linearLayout!!.visibility = View.VISIBLE
                    }
                    progressBar!!.visibility = View.GONE
                }
                LoadingStatus.FAILURE -> {
                    linearLayout!!.visibility = View.VISIBLE
                    progressBar!!.visibility = View.GONE
                }
                else -> {}
            }
        }
    }

    /**
     * checks if music list already available in local app storage otherwise scans media storage for music
     */
    private fun setUpMusicScanner() {
        //Fetch Music List along with it's metadata and save it in "dataList"
        val loadList: ArrayList<Music>? = storageUtil?.loadInitialList()
        if (loadList.isNotNullAndNotEmpty()) {
            setMusicAdapter(loadList)
        } else {
            if (!isChecking) {
                checkForUpdateList(true)
            }
        }
    }

    private fun setMusicAdapter(musicArrayList: ArrayList<Music>?) {
        runOnUiThread {
            if (musicMainAdapter == null) {
                val manager = LinearLayoutManager(this@MainActivity)
                musicMainAdapter = MusicMainAdapter(this@MainActivity, musicArrayList)
                musicRecyclerView!!.setHasFixedSize(true)
                musicRecyclerView!!.layoutManager = manager
                musicRecyclerView!!.adapter = musicMainAdapter
            } else {
                musicMainAdapter?.updateMusicListItems(musicArrayList)
                storageUtil?.saveInitialList(musicArrayList!!)
            }
        }
    }


    @SuppressLint("NonConstantResourceId")
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawer!!.closeDrawer(GravityCompat.START)
        val fragmentManager = supportFragmentManager
        val fragment1 = fragmentManager.findFragmentByTag(SETTINGS_FRAGMENT_TAG)
        val fragment2 = fragmentManager.findFragmentByTag(PLAYLISTS_FRAGMENT_TAG)
        val fragment3 = fragmentManager.findFragmentByTag(ABOUT_FRAGMENT_TAG)
        val fragment4 = fragmentManager.findFragmentByTag(FAVORITE_FRAGMENT_TAG)
        val fragment5 = fragmentManager.findFragmentByTag(SEARCH_FRAGMENT_TAG)
        val fragment6 = fragmentManager.findFragmentByTag(LAST_ADDED_FRAGMENT_TAG)
        when (item.itemId) {
            R.id.navigation_home -> {
                if (fragment1 != null || fragment2 != null || fragment3 != null || fragment6 != null) {
                    fragmentManager.popBackStackImmediate()
                }
            }
            R.id.navigation_setting -> {
                if (fragment2 != null || fragment3 != null || fragment5 != null || fragment6 != null) {
                    fragmentManager.popBackStackImmediate()
                }
                if (fragment1 == null) {
                    replaceFragment(
                        R.id.sec_container,
                        SettingsFragment(),
                        android.R.transition.slide_right,
                        SETTINGS_FRAGMENT_TAG
                    )
                }
            }
            R.id.navigation_playlist -> {
                if (fragment1 != null || fragment3 != null || fragment4 != null || fragment5 != null || fragment6 != null) {
                    fragmentManager.popBackStackImmediate()
                }
                if (fragment2 == null) {
                    replaceFragment(
                        R.id.sec_container,
                        PlaylistsFragment(),
                        android.R.transition.slide_right,
                        PLAYLISTS_FRAGMENT_TAG
                    )
                }
            }
            R.id.navigation_about -> {
                if (fragment1 != null || fragment2 != null || fragment5 != null || fragment6 != null) {
                    fragmentManager.popBackStackImmediate()
                }
                if (fragment3 == null) {
                    replaceFragment(
                        R.id.sec_container,
                        AboutFragment(),
                        android.R.transition.slide_right,
                        ABOUT_FRAGMENT_TAG
                    )
                }
            }
            R.id.navigation_last_added -> {
                if (fragment1 != null || fragment2 != null || fragment5 != null || fragment3 != null) {
                    fragmentManager.popBackStackImmediate()
                }
                if (fragment6 == null) {
                    replaceFragment(
                        R.id.sec_container,
                        LastAddedFragment(),
                        android.R.transition.slide_right,
                        LAST_ADDED_FRAGMENT_TAG
                    )
                }
            }
            R.id.navigation_donate -> {
                donationSheetBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
        }
        return true
    }

    /**
     * Fragment replacer
     *
     * @param container the layout id you want to replace with
     * @param fragment  new Fragment you want to replace
     * @param animation ex- android.R.transition.explode
     * @param tag       fragment tag to call it later
     */
    private fun replaceFragment(container: Int, fragment: Fragment, animation: Int, tag: String) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        fragment.enterTransition =
            TransitionInflater.from(this).inflateTransition(animation)
        transaction.replace(container, fragment, tag).addToBackStack(null).commit()
    }

    fun openBottomSheet(bundle: Bundle) {
        setLyricListAdapter(bundle)
        lyricsListBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
    }

    @SuppressLint("NonConstantResourceId")
    override fun onClick(v: View) {
        closeOptionSheet()
        closePlOptionSheet()
        when (v.id) {
            R.id.add_play_next_option -> {
                addToNextPlay(selectedItem)
            }
            R.id.add_to_queue_option -> {
                addToQueue(selectedItem)
            }
            R.id.add_to_playlist_option -> {
                addToPlaylist(selectedItem)
            }
            R.id.set_ringtone_option -> {
                setRingtone(selectedItem)
            }
            R.id.delete_music_option -> {
                val list: MutableList<Music?> = ArrayList()
                list.add(selectedItem)
                deleteFromDevice(list)
            }
            R.id.tagEditor_option -> {
                openTagEditor(selectedItem)
            }
            R.id.addLyrics_option -> {
                bottomSheetPlayerFragment!!.setLyricsLayout(selectedItem)
            }
            R.id.details_option -> {
                openDetailsBox(selectedItem)
            }
            R.id.share_music_option -> {
                openShare(selectedItem)
            }
            R.id.add_to_favourites_option -> {
                bottomSheetPlayerFragment!!.addFavorite(storageUtil!!, selectedItem, addToFav)
            }
            R.id.add_play_next_pl_option -> {
                addToNextPlayPl(plItemSelected)
            }
            R.id.add_to_queue_pl_option -> {
                addToQueuePl(plItemSelected)
            }
            R.id.rename_pl_option -> {
                openRenameDialog(plItemSelected)
            }
            R.id.choose_cover_option -> {
                changeUriPl(plItemSelected)
            }
            R.id.delete_pl_option -> {
                deletePl(plItemSelected)
            }
            R.id.remove_music_option -> {
                removeFromList(selectedItem, optionTag)
            }
        }
    }

    private fun removeFromList(music: Music?, optionTag: OptionSheetEnum?) {
        if (optionTag == OptionSheetEnum.OPEN_PLAYLIST) {
            EventBus.getDefault().post(RemoveFromPlaylistEvent(music))
            //solve removed song not loading in playlist adapter
        } else if (optionTag == OptionSheetEnum.FAVORITE_LIST) {
            EventBus.getDefault().post(RemoveFromFavoriteEvent(music))
        }
    }

    private fun openRenameDialog(playlist: Playlist?) {
        val builder = MaterialAlertDialogBuilder(this)
        val customLayout = layoutInflater.inflate(R.layout.rename_playlist_layout, null)
        builder.setView(customLayout)
        builder.setCancelable(true)
        playlistArrayList = storageUtil!!.allPlaylist
        val editText = customLayout.findViewById<EditText>(R.id.edit_playlist_rename)
        editText.setText(playlist!!.name)
        builder.setPositiveButton("OK") { dialog: DialogInterface, i: Int ->
            val plKey = editText.text.toString().trim { it <= ' ' }
            val playlistNames = ArrayList<String>()
            for (playlist1 in playlistArrayList!!) {
                playlistNames.add(playlist1.name)
            }
            if (plKey != "") {
                if (!playlistNames.contains(plKey)) {
                    renamePl(playlist, plKey)
                    dialog.dismiss()
                } else {
                    editText.error = "Name already exist"
                }
            } else {
                editText.error = "Empty"
            }
        }
        builder.setNegativeButton("Cancel", null)
        val renameDialog = builder.create()
        renameDialog.show()
    }

    private fun deletePl(playlist: Playlist?) {
        playlistFragment!!.playlistList.remove(playlist)
        storageUtil!!.removePlayList(playlist!!.name)
        val arrayList = storageUtil!!.allPlaylist
        playlistFragment!!.playlistAdapter.updateView(arrayList)
    }

    private fun renamePl(playlist: Playlist?, newName: String) {
        storageUtil!!.replacePlaylist(playlist!!, newName, playlist.coverUri)
        val arrayList = storageUtil?.allPlaylist
        playlistFragment?.playlistAdapter?.updateView(arrayList)
    }

    private fun changeUriPl(playlist: Playlist?) {
        pl_name = playlist!!.name
        pickImage(pickIntentForPLCoverChange, mediaPickerForPLCoverChange)
    }

    private fun addToQueuePl(playlist: Playlist?) {
        val list = playlist!!.musicList
        bottomSheetPlayerFragment!!.queueAdapter!!.updateListInsertedLast(list)
    }

    private fun addToNextPlayPl(playlist: Playlist?) {
        val list = playlist!!.musicList
        bottomSheetPlayerFragment!!.queueAdapter!!.updateListInserted(list)
    }

    private fun closePlOptionSheet() {
        plSheetBehavior!!.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun addToPlaylist(music: Music?) {
        openPlaylistDialog(music)
    }

    private fun openPlaylistDialog(music: Music?) {
        val builder = MaterialAlertDialogBuilder(this)
        val customLayout = layoutInflater.inflate(R.layout.playlist_dialog_layout, null)
        builder.setView(customLayout)
        builder.setCancelable(true)
        //Initialize Dialogue Box UI Items
        playlistArrayList = storageUtil!!.allPlaylist
        val image = customLayout.findViewById<ImageView>(R.id.add_to_fav_dialog_box_img)
        if (!storageUtil!!.checkFavourite(music)) {
            image.setImageResource(R.drawable.ic_favorite_border)
        } else if (storageUtil!!.checkFavourite(music)) {
            image.setImageResource(R.drawable.ic_favorite)
        }
        plDialogRecyclerView = customLayout.findViewById(R.id.playlist_dialog_recycler)
        val createPlaylistBtnDialog = customLayout.findViewById<View>(R.id.create_playlist)
        val addFavBtnDialog = customLayout.findViewById<View>(R.id.add_to_fav_dialog_box)
        noPl_tv = customLayout.findViewById(R.id.text_no_pl)
        createPlaylistBtnDialog.setOnClickListener { v: View? -> openCreatePlaylistDialog(music) }
        addFavBtnDialog.setOnClickListener { v: View? ->
            addToFavorite(music)
            addToPlDialog!!.dismiss()
        }
        val manager: LinearLayoutManager = LinearLayoutManagerWrapper(this)
        plDialogRecyclerView?.layoutManager = manager
        if (playlistArrayList != null) {
            playlistDialogAdapter = PlaylistDialogAdapter(this, playlistArrayList, music)
            if (playlistArrayList!!.isEmpty()) {
                noPl_tv?.visibility = View.VISIBLE
            } else {
                noPl_tv?.visibility = View.GONE
            }
            plDialogRecyclerView?.adapter = playlistDialogAdapter
        } else {
            noPl_tv?.visibility = View.VISIBLE
        }
        addToPlDialog = builder.create()
        addToPlDialog!!.show()
    }

    private fun addToFavorite(music: Music?) {
        storageUtil!!.saveFavorite(music)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun openCreatePlaylistDialog(music: Music?) {
        val builder = MaterialAlertDialogBuilder(this)
        val customLayout = layoutInflater.inflate(R.layout.create_playlist_layout, null)
        builder.setView(customLayout)
        builder.setCancelable(true)
        val editText = customLayout.findViewById<EditText>(R.id.edit_playlist_name)
        playlist_image_View = customLayout.findViewById(R.id.playlist_image_view)
        val playerPickCover_l = customLayout.findViewById<View>(R.id.playlist_cover_pick)
        playerPickCover_l.setOnClickListener { v: View? ->
            pickImage(
                pickIntentForPLCover,
                mediaPickerForPLCover
            )
        }

        //check if previous list contains the same name as we are saving
        builder.setPositiveButton("OK") { dialog: DialogInterface, i: Int ->
            val plKey = editText.text.toString().trim { it <= ' ' }
            val plCoverUri = if (playListImageUri != null) playListImageUri.toString() else ""
            val playlistNames = ArrayList<String>()
            for (playlist in playlistArrayList!!) {
                playlistNames.add(playlist.name)
            }
            if (plKey != "") {
                if (!playlistNames.contains(plKey)) {
                    storageUtil!!.createPlaylist(plKey, plCoverUri)
                    val allList = storageUtil!!.allPlaylist
                    if (playlistArrayList != null) {
                        playlistArrayList!!.clear()
                        playlistArrayList!!.addAll(allList)
                        if (noPl_tv != null && noPl_tv!!.visibility == View.VISIBLE) {
                            noPl_tv!!.visibility = View.GONE
                        }
                        playlistDialogAdapter!!.notifyDataSetChanged()
                        playListImageUri = null
                    } else {
                        playlistDialogAdapter = PlaylistDialogAdapter(this, allList, music)
                        plDialogRecyclerView!!.adapter = playlistDialogAdapter
                    }
                    dialog.dismiss()
                } else {
                    editText.error = "Name already exist"
                }
            } else {
                editText.error = "Empty"
            }
        }
        builder.setNegativeButton("Cancel", null)
        val plDialog = builder.create()
        plDialog.show()
    }

    private fun openShare(music: Music?) {
        val intent = MusicUtil.createShareSongFileIntent(this, music)
        startActivity(Intent.createChooser(intent, "Share Via ..."))
    }

    private fun openDetailsBox(music: Music?) {
        detailsSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
        var bitrateInNum = 0
        val bitrate = music!!.bitrate
        if (!TextUtils.isEmpty(bitrate)) {
            bitrateInNum = bitrate.toInt() / 1000
        }
        val finalBitrate = "$bitrateInNum KBPS"
        songPathTv!!.text = music.path
        songNameTv!!.text = music.name
        songArtistTv!!.text = music.artist
        songSizeTv!!.text = convertByteSizeToReadableSize(music.size)
        songAlbumTv!!.text = music.album
        var genre = music.genre
        if (TextUtils.isEmpty(genre)) {
            genre = "Unknown"
        }
        songGenreTv!!.text = genre
        songBitrateTv!!.text = finalBitrate
    }

    private fun convertByteSizeToReadableSize(fileSize: String): String {
        var size = fileSize.toFloat() / (1024 * 1024)
        var pos = size.toString().indexOf(".")
        var beforeDot = size.toString().substring(0, pos)
        var afterDot: String
        afterDot = try {
            size.toString().substring(pos, pos + 3)
        } catch (e: IndexOutOfBoundsException) {
            "0"
        }
        var sizeExt = " mb"
        val beforeDotInt = beforeDot.toInt()
        if (beforeDotInt % 1024 != beforeDotInt) {
            val `in` = beforeDot.toFloat()
            size = `in` / 1024
            pos = size.toString().indexOf(".")
            beforeDot = size.toString().substring(0, pos)
            afterDot = try {
                size.toString().substring(pos, pos + 3)
            } catch (e: IndexOutOfBoundsException) {
                "0"
            }
            sizeExt = " gb"
        }
        return beforeDot + afterDot + sizeExt
    }

    private fun openTagEditor(itemSelected: Music?) {
        val fragmentManager = supportFragmentManager
        val fragment1 = fragmentManager.findFragmentByTag(SEARCH_FRAGMENT_TAG)
        val fragment2 = fragmentManager.findFragmentByTag(TAG_EDITOR_FRAGMENT_TAG)
        if (fragment1 != null) {
            fragmentManager.popBackStackImmediate()
        }
        if (mainPlayerSheetBehavior!!.state == BottomSheetBehavior.STATE_EXPANDED) {
            mainPlayerSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        val encodedMessage = MusicHelper.encode(itemSelected)
        if (fragment2 == null) {
            val fragment = TagEditorFragment()
            val bundle = Bundle()
            bundle.putSerializable("currentMusic", encodedMessage)
            fragment.arguments = bundle
            replaceFragment(
                R.id.sec_container,
                fragment,
                android.R.transition.no_transition,
                TAG_EDITOR_FRAGMENT_TAG
            )
        }
    }

    /**
     * get Content uri of music (Required to delete files in android 11 and above)
     *
     * @param music music
     * @return returns content uri of given music
     */
    private fun getContentUri(music: Music?): Uri? {
        music?.let {
            val id = it.id.toInt()
            val baseUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            return Uri.withAppendedPath(baseUri, "" + id)
        }
        return null
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private inner class CustomCallStateListener : PhoneStateCallback() {
        override fun onCallStateChanged(state: Int) {
            takeActionOnCall(state)
        }
    }

    companion object {
        const val BROADCAST_PLAY_NEW_MUSIC = "com.atomykcoder.atomykplay.PlayNewMusic"
        const val BROADCAST_PAUSE_PLAY_MUSIC = "com.atomykcoder.atomykplay.PausePlayMusic"
        const val BROADCAST_STOP_MUSIC = "com.atomykcoder.atomykplay.StopMusic"
        const val BROADCAST_PLAY_NEXT_MUSIC = "com.atomykcoder.atomykplay.PlayNextMusic"
        const val BROADCAST_PLAY_PREVIOUS_MUSIC = "com.atomykcoder.atomykplay.PlayPreviousMusic"
        const val SETTINGS_FRAGMENT_TAG = "SettingsFragment"
        const val ADD_LYRICS_FRAGMENT_TAG = "AddLyricsFragment"
        const val SEARCH_FRAGMENT_TAG = "SearchFragment"
        const val FAVORITE_FRAGMENT_TAG = "FavoritesFragment"
        const val TAG_EDITOR_FRAGMENT_TAG = "TagEditorFragment"
        const val PLAYLISTS_FRAGMENT_TAG = "PlaylistsFragment"
        const val ABOUT_FRAGMENT_TAG = "AboutFragment"
        const val OPEN_PLAYLIST_FRAGMENT_TAG = "OpenPlayListFragment"
        const val LAST_ADDED_FRAGMENT_TAG = "LastAddedFragment"
        var service_bound = false
        var is_granted = false
        var phone_ringing = false
        var service_stopped = false
        var media_player_service: MediaPlayerService? = null
    }
}