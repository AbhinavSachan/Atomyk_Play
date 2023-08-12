package com.atomykcoder.atomykplay.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.atomykcoder.atomykplay.ApplicationClass
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.activities.MainActivity
import com.atomykcoder.atomykplay.adapters.BeautifyListAdapter
import com.atomykcoder.atomykplay.adapters.BlockFolderListAdapter
import com.atomykcoder.atomykplay.customScripts.LinearLayoutManagerWrapper
import com.atomykcoder.atomykplay.utils.AndroidUtil.setSystemDrawBehindBars
import com.atomykcoder.atomykplay.utils.AndroidUtil.setTheme
import com.atomykcoder.atomykplay.utils.StorageUtil.SettingsStorage
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : Fragment(), OnSeekBarChangeListener {

    companion object {
        @JvmStatic
        fun newInstance() = SettingsFragment()
    }

    private lateinit var light_theme_btn: RadioButton
    private lateinit var dark_theme_btn: RadioButton
    private lateinit var songInfoSwi: SwitchCompat
    private lateinit var artistSwi: SwitchCompat
    private lateinit var extraSwi: SwitchCompat
    private lateinit var autoPlaySwi: SwitchCompat
    private lateinit var autoPlayBtSwi: SwitchCompat
    private lateinit var keepShuffleSwi: SwitchCompat
    private lateinit var lowerVolSwi: SwitchCompat
    private lateinit var selfStopSwi: SwitchCompat
    private lateinit var keepScreenOnSwi: SwitchCompat
    private lateinit var oneClickSkipSwi: SwitchCompat
    private lateinit var scanAllSwi: SwitchCompat
    private lateinit var beautifySwi: SwitchCompat
    private lateinit var hideNbSwi: SwitchCompat
    private lateinit var hideSbSwi: SwitchCompat
    private lateinit var enhanceAudioSwi: SwitchCompat
    private lateinit var bassLevelSeekbar: SeekBar
    private lateinit var virtualizerStrengthSeekbar: SeekBar
    private var dark = false
    private var showInfo = false
    private var showArtist = false
    private var showExtra = false
    private var autoPlay = false
    private var autoPlayBt = false
    private var keepShuffle = false
    private var lowerVol = false
    private var selfStop = false
    private var keepScreenOn = false
    private var oneClickSkip = false
    private var beautify = false
    private var scanAll = false
    private var hideSb = false
    private var hideNb = false
    private var enhanceAudio = false
    private var bassLevel = 0
    private var virtualizerStrength = 0
    private var settingsStorage: SettingsStorage? = null
    private var mainActivity: MainActivity? = null
    private val mGetTreeLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            assert(result.data != null)
            val treeUri = result.data!!.data
            val treePath = treeUri!!.path
            val pathUri = convertTreeUriToPathUri(treePath)
            if (pathUri != null) {
                //save path in blacklist storage
                settingsStorage!!.saveInBlackList(pathUri)
                mainActivity!!.checkForUpdateList(true)
            }
        }
    }
    private var blacklistDialog: AlertDialog? = null
    private var filterDurDialog: AlertDialog? = null
    private var beautifyListAdapter: BeautifyListAdapter? = null
    private var noTagTV: TextView? = null
    private var beautifyList: ArrayList<String?>? = null
    private var replacingTagList: ArrayList<String>? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        settingsStorage = SettingsStorage(requireContext())
        mainActivity = requireContext() as MainActivity
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_settings)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener { v: View? -> requireActivity().onBackPressed() }
        //saved values
        dark = settingsStorage!!.loadIsThemeDark()
        showInfo = settingsStorage!!.loadShowInfo()
        showArtist = settingsStorage!!.loadShowArtist()
        showExtra = settingsStorage!!.loadExtraCon()
        autoPlay = settingsStorage!!.loadAutoPlay()
        autoPlayBt = settingsStorage!!.loadAutoPlayBt()
        keepShuffle = settingsStorage!!.loadKeepShuffle()
        lowerVol = settingsStorage!!.loadLowerVol()
        selfStop = settingsStorage!!.loadSelfStop()
        keepScreenOn = settingsStorage!!.loadKeepScreenOn()
        oneClickSkip = settingsStorage!!.loadOneClickSkip()
        beautify = settingsStorage!!.loadBeautifyName()
        scanAll = settingsStorage!!.loadScanAllMusic()
        hideSb = settingsStorage!!.loadIsStatusBarHidden()
        hideNb = settingsStorage!!.loadIsNavBarHidden()
        enhanceAudio = settingsStorage!!.loadEnhanceAudio()
        bassLevel = settingsStorage!!.loadBassLevel()
        virtualizerStrength = settingsStorage!!.loadVirLevel()

        //Player settings
        songInfoSwi = view.findViewById(R.id.show_file_info_swi)
        val songInfoLl = view.findViewById<View>(R.id.show_file_info_ll)
        artistSwi = view.findViewById(R.id.show_artist_swi)
        val artistLl = view.findViewById<View>(R.id.show_artist_ll)
        extraSwi = view.findViewById(R.id.show_extra_swi)
        val extraLl = view.findViewById<View>(R.id.show_extra_ll)
        keepScreenOnSwi = view.findViewById(R.id.keep_screen_swi)
        val keepScreenOnLl = view.findViewById<View>(R.id.keep_screen_ll)
        beautifySwi = view.findViewById(R.id.beautify_swi)
        val beautifyLl = view.findViewById<View>(R.id.beautify_ll)
        val addBeautifyTags = view.findViewById<Button>(R.id.beautify_add_tag_btn)
        scanAllSwi = view.findViewById(R.id.should_scan_all_swi)
        val scanAllLl = view.findViewById<View>(R.id.should_scan_all_ll)
        hideSbSwi = view.findViewById(R.id.hide_status_bar_swi)
        val hideSbLl = view.findViewById<View>(R.id.hide_status_bar_ll)
        hideNbSwi = view.findViewById(R.id.hide_nav_bar_swi)
        val hideNbLl = view.findViewById<View>(R.id.hide_nav_bar_ll)
        enhanceAudioSwi = view.findViewById(R.id.enhanceAudio_swi)
        val enhanceAudioLl = view.findViewById<View>(R.id.enhanceAudio_ll)

        //audio settings
        autoPlaySwi = view.findViewById(R.id.autoPlay_swi)
        val autoPlayLl = view.findViewById<View>(R.id.autoPlay_ll)
        autoPlayBtSwi = view.findViewById(R.id.autoPlayBt_swi)
        val autoPlayBtLl = view.findViewById<View>(R.id.autoPlayBt_ll)
        keepShuffleSwi = view.findViewById(R.id.keep_shuffle_swi)
        val keepShuffleLl = view.findViewById<View>(R.id.keep_shuffle_ll)
        lowerVolSwi = view.findViewById(R.id.lower_vol_swi)
        val lowerVolLl = view.findViewById<View>(R.id.lower_vol_ll)
        selfStopSwi = view.findViewById(R.id.self_stop_swi)
        val selfStopLl = view.findViewById<View>(R.id.self_stop_ll)
        oneClickSkipSwi = view.findViewById(R.id.one_click_skip_swi)
        val oneClickSkipLl = view.findViewById<View>(R.id.one_click_skip_ll)

        //filter settings
        val blackListLl = view.findViewById<View>(R.id.blacklist_ll)
        val filterDurLl = view.findViewById<View>(R.id.filter_dur_ll)

        //theme settings
        val radioGroup = view.findViewById<RadioGroup>(R.id.radio_group_theme)
        light_theme_btn = view.findViewById(R.id.light_button)
        dark_theme_btn = view.findViewById(R.id.dark_button)

        bassLevelSeekbar = view.findViewById(R.id.bass_seek_bar)
        virtualizerStrengthSeekbar = view.findViewById(R.id.virtualizer_seek_bar)
        val levelLayout = view.findViewById<View>(R.id.level_layout)

        setSeekLayout(enhanceAudio, levelLayout)

        addBeautifyTags.isEnabled = beautify
        setButtonState()

        //listeners
        songInfoLl.setOnClickListener { v: View? -> songInfoSwi.isChecked = !songInfoSwi.isChecked }
        artistLl.setOnClickListener { v: View? -> artistSwi.isChecked = !artistSwi.isChecked }
        extraLl.setOnClickListener { v: View? -> extraSwi.isChecked = !extraSwi.isChecked }
        autoPlayLl.setOnClickListener { v: View? -> autoPlaySwi.isChecked = !autoPlaySwi.isChecked }
        autoPlayBtLl.setOnClickListener { v: View? -> autoPlayBtSwi.isChecked = !autoPlayBtSwi.isChecked }
        keepShuffleLl.setOnClickListener { v: View? ->
            keepShuffleSwi.isChecked = !keepShuffleSwi.isChecked
        }
        lowerVolLl.setOnClickListener { v: View? -> lowerVolSwi.isChecked = !lowerVolSwi.isChecked }
        selfStopLl.setOnClickListener { v: View? -> selfStopSwi.isChecked = !selfStopSwi.isChecked }
        keepScreenOnLl.setOnClickListener { v: View? ->
            keepScreenOnSwi.isChecked = !keepScreenOnSwi.isChecked
        }
        oneClickSkipLl.setOnClickListener { v: View? ->
            oneClickSkipSwi.isChecked = !oneClickSkipSwi.isChecked
        }
        beautifyLl.setOnClickListener { v: View? -> beautifySwi.isChecked = !beautifySwi.isChecked }
        enhanceAudioLl.setOnClickListener { v: View? ->
            enhanceAudioSwi.isChecked = !enhanceAudioSwi.isChecked
        }

        addBeautifyTags.setOnClickListener { v: View? -> showBeatifyTagDialog() }
        scanAllLl.setOnClickListener { v: View? -> scanAllSwi.isChecked = !scanAllSwi.isChecked }
        hideSbLl.setOnClickListener { v: View? -> hideSbSwi.isChecked = !hideSbSwi.isChecked }
        hideNbLl.setOnClickListener { v: View? -> hideNbSwi.isChecked = !hideNbSwi.isChecked }
        blackListLl.setOnClickListener { v: View? -> openBlackListDialogue() }
        filterDurLl.setOnClickListener { v: View? -> openFilterDurationDialog() }
        songInfoSwi.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            songInfoSwi.isChecked = isChecked
            settingsStorage!!.showInfo(isChecked)
            hideInfo(isChecked)
        }
        artistSwi.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            artistSwi.isChecked = isChecked
            settingsStorage!!.showArtist(isChecked)
            hideArtist(isChecked)
        }
        extraSwi.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            extraSwi.isChecked = isChecked
            settingsStorage!!.showExtraCon(isChecked)
            hideExtra(isChecked)
        }
        autoPlaySwi.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            autoPlaySwi.isChecked = isChecked
            settingsStorage!!.autoPlay(isChecked)
        }
        autoPlayBtSwi.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            autoPlayBtSwi.isChecked = isChecked
            settingsStorage!!.autoPlayBt(isChecked)
        }
        keepShuffleSwi.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            keepShuffleSwi.isChecked = isChecked
            settingsStorage!!.keepShuffle(isChecked)
        }
        lowerVolSwi.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            lowerVolSwi.isChecked = isChecked
            settingsStorage!!.lowerVol(isChecked)
        }
        selfStopSwi.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            selfStopSwi.isChecked = isChecked
            settingsStorage!!.setSelfStop(isChecked)
        }
        keepScreenOnSwi.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            keepScreenOnSwi.isChecked = isChecked
            settingsStorage!!.keepScreenOn(isChecked)
        }
        oneClickSkipSwi.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            oneClickSkipSwi.isChecked = isChecked
            settingsStorage!!.oneClickSkip(isChecked)
        }
        beautifySwi.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            beautifySwi.isChecked = isChecked
            settingsStorage?.beautifyName(isChecked)
            if (!isChecked) {
                settingsStorage?.clearBeautifyTags()
                settingsStorage?.clearReplacingTags()
            }
            addBeautifyTags.isEnabled = isChecked
            mainActivity?.checkForUpdateList(true)
        }
        scanAllSwi.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            scanAllSwi.isChecked = isChecked
            settingsStorage?.scanAllMusic(isChecked)
            mainActivity?.checkForUpdateList(true)
        }

        bassLevelSeekbar.setOnSeekBarChangeListener(this)
        virtualizerStrengthSeekbar.setOnSeekBarChangeListener(this)
        setSeekbarMax(bassLevelSeekbar)
        setSeekbarMax(virtualizerStrengthSeekbar)

        enhanceAudioSwi.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            enhanceAudioSwi.isChecked = isChecked
            setSeekLayout(isChecked, levelLayout)
            if (isChecked) {
                MainActivity.media_player_service?.apply {
                    initiateEqualizerUtil().run {
                        setBassLevel(settingsStorage!!.loadBassLevel())
                        setVirtualizerStrength(settingsStorage!!.loadVirLevel())
                    }
                }
            } else {
                MainActivity.media_player_service?.disableEqualizerUtil()
            }
            settingsStorage?.enableEnhanceAudio(isChecked)
        }
        hideSbSwi.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            hideSbSwi.isChecked = isChecked
            settingsStorage?.saveHideStatusBar(isChecked)
            setSystemDrawBehindBars(
                mainActivity!!.window,
                dark,
                mainActivity!!.drawer!!,
                Color.TRANSPARENT,
                mainActivity!!.resources.getColor(R.color.player_bg, null),
                isChecked,
                hideNb
            )
            hideSb = isChecked
        }
        hideNbSwi.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            hideNbSwi.isChecked = isChecked
            settingsStorage!!.saveHideNavBar(isChecked)
            setSystemDrawBehindBars(
                mainActivity!!.window,
                dark,
                mainActivity!!.drawer!!,
                Color.TRANSPARENT,
                mainActivity!!.resources.getColor(R.color.player_bg, null),
                hideSb,
                isChecked
            )
            hideNb = isChecked
        }

        //Check if any radio button is pressed
        radioGroup.setOnCheckedChangeListener { group: RadioGroup?, checkedId: Int ->
            setDark(
                checkedId
            )
        }
        return view
    }

    private fun setSeekLayout(enhanceAudio: Boolean, view: View) {
        if (enhanceAudio) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }

    private fun setSeekbarMax(seekBar: SeekBar?) {
        seekBar?.max = 100
    }

    override fun onStop() {
        super.onStop()
        if (blacklistDialog != null && blacklistDialog!!.isShowing) {
            blacklistDialog!!.dismiss()
        }
        if (filterDurDialog != null && filterDurDialog!!.isShowing) {
            filterDurDialog!!.dismiss()
        }
    }

    private fun showBeatifyTagDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val customLayout = layoutInflater.inflate(R.layout.black_list_dialog, null)
        builder.setView(customLayout)
        builder.setCancelable(true)
        val directory_icon =
            customLayout.findViewById<ImageView>(R.id.blacklist_open_directory_icon)
        val recyclerView = customLayout.findViewById<RecyclerView>(R.id.blacklist_recycler)
        noTagTV = customLayout.findViewById(R.id.text_no_folders)
        val heading = customLayout.findViewById<TextView>(R.id.textview_black_list)
        heading.text = getString(R.string.add_tag_removal)
        replacingTagList = settingsStorage?.allReplacingTag
        beautifyList = settingsStorage?.allBeautifyTag as ArrayList<String?>?
        beautifyListAdapter =
            BeautifyListAdapter(beautifyList, requireContext(), replacingTagList!!)
        if (beautifyList != null) {
            if (beautifyList!!.isEmpty()) {
                noTagTV?.visibility = View.VISIBLE
            } else {
                noTagTV?.visibility = View.GONE
            }
        } else {
            noTagTV?.visibility = View.VISIBLE
        }
        recyclerView.layoutManager = LinearLayoutManagerWrapper(context)
        recyclerView.adapter = beautifyListAdapter
        // adapter to load in spinner dropdown

        // start "SELECT FOLDER" activity when we click on directory icon
        directory_icon.setOnClickListener { view: View? -> showAddTagDialog() }
        builder.setNegativeButton("OK", null)
        val dialog = builder.create()
        dialog.show()
    }

    private fun showAddTagDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val customLayout = layoutInflater.inflate(R.layout.replacing_tag_layout, null)
        builder.setView(customLayout)
        builder.setCancelable(true)
        val editText = customLayout.findViewById<EditText>(R.id.edit_tag_name)
        val editText1 = customLayout.findViewById<EditText>(R.id.edit_replacing_tag)
        builder.setPositiveButton("OK") { dialog: DialogInterface, i: Int ->
            val tag = editText.text.toString()
            val replacingTag = editText1.text.toString()
            if (TextUtils.isEmpty(tag)) {
                editText.error = "Empty"
            } else {
                settingsStorage!!.addBeautifyTag(tag)
                settingsStorage!!.addReplacingTag(replacingTag)
                beautifyList!!.add(tag)
                replacingTagList!!.add(replacingTag)
                if (noTagTV != null) {
                    if (noTagTV!!.visibility == View.VISIBLE) {
                        noTagTV!!.visibility = View.GONE
                    }
                }
                beautifyListAdapter!!.notifyItemInserted(beautifyList!!.indexOf(tag))
                mainActivity!!.checkForUpdateList(false)
                dialog.cancel()
            }
        }
        builder.setNegativeButton("Cancel", null)
        val dialog = builder.create()
        dialog.show()
    }

    private fun openFilterDurationDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val customLayout = layoutInflater.inflate(R.layout.filter_duration_dialog, null)
        builder.setView(customLayout)
        builder.setCancelable(true)
        val filter_time_tv = customLayout.findViewById<TextView>(R.id.filter_time_textview)
        val filter_dur_seekbar = customLayout.findViewById<SeekBar>(R.id.filter_dur_seekBar)

        //set max in seconds
        filter_dur_seekbar.max = 120

        // settings previous data
        val previousDur = settingsStorage!!.loadFilterDur()
        filter_dur_seekbar.progress = previousDur
        filter_time_tv.text = previousDur.toString()

        // seekbar change listener
        filter_dur_seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                filter_time_tv.text = seekBar.progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        builder.setPositiveButton("Filter") { dialog: DialogInterface, _: Int ->
            settingsStorage!!.saveFilterDur(filter_time_tv.text.toString().toInt())
            mainActivity!!.checkForUpdateList(true)
            dialog.cancel()
        }
        filterDurDialog = builder.create()
        filterDurDialog!!.show()
    }

    private fun openBlackListDialogue() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val customLayout = layoutInflater.inflate(R.layout.black_list_dialog, null)
        builder.setView(customLayout)
        builder.setCancelable(true)
        val directory_icon =
            customLayout.findViewById<ImageView>(R.id.blacklist_open_directory_icon)
        val recyclerView = customLayout.findViewById<RecyclerView>(R.id.blacklist_recycler)
        val textView = customLayout.findViewById<TextView>(R.id.text_no_folders)
        val blacklist = settingsStorage!!.loadBlackList()
        val adapter = BlockFolderListAdapter(blacklist, requireContext())
        if (blacklist.isEmpty()) {
            textView.visibility = View.VISIBLE
        } else {
            textView.visibility = View.GONE
        }
        recyclerView.layoutManager = LinearLayoutManagerWrapper(context)
        recyclerView.adapter = adapter
        // adapter to load in spinner dropdown

        // start "SELECT FOLDER" activity when we click on directory icon
        directory_icon.setOnClickListener { view: View? ->
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            mGetTreeLauncher.launch(intent)
        }
        builder.setNegativeButton("OK", null)
        //finally show the blacklistDialog
        blacklistDialog = builder.create()
        blacklistDialog!!.show()
    }

    private fun showToast(s: String) {
        (requireContext().applicationContext as ApplicationClass).showToast(s)
    }

    private fun hideExtra(v: Boolean) {
        assert(mainActivity!!.bottomSheetPlayerFragment!!.miniNext != null)
        if (v) {
            mainActivity!!.bottomSheetPlayerFragment!!.miniNext!!.visibility = View.VISIBLE
        } else {
            mainActivity!!.bottomSheetPlayerFragment!!.miniNext!!.visibility = View.GONE
        }
    }

    private fun hideArtist(v: Boolean) {
        assert(mainActivity!!.bottomSheetPlayerFragment!!.miniArtistText != null)
        if (v) {
            mainActivity!!.bottomSheetPlayerFragment!!.miniArtistText!!.visibility =
                View.VISIBLE
        } else {
            mainActivity!!.bottomSheetPlayerFragment!!.miniArtistText!!.visibility = View.GONE
        }
    }

    private fun hideInfo(v: Boolean) {
        assert(mainActivity!!.bottomSheetPlayerFragment!!.infoLayout != null)
        if (v) {
            mainActivity!!.bottomSheetPlayerFragment!!.infoLayout!!.visibility = View.VISIBLE
        } else {
            mainActivity!!.bottomSheetPlayerFragment!!.infoLayout!!.visibility = View.GONE
        }
    }

    /**
     * set switches when the activity start
     */
    private fun setButtonState() {
        songInfoSwi.isChecked = showInfo
        artistSwi.isChecked = showArtist
        extraSwi.isChecked = showExtra
        autoPlaySwi.isChecked = autoPlay
        keepShuffleSwi.isChecked = keepShuffle
        lowerVolSwi.isChecked = lowerVol
        selfStopSwi.isChecked = selfStop
        keepScreenOnSwi.isChecked = keepScreenOn
        oneClickSkipSwi.isChecked = oneClickSkip
        beautifySwi.isChecked = beautify
        scanAllSwi.isChecked = scanAll
        hideSbSwi.isChecked = hideSb
        hideNbSwi.isChecked = hideNb
        enhanceAudioSwi.isChecked = enhanceAudio
        bassLevelSeekbar.progress = bassLevel
        virtualizerStrengthSeekbar.progress = virtualizerStrength
        if (!dark) {
            light_theme_btn.isChecked = true
        } else {
            dark_theme_btn.isChecked = true
        }
    }

    /**
     * converts tree path uri to usable path uri (WORKS WITH BOTH INTERNAL AND EXTERNAL STORAGE)
     *
     * @param treePath tree path to be converted
     * @return returns usable path uri
     */
    private fun convertTreeUriToPathUri(treePath: String?): String? {
        var treePath1 = treePath
        if (treePath1!!.contains("/tree/primary:")) {
            treePath1 = treePath1.replace("/tree/primary:", "/storage/emulated/0/")
        } else {
            val colonIndex = treePath1.indexOf(":")
            val sdCard: String
            val folders: String
            try {
                sdCard = treePath1.substring(6, colonIndex)
                folders = treePath1.substring(colonIndex + 1)
            } catch (e: IndexOutOfBoundsException) {
                showToast("Cannot recognise path")
                return null
            }
            treePath1 = "/storage/$sdCard/$folders"
        }
        return treePath1
    }

    @SuppressLint("NonConstantResourceId")
    private fun setDark(checkedId: Int) {
        when (checkedId) {
            R.id.light_button -> {
                setTheme(requireActivity().window, false)
                dark = false
            }

            R.id.dark_button -> {
                setTheme(requireActivity().window, true)
                dark = true
            }
        }
        settingsStorage!!.saveThemeDark(dark)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (seekBar == bassLevelSeekbar) {
            MainActivity.media_player_service?.setBassLevel(progress)
        } else if (seekBar == virtualizerStrengthSeekbar) {
            MainActivity.media_player_service?.setVirtualizerStrength(progress)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        if (seekBar == bassLevelSeekbar) {
            settingsStorage?.saveBassLevel(seekBar.progress)
        } else if (seekBar == virtualizerStrengthSeekbar) {
            settingsStorage?.saveVirLevel(seekBar.progress)
        }
    }
}