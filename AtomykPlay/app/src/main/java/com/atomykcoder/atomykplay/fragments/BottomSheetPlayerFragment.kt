package com.atomykcoder.atomykplay.fragments

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import androidx.transition.TransitionInflater
import com.airbnb.lottie.LottieAnimationView
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.activities.MainActivity
import com.atomykcoder.atomykplay.adapters.MusicLyricsAdapter
import com.atomykcoder.atomykplay.adapters.MusicQueueAdapter
import com.atomykcoder.atomykplay.adapters.SimpleTouchCallback
import com.atomykcoder.atomykplay.classes.ApplicationClass
import com.atomykcoder.atomykplay.classes.GlideBuilt
import com.atomykcoder.atomykplay.customScripts.CenterSmoothScrollScript.CenterSmoothScroller
import com.atomykcoder.atomykplay.customScripts.CustomBottomSheet
import com.atomykcoder.atomykplay.customScripts.LinearLayoutManagerWrapper
import com.atomykcoder.atomykplay.data.Music
import com.atomykcoder.atomykplay.dataModels.LRCMap
import com.atomykcoder.atomykplay.enums.OptionSheetEnum
import com.atomykcoder.atomykplay.enums.PlaybackStatus
import com.atomykcoder.atomykplay.events.*
import com.atomykcoder.atomykplay.helperFunctions.MusicHelper
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener
import com.atomykcoder.atomykplay.utils.StorageUtil
import com.atomykcoder.atomykplay.utils.StorageUtil.SettingsStorage
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BottomSheetPlayerFragment : Fragment(), OnSeekBarChangeListener, OnDragStartListener {
    private val lyricsArrayList = ArrayList<String>()

    @JvmField
    var queueSheetBehaviour: CustomBottomSheet<View?>? = null
    var lyricsRunnable: Runnable? = null
    var lyricsHandler: Handler? = null
    var mini_progress: LinearProgressIndicator? = null
    var mini_pause: ImageView? = null

    //main player seekbar
    var seekBarMain: SeekBar? = null
    var playImg: ImageView? = null
    var curPosTv: TextView? = null
    var mini_cover: ImageView? = null

    @JvmField
    var mini_next: ImageView? = null
    var mini_name_text: TextView? = null

    @JvmField
    var mini_artist_text: TextView? = null

    @JvmField
    var mini_play_view: View? = null

    @JvmField
    var player_layout: View? = null
    var optionImg: ImageView? = null

    @JvmField
    var info_layout: View? = null

    @JvmField
    var queueAdapter: MusicQueueAdapter? = null
    private var musicArrayList: ArrayList<Music>? = null
    private var glideBuilt: GlideBuilt? = null
    private var gradientTop: GradientDrawable? = null
    private var gradientBottom: GradientDrawable? = null
    private var userScrolling = false
    var onScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == 0) {
                userScrolling = false
            } else if (newState == 1 || newState == 2) {
                userScrolling = true
            }
            super.onScrollStateChanged(recyclerView, newState)
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
        }
    }
    private var context: Context? = null
    private var durationTv: TextView? = null
    private var playerCoverImage: ImageView? = null
    private var favoriteImg: ImageView? = null
    private var playerSongNameTv: TextView? = null
    private var playerArtistNameTv: TextView? = null
    private var mimeTv: TextView? = null
    private var bitrateTv: TextView? = null
    private var timerTv: TextView? = null
    private var noLyricsLayout: View? = null

    //cover image view
    private var lyricsRecyclerView: RecyclerView? = null
    private var lrcMap: LRCMap? = null
    private var lm: RecyclerView.LayoutManager? = null
    private var songName: String? = null
    private var artistName: String? = null
    private var mimeType: String? = null
    private var duration: String? = null
    private var bitrate: String? = null
    private var queueBottomSheet: View? = null
    private var repeatImg: ImageView? = null
    private var shuffleImg: ImageView? = null
    private var timerImg: ImageView? = null
    private var likeAnim: LottieAnimationView? = null

    //setting up mini player layout
    //calling it from service when player is prepared and also calling it in this fragment class
    //to set it on app start ☺
    private var storageUtil: StorageUtil? = null
    private var itemTouchHelper: ItemTouchHelper? = null
    private var queueRecyclerView: RecyclerView? = null
    private var lyricsRelativeLayout: View? = null
    private var mainActivity: MainActivity? = null
    private var coverCardView: CardView? = null
    private var lyricsImg: ImageView? = null
    private var queueCoverImg: ImageView? = null
    private var shadowPlayer: View? = null
    private var songNameQueueItem: TextView? = null
    private var artistQueueItem: TextView? = null
    private var settingsStorage: SettingsStorage? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var lyricsAdapter: MusicLyricsAdapter? = null
    private var timerDialogue: Dialog? = null
    private var activeMusic: Music? = null
    private var app_paused = false
    private var should_refresh_layout = true
    private var tempColor = 0
    private val color = MutableLiveData<Int>()
    private var lastClickTime: Long = 0
    private var executorService: ExecutorService? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private fun setThemeColorForApp(color: Int) {
        this.color.value = color
    }

    val themeColor: LiveData<Int>
        get() = color

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_player, container, false)
        context = requireContext()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        glideBuilt = GlideBuilt(requireContext())

        //StorageUtil initialization
        storageUtil = StorageUtil(requireContext())
        executorService = Executors.newFixedThreadPool(10)
        if (savedInstanceState == null) {
            if (activeMusic == null) {
                activeMusic = currentMusic
            }
        } else {
            activeMusic = MusicHelper.decode(savedInstanceState.getString("activeMusic"))
        }
        if (activeMusic != null) {
            lrcMap = storageUtil?.loadLyrics(activeMusic?.id)
        }
        settingsStorage = SettingsStorage(requireContext())
        mainActivity = requireContext() as MainActivity
        playing_same_song = false

        //Mini player items initializations
        mini_play_view = view.findViewById(R.id.mini_player_layout) //○
        mini_cover = view.findViewById(R.id.song_album_cover_mini) //○
        mini_artist_text = view.findViewById(R.id.song_artist_name_mini) //○
        mini_name_text = view.findViewById(R.id.song_name_mini) //○
        mini_next = view.findViewById(R.id.more_option_i_btn_next) //○
        mini_pause = view.findViewById(R.id.more_option_i_btn_play) //○
        mini_progress = view.findViewById(R.id.mini_player_progress) //○

        //Main player items initializations
        player_layout = view.findViewById(R.id.player_layout) //○
        info_layout = view.findViewById(R.id.linear_info_layout)
        playerCoverImage = view.findViewById(R.id.player_cover_iv) //○
        seekBarMain = view.findViewById(R.id.player_seek_bar) //○
        val queImg = view.findViewById<ImageView>(R.id.player_que_iv) //○
        repeatImg = view.findViewById(R.id.player_repeat_iv) //○
        val previousImg = view.findViewById<ImageView>(R.id.player_previous_iv) //○
        playImg = view.findViewById(R.id.player_play_iv) //○
        val nextImg = view.findViewById<ImageView>(R.id.player_next_iv) //○
        shuffleImg = view.findViewById(R.id.player_shuffle_iv) //○
        favoriteImg = view.findViewById(R.id.player_favorite_iv) //○
        timerImg = view.findViewById(R.id.player_timer_iv) //○
        likeAnim = view.findViewById(R.id.like_anim)
        optionImg = view.findViewById(R.id.player_option_iv) //○
        playerSongNameTv = view.findViewById(R.id.player_song_name_tv) //○
        playerArtistNameTv = view.findViewById(R.id.player_song_artist_name_tv) //○
        bitrateTv = view.findViewById(R.id.player_bitrate_tv) //○
        mimeTv = view.findViewById(R.id.player_mime_tv) //○
        val playCv = view.findViewById<CardView>(R.id.player_play_cv)
        durationTv = view.findViewById(R.id.player_duration_tv) //○
        curPosTv = view.findViewById(R.id.player_current_pos_tv) //○
        lyricsImg = view.findViewById(R.id.player_lyrics_ll)
        timerTv = view.findViewById(R.id.countdown_tv)
        queueCoverImg = view.findViewById(R.id.song_album_cover_queue_item)
        val queueItem = view.findViewById<MaterialCardView>(R.id.queue_music_item)
        songNameQueueItem = view.findViewById(R.id.song_name_queue_item)
        artistQueueItem = view.findViewById(R.id.song_artist_name_queue_item)
        shadowPlayer = view.findViewById(R.id.shadow_player)
        coverCardView = view.findViewById(R.id.card_view_for_cover)
        lyricsRelativeLayout = view.findViewById(R.id.lyrics_relative_layout)
        val grad_view_top = view.findViewById<View>(R.id.gradient_top)
        val grad_view_bot = view.findViewById<View>(R.id.gradient_bottom)
        gradientTop = grad_view_top.background as GradientDrawable
        gradientBottom = grad_view_bot.background as GradientDrawable
        setAccorToSettings()

        //click listeners on mini player
        //and sending broadcast on click

        //play pause
        mini_pause?.setOnClickListener { mainActivity!!.pausePlayAudio() }
        //next on mini player
        mini_next?.setOnClickListener { mainActivity!!.playNextAudio() }
        //main player events
        previousImg.setOnClickListener { mainActivity!!.playPreviousAudio() }
        nextImg.setOnClickListener { mainActivity!!.playNextAudio() }
        playCv.setOnClickListener { mainActivity!!.pausePlayAudio() }
        queImg.setOnClickListener { openQue() }
        repeatImg?.setOnClickListener { repeatFun() }
        shuffleImg?.setOnClickListener { shuffleList() }
        favoriteImg?.setOnClickListener {
            addFavorite(
                storageUtil!!, activeMusic, favoriteImg
            )
        }
        timerImg?.setOnClickListener { setTimer() }
        timerTv?.setOnClickListener {
            if (MainActivity.media_player_service != null) {
                MainActivity.media_player_service?.cancelTimer()
            } else {
                showToast("Music service is not running")
            }
        }
        lyricsImg?.setOnClickListener { openLyricsPanel() }
        queueItem.setOnClickListener { scrollToCurSong() }
        lyricsImg?.setOnLongClickListener {
            setLyricsLayout(activeMusic)
            false
        }
        //        top right option button
        optionImg?.setOnClickListener { optionMenu(activeMusic) }
        queueRecyclerView = view.findViewById(R.id.queue_music_recycler)
        linearLayoutManager = LinearLayoutManagerWrapper(getContext())
        musicArrayList = storageUtil?.loadQueueList()
        queueBottomSheet = view.findViewById(R.id.queue_bottom_sheet)

        //lyrics layout related initializations
        val button = view.findViewById<TextView>(R.id.btn_add_lyrics)
        lyricsRecyclerView = view.findViewById(R.id.lyrics_recycler_view)
        noLyricsLayout = view.findViewById(R.id.no_lyrics_layout)
        lyricsRecyclerView?.addOnScrollListener(onScrollListener)
        button.setOnClickListener { v: View? -> setLyricsLayout(activeMusic) }
        seekBarMain?.setOnSeekBarChangeListener(this)
        //for animation
        playerSongNameTv?.isSelected = true
        mini_name_text?.isSelected = true
        mini_play_view?.setOnClickListener {
            val sheet = mainActivity?.mainPlayerSheetBehavior
            if (sheet?.state == BottomSheetBehavior.STATE_COLLAPSED) {
                sheet.setState(BottomSheetBehavior.STATE_EXPANDED)
            } else {
                sheet?.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
        }
        tempColor = resources.getColor(R.color.player_bg, Resources.getSystem().newTheme())
        setupQueueBottomSheet()
        return view
    }

    override fun onStart() {
        super.onStart()
        setButton(activeMusic)
        if (should_refresh_layout) {
            setPreviousData(activeMusic)
        }
        if (mainActivity?.mainPlayerSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
            mini_play_view!!.alpha = 0f
            mini_play_view!!.visibility = View.INVISIBLE
        } else if (mainActivity?.mainPlayerSheetBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
            player_layout!!.alpha = 0f
            player_layout!!.visibility = View.INVISIBLE
        }
        if (queueSheetBehaviour!!.state == BottomSheetBehavior.STATE_EXPANDED) {
            queueRecyclerView!!.visibility = View.VISIBLE
        }
        app_paused = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val music = MusicHelper.encode(activeMusic)
        outState.putString("activeMusic", music)
    }

    override fun onStop() {
        super.onStop()
        should_refresh_layout = false
        if (queueSheetBehaviour!!.state == BottomSheetBehavior.STATE_EXPANDED) {
            queueRecyclerView!!.visibility = View.GONE
        }
        app_paused = true
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        lyricsRecyclerView?.clearOnScrollListeners()
        executorService?.shutdown()
    }

    /**
     * this is for queue item click
     */
    private fun scrollToCurSong() {
        val index = storageUtil?.loadMusicIndex() ?:0
        linearLayoutManager?.scrollToPositionWithOffset(index, 0)
    }

    /**
     * This method sets elements according to settings values
     */
    private fun setAccorToSettings() {
        if (settingsStorage!!.loadShowArtist()) {
            mini_artist_text!!.visibility = View.VISIBLE
        } else {
            mini_artist_text!!.visibility = View.GONE
        }
        if (settingsStorage!!.loadShowInfo()) {
            info_layout!!.visibility = View.VISIBLE
        } else {
            info_layout!!.visibility = View.GONE
        }
        if (settingsStorage!!.loadExtraCon()) {
            mini_next!!.visibility = View.VISIBLE
        } else {
            mini_next!!.visibility = View.GONE
        }
    }

    @Subscribe
    fun removeLyricsHandler(event: RemoveLyricsHandlerEvent?) {
        if (lyricsHandler != null) {
            lyricsHandler!!.removeCallbacks(lyricsRunnable!!)
        }
    }

    // Don't Remove This Event Bus is using this method (It might look unused still DON'T REMOVE
    private fun generateThemeColor(image: Bitmap): Int {
        val palette = Palette.Builder(image).generate()
        return if (settingsStorage!!.loadIsThemeDark()) {
            palette.getDarkMutedColor(
                resources.getColor(
                    R.color.player_bg,
                    Resources.getSystem().newTheme()
                )
            )
        } else {
            palette.getLightMutedColor(
                resources.getColor(
                    R.color.player_bg,
                    Resources.getSystem().newTheme()
                )
            )
        }
    }

    private fun setThemeColorInPlayer(animateFrom: Int, animateTo: Int) {
        val colorAnimation = ValueAnimator.ofArgb(animateFrom, animateTo)
        colorAnimation.duration = 400
        colorAnimation.addUpdateListener { animator: ValueAnimator ->
            player_layout!!.setBackgroundColor(
                (animator.animatedValue as Int)
            )
        }
        colorAnimation.start()
    }

    private fun setThemeColorLyricView(color: Int) {
        gradientTop!!.mutate()
        gradientBottom!!.mutate()
        gradientTop!!.colors = intArrayOf(Color.TRANSPARENT, color)
        gradientBottom!!.colors = intArrayOf(color, Color.TRANSPARENT)
    }

    @Subscribe
    fun setMainPlayerLayout(event: SetMainLayoutEvent) {
        if (playing_same_song) {
            if (activeMusic!!.id == event.activeMusic.id) {
                return
            }
        }
        activeMusic = event.activeMusic
        if (!app_paused) {
            if (activeMusic != null) {
                should_refresh_layout = false
                var image = event.image
                tempColor = if (image != null) {
                    val themeColor = generateThemeColor(image)
                    setThemeColorForApp(themeColor)
                    setThemeColorInPlayer(tempColor, themeColor)
                    setThemeColorLyricView(themeColor)
                    themeColor
                } else {
                    setThemeColorForApp(
                        resources.getColor(
                            R.color.player_bg,
                            Resources.getSystem().newTheme()
                        )
                    )
                    setThemeColorInPlayer(
                        tempColor,
                        resources.getColor(R.color.player_bg, Resources.getSystem().newTheme())
                    )
                    setThemeColorLyricView(
                        resources.getColor(
                            R.color.player_bg,
                            Resources.getSystem().newTheme()
                        )
                    )
                    resources.getColor(R.color.player_bg, Resources.getSystem().newTheme())
                }
                songName = activeMusic!!.name
                bitrate = activeMusic!!.bitrate
                artistName = activeMusic!!.artist
                mimeType = try {
                    getMime(activeMusic!!.mimeType)!!.uppercase(Locale.getDefault())
                } catch (e: Exception) {
                    "mp3"
                }
                duration = activeMusic!!.duration
                var convertedDur: String? = "00:00"
                try {
                    convertedDur = MusicHelper.convertDuration(duration)
                } catch (ignored: Exception) {
                }
                var bitrateInNum = 0
                try {
                    if (bitrate != "") {
                        bitrateInNum = bitrate!!.toInt() / 1000
                    }
                } catch (ignored: NumberFormatException) {
                }
                val finalBitrate = "$bitrateInNum KBPS"
                if (!storageUtil!!.checkFavourite(activeMusic)) {
                    favoriteImg!!.setImageResource(R.drawable.ic_favorite_border)
                } else if (storageUtil!!.checkFavourite(activeMusic)) {
                    favoriteImg!!.setImageResource(R.drawable.ic_favorite)
                }
                if (!storageUtil!!.loadShuffle()) {
                    shuffleImg!!.setImageResource(R.drawable.ic_shuffle_empty)
                } else if (storageUtil!!.loadShuffle()) {
                    shuffleImg!!.setImageResource(R.drawable.ic_shuffle)
                }
                try {
                    playerSongNameTv!!.text = songName
                    songNameQueueItem!!.text = songName
                    playerArtistNameTv!!.text = artistName
                    artistQueueItem!!.text = artistName
                    mimeTv!!.text = mimeType
                    durationTv!!.text = convertedDur
                    mini_name_text!!.text = songName
                    bitrateTv!!.text = finalBitrate
                    mini_artist_text!!.text = artistName
                } catch (ignored: Exception) {
                }
                try {
                    seekBarMain!!.max = duration!!.toInt()
                    mini_progress!!.max = duration!!.toInt()
                } catch (e: NumberFormatException) {
                    showToast("Something went wrong")
                }
                glideBuilt!!.glideBitmap(image, R.drawable.ic_music, playerCoverImage, 512, true)
                glideBuilt!!.glideBitmap(image, R.drawable.ic_music, mini_cover, 128, true)
                glideBuilt!!.glideBitmap(image, R.drawable.ic_music, queueCoverImg, 128, false)
                mainActivity!!.setDataInNavigation(songName, artistName, image)
                if (image != null && image.isRecycled) {
                    //Don't remove this it will prevent app from crashing if bitmap was trying to recycle from instance
                    image = null
                }
            }
        } else {
            should_refresh_layout = true
        }
        EventBus.getDefault().post(RunnableSyncLyricsEvent())
    }

    private fun showToast(s: String) {
        (requireContext().applicationContext as ApplicationClass).showToast(s)
    }

    @SuppressLint("SetTextI18n")
    fun resetMainPlayerLayout() {
        playerSongNameTv!!.text = "Song"
        playerArtistNameTv!!.text = "Artist"
        mimeTv!!.text = "MP3"
        bitrateTv!!.text = "0 KBPS"
        curPosTv!!.text = "00:00"
        durationTv!!.text = "-00:00"
        mini_name_text!!.text = "Song Name"
        mini_artist_text!!.text = "Artist Name"
        seekBarMain!!.max = 0
        mini_progress!!.max = 0
        seekBarMain!!.progress = 0
        mini_progress!!.progress = 0
        glideBuilt!!.glideBitmap(null, R.drawable.ic_music, playerCoverImage, 512, false)
        glideBuilt!!.glideBitmap(null, R.drawable.ic_music, mini_cover, 128, false)
    }

    @Subscribe
    fun runnableSyncLyrics(event: RunnableSyncLyricsEvent?) {
        if (activeMusic != null) {
            lrcMap = storageUtil!!.loadLyrics(activeMusic!!.id)
            if (lrcMap != null) {
                noLyricsLayout!!.visibility = View.GONE
                lyricsRecyclerView!!.visibility = View.VISIBLE
                lyricsArrayList.clear()
                lyricsArrayList.addAll(lrcMap!!.lyrics)
                setLyricsAdapter()
                lyricsHandler = Handler(Looper.getMainLooper())
                EventBus.getDefault().post(PrepareRunnableEvent())
            } else {
                lyricsRecyclerView!!.visibility = View.GONE
                noLyricsLayout!!.visibility = View.VISIBLE
            }
        }
    }

    // Don't Remove This Event Bus is using this method (It might look unused still DON'T REMOVE
    @Subscribe
    fun prepareRunnable(event: PrepareRunnableEvent?) {
        if (lyricsHandler != null) {
            lyricsRunnable = Runnable {
                var nextStampInMillis = 0
                var currPosInMillis = 0
                if (MainActivity.media_player_service != null) {
                    if (!MainActivity.media_player_service?.isMediaPlaying!!) return@Runnable
                    val nextStamp = getNextStamp(lrcMap)
                    if (nextStamp != "") {
                        nextStampInMillis = MusicHelper.convertToMillis(nextStamp)
                        currPosInMillis = MainActivity.media_player_service?.currentMediaPosition!!
                    }
                }
                if (lrcMap != null) {
                    if (lyricsRecyclerView!!.visibility == View.VISIBLE) {
                        if (!userScrolling) if (lrcMap!!.containsStamp(currentStamp)) {
                            scrollToPosition(lrcMap!!.getIndexAtStamp(currentStamp))
                        }
                    }
                }
                lyricsHandler!!.postDelayed(
                    lyricsRunnable!!,
                    (nextStampInMillis - currPosInMillis).toLong()
                )
            }
            lyricsHandler!!.postDelayed(lyricsRunnable!!, 0)
        }
    }

    @Subscribe
    fun handleMusicProgressUpdate(event: UpdateMusicProgressEvent) {
        mini_progress!!.progress = event.position
        seekBarMain!!.progress = event.position
        val cur = MusicHelper.convertDuration(event.position.toString())
        curPosTv!!.text = cur
    }

    @Subscribe
    fun handleMusicImageUpdate(event: UpdateMusicImageEvent) {
        if (event.shouldDisplayPlayImage) {
            mini_pause!!.setImageResource(R.drawable.ic_play_mini)
            playImg!!.setImageResource(R.drawable.ic_play_main)
        } else {
            mini_pause!!.setImageResource(R.drawable.ic_pause_mini)
            playImg!!.setImageResource(R.drawable.ic_pause_main)
        }
    }

    private val currentStamp: String
        get() {
            var currPosInMillis = 0
            if (MainActivity.media_player_service != null) currPosInMillis =
                MainActivity.media_player_service?.currentMediaPosition!!
            return "[" + MusicHelper.convertDuration(currPosInMillis.toString()) + "]"
        }

    private fun getNextStamp(_lrcMap: LRCMap?): String {
        val curStamp = currentStamp
        var currIndex = -1
        if (_lrcMap != null) {
            currIndex = _lrcMap.getIndexAtStamp(curStamp)
        }
        if (currIndex == -1) return ""
        return if (currIndex == _lrcMap!!.size() - 1) _lrcMap.getStampAt(currIndex) else _lrcMap.getStampAt(
            currIndex + 1
        )
    }

    private fun scrollToPosition(position: Int) {
        val smoothScroller: SmoothScroller = CenterSmoothScroller(context)
        smoothScroller.targetPosition = position
        lm!!.startSmoothScroll(smoothScroller)
    }

    //give the mime type value and it will return extension
    fun getMime(filePath: String?): String? {
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(filePath)
    }

    private fun setLyricsAdapter() {
        lm = LinearLayoutManager(context) // or whatever layout manager you need
        lyricsRecyclerView!!.layoutManager = lm
        lyricsAdapter = MusicLyricsAdapter(context, lyricsArrayList)
        lyricsRecyclerView!!.adapter = lyricsAdapter
    }

    fun setLyricsLayout(music: Music?) {
        val fragmentManager = requireActivity().supportFragmentManager
        val fragment = fragmentManager.findFragmentByTag(MainActivity.ADD_LYRICS_FRAGMENT_TAG)
        if (fragment != null) {
            fragmentManager.popBackStackImmediate()
        }
        val bundle = Bundle()

        //encode music
        val encodedMessage = MusicHelper.encode(music)
        bundle.putSerializable("selectedMusic", encodedMessage)
        val addLyricsFragment = AddLyricsFragment()
        addLyricsFragment.arguments = bundle
        addLyricsFragment.enterTransition =
            TransitionInflater.from(requireContext())
                .inflateTransition(android.R.transition.slide_top)
        if (mainActivity!!.mainPlayerSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
            mainActivity!!.mainPlayerSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(
            R.id.sec_container,
            addLyricsFragment,
            MainActivity.ADD_LYRICS_FRAGMENT_TAG
        )
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun setupQueueBottomSheet() {
        queueRecyclerView!!.layoutManager = linearLayoutManager
        setQueueAdapter()
        queueSheetBehaviour =
            BottomSheetBehavior.from(queueBottomSheet!!) as CustomBottomSheet<View?>
        queueSheetBehaviour!!.isHideable = true
        queueSheetBehaviour!!.skipCollapsed = true
        queueSheetBehaviour!!.state = BottomSheetBehavior.STATE_HIDDEN
        queueSheetBehaviour!!.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                (context as MainActivity?)!!.mainPlayerSheetBehavior?.isEnableCollapse(newState == BottomSheetBehavior.STATE_EXPANDED || newState == BottomSheetBehavior.STATE_DRAGGING)
                if (queueSheetBehaviour!!.state == BottomSheetBehavior.STATE_EXPANDED) {
                    shadowPlayer!!.alpha = 1f
                } else if (queueSheetBehaviour!!.state == BottomSheetBehavior.STATE_HIDDEN) {
                    queueRecyclerView!!.visibility = View.GONE
                    shadowPlayer!!.alpha = 0f
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                bottomSheet.alpha = 0 + (slideOffset + 1f)
                shadowPlayer!!.alpha = 0 + (slideOffset + 1f)
            }
        })
    }

    /**
     * Setting adapter in queue list
     */
    private fun setQueueAdapter() {
        if (musicArrayList != null) {
            queueAdapter = MusicQueueAdapter(requireContext(), musicArrayList, this)
            queueRecyclerView!!.adapter = queueAdapter
            val callback: ItemTouchHelper.Callback = SimpleTouchCallback(queueAdapter)
            itemTouchHelper = ItemTouchHelper(callback)
            itemTouchHelper!!.attachToRecyclerView(queueRecyclerView)
        }
    }

    /**
     * Updating adapter in queue list
     */
    fun updateQueueAdapter(list: ArrayList<Music>) {

        // assign class member id list to new updated _id-list
        musicArrayList = ArrayList(list)
        queueAdapter!!.updateMusicListItems(musicArrayList)
    }

    /**
     * it sets visibility of cover image and lyrics recyclerView
     */
    private fun openLyricsPanel() {
        if (coverCardView!!.visibility == View.VISIBLE) {
            lyricsImg!!.setImageResource(R.drawable.ic_lyrics_off)
            coverCardView!!.visibility = View.GONE
            lyricsRelativeLayout!!.visibility = View.VISIBLE
            lyricsRelativeLayout!!.keepScreenOn = settingsStorage!!.loadKeepScreenOn()
        } else if (coverCardView!!.visibility == View.GONE) {
            lyricsImg!!.setImageResource(R.drawable.ic_lyrics)
            coverCardView!!.visibility = View.VISIBLE
            lyricsRelativeLayout!!.visibility = View.GONE
            lyricsRelativeLayout!!.keepScreenOn = false
        }
    }

    private fun isMusicNotAvailable(currentItem: Music?): Boolean {
        if (!doesMusicExists(currentItem)) {
            Toast.makeText(context, "Song is unavailable", Toast.LENGTH_SHORT).show()
            mainActivity!!.updateAdaptersForRemovedItem()
            return true
        }
        return false
    }

    protected fun doesMusicExists(music: Music?): Boolean {
        val file = File(music!!.path)
        return file.exists()
    }

    private fun optionMenu(music: Music?) {
        if (isMusicNotAvailable(music)) return
        //add a bottom sheet to show music options like set to ringtone ,audio details ,add to playlist etc.
        if (music != null) mainActivity!!.openOptionMenu(music, OptionSheetEnum.MAIN_LIST)
    }

    //region Timer setup
    private fun setTimer() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val customLayout = layoutInflater.inflate(R.layout.timer_dialog, null)
        builder.setView(customLayout)
        builder.setCancelable(true)

        //Initialize Dialogue Box UI Items
        val showTimeText = customLayout.findViewById<TextView>(R.id.timer_time_textview)
        val timerSeekBar = customLayout.findViewById<SeekBar>(R.id.timer_time_seekbar)

        //Set TextView Initially based on seekbar progress
        val finalTextTime = (timerSeekBar.progress + 5).toString()
        showTimeText.text = finalTextTime

        //Update Text based on Seekbar Progress
        timerSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, pos: Int, b: Boolean) {
                var progress = pos
                progress *= 5
                val finalProgress = (progress + 5).toString()
                showTimeText.text = finalProgress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        //Dialogue Box Confirm Button Listener
        builder.setPositiveButton("Start") { dialog: DialogInterface, i: Int ->
            dialog.cancel()
            if (MainActivity.media_player_service != null) {
                MainActivity.media_player_service?.setTimer(timerSeekBar.progress)
            } else {
                showToast("Music service is not running")
            }
        }
        //Show Timer Dialogue Box
        timerDialogue = builder.create()
        timerDialogue?.show()
    }

    @Subscribe
    fun setTimerLiveText(setTimerText: SetTimerText) {
        if (timerTv!!.visibility == View.GONE) {
            timerTv!!.visibility = View.VISIBLE
            timerImg!!.visibility = View.GONE
        }
        timerTv!!.text = setTimerText.liveTimerText
    }

    @Subscribe
    fun setTimerFinished(timerFinished: TimerFinished?) {
        timerTv!!.visibility = View.GONE
        timerImg!!.visibility = View.VISIBLE
    }

    fun addFavorite(storageUtil: StorageUtil, music: Music?, imageView: ImageView?) {
        if (music != null) {
            if (!storageUtil.checkFavourite(music)) {
                storageUtil.saveFavorite(music)
                if (music == activeMusic) {
                    favoriteImg?.setImageResource(R.drawable.ic_favorite)

                    likeAnim?.visibility = View.VISIBLE
                    likeAnim?.playAnimation()
                    likeAnim?.addAnimatorListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {}
                        override fun onAnimationEnd(animation: Animator) {
                            likeAnim?.visibility = View.INVISIBLE
                        }

                        override fun onAnimationCancel(animation: Animator) {}
                        override fun onAnimationRepeat(animation: Animator) {}
                    })
                }
                imageView?.setImageResource(R.drawable.ic_favorite)
            } else if (storageUtil.checkFavourite(music)) {
                storageUtil.removeFavorite(music)
                if (music == activeMusic) {
                    favoriteImg?.setImageResource(R.drawable.ic_favorite_border)
                }
                imageView?.setImageResource(R.drawable.ic_favorite_border)
            }
        }
    }

    private val currentMusic: Music?
        get() {
            val musicList = storageUtil!!.loadQueueList()
            var activeMusic: Music? = null
            val musicIndex: Int = storageUtil!!.loadMusicIndex()
            if (musicList.isNotNullAndNotEmpty()) activeMusic =
                if (musicIndex != -1 && musicIndex < musicList.size) {
                    musicList[musicIndex]
                } else {
                    musicList[0]
                }
            return activeMusic
        }

    private fun <T> Collection<T>?.isNotNullAndNotEmpty(): Boolean {
        return this != null && this.isNotEmpty()
    }

    private fun shuffleList() {
        if (shouldIgnoreClick()) {
            return
        }
        //shuffle list program
        if (!storageUtil!!.loadShuffle()) {
            shuffleImg!!.setImageResource(R.drawable.ic_shuffle)
            storageUtil!!.saveShuffle(true)
            shuffleListAndSave(activeMusic)
        } else if (storageUtil!!.loadShuffle()) {
            shuffleImg!!.setImageResource(R.drawable.ic_shuffle_empty)
            storageUtil!!.saveShuffle(false)
            restoreLastListAndPos(activeMusic)
        }
    }

    private fun shouldIgnoreClick(): Boolean {
        val delay: Long = 600
        if (SystemClock.elapsedRealtime() < lastClickTime + delay) {
            return true
        }
        lastClickTime = SystemClock.elapsedRealtime()
        return false
    }

    private fun shuffleListAndSave(activeMusic: Music?) {
        val musicList = storageUtil?.loadQueueList()
        val musicIndex = storageUtil!!.loadMusicIndex()
        if (musicList != null) {
            try {
                CompletableFuture.supplyAsync({
                    storageUtil!!.saveTempMusicList(musicList)
                    musicList.removeAt(musicIndex)
                    musicList.shuffle()
                    activeMusic?.let { musicList.add(0, it) }
                    storageUtil!!.saveQueueList(musicList)
                    storageUtil!!.saveMusicIndex(0)
                    musicList
                }, executorService).thenAcceptAsync {
                    requireActivity().runOnUiThread {
                        updateQueueAdapter(musicList)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun restoreLastListAndPos(activeMusic: Music?) {
        val tempList = storageUtil?.loadTempMusicList()
        if (tempList != null) {
            // do in background code here
            executorService!!.execute {
                val index: Int = tempList.indexOf(activeMusic)
                if (index != -1) {
                    storageUtil!!.saveMusicIndex(index)
                }
                storageUtil!!.saveQueueList(tempList)
                // post-execute code here
                requireActivity().runOnUiThread { updateQueueAdapter(tempList) }
            }
        }
    }

    private fun repeatFun() {
        //function for music list and only one music repeat and save that state in sharedPreference
        if (storageUtil!!.loadRepeatStatus() == StorageUtil.no_repeat) {
            repeatImg!!.setImageResource(R.drawable.ic_repeat)
            storageUtil!!.saveRepeatStatus(StorageUtil.repeat)
        } else if (storageUtil!!.loadRepeatStatus() == StorageUtil.repeat) {
            repeatImg!!.setImageResource(R.drawable.ic_repeat_one)
            storageUtil!!.saveRepeatStatus(StorageUtil.repeat_one)
        } else if (storageUtil!!.loadRepeatStatus() == StorageUtil.repeat_one) {
            repeatImg!!.setImageResource(R.drawable.ic_repeat_empty)
            storageUtil!!.saveRepeatStatus(StorageUtil.no_repeat)
        }
    }

    private fun openQue() {
        queueRecyclerView!!.visibility = View.VISIBLE
        queueSheetBehaviour!!.state = BottomSheetBehavior.STATE_EXPANDED
        queueBottomSheet!!.alpha = 1f
    }

    /**
     * just for startup
     *
     * @param activeMusic active music
     */
    private fun setButton(activeMusic: Music?) {
        //setting all buttons state from storage on startup
        //for repeat button
        if (storageUtil!!.loadRepeatStatus() == StorageUtil.no_repeat) {
            repeatImg!!.setImageResource(R.drawable.ic_repeat_empty)
        } else if (storageUtil!!.loadRepeatStatus() == StorageUtil.repeat) {
            repeatImg!!.setImageResource(R.drawable.ic_repeat)
        } else if (storageUtil!!.loadRepeatStatus() == StorageUtil.repeat_one) {
            repeatImg!!.setImageResource(R.drawable.ic_repeat_one)
        }

        //for shuffle button
        if (!storageUtil!!.loadShuffle()) {
            shuffleImg!!.setImageResource(R.drawable.ic_shuffle_empty)
        } else if (storageUtil!!.loadShuffle()) {
            shuffleImg!!.setImageResource(R.drawable.ic_shuffle)
        }
        if (activeMusic != null) {
            if (!storageUtil!!.checkFavourite(activeMusic)) {
                favoriteImg!!.setImageResource(R.drawable.ic_favorite_border)
            } else if (storageUtil!!.checkFavourite(activeMusic)) {
                favoriteImg!!.setImageResource(R.drawable.ic_favorite)
            }
        }
        //layout setup ☺
        if (MainActivity.service_bound) {
            if (MainActivity.media_player_service != null) if (MainActivity.media_player_service?.isMediaPlaying!!) {
                MainActivity.media_player_service?.setIcon(PlaybackStatus.PLAYING)
            } else {
                MainActivity.media_player_service?.setIcon(PlaybackStatus.PAUSED)
            }
        }
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            curPosTv!!.text = MusicHelper.convertDuration(progress.toString())
            mini_progress!!.progress = progress
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        //removing handler so we can change position of seekbar
        if (MainActivity.service_bound) {
            if (MainActivity.media_player_service?.seekBarHandler != null) MainActivity.media_player_service?.seekBarHandler!!.removeCallbacks(
                MainActivity.media_player_service?.seekBarRunnable!!
            )
        }
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        //clearing the storage before putting new value
        storageUtil!!.clearMusicLastPos()

        //storing the current position of seekbar in storage so we can access it from services
        storageUtil!!.saveMusicLastPos(seekBar.progress)

        //first checking setting the media seek to current position of seek bar and then setting all data in UI
        if (MainActivity.service_bound) {
            if (MainActivity.media_player_service?.seekBarHandler != null) {
                MainActivity.media_player_service?.seekBarHandler!!.removeCallbacks(MainActivity.media_player_service?.seekBarRunnable!!)
            }
            MainActivity.media_player_service?.seekMediaTo(seekBar.progress)
            if (MainActivity.media_player_service?.isMediaPlaying!!) {
                MainActivity.media_player_service?.buildNotification(PlaybackStatus.PLAYING, 1f)
            } else {
                MainActivity.media_player_service?.buildNotification(PlaybackStatus.PAUSED, 0f)
            }
            MainActivity.media_player_service?.setSeekBar()
        } else {
            mini_progress!!.progress = seekBar.progress
        }
    }

    /**
     * seekTo the position of line you clicked in lyrics
     *
     * @param pos clicked lyric time stamp position
     */
    fun skipToPosition(pos: Int) {
        scrollToPosition(pos)
        val position = MusicHelper.convertToMillis(lrcMap!!.getStampAt(pos))
        if (lrcMap != null) {
            seekBarMain!!.progress = position
            curPosTv!!.text = MusicHelper.convertDuration(position.toString())
            //clearing the storage before putting new value
            storageUtil!!.clearMusicLastPos()

            //storing the current position of seekbar in storage so we can access it from services
            storageUtil!!.saveMusicLastPos(position)

            //first checking setting the media seek to current position of seek bar and then setting all data in UI
            if (MainActivity.service_bound) {
                if (MainActivity.media_player_service?.seekBarHandler != null) {
                    MainActivity.media_player_service?.seekBarHandler!!.removeCallbacks(MainActivity.media_player_service?.seekBarRunnable!!)
                }
                MainActivity.media_player_service?.seekMediaTo(position)
                if (MainActivity.media_player_service?.isMediaPlaying!!) {
                    MainActivity.media_player_service?.buildNotification(PlaybackStatus.PLAYING, 1f)
                } else {
                    MainActivity.media_player_service?.buildNotification(PlaybackStatus.PAUSED, 0f)
                }
                MainActivity.media_player_service?.setSeekBar()
            } else {
                mini_progress!!.progress = position
            }
        }
    }

    fun setPreviousData(activeMusic: Music?) {
        if (activeMusic != null) {
            coroutineScope.launch {
                //image decoder
                var image: Bitmap? = null
                try {
                    MediaMetadataRetriever().use { mediaMetadataRetriever ->
                        mediaMetadataRetriever.setDataSource(activeMusic.path)
                        val art = mediaMetadataRetriever.embeddedPicture
                        try {
                            image = BitmapFactory.decodeByteArray(art, 0, art!!.size)
                        } catch (ignored: Exception) {
                        }
                    }
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                val finalImage = image
                requireActivity().runOnUiThread {
                    EventBus.getDefault().post(SetMainLayoutEvent(activeMusic, finalImage))
                }
            }
        }
        if (activeMusic != null) {
            seekBarMain!!.max = activeMusic.duration.toInt()
            mini_progress!!.max = activeMusic.duration.toInt()
            durationTv!!.text = MusicHelper.convertDuration(activeMusic.duration)
            val resumePosition = storageUtil!!.loadMusicLastPos()
            if (resumePosition != -1) {
                seekBarMain!!.progress = resumePosition
                mini_progress!!.setProgress(resumePosition, true)
                val cur = MusicHelper.convertDuration(resumePosition.toString())
                curPosTv!!.text = cur
            }
        }
    }

    override fun onDragStart(viewHolder: RecyclerView.ViewHolder) {
        if (musicArrayList != null) {
            itemTouchHelper!!.startDrag(viewHolder)
        }
    }

    companion object {
        var playing_same_song = false
    }
}