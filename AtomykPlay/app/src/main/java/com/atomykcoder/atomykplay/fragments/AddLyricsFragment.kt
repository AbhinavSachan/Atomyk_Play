package com.atomykcoder.atomykplay.fragments

import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.constants.FragmentTags.ADD_LYRICS_FRAGMENT_TAG
import com.atomykcoder.atomykplay.data.BaseFragment
import com.atomykcoder.atomykplay.events.RunnableSyncLyricsEvent
import com.atomykcoder.atomykplay.helperFunctions.FetchLyrics
import com.atomykcoder.atomykplay.helperFunctions.MusicHelper
import com.atomykcoder.atomykplay.models.LRCMap
import com.atomykcoder.atomykplay.models.Music
import com.atomykcoder.atomykplay.ui.MainActivity
import com.atomykcoder.atomykplay.utils.StorageUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.lang.ref.WeakReference
import java.util.Locale

private const val ARG_SONG = "selectedMusic"

class AddLyricsFragment : BaseFragment() {
    companion object {
        @JvmStatic
        fun newInstance(song: String) = AddLyricsFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_SONG, song)
            }
        }
    }

    private var selectedMusic: Music? = null
    private val lrcMap = LRCMap()
    private var songName: String? = null
    private var editTextLyrics: EditText? = null
    private var progressBar: ProgressBar? = null
    private var artistName: String? = null
    private var nameEditText: EditText? = null
    private var artistEditText: EditText? = null
    private var storageUtil: StorageUtil? = null
    private var btnFind: Button? = null
    private var dialog: AlertDialog? = null
    private var name: String? = null
    private var artist: String? = null
    private var musicId: String? = null
    private var mainActivity: WeakReference<MainActivity>? = null
    private lateinit var view: View
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val coroutineScopeMain = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val decodeMessage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getSerializable(ARG_SONG, String::class.java)
            } else {
                it.getSerializable(ARG_SONG) as String
            }
            selectedMusic = MusicHelper.decode(decodeMessage)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_lyrics, container, false)
        editTextLyrics = view.findViewById(R.id.edit_lyrics)
        mainActivity = WeakReference(context as? MainActivity)
        val saveBtn = view.findViewById<Button>(R.id.btn_save)
        btnFind = view.findViewById(R.id.btn_find)
        progressBar = view.findViewById(R.id.progress_lyrics)
        storageUtil = StorageUtil(requireContext())
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_add_lyric)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            mainActivity?.get()?.lyricsListBehavior?.apply {
                if (state == BottomSheetBehavior.STATE_COLLAPSED || state == BottomSheetBehavior.STATE_EXPANDED) {
                    state = BottomSheetBehavior.STATE_HIDDEN
                }
            }.run {
                mainActivity?.get()?.onBackPressed()
            }
        }
        name = selectedMusic?.name ?: ""
        artist = selectedMusic?.artist ?: ""
        musicId = selectedMusic?.id ?: ""
        val nameText = view.findViewById<TextView>(R.id.song_name_tv)
        nameText.text = name
        saveBtn.setOnClickListener { saveMusic() }
        btnFind?.setOnClickListener { setDialogBox() }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        btnFind = null
        editTextLyrics = null
    }

    private fun saveMusic() {
        if (lrcMap.isEmpty) {
            if (editTextLyrics!!.text.toString().trim { it <= ' ' } != "") {
                val lyricsSplitByNewLine = editTextLyrics!!.text.toString()
                lrcMap.addAll(MusicHelper.getLrcMap(lyricsSplitByNewLine))
                saveLyrics()
            } else {
                storageUtil?.removeLyrics(musicId)
            }
        } else {
            saveLyrics()
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    private fun saveLyrics() {
        if (storageUtil!!.loadLyrics(musicId) == null) {
            storageUtil?.saveLyrics(musicId, lrcMap)
        } else {
            storageUtil!!.removeLyrics(musicId)
            storageUtil?.saveLyrics(musicId, lrcMap)
        }
        showToast("Saved")
        EventBus.getDefault().post(RunnableSyncLyricsEvent())
    }

    private fun setDialogBox() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val customLayout = layoutInflater.inflate(R.layout.edit_name_dialog_box, null)
        builder.setView(customLayout)
        builder.setCancelable(true)

        //Initialize Dialogue Box UI Items
        nameEditText = customLayout.findViewById(R.id.edit_song_name)
        artistEditText = customLayout.findViewById(R.id.edit_artist_name)
        nameEditText?.setText(name)
        artistEditText?.setText(artist)
        builder.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            btnFind?.visibility = View.GONE
            fetchLyrics()
        }
        builder.setNegativeButton("Cancel", null)
        dialog = builder.create()
        dialog!!.show()
    }

    private fun fetchLyrics() {
        //show lyrics in bottom sheet
        artistName = artistEditText!!.text.toString().lowercase(Locale.getDefault()).trim()
        songName = nameEditText!!.text.toString().lowercase(Locale.getDefault()).trim()


        //clear hashmap prior to retrieving data
        lrcMap.clear()
        val fetchLyrics = FetchLyrics()
        try {
            // pre-execute some code here
            fetchLyrics.onPreExecute(progressBar!!)

            // do in background code here
            coroutineScope.launch {
                var lyricsItems: Bundle? = null
                try {
                    lyricsItems = fetchLyrics.fetchList(songName, artistName)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val finalLyricsItems = lyricsItems
                coroutineScopeMain.launch {
                    if (finalLyricsItems == null ||
                        finalLyricsItems.getStringArrayList("titles")!!.isEmpty()
                    ) {
                        showToast("No Lyrics Found")
                    } else {
                        val fragment =
                            mainActivity?.get()?.supportFragmentManager?.findFragmentByTag(
                                ADD_LYRICS_FRAGMENT_TAG
                            )
                        fragment?.let {
                            mainActivity?.get()?.openBottomSheet(finalLyricsItems)
                        }
                    }
                    fetchLyrics.onPostExecute(progressBar!!)
                    btnFind?.visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadSelectedLyrics(href: String?) {
        val fetchLyrics = FetchLyrics()
        try {
            // pre-execute some code here
            fetchLyrics.onPreExecute(progressBar!!)
            btnFind?.visibility = View.GONE

            // do in background code here
            coroutineScope.launch {
                val unfilteredLyrics = fetchLyrics.fetchTimeStamps(href!!)
                coroutineScopeMain.launch {
                    fetchLyrics.onPostExecute(progressBar!!)
                    btnFind?.visibility = View.VISIBLE
                    val filteredLyrics = MusicHelper.splitLyricsByNewLine(unfilteredLyrics)
                    editTextLyrics!!.setText(filteredLyrics)
                    lrcMap.clear()
                    lrcMap.addAll(MusicHelper.getLrcMap(filteredLyrics))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}