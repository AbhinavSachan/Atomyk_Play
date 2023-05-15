package com.atomykcoder.atomykplay.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.fragment.app.Fragment
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.activities.MainActivity
import com.atomykcoder.atomykplay.dataModels.LRCMap
import com.atomykcoder.atomykplay.events.RunnableSyncLyricsEvent
import com.atomykcoder.atomykplay.helperFunctions.FetchLyrics
import com.atomykcoder.atomykplay.helperFunctions.MusicHelper
import com.atomykcoder.atomykplay.utils.StorageUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.greenrobot.eventbus.EventBus
import java.util.Locale
import java.util.concurrent.Executors

class AddLyricsFragment : Fragment() {
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
    private lateinit var view: View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_lyrics, container, false)
        val decodeMessage =
            (if (arguments != null) arguments!!.getSerializable("selectedMusic") else null) as String?
        val selectedMusic = MusicHelper.decode(decodeMessage)
        editTextLyrics = view.findViewById(R.id.edit_lyrics)
        val saveBtn = view.findViewById<Button>(R.id.btn_save)
        btnFind = view.findViewById(R.id.btn_find)
        progressBar = view.findViewById(R.id.progress_lyrics)
        storageUtil = StorageUtil(requireContext())
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_add_lyric)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            val act = requireActivity() as MainActivity
            act.lyricsListBehavior?.apply {
                if (state == BottomSheetBehavior.STATE_COLLAPSED || state == BottomSheetBehavior.STATE_EXPANDED){
                    state = BottomSheetBehavior.STATE_HIDDEN
                }
            }.run {
                act.onBackPressed()
            }
        }
        name = if (selectedMusic != null) selectedMusic.name else ""
        artist = if (selectedMusic != null) selectedMusic.artist else ""
        musicId = if (selectedMusic != null) selectedMusic.id else ""
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
            storageUtil!!.saveLyrics(musicId, lrcMap)
        } else {
            storageUtil!!.removeLyrics(musicId)
            storageUtil!!.saveLyrics(musicId, lrcMap)
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
        builder.setPositiveButton("OK") { dialog: DialogInterface?, i: Int ->
            btnFind!!.visibility = View.GONE
            fetchLyrics()
        }
        builder.setNegativeButton("Cancel", null)
        dialog = builder.create()
        dialog!!.show()
    }

    private fun fetchLyrics() {
        //show lyrics in bottom sheet
        artistName =
            artistEditText!!.text.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
        songName = nameEditText!!.text.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }


        //clear hashmap prior to retrieving data
        lrcMap.clear()
        val service = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        val fetchLyrics = FetchLyrics()
        try {
            // pre-execute some code here
            fetchLyrics.onPreExecute(progressBar!!)

            // do in background code here
            service.execute {
                var lyricsItems: Bundle? = null
                try {
                    lyricsItems = fetchLyrics.fetchList("$songName $artistName")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val finalLyricsItems = lyricsItems
                handler.post {
                    if (finalLyricsItems == null ||
                        finalLyricsItems.getStringArrayList("titles")!!.isEmpty()
                    ) {
                        Toast.makeText(requireContext(), "No Lyrics Found", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        (requireContext() as MainActivity).openBottomSheet(finalLyricsItems)
                    }
                    fetchLyrics.onPostExecute(progressBar!!)
                    btnFind!!.visibility = View.VISIBLE
                }
            }

            // stopping the background thread (crucial)
            service.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadSelectedLyrics(href: String?) {
        val fetchLyrics = FetchLyrics()
        try {
            val service = Executors.newSingleThreadExecutor()
            val handler = Handler(Looper.getMainLooper())

            // pre-execute some code here
            fetchLyrics.onPreExecute(progressBar!!)
            btnFind!!.visibility = View.GONE

            // do in background code here
            service.execute {
                val unfilteredLyrics = fetchLyrics.fetchTimeStamps(href!!)
                handler.post {
                    fetchLyrics.onPostExecute(progressBar!!)
                    btnFind!!.visibility = View.VISIBLE
                    val filteredLyrics = MusicHelper.splitLyricsByNewLine(unfilteredLyrics)
                    editTextLyrics!!.setText(filteredLyrics)
                    lrcMap.clear()
                    lrcMap.addAll(MusicHelper.getLrcMap(filteredLyrics))
                }
            }
            // stopping the background thread (crucial)
            service.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}