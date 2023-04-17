package com.atomykcoder.atomykplay.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.adapters.MusicMainAdapter
import com.atomykcoder.atomykplay.customScripts.LinearLayoutManagerWrapper
import com.atomykcoder.atomykplay.data.Music
import com.atomykcoder.atomykplay.helperFunctions.Logger
import com.atomykcoder.atomykplay.utils.StorageUtil
import com.atomykcoder.atomykplay.utils.StorageUtil.SettingsStorage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class LastAddedFragment : Fragment() {
    private val firstOptionValue = 30
    private val secondOptionValue = 90
    private val thirdOptionValue = 180
    private val fourthOptionValue = 360

    @JvmField
    var adapter: MusicMainAdapter? = null
    private var lastAddedMusicList: ArrayList<Music>? = null
    private var filterDialog: Dialog? = null
    private var songCountTv: TextView? = null
    private var settingsStorage: SettingsStorage? = null
    private var initialMusicList: ArrayList<Music>? = null
    private var progressDialog: ProgressBar? = null
    private val coroutineMainScope = CoroutineScope(Dispatchers.Main)
    private val coroutineDefaultScope = CoroutineScope(Dispatchers.Default)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_last_added, container, false)

        //get references
        val recyclerView = view.findViewById<RecyclerView>(R.id.last_added_recycle_view)
        settingsStorage = SettingsStorage(requireContext())
        val filterButton = view.findViewById<View>(R.id.filter_last_added_btn)
        val backImageView = view.findViewById<ImageView>(R.id.close_filter_btn)
        progressDialog = view.findViewById(R.id.progress_bar_last_added)
        songCountTv = view.findViewById(R.id.count_of_lastAdded)
        // initialize/load music array lists
        initialMusicList = StorageUtil(
            requireContext()
        ).loadInitialList()
        lastAddedMusicList = ArrayList()

        //sort initial music list by date in reverse order
        val locale: Locale = Locale.getDefault()
        initialMusicList?.sortWith(object : Comparator<Music> {
            val dateFormat: DateFormat = SimpleDateFormat("dd MMMM yyyy", locale)
            override fun compare(t1: Music, t2: Music): Int {
                try {
                    val d1 = dateFormat.parse(t1.dateAdded)
                    val d2 = dateFormat.parse(t2.dateAdded)
                    var i = 0
                    if (d1 != null) {
                        i = d1.compareTo(d2)
                    }
                    if (i != 0) return -i
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
                return 0
            }
        })
        //set recyclerview and adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManagerWrapper(context)
        adapter = MusicMainAdapter(requireContext(), lastAddedMusicList)
        recyclerView.adapter = adapter
        loadLastAddedList(settingsStorage?.loadLastAddedDur())
        // back button click listener
        backImageView.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // filter click listener
        filterButton.setOnClickListener { openDialogFilter() }
        return view
    }

    override fun onStop() {
        super.onStop()
        if (filterDialog != null) {
            if (filterDialog!!.isShowing) {
                filterDialog!!.dismiss()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun openDialogFilter() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle("Filter")
        val savedState = settingsStorage?.loadLastAddedDur() ?:0
        val options = arrayOf("Last month", "Last three months", "Last six months", "Last year")
        builder.setSingleChoiceItems(options, savedState) { dialog, which ->
            loadLastAddedList(which)
            dialog.dismiss()
        }

        filterDialog = builder.create()
        filterDialog?.show()
    }

    /**
     * load all last added songs based on selected radio buttons
     *
     * @param i radio id
     */
    private fun loadLastAddedList(i: Int?) {
        when (i) {
            0 -> startThread(firstOptionValue)
            1 -> startThread(secondOptionValue)
            2 -> startThread(thirdOptionValue)
            3 -> startThread(fourthOptionValue)
        }
        settingsStorage!!.setLastAddedDur(i ?:0)
    }

    /**
     * start thread to handle loading music list within range
     *
     * @param maxValue clamp at max value
     */
    private fun startThread(maxValue: Int) {
        progressDialog!!.visibility = View.VISIBLE
        coroutineDefaultScope.launch {
            lastAddedMusicList!!.clear()
            try {
                lastAddedMusicList!!.addAll(getLastAddedMusicList(maxValue.toLong()))
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            coroutineMainScope.launch {
                progressDialog!!.visibility = View.GONE
                val num = lastAddedMusicList?.size?.toString() + " Songs"
                songCountTv!!.text = num
                notifyAdapter()
            }
        }
    }

    @Throws(ParseException::class)
    private fun getLastAddedMusicList(max: Long): ArrayList<Music> {
        val result = ArrayList<Music>()

        // dtf for parsing string to date
        val dateFormat: DateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

        //get "current" date
        val previousDate = dateFormat.parse(dateFormat.format(Date()))

        // subtract days from current date and turn it into an older date
        if (previousDate != null) {
            previousDate.time = previousDate.time - max * 86400 * 1000
        }

        //loop through music list and if a music date is older than given date, then break;
        for (music in initialMusicList!!) {
            val musicDate = dateFormat.parse(music.dateAdded)
            if (musicDate != null && musicDate < previousDate) {
                break
            }
            result.add(music)
        }

        //return music ranging between current date and given older date
        return result
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun notifyAdapter() {
        adapter!!.notifyDataSetChanged()
    }
}