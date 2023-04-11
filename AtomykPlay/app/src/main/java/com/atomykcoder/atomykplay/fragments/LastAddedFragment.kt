package com.atomykcoder.atomykplay.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.adapters.MusicMainAdapter
import com.atomykcoder.atomykplay.customScripts.LinearLayoutManagerWrapper
import com.atomykcoder.atomykplay.data.Music
import com.atomykcoder.atomykplay.utils.StorageUtil
import com.atomykcoder.atomykplay.utils.StorageUtil.SettingsStorage
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
    private var firstRadio: RadioButton? = null
    private var secondRadio: RadioButton? = null
    private var thirdRadio: RadioButton? = null
    private var fourthRadio: RadioButton? = null
    private var lastAddedMusicList: ArrayList<Music>? = null
    private var filterDialog: Dialog? = null
    private var songCountTv: TextView? = null
    private var settingsStorage: SettingsStorage? = null
    private var initialMusicList: ArrayList<Music>? = null
    private var progressDialog: ProgressDialog? = null
    private val coroutineMainScope = CoroutineScope(Dispatchers.Main)
    private val coroutineDefaultScope = CoroutineScope(Dispatchers.Default)


    override fun onDestroy() {
        super.onDestroy()
        firstRadio = null
        secondRadio = null
        thirdRadio = null
        fourthRadio = null
        songCountTv = null
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_last_added, container, false)

        //get references
        val recyclerView = view.findViewById<RecyclerView>(R.id.last_added_recycle_view)
        settingsStorage = SettingsStorage(requireContext())
        val filterButton = view.findViewById<View>(R.id.filter_last_added_btn)
        val backImageView = view.findViewById<ImageView>(R.id.close_filter_btn)
        progressDialog = ProgressDialog(requireContext())
        songCountTv = view.findViewById(R.id.count_of_lastAdded)
        // initialize/load music array lists
        initialMusicList = StorageUtil(
            context
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
        adapter = MusicMainAdapter(context, lastAddedMusicList)
        recyclerView.adapter = adapter
        loadLastAddedList(settingsStorage!!.loadLastAddedDur())
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
        filterDialog = Dialog(requireContext())
        filterDialog!!.setContentView(R.layout.last_added_filter_dialog)
        val savedState = settingsStorage!!.loadLastAddedDur()
        val radioGroup1 = filterDialog!!.findViewById<RadioGroup>(R.id.last_Added_radio_group)
        firstRadio = filterDialog!!.findViewById(R.id.last_added_rb_1)
        secondRadio = filterDialog!!.findViewById(R.id.last_added_rb_2)
        thirdRadio = filterDialog!!.findViewById(R.id.last_added_rb_3)
        fourthRadio = filterDialog!!.findViewById(R.id.last_added_rb_4)
        when (savedState) {
            1 -> firstRadio?.isChecked = true
            2 -> secondRadio?.isChecked = true
            3 -> thirdRadio?.isChecked = true
            4 -> fourthRadio?.isChecked = true
        }
        firstRadio?.text = "Last month"
        secondRadio?.text = "Last three months"
        thirdRadio?.text = "Last six months"
        fourthRadio?.text = "Last year"
        radioGroup1.setOnCheckedChangeListener { _: RadioGroup?, _: Int ->
            val i = radioID
            loadLastAddedList(i)
            filterDialog!!.dismiss()
        }
        filterDialog!!.show()
    }

    /**
     * load all last added songs based on selected radio buttons
     *
     * @param i radio id
     */
    private fun loadLastAddedList(i: Int) {
        when (i) {
            1 -> startThread(firstOptionValue)
            2 -> startThread(secondOptionValue)
            3 -> startThread(thirdOptionValue)
            4 -> startThread(fourthOptionValue)
        }
        settingsStorage!!.setLastAddedDur(i)
    }

    /**
     * start thread to handle loading music list within range
     *
     * @param maxValue clamp at max value
     */
    private fun startThread(maxValue: Int) {
        progressDialog!!.setMessage("Calculating...")
        progressDialog!!.show()
        coroutineDefaultScope.launch {
            lastAddedMusicList!!.clear()
            try {
                lastAddedMusicList!!.addAll(getLastAddedMusicList(maxValue.toLong()))
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            coroutineMainScope.launch {
                progressDialog!!.dismiss()
                val num = lastAddedMusicList!!.size.toString() + " Songs"
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

    /**
     * radio id check
     *
     * @return returns selected radio button id
     */
    private val radioID: Int
        get() {
            var radioId = 0
            if (firstRadio!!.isChecked) {
                radioId = 1
            } else if (secondRadio!!.isChecked) {
                radioId = 2
            } else if (thirdRadio!!.isChecked) {
                radioId = 3
            } else if (fourthRadio!!.isChecked) {
                radioId = 4
            }
            return radioId
        }

    @SuppressLint("NotifyDataSetChanged")
    private fun notifyAdapter() {
        adapter!!.notifyDataSetChanged()
    }
}