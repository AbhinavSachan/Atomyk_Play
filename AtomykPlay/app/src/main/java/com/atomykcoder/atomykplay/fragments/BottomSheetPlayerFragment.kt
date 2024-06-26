package com.atomykcoder.atomykplay.fragments

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.GONE
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.SeekBar.VISIBLE
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import androidx.transition.TransitionInflater
import com.airbnb.lottie.LottieAnimationView
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.adapters.MusicLyricsAdapter
import com.atomykcoder.atomykplay.adapters.MusicQueueAdapter
import com.atomykcoder.atomykplay.adapters.SimpleTouchCallback
import com.atomykcoder.atomykplay.constants.FragmentTags.ADD_LYRICS_FRAGMENT_TAG
import com.atomykcoder.atomykplay.constants.RepeatModes
import com.atomykcoder.atomykplay.constants.ShuffleModes
import com.atomykcoder.atomykplay.data.BaseFragment
import com.atomykcoder.atomykplay.enums.OptionSheetEnum
import com.atomykcoder.atomykplay.enums.PlaybackStatus
import com.atomykcoder.atomykplay.events.PrepareRunnableEvent
import com.atomykcoder.atomykplay.events.RemoveLyricsHandlerEvent
import com.atomykcoder.atomykplay.events.RunnableSyncLyricsEvent
import com.atomykcoder.atomykplay.events.SetImageInMainPlayer
import com.atomykcoder.atomykplay.events.SetMainLayoutEvent
import com.atomykcoder.atomykplay.events.SetTimerText
import com.atomykcoder.atomykplay.events.StopTextAnim
import com.atomykcoder.atomykplay.events.TimerFinished
import com.atomykcoder.atomykplay.events.UpdateMusicImageEvent
import com.atomykcoder.atomykplay.events.UpdateMusicProgressEvent
import com.atomykcoder.atomykplay.helperFunctions.AudioFileCover
import com.atomykcoder.atomykplay.helperFunctions.GlideApp
import com.atomykcoder.atomykplay.helperFunctions.Logger
import com.atomykcoder.atomykplay.helperFunctions.MusicHelper
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener
import com.atomykcoder.atomykplay.models.LRCMap
import com.atomykcoder.atomykplay.models.Music
import com.atomykcoder.atomykplay.scripts.CenterSmoothScrollScript.CenterSmoothScroller
import com.atomykcoder.atomykplay.scripts.CustomBottomSheet
import com.atomykcoder.atomykplay.scripts.LinearLayoutManagerWrapper
import com.atomykcoder.atomykplay.ui.MainActivity
import com.atomykcoder.atomykplay.utils.AbhinavAnimationUtil.buttonShakeAnimation
import com.atomykcoder.atomykplay.utils.AndroidUtil.pxToDp
import com.atomykcoder.atomykplay.utils.AndroidUtil.toUnscaledBitmap
import com.atomykcoder.atomykplay.utils.StorageUtil
import com.atomykcoder.atomykplay.utils.StorageUtil.SettingsStorage
import com.atomykcoder.atomykplay.utils.loadAlbumArt
import com.atomykcoder.atomykplay.utils.loadImageFromBitmap
import com.atomykcoder.atomykplay.utils.showToast
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
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
import java.lang.ref.WeakReference
import java.util.Locale
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class BottomSheetPlayerFragment : BaseFragment(), OnSeekBarChangeListener, OnDragStartListener,
    OnClickListener {

    private val lyricsArrayList = ArrayList<String>()

    private var queueSheetBehaviour: CustomBottomSheet<View>? = null
    private var miniNext: ImageView? = null
    private var miniArtistText: TextView? = null

    private var miniPlayView: View? = null
    private var playerLayout: View? = null

    private var infoLayout: View? = null

    fun setQueueSheetBehaviour(behaviour: Int) {
        queueSheetBehaviour?.state = behaviour
    }

    fun getQueueSheetBehaviour(): Int? = queueSheetBehaviour?.state

    fun onBottomSheetCollapsed() {
        playerLayout?.visibility = View.INVISIBLE
        miniPlayView?.visibility = View.VISIBLE
        miniPlayView?.alpha = 1f
        playerLayout?.alpha = 0f
    }

    fun onBottomSheetExpanded() {
        playerLayout?.visibility = View.VISIBLE
        miniPlayView?.visibility = View.INVISIBLE
        miniPlayView?.alpha = 0f
        playerLayout?.alpha = 1f
    }

    fun onBottomSheetSliding(slideOffset: Float) {
        miniPlayView?.visibility = View.VISIBLE
        playerLayout?.visibility = View.VISIBLE
        miniPlayView?.alpha = 1 - slideOffset * 35
        playerLayout?.alpha = 0 + slideOffset
    }

    fun setInfoLayoutVisibility(visibility: Int) {
        infoLayout?.visibility = visibility
    }

    fun setMiniArtistVisibility(visibility: Int) {
        miniArtistText?.visibility = visibility
    }

    fun setMiniNextVisibility(visibility: Int) {
        miniNext?.visibility = visibility
    }

    private var queueAdapter: MusicQueueAdapter? = null
    fun clearQueueAdapter() {
        queueAdapter?.clearList()
    }

    /**
     * this method is only to add new music to last queue item
     */
    fun addToQueue(music: Music?) {
        queueAdapter?.updateItemInsertedLast(music)
    }

    /**
     * this method is only to add new music to next queue item
     */
    fun addToNext(music: Music?) {
        queueAdapter?.updateItemInserted(music)
    }

    /**
     * this method is only to add new music to last queue item
     */
    fun addToQueue(music: ArrayList<Music>) {
        queueAdapter?.updateListInsertedLast(music)
    }

    /**
     * this method is only to remove music from queue
     */
    fun removeMusicFromQueue(music: Music?) {
        queueAdapter?.removeItem(music)
    }

    /**
     * this method is only to add new music to next queue item
     */
    fun addToNext(music: ArrayList<Music>) {
        queueAdapter?.updateListInserted(music)
    }

    private var lyricsRunnable: Runnable? = null
    private var lyricsHandler: Handler? = null
    private var miniProgress: LinearProgressIndicator? = null

    private var handler: Handler? = null

    private var miniPause: ImageView? = null

    //main player seekbar
    private var seekBarMain: SeekBar? = null
    private var playImg: ImageView? = null
    private var curPosTv: TextView? = null

    private var miniCover: ImageView? = null

    private var miniNameText: TextView? = null

    private var optionImg: ImageView? = null
    private var musicArrayList: ArrayList<Music>? = null
    private var userScrolling = false
    private var onScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    userScrolling = false
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    userScrolling = true
                }
                super.onScrollStateChanged(recyclerView, newState)
            }

        }
    private var _context: Context? = null
    private val context1: Context?
        get() {
            return _context
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        _context = context
    }

    override fun onDetach() {
        super.onDetach()
        _context = null
    }

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
    private lateinit var storageUtil: StorageUtil
    private var itemTouchHelper: ItemTouchHelper? = null
    private var queueRecyclerView: RecyclerView? = null
    private var lyricsRelativeLayout: View? = null
    private var mainActivity: MainActivity? = null
    private var coverCardView: CardView? = null
    private var lyricsCardView: CardView? = null
    private var lyricsImg: ImageView? = null
    private var queueCoverImg: ImageView? = null
    private var shadowPlayer: View? = null
    private var songNameQueueItem: TextView? = null
    private var artistQueueItem: TextView? = null
    private lateinit var settingsStorage: SettingsStorage
    private var linearLayoutManager: LinearLayoutManager? = null
    private var lyricsAdapter: MusicLyricsAdapter? = null
    private var timerDialogue: Dialog? = null
    private var activeMusic: Music? = null
    private var appPaused = false
    private var shouldRefreshLayout = true
    private var tempColor = 0
    private var lastClickTime: Long = 0
    private lateinit var executorService: ExecutorService
    private val coroutineScope by lazy { CoroutineScope(Dispatchers.IO) }
    private val coroutineScopeMain by lazy { CoroutineScope(Dispatchers.Main) }
    private var gradientTop: View? = null
    private var gradientBottom: View? = null
    private var coverLayout: View? = null

//    private var _binding:FragmentPlayerBinding? = null
//    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init(savedInstanceState)
    }

    private fun init(savedInstanceState: Bundle?) {
        initComponents()
        if (savedInstanceState == null) {
            if (activeMusic == null) {
                activeMusic = currentMusic
            }
        } else {
            activeMusic = MusicHelper.decode(savedInstanceState.getString("activeMusic"))
        }
    }

    private fun initComponents() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        context1?.let {
            storageUtil = StorageUtil(it.applicationContext)
            settingsStorage = SettingsStorage(it.applicationContext)
            linearLayoutManager = LinearLayoutManagerWrapper(it)
            mainActivity = WeakReference(it as MainActivity).get()
        }

        executorService = Executors.newFixedThreadPool(10)

        //StorageUtil initialization

        handler = Handler(Looper.getMainLooper())

        if (activeMusic != null) {
            lrcMap = storageUtil.loadLyrics(activeMusic!!.id)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_player, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Mini player items initializations
        miniPlayView = view.findViewById(R.id.mini_player_layout) //○

        miniCover = view.findViewById(R.id.song_album_cover_mini) //○
        miniArtistText = view.findViewById(R.id.song_artist_name_mini) //○
        miniNameText = view.findViewById(R.id.song_name_mini) //○
        miniNext = view.findViewById(R.id.more_option_i_btn_next) //○
        miniPause = view.findViewById(R.id.more_option_i_btn_play) //○
        miniProgress = view.findViewById(R.id.mini_player_progress) //○

        //Main player items initializations
        playerLayout = view.findViewById(R.id.player_layout) //○
        infoLayout = view.findViewById(R.id.linear_info_layout)
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
        lyricsCardView = view.findViewById(R.id.card_view_for_lyrics)
        lyricsRelativeLayout = view.findViewById(R.id.lyrics_relative_layout)
        gradientTop = view.findViewById(R.id.gradient_top)
        gradientBottom = view.findViewById(R.id.gradient_bottom)
        coverLayout = view.findViewById(R.id.coverRelativeLayout)

        playing_same_song = false

        setAccorToSettings()

        setSizeOfCoverLayout(coverLayout)
        setObserverForBottomSheetHeight(miniPlayView)

        //click listeners on mini player
        //and sending broadcast on click

        //play pause
        miniPause?.setOnClickListener {
            it.buttonShakeAnimation()
            mainActivity?.pausePlayAudio()
        }
        //next on mini player
        miniNext?.setOnClickListener {
            if (!shouldIgnoreClick()) {
                stopAnimText(StopTextAnim())
            }
            mainActivity?.playNextAudio()
        }
        //main player events
        previousImg.setOnClickListener {
            if (!shouldIgnoreClick()) {
                stopAnimText(StopTextAnim())
            }
            mainActivity?.playPreviousAudio()
        }
        nextImg.setOnClickListener {
            if (!shouldIgnoreClick()) {
                stopAnimText(StopTextAnim())
            }
            mainActivity?.playNextAudio()
        }
        playImg?.setOnClickListener {
            it.buttonShakeAnimation()
            mainActivity?.pausePlayAudio()
        }
        queImg.setOnClickListener { openQue() }
        repeatImg?.setOnClickListener { repeatFun() }
        shuffleImg?.setOnClickListener { shuffleList() }
        favoriteImg?.setOnClickListener {
            addFavorite(
                storageUtil, activeMusic, favoriteImg
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
        queueBottomSheet = view.findViewById(R.id.queue_bottom_sheet)
        val button = view.findViewById<TextView>(R.id.btn_add_lyrics)
        lyricsRecyclerView = view.findViewById(R.id.lyrics_recycler_view)
        noLyricsLayout = view.findViewById(R.id.no_lyrics_layout)
        musicArrayList = storageUtil.loadQueueList()

        //lyrics layout related initializations
        lyricsRecyclerView?.addOnScrollListener(onScrollListener)
        button.setOnClickListener { setLyricsLayout(activeMusic) }
        seekBarMain?.setOnSeekBarChangeListener(this)
        miniPlayView?.setOnClickListener {
            val sheet = mainActivity?.mainPlayerSheetBehavior
            if (sheet?.state == BottomSheetBehavior.STATE_COLLAPSED) {
                sheet.setState(BottomSheetBehavior.STATE_EXPANDED)
            } else {
                sheet?.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
        }
        tempColor = resources.getColor(R.color.player_bg, Resources.getSystem().newTheme())
        setupQueueBottomSheet()
    }

    override fun onClick(p0: View?) {

    }

    private fun setObserverForBottomSheetHeight(view: View?) {
        view?.let {
            val vto: ViewTreeObserver = it.viewTreeObserver
            vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val miniPlayerHeight = it.measuredHeight
                    Logger.normalLog("height bf - $miniPlayerHeight")
                    if (miniPlayerHeight != 0) {
                        mainActivity?.setPlayerBottomSheet(miniPlayerHeight)
                    }
                    it.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }
    }

    private fun setSizeOfCoverLayout(view: View?) {
        view?.let {
            val vto: ViewTreeObserver = it.viewTreeObserver
            vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val viewSize: Int = it.width.coerceAtMost(it.height)
                    if (viewSize != 0) {
                        val layoutParams = view.layoutParams as ConstraintLayout.LayoutParams
                        layoutParams.matchConstraintMaxWidth = viewSize
                        layoutParams.matchConstraintMaxHeight = viewSize
                        view.layoutParams = layoutParams
                    }

                    // Remove the listener to avoid multiple callbacks
                    it.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }
    }

    private fun animateText() {
        //for animation
        playerSongNameTv?.isSelected = true
        miniNameText?.isSelected = true
        songNameQueueItem?.isSelected = true

    }

    @Subscribe
    fun stopAnimText(result: StopTextAnim) {
        playerSongNameTv?.isSelected = false
        miniNameText?.isSelected = false
        songNameQueueItem?.isSelected = false
    }

    override fun onResume() {
        super.onResume()
        appPaused = false

        if (mainActivity?.mainPlayerSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
            miniPlayView?.alpha = 0f
            miniPlayView?.visibility = View.INVISIBLE
        } else if (mainActivity?.mainPlayerSheetBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
            playerLayout?.alpha = 0f
            playerLayout?.visibility = View.INVISIBLE
        }
        if (queueSheetBehaviour?.state == BottomSheetBehavior.STATE_EXPANDED) {
            queueRecyclerView?.visibility = View.VISIBLE
        }
        if (shouldRefreshLayout) {
            setButton(activeMusic)
            setPreviousData(activeMusic)
        }
        animateText()
    }

    override fun onPause() {
        super.onPause()
        appPaused = true
        stopAnimText(StopTextAnim())
        if (queueSheetBehaviour!!.state == BottomSheetBehavior.STATE_EXPANDED) {
            queueRecyclerView!!.visibility = View.GONE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val music = MusicHelper.encode(activeMusic)
        outState.putString("activeMusic", music)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
        lyricsRecyclerView?.clearOnScrollListeners()
        executorService.shutdown()

        queueRecyclerView = null
        queueBottomSheet = null
        lyricsRecyclerView = null
        noLyricsLayout = null
        miniPlayView = null
        miniCover = null
        miniArtistText = null
        miniNameText = null
        miniNext = null
        miniPause = null
        miniProgress = null
        playerLayout = null
        infoLayout = null
        playerCoverImage = null
        seekBarMain = null
        repeatImg = null
        playImg = null
        shuffleImg = null
        favoriteImg = null
        timerImg = null
        likeAnim = null
        optionImg = null
        playerSongNameTv = null
        playerArtistNameTv = null
        bitrateTv = null
        mimeTv = null
        durationTv = null
        curPosTv = null
        lyricsImg = null
        timerTv = null
        queueCoverImg = null
        songNameQueueItem = null
        artistQueueItem = null
        shadowPlayer = null
        coverCardView = null
        lyricsCardView = null
        lyricsRelativeLayout = null
        gradientTop = null
        gradientBottom = null
        coverLayout = null
    }

    /**
     * this is for queue item click
     */
    private fun scrollToCurSong() {
        val index = storageUtil.loadMusicIndex()
        linearLayoutManager?.scrollToPositionWithOffset(index, 0)
    }

    /**
     * This method sets elements according to settings values
     */
    private fun setAccorToSettings() {
        if (settingsStorage.loadShowArtist()) {
            miniArtistText!!.visibility = View.VISIBLE
        } else {
            miniArtistText!!.visibility = View.GONE
        }
        if (settingsStorage.loadShowInfo()) {
            infoLayout!!.visibility = View.VISIBLE
        } else {
            infoLayout!!.visibility = View.GONE
        }
        if (settingsStorage.loadExtraCon()) {
            miniNext!!.visibility = View.VISIBLE
        } else {
            miniNext!!.visibility = View.GONE
        }
    }

    @Subscribe
    fun removeLyricsHandler(event: RemoveLyricsHandlerEvent?) {
        if (lyricsHandler != null) {
            lyricsHandler!!.removeCallbacks(lyricsRunnable!!)
        }
    }

    private fun generateThemeColor(image: Bitmap): Int {
        val palette = Palette.Builder(image).generate()
        if (settingsStorage.loadIsThemeDark()) {
            var color = palette.getDarkVibrantColor(
                getColor(R.color.white)
            )
            if (color == getColor(R.color.white)) {
                color = palette.getDarkMutedColor(
                    getColor(R.color.white)
                )
            }
            if (color == getColor(R.color.white)) {
                color = palette.getMutedColor(
                    getColor(R.color.white)
                )
            }
            return color
        } else {
            var color = palette.getLightVibrantColor(
                getColor(R.color.white)
            )
            if (color == getColor(R.color.white)) {
                color = palette.getLightMutedColor(
                    getColor(R.color.white)
                )
            }
            if (color == getColor(R.color.white)) {
                color = palette.getDominantColor(
                    getColor(R.color.white)
                )
            }
            return color
        }
    }

    private fun generatePalette(image: Bitmap): Palette {
        return Palette.Builder(image).generate()
    }

    private fun getColor(id: Int): Int {
        return try {
            if (isAdded) {
                requireContext().resources.getColor(
                    id,
                    Resources.getSystem().newTheme()
                )
            } else -1
        } catch (e: NotFoundException) {
            -1
        } catch (e: IllegalStateException) {
            -1
        }
    }

    private fun setCurColorInPlayer(animateFrom: Int, animateTo: Int) {
        handler?.post {
            val colorAnimation = ValueAnimator.ofArgb(animateFrom, animateTo)
            colorAnimation.duration = 500
            colorAnimation.addUpdateListener { animator: ValueAnimator ->
                val color = animator.animatedValue as Int
                val gradientBg = GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP,
                    intArrayOf(getColor(R.color.white), color)
                )
                handler?.post { playerLayout?.background = gradientBg }
            }
            colorAnimation.start()
        }
    }


    private fun setCurColorInLyricView(animateFrom: Int, animateTo: Int) {
        lyricsCardView?.setCardBackgroundColor(animateTo)
    }

    @Subscribe
    fun setMainPlayerLayout(event: SetMainLayoutEvent) {
        if (playing_same_song) {
            if (activeMusic!!.id == event.activeMusic?.id) {
                animateText()
                return
            }
        }
        activeMusic = event.activeMusic
        if (appPaused) {
            shouldRefreshLayout = true
        } else {
            if (activeMusic != null) {
                shouldRefreshLayout = false
                songName = activeMusic!!.name
                bitrate = activeMusic!!.bitrate
                artistName = activeMusic!!.artist
                duration = activeMusic!!.duration
                mimeType = "MP3"
                var bitrateInNum = 0
                coroutineScope.launch {
                    if (settingsStorage.loadExtraCon()) {
                        try {
                            getMime(activeMusic!!.mimeType)!!.uppercase(Locale.getDefault())
                        } catch (_: Exception) {
                        }
                    }
                    var convertedDur: String? = "00:00"
                    try {
                        convertedDur = MusicHelper.convertDuration(duration!!)
                    } catch (ignored: Exception) {
                    }
                    try {
                        if (bitrate != "") {
                            bitrateInNum = bitrate!!.toInt() / 1000
                        }
                    } catch (_: NumberFormatException) {
                    } catch (_: Exception) {
                    }
                    val finalBitrate = "$bitrateInNum KBPS"
                    coroutineScopeMain.launch {
                        mimeTv!!.text = mimeType
                        durationTv!!.text = convertedDur
                        bitrateTv!!.text = finalBitrate
                    }
                }
                if (!storageUtil.checkFavourite(activeMusic)) {
                    favoriteImg!!.setImageResource(R.drawable.ic_favorite_border)
                } else if (storageUtil.checkFavourite(activeMusic)) {
                    favoriteImg!!.setImageResource(R.drawable.ic_favorite)
                }
                if (storageUtil.loadShuffle() == ShuffleModes.SHUFFLE_MODE_NONE) {
                    shuffleImg!!.setImageResource(R.drawable.ic_shuffle_empty)
                } else if (storageUtil.loadShuffle() == ShuffleModes.SHUFFLE_MODE_ALL) {
                    shuffleImg!!.setImageResource(R.drawable.ic_shuffle)
                }
                try {
                    playerSongNameTv!!.text = songName
                    playerArtistNameTv!!.text = artistName
                    miniNameText!!.text = songName
                    miniArtistText!!.text = artistName
                    queueSheetBehaviour?.let {
                        if (it.state == BottomSheetBehavior.STATE_EXPANDED) {
                            songNameQueueItem!!.text = songName
                            artistQueueItem!!.text = artistName
                        }
                    }
                } catch (ignored: Exception) {
                }
                try {
                    seekBarMain!!.max = duration!!.toInt()
                    miniProgress!!.max = duration!!.toInt()
                } catch (e: NumberFormatException) {
                    showToast()
                }
                animateText()
                mainActivity!!.setDataInNavigation(songName, artistName)
            }
        }
        runnableSyncLyrics(RunnableSyncLyricsEvent())
    }

    @Synchronized
    @Subscribe
    fun setPlayerImages(result: SetImageInMainPlayer) {
        if (playing_same_song) {
            if (activeMusic!!.id == result.activeMusic?.id) {
                return
            }
        }
        if (!appPaused) {
            if (activeMusic != null) {
                val image = result.image

                setImages(result)
                executorService.execute {//image decoder
                    val arrayOfBitmaps = arrayOf<Bitmap?>(null)
                    var art: ByteArray?
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            MediaMetadataRetriever().use { mediaMetadataRetriever ->
                                mediaMetadataRetriever.setDataSource(activeMusic!!.path)
                                art = mediaMetadataRetriever.embeddedPicture
                                mediaMetadataRetriever.release()
                            }
                        } else {
                            val metadataRetriever = MediaMetadataRetriever()
                            metadataRetriever.setDataSource(activeMusic!!.path)
                            art = metadataRetriever.embeddedPicture
                            metadataRetriever.release()
                        }
                    } catch (ignored: IOException) {
                        art = null
                    }
                    try {
                        arrayOfBitmaps[0] =
                            art?.size?.let { BitmapFactory.decodeByteArray(art, 0, it) }
                    } catch (ignored: Exception) {
                        arrayOfBitmaps[0] = null
                    }
                    val finalImage = arrayOfBitmaps[0]
                    (context1 as AppCompatActivity?)?.runOnUiThread {
                        tempColor = if (finalImage != null) {
                            if (image == null) {
                                setImages(SetImageInMainPlayer(finalImage, activeMusic))
                            }
                            val themeColor = generateThemeColor(finalImage)
                            val palette = generatePalette(finalImage)
                            setCurColorInPlayer(tempColor, themeColor)
                            setCurColorInLyricView(tempColor, getColor(R.color.player_bg))
                            themeColor
                        } else {
                            setCurColorInPlayer(tempColor, getColor(R.color.player_bg))
                            setCurColorInLyricView(tempColor, getColor(R.color.player_bg))
                            getColor(R.color.player_bg)
                        }
                    }
                }
            }
        }

    }

    private fun setImages(result: SetImageInMainPlayer) {
        handler?.post {
            var image = result.image
            try {
                activeMusic?.let {
                    playerCoverImage?.loadImageFromBitmap(
                        image,
                        R.drawable.ic_music,
                        512,
                        true
                    )
                    miniCover?.loadImageFromBitmap(
                        image,
                        R.drawable.ic_music,
                        128,
                        true
                    )
                    queueSheetBehaviour?.let { sheet ->
                        if (sheet.state == BottomSheetBehavior.STATE_EXPANDED) {
                            queueCoverImg?.loadImageFromBitmap(
                                image,
                                R.drawable.ic_music,
                                128,
                                false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mainActivity!!.setImageInNavigation(image)
            //Don't remove this it will prevent app from crashing if bitmap was trying to recycle from instance
            image = null
        }
    }

    @SuppressLint("SetTextI18n")
    fun resetMainPlayerLayout() {
        playerSongNameTv!!.text = "Song"
        playerArtistNameTv!!.text = "Artist"
        mimeTv?.text = "MP3"
        bitrateTv?.text = "0 KBPS"
        curPosTv?.text = "00:00"
        durationTv?.text = "-00:00"
        miniNameText?.text = "Song Name"
        miniArtistText?.text = "Artist Name"
        seekBarMain?.max = 0
        miniProgress?.max = 0
        seekBarMain?.progress = 0
        miniProgress?.progress = 0
        if (activity?.isDestroyed == false || !isDetached) {
            playerCoverImage?.loadImageFromBitmap(null, R.drawable.ic_music, 512, false)
            miniCover?.loadImageFromBitmap(null, R.drawable.ic_music, 128, false)
        }
    }

    @Subscribe
    fun runnableSyncLyrics(event: RunnableSyncLyricsEvent?) {
        if (activeMusic != null) {
            lrcMap = storageUtil.loadLyrics(activeMusic!!.id)
            if (lrcMap != null) {
                noLyricsLayout?.visibility = View.GONE
                lyricsRecyclerView?.visibility = View.VISIBLE
                lyricsArrayList.clear()
                lyricsArrayList.addAll(lrcMap!!.lyrics)
                setLyricsAdapter()
                lyricsHandler = Handler(Looper.getMainLooper())
                EventBus.getDefault().post(PrepareRunnableEvent())
            } else {
                lyricsRecyclerView?.visibility = View.GONE
                noLyricsLayout?.visibility = View.VISIBLE
            }
        }
    }

    @Subscribe
    fun prepareRunnable(event: PrepareRunnableEvent?) {
        if (lyricsHandler != null) {
            lyricsRunnable = Runnable {
                var nextStampInMillis = 0
                var currPosInMillis = 0
                val curStamp = currentStamp
                if (MainActivity.media_player_service != null) {
                    if (!MainActivity.media_player_service?.isMediaPlaying!!) return@Runnable
                    val nextStamp = getNextStamp(lrcMap, curStamp)
                    if (nextStamp != "") {
                        nextStampInMillis = MusicHelper.convertToMillis(nextStamp)

                        currPosInMillis = MainActivity.media_player_service?.currentMediaPosition!!

                    }
                }
                if (lrcMap != null) {
                    if (lyricsRecyclerView!!.visibility == View.VISIBLE) {
                        if (!userScrolling) if (lrcMap!!.containsStamp(curStamp)) {
                            scrollToPosition(lrcMap!!.getIndexAtStamp(curStamp))
                        }
                    }
                }
                lyricsHandler?.postDelayed(
                    lyricsRunnable!!,
                    (nextStampInMillis - currPosInMillis).toLong()
                )
            }
            lyricsHandler?.postDelayed(lyricsRunnable!!, 0)
        }
    }

    @Subscribe
    fun handleMusicProgressUpdate(event: UpdateMusicProgressEvent) {
        miniProgress?.progress = event.position
        seekBarMain?.progress = event.position
        val cur = MusicHelper.convertDuration(event.position.toString())
        curPosTv?.text = cur
    }

    @Subscribe
    fun handleMusicImageUpdate(event: UpdateMusicImageEvent) {
        if (event.shouldDisplayPlayImage) {
            miniPause?.setImageResource(R.drawable.ic_play_mini)
            playImg?.setImageResource(R.drawable.ic_play_main)
        } else {
            miniPause?.setImageResource(R.drawable.ic_pause_mini)
            playImg?.setImageResource(R.drawable.ic_pause_main)
        }
    }

    private val currentStamp: String
        get() {
            var currPosInMillis = 0
            if (MainActivity.media_player_service != null) currPosInMillis =
                MainActivity.media_player_service?.currentMediaPosition!!

            return "[" + MusicHelper.convertDurationForLyrics(currPosInMillis.toString()) + "]"
        }

    private fun getNextStamp(_lrcMap: LRCMap?, curStamp: String): String {
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
        if (position == -1) {
            return
        }
        val smoothScroller: SmoothScroller = CenterSmoothScroller(context1)
        smoothScroller.targetPosition = position
        try {
            lm?.startSmoothScroll(smoothScroller)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //give the mime type value and it will return extension
    private fun getMime(filePath: String?): String? {
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(filePath)
    }

    private fun setLyricsAdapter() {
        lm = LinearLayoutManagerWrapper(context1) // or whatever layout manager you need
        lyricsRecyclerView?.layoutManager = lm
        lyricsAdapter = context1?.let { MusicLyricsAdapter(it, lyricsArrayList) }
        if (lyricsArrayList.isEmpty()) {
            noLyricsLayout?.visibility = View.VISIBLE
        } else {
            noLyricsLayout?.visibility = View.GONE
        }
        lyricsRecyclerView?.adapter = lyricsAdapter
    }

    fun setLyricsLayout(music: Music?) {
        val fragmentManager = requireActivity().supportFragmentManager
        val fragment = fragmentManager.findFragmentByTag(ADD_LYRICS_FRAGMENT_TAG)
        if (fragment != null) {
            fragmentManager.popBackStackImmediate()
        }

        //encode music
        val encodedMessage = MusicHelper.encode(music)
        val addLyricsFragment = AddLyricsFragment.newInstance(encodedMessage)
        addLyricsFragment.enterTransition =
            TransitionInflater.from(requireContext())
                .inflateTransition(android.R.transition.slide_top)
        if (mainActivity!!.mainPlayerSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
            mainActivity!!.mainPlayerSheetBehavior?.setState(BottomSheetBehavior.STATE_COLLAPSED)
        }
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(
            R.id.sec_container,
            addLyricsFragment,
            ADD_LYRICS_FRAGMENT_TAG
        )
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun setupQueueBottomSheet() {
        queueRecyclerView!!.layoutManager = linearLayoutManager
        setQueueAdapter()
        queueSheetBehaviour =
            BottomSheetBehavior.from(queueBottomSheet!!) as? CustomBottomSheet<View>
        queueSheetBehaviour!!.isHideable = true
        queueSheetBehaviour!!.skipCollapsed = true
        queueSheetBehaviour!!.state = BottomSheetBehavior.STATE_HIDDEN
        queueSheetBehaviour!!.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                mainActivity?.mainPlayerSheetBehavior?.isEnableCollapse(newState == BottomSheetBehavior.STATE_EXPANDED || newState == BottomSheetBehavior.STATE_DRAGGING)
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
            queueAdapter = context1?.let { MusicQueueAdapter(it, musicArrayList, this) }
            queueAdapter?.setHasStableIds(true)
            queueRecyclerView?.setHasFixedSize(true)
            queueRecyclerView?.setItemViewCacheSize(5)
            queueRecyclerView?.adapter = queueAdapter
            val callback: ItemTouchHelper.Callback = SimpleTouchCallback(queueAdapter)
            itemTouchHelper = ItemTouchHelper(callback)
            itemTouchHelper?.attachToRecyclerView(queueRecyclerView)
        }
    }

    /**
     * Updating adapter in queue list
     */
    fun updateQueueAdapter(list: ArrayList<Music>) {
        // assign class member id list to new updated _id-list
        musicArrayList = ArrayList(list)
        queueAdapter?.updateMusicListItems(musicArrayList!!)
    }

    /**
     * it sets visibility of cover image and lyrics recyclerView
     */
    private fun openLyricsPanel() {
        if (coverCardView!!.visibility == View.VISIBLE) {
            lyricsImg!!.setImageResource(R.drawable.ic_lyrics_off)
            coverCardView!!.visibility = View.GONE
            lyricsCardView!!.visibility = View.VISIBLE
            lyricsRelativeLayout!!.visibility = View.VISIBLE
            lyricsRelativeLayout!!.keepScreenOn = settingsStorage.loadKeepScreenOn()

            lyricsCardView?.let {
                val vto: ViewTreeObserver = it.viewTreeObserver
                vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        val cardViewHeight: Int = it.height
                        // Use the cardViewHeight as needed
                        if (pxToDp(resources, cardViewHeight) < 300) {
                            gradientBottom?.visibility = GONE
                            gradientTop?.visibility = GONE
                        } else {
                            gradientBottom?.visibility = VISIBLE
                            gradientTop?.visibility = VISIBLE
                        }

                        // Remove the listener to avoid multiple callbacks
                        it.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })
            }

        } else if (coverCardView!!.visibility == View.GONE) {
            lyricsImg!!.setImageResource(R.drawable.ic_lyrics)
            coverCardView!!.visibility = View.VISIBLE
            lyricsCardView!!.visibility = View.GONE
            lyricsRelativeLayout!!.visibility = View.GONE
            lyricsRelativeLayout!!.keepScreenOn = false
        }
    }

    private fun isMusicNotAvailable(currentItem: Music?): Boolean {
        if (!doesMusicExists(currentItem)) {
            Toast.makeText(context1, "Song is unavailable", Toast.LENGTH_SHORT).show()
            mainActivity!!.updateAdaptersForRemovedItem()
            return true
        }
        return false
    }

    private fun doesMusicExists(music: Music?): Boolean {
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
        val builder = context1?.let { MaterialAlertDialogBuilder(it) }
        val customLayout = layoutInflater.inflate(R.layout.timer_dialog, null)
        builder?.setView(customLayout)
        builder?.setCancelable(true)

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
        builder?.setPositiveButton("Start") { dialog: DialogInterface, _: Int ->
            dialog.cancel()
            if (MainActivity.media_player_service != null) {
                MainActivity.media_player_service?.setTimer(timerSeekBar.progress)
            } else {
                showToast("Music service is not running")
            }
        }
        //Show Timer Dialogue Box
        timerDialogue = builder?.create()
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
            val musicList = storageUtil.loadQueueList()
            var activeMusic: Music? = null
            val musicIndex: Int = storageUtil.loadMusicIndex()
            if (musicList.isNotNullAndNotEmpty()) activeMusic =
                if (musicIndex != -1 && musicIndex < musicList.size) {
                    musicList[musicIndex]
                } else {
                    musicList[0]
                }
            return activeMusic
        }

    private fun <T> Collection<T>?.isNotNullAndNotEmpty(): Boolean {
        return !this.isNullOrEmpty()
    }

    private fun shuffleList() {
        if (shouldIgnoreClick()) {
            return
        }
        //shuffle list program
        if (storageUtil.loadShuffle() == ShuffleModes.SHUFFLE_MODE_NONE) {
            shuffleImg!!.setImageResource(R.drawable.ic_shuffle)
            storageUtil.saveShuffle(ShuffleModes.SHUFFLE_MODE_ALL)
            shuffleListAndSave(activeMusic)
        } else if (storageUtil.loadShuffle() == ShuffleModes.SHUFFLE_MODE_ALL) {
            shuffleImg!!.setImageResource(R.drawable.ic_shuffle_empty)
            storageUtil.saveShuffle(ShuffleModes.SHUFFLE_MODE_NONE)
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
        val musicList = storageUtil.loadQueueList()
        val musicIndex = storageUtil.loadMusicIndex()
        try {
            CompletableFuture.supplyAsync({
                storageUtil.saveTempMusicList(musicList)
                musicList.removeAt(musicIndex)
                musicList.shuffle()
                activeMusic?.let { musicList.add(0, it) }
                storageUtil.saveQueueList(musicList)
                storageUtil.saveMusicIndex(0)
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

    private fun restoreLastListAndPos(activeMusic: Music?) {
        val tempList = storageUtil.loadTempMusicList()
        executorService.execute {
            val index: Int = tempList.indexOf(activeMusic)
            if (index != -1) {
                storageUtil.saveMusicIndex(index)
            }
            storageUtil.saveQueueList(tempList)
            // post-execute code here
            requireActivity().runOnUiThread { updateQueueAdapter(tempList) }
        }
    }

    private fun repeatFun() {
        //function for music list and only one music repeat and save that state in sharedPreference
        if (storageUtil.loadRepeatStatus() == RepeatModes.REPEAT_MODE_NONE) {
            repeatImg!!.setImageResource(R.drawable.ic_repeat)
            storageUtil.saveRepeatStatus(RepeatModes.REPEAT_MODE_ALL)
        } else if (storageUtil.loadRepeatStatus() == RepeatModes.REPEAT_MODE_ALL) {
            repeatImg!!.setImageResource(R.drawable.ic_repeat_one)
            storageUtil.saveRepeatStatus(RepeatModes.REPEAT_MODE_ONE)
        } else if (storageUtil.loadRepeatStatus() == RepeatModes.REPEAT_MODE_ONE) {
            repeatImg!!.setImageResource(R.drawable.ic_repeat_empty)
            storageUtil.saveRepeatStatus(RepeatModes.REPEAT_MODE_NONE)
        }
    }

    private fun openQue() {
        queueRecyclerView!!.visibility = View.VISIBLE
        activeMusic?.let {
            songNameQueueItem!!.text = it.name
            artistQueueItem!!.text = it.artist
            queueCoverImg?.loadAlbumArt(it.path, R.drawable.ic_music, 128, false)
        }
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
        if (storageUtil.loadRepeatStatus() == RepeatModes.REPEAT_MODE_NONE) {
            repeatImg!!.setImageResource(R.drawable.ic_repeat_empty)
        } else if (storageUtil.loadRepeatStatus() == RepeatModes.REPEAT_MODE_ALL) {
            repeatImg!!.setImageResource(R.drawable.ic_repeat)
        } else if (storageUtil.loadRepeatStatus() == RepeatModes.REPEAT_MODE_ONE) {
            repeatImg!!.setImageResource(R.drawable.ic_repeat_one)
        }

        //for shuffle button
        if (storageUtil.loadShuffle() == ShuffleModes.SHUFFLE_MODE_NONE) {
            shuffleImg!!.setImageResource(R.drawable.ic_shuffle_empty)
        } else if (storageUtil.loadShuffle() == ShuffleModes.SHUFFLE_MODE_ALL) {
            shuffleImg!!.setImageResource(R.drawable.ic_shuffle)
        }
        if (activeMusic != null) {
            if (!storageUtil.checkFavourite(activeMusic)) {
                favoriteImg!!.setImageResource(R.drawable.ic_favorite_border)
            } else if (storageUtil.checkFavourite(activeMusic)) {
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
            miniProgress!!.progress = progress
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        //removing handler so we can change position of seekbar
        if (MainActivity.service_bound) {
            MainActivity.media_player_service?.seekBarRunnable?.let {
                MainActivity.media_player_service?.seekBarHandler?.removeCallbacks(
                    it
                )
            }
        }
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        //clearing the storage before putting new value
        storageUtil.clearMusicLastPos()

        //storing the current position of seekbar in storage so we can access it from services
        storageUtil.saveMusicLastPos(seekBar.progress)

        //first checking setting the media seek to current position of seek bar and then setting all data in UI
        if (MainActivity.service_bound) {
            MainActivity.media_player_service?.seekBarRunnable?.let {
                MainActivity.media_player_service?.seekBarHandler?.removeCallbacks(it)
            }
            MainActivity.media_player_service?.seekMediaTo(seekBar.progress)
            if (MainActivity.media_player_service?.isMediaPlaying!!) {
                MainActivity.media_player_service?.buildNotification(PlaybackStatus.PLAYING, 1f)
            } else {
                MainActivity.media_player_service?.buildNotification(PlaybackStatus.PAUSED, 0f)
            }
            MainActivity.media_player_service?.setSeekBar()
        } else {
            miniProgress?.progress = seekBar.progress
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
            seekBarMain?.progress = position
            curPosTv?.text = MusicHelper.convertDuration(position.toString())
            //clearing the storage before putting new value
            storageUtil.clearMusicLastPos()

            //storing the current position of seekbar in storage so we can access it from services
            storageUtil.saveMusicLastPos(position)

            //first checking setting the media seek to current position of seek bar and then setting all data in UI
            if (MainActivity.service_bound) {
                MainActivity.media_player_service?.seekBarRunnable?.let {
                    MainActivity.media_player_service?.seekBarHandler?.removeCallbacks(it)
                }
                MainActivity.media_player_service?.seekMediaTo(position)
                if (MainActivity.media_player_service?.isMediaPlaying!!) {
                    MainActivity.media_player_service?.buildNotification(PlaybackStatus.PLAYING, 1f)
                } else {
                    MainActivity.media_player_service?.buildNotification(PlaybackStatus.PAUSED, 0f)
                }
                MainActivity.media_player_service?.setSeekBar()
            } else {
                miniProgress?.progress = position
            }
        }
    }

    private fun setPreviousData(activeMusic: Music?) {
        if (activeMusic == null) return
        setMainPlayerLayout(SetMainLayoutEvent(activeMusic))
        var finalImage: Bitmap?
        coroutineScope.launch {
            GlideApp.with(requireContext()).load(AudioFileCover(activeMusic.path))
                .override(512)
                .into(object :
                    CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        finalImage = resource.toUnscaledBitmap()
                        setPlayerImages(SetImageInMainPlayer(finalImage, activeMusic))
                        finalImage = null
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        setPlayerImages(SetImageInMainPlayer(null, activeMusic))
                        finalImage = null
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        setPlayerImages(SetImageInMainPlayer(null, activeMusic))
                        finalImage = null
                    }
                })
        }

        seekBarMain?.max = activeMusic.duration.toInt()
        miniProgress?.max = activeMusic.duration.toInt()
        durationTv?.text = MusicHelper.convertDuration(activeMusic.duration)
        val resumePosition = storageUtil.loadMusicLastPos()
        if (resumePosition != -1) {
            seekBarMain?.progress = resumePosition
            miniProgress?.setProgress(resumePosition, true)
            val cur = MusicHelper.convertDuration(resumePosition.toString())
            curPosTv?.text = cur
        }
    }

    override fun onDragStart(viewHolder: RecyclerView.ViewHolder) {
        if (musicArrayList != null) {
            itemTouchHelper?.startDrag(viewHolder)
        }
    }

    companion object {
        var playing_same_song = false

        @JvmStatic
        fun newInstance() = BottomSheetPlayerFragment()
    }
}