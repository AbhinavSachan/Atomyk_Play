package com.atomykcoder.atomykplay.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.adapters.BeautifyListAdapter;
import com.atomykcoder.atomykplay.adapters.BlockFolderListAdapter;
import com.atomykcoder.atomykplay.classes.ApplicationClass;
import com.atomykcoder.atomykplay.customScripts.LinearLayoutManagerWrapper;
import com.atomykcoder.atomykplay.utils.AndroidUtil;
import com.atomykcoder.atomykplay.utils.StorageUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;


public class SettingsFragment extends Fragment {

    private RadioButton light_theme_btn, dark_theme_btn;
    private SwitchCompat songInfoSwi, artistSwi, extraSwi, autoPlaySwi, keepShuffleSwi, lowerVolSwi, selfStopSwi, keepScreenOnSwi, oneClickSkipSwi, scanAllSwi, beautifySwi, hideNbSwi, hideSbSwi;
    private boolean dark;
    private boolean showInfo, showArtist, showExtra, autoPlay, keepShuffle, lowerVol, selfStop, keepScreenOn, oneClickSkip, beautify, scanAll, hideSb, hideNb;
    private StorageUtil.SettingsStorage settingsStorage;
    private MainActivity mainActivity;
    private final ActivityResultLauncher<Intent> mGetTreeLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    assert result.getData() != null;
                    Uri treeUri = result.getData().getData();

                    String treePath = treeUri.getPath();
                    String pathUri = convertTreeUriToPathUri(treePath);
                    if (pathUri != null) {
                        //save path in blacklist storage
                        settingsStorage.saveInBlackList(pathUri);
                        mainActivity.checkForUpdateList(true);
                    }
                }
            });
    private AlertDialog blacklistDialog, filterDurDialog;
    private BeautifyListAdapter beautifyListAdapter;
    private TextView noTagTV;
    private ArrayList<String> beautifyList;
    private ArrayList<String> replacingTagList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        settingsStorage = new StorageUtil.SettingsStorage(requireContext());
        mainActivity = (MainActivity) requireContext();
        Toolbar toolbar = view.findViewById(R.id.toolbar_settings);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        //saved values
        dark = settingsStorage.loadIsThemeDark();
        showInfo = settingsStorage.loadShowInfo();
        showArtist = settingsStorage.loadShowArtist();
        showExtra = settingsStorage.loadExtraCon();
        autoPlay = settingsStorage.loadAutoPlay();
        keepShuffle = settingsStorage.loadKeepShuffle();
        lowerVol = settingsStorage.loadLowerVol();
        selfStop = settingsStorage.loadSelfStop();
        keepScreenOn = settingsStorage.loadKeepScreenOn();
        oneClickSkip = settingsStorage.loadOneClickSkip();
        beautify = settingsStorage.loadBeautifyName();
        scanAll = settingsStorage.loadScanAllMusic();
        hideSb = settingsStorage.loadIsStatusBarHidden();
        hideNb = settingsStorage.loadIsNavBarHidden();


        //Player settings
        songInfoSwi = view.findViewById(R.id.show_file_info_swi);
        View songInfoLl = view.findViewById(R.id.show_file_info_ll);
        artistSwi = view.findViewById(R.id.show_artist_swi);
        View artistLl = view.findViewById(R.id.show_artist_ll);
        extraSwi = view.findViewById(R.id.show_extra_swi);
        View extraLl = view.findViewById(R.id.show_extra_ll);
        keepScreenOnSwi = view.findViewById(R.id.keep_screen_swi);
        View keepScreenOnLl = view.findViewById(R.id.keep_screen_ll);
        beautifySwi = view.findViewById(R.id.beautify_swi);
        View beautifyLl = view.findViewById(R.id.beautify_ll);
        Button addBeautifyTags = view.findViewById(R.id.beautify_add_tag_btn);
        scanAllSwi = view.findViewById(R.id.should_scan_all_swi);
        View scanAllLl = view.findViewById(R.id.should_scan_all_ll);
        hideSbSwi = view.findViewById(R.id.hide_status_bar_swi);
        View hideSbLl = view.findViewById(R.id.hide_status_bar_ll);
        hideNbSwi = view.findViewById(R.id.hide_nav_bar_swi);
        View hideNbLl = view.findViewById(R.id.hide_nav_bar_ll);

        //audio settings
        autoPlaySwi = view.findViewById(R.id.autoPlay_swi);
        View autoPlayLl = view.findViewById(R.id.autoPlay_ll);
        keepShuffleSwi = view.findViewById(R.id.keep_shuffle_swi);
        View keepShuffleLl = view.findViewById(R.id.keep_shuffle_ll);
        lowerVolSwi = view.findViewById(R.id.lower_vol_swi);
        View lowerVolLl = view.findViewById(R.id.lower_vol_ll);
        selfStopSwi = view.findViewById(R.id.self_stop_swi);
        View selfStopLl = view.findViewById(R.id.self_stop_ll);
        oneClickSkipSwi = view.findViewById(R.id.one_click_skip_swi);
        View oneClickSkipLl = view.findViewById(R.id.one_click_skip_ll);

        //filter settings
        View blackListLl = view.findViewById(R.id.blacklist_ll);
        View filterDurLl = view.findViewById(R.id.filter_dur_ll);

        //theme settings
        RadioGroup radioGroup = view.findViewById(R.id.radio_group_theme);
        light_theme_btn = view.findViewById(R.id.light_button);
        dark_theme_btn = view.findViewById(R.id.dark_button);

        addBeautifyTags.setEnabled(beautify);

        setButtonState();

        //listeners
        songInfoLl.setOnClickListener(v -> songInfoSwi.setChecked(!songInfoSwi.isChecked()));
        artistLl.setOnClickListener(v -> artistSwi.setChecked(!artistSwi.isChecked()));
        extraLl.setOnClickListener(v -> extraSwi.setChecked(!extraSwi.isChecked()));
        autoPlayLl.setOnClickListener(v -> autoPlaySwi.setChecked(!autoPlaySwi.isChecked()));
        keepShuffleLl.setOnClickListener(v -> keepShuffleSwi.setChecked(!keepShuffleSwi.isChecked()));
        lowerVolLl.setOnClickListener(v -> lowerVolSwi.setChecked(!lowerVolSwi.isChecked()));
        selfStopLl.setOnClickListener(v -> selfStopSwi.setChecked(!selfStopSwi.isChecked()));
        keepScreenOnLl.setOnClickListener(v -> keepScreenOnSwi.setChecked(!keepScreenOnSwi.isChecked()));
        oneClickSkipLl.setOnClickListener(v -> oneClickSkipSwi.setChecked(!oneClickSkipSwi.isChecked()));
        beautifyLl.setOnClickListener(v -> beautifySwi.setChecked(!beautifySwi.isChecked()));
        addBeautifyTags.setOnClickListener(v -> showBeatifyTagDialog());
        scanAllLl.setOnClickListener(v -> scanAllSwi.setChecked(!scanAllSwi.isChecked()));
        hideSbLl.setOnClickListener(v -> hideSbSwi.setChecked(!hideSbSwi.isChecked()));
        hideNbLl.setOnClickListener(v -> hideNbSwi.setChecked(!hideNbSwi.isChecked()));
        blackListLl.setOnClickListener(v -> openBlackListDialogue());
        filterDurLl.setOnClickListener(v -> openFilterDurationDialog());

        songInfoSwi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            songInfoSwi.setChecked(isChecked);
            settingsStorage.showInfo(isChecked);
            hideInfo(isChecked);
        });
        artistSwi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            artistSwi.setChecked(isChecked);
            settingsStorage.showArtist(isChecked);
            hideArtist(isChecked);
        });
        extraSwi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            extraSwi.setChecked(isChecked);
            settingsStorage.showExtraCon(isChecked);
            hideExtra(isChecked);
        });
        autoPlaySwi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            autoPlaySwi.setChecked(isChecked);
            settingsStorage.autoPlay(isChecked);
        });
        keepShuffleSwi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            keepShuffleSwi.setChecked(isChecked);
            settingsStorage.keepShuffle(isChecked);
        });
        lowerVolSwi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            lowerVolSwi.setChecked(isChecked);
            settingsStorage.lowerVol(isChecked);
        });
        selfStopSwi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            selfStopSwi.setChecked(isChecked);
            settingsStorage.setSelfStop(isChecked);
        });
        keepScreenOnSwi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            keepScreenOnSwi.setChecked(isChecked);
            settingsStorage.keepScreenOn(isChecked);
        });
        oneClickSkipSwi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            oneClickSkipSwi.setChecked(isChecked);
            settingsStorage.oneClickSkip(isChecked);
        });
        beautifySwi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            beautifySwi.setChecked(isChecked);
            settingsStorage.beautifyName(isChecked);
            if (!isChecked) {
                settingsStorage.clearBeautifyTags();
                settingsStorage.clearReplacingTags();
            }
            addBeautifyTags.setEnabled(isChecked);
            mainActivity.checkForUpdateList(true);
        });
        scanAllSwi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            scanAllSwi.setChecked(isChecked);
            settingsStorage.scanAllMusic(isChecked);
            mainActivity.checkForUpdateList(true);
        });
        hideSbSwi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hideSbSwi.setChecked(isChecked);
            settingsStorage.saveHideStatusBar(isChecked);
            AndroidUtil.Companion.setSystemDrawBehindBars(
                    mainActivity.getWindow(),
                    dark,
                    mainActivity.getDrawer(),
                    Color.TRANSPARENT,
                    mainActivity.getResources().getColor(R.color.player_bg, null),
                    isChecked,
                    hideNb);
            hideSb = isChecked;
        });
        hideNbSwi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hideNbSwi.setChecked(isChecked);
            settingsStorage.saveHideNavBar(isChecked);
            AndroidUtil.Companion.setSystemDrawBehindBars(
                    mainActivity.getWindow(),
                    dark,
                    mainActivity.getDrawer(),
                    Color.TRANSPARENT,
                    mainActivity.getResources().getColor(R.color.player_bg, null),
                    hideSb,
                    isChecked);
            hideNb = isChecked;
        });

        //Check if any radio button is pressed
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            setDark(checkedId);
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (blacklistDialog != null && blacklistDialog.isShowing()) {
            blacklistDialog.dismiss();
        }
        if (filterDurDialog != null && filterDurDialog.isShowing()) {
            filterDurDialog.dismiss();
        }
    }

    private void showBeatifyTagDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        View customLayout = getLayoutInflater().inflate(R.layout.black_list_dialog, null);
        builder.setView(customLayout);
        builder.setCancelable(true);
        ImageView directory_icon = customLayout.findViewById(R.id.blacklist_open_directory_icon);
        RecyclerView recyclerView = customLayout.findViewById(R.id.blacklist_recycler);
        noTagTV = customLayout.findViewById(R.id.text_no_folders);
        TextView heading = customLayout.findViewById(R.id.textview_black_list);
        heading.setText(getString(R.string.add_tag_removal));
        replacingTagList = settingsStorage.getAllReplacingTag();
        beautifyList = settingsStorage.getAllBeautifyTag();

        beautifyListAdapter = new BeautifyListAdapter(beautifyList, requireContext(), replacingTagList);


        if (beautifyList != null) {
            if (beautifyList.isEmpty()) {
                noTagTV.setVisibility(View.VISIBLE);
            } else {
                noTagTV.setVisibility(View.GONE);
            }
        } else {
            noTagTV.setVisibility(View.VISIBLE);
        }
        recyclerView.setLayoutManager(new LinearLayoutManagerWrapper(getContext()));
        recyclerView.setAdapter(beautifyListAdapter);
        // adapter to load in spinner dropdown

        // start "SELECT FOLDER" activity when we click on directory icon
        directory_icon.setOnClickListener(view -> showAddTagDialog());

        builder.setNegativeButton("OK", null);
        AlertDialog dialog;
        dialog = builder.create();
        dialog.show();
    }

    private void showAddTagDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        View customLayout = getLayoutInflater().inflate(R.layout.replacing_tag_layout, null);
        builder.setView(customLayout);
        builder.setCancelable(true);
        EditText editText = customLayout.findViewById(R.id.edit_tag_name);
        EditText editText1 = customLayout.findViewById(R.id.edit_replacing_tag);
        builder.setPositiveButton("OK", (dialog, i) -> {
            String tag = editText.getText().toString();
            String replacingTag = editText1.getText().toString();

            if (TextUtils.isEmpty(tag)) {
                editText.setError("Empty");
            } else {
                settingsStorage.addBeautifyTag(tag);
                settingsStorage.addReplacingTag(replacingTag);
                beautifyList.add(tag);
                replacingTagList.add(replacingTag);
                if (noTagTV != null) {
                    if (noTagTV.getVisibility() == View.VISIBLE) {
                        noTagTV.setVisibility(View.GONE);
                    }
                }
                beautifyListAdapter.notifyItemInserted(beautifyList.indexOf(tag));
                mainActivity.checkForUpdateList(false);
                dialog.cancel();
            }
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog;
        dialog = builder.create();
        dialog.show();
    }

    private void openFilterDurationDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        View customLayout = getLayoutInflater().inflate(R.layout.filter_duration_dialog, null);
        builder.setView(customLayout);
        builder.setCancelable(true);

        TextView filter_time_tv = customLayout.findViewById(R.id.filter_time_textview);
        SeekBar filter_dur_seekbar = customLayout.findViewById(R.id.filter_dur_seekBar);

        //set max in seconds
        filter_dur_seekbar.setMax(120);

        // settings previous data
        int previousDur = settingsStorage.loadFilterDur();
        filter_dur_seekbar.setProgress(previousDur);
        filter_time_tv.setText(String.valueOf(previousDur));

        // seekbar change listener
        filter_dur_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                filter_time_tv.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        builder.setPositiveButton("Filter", (dialog, which) -> {
            settingsStorage.saveFilterDur(Integer.parseInt(filter_time_tv.getText().toString()));
            mainActivity.checkForUpdateList(true);
            dialog.cancel();
        });
        filterDurDialog = builder.create();
        filterDurDialog.show();
    }

    private void openBlackListDialogue() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        View customLayout = getLayoutInflater().inflate(R.layout.black_list_dialog, null);
        builder.setView(customLayout);
        builder.setCancelable(true);

        ImageView directory_icon = customLayout.findViewById(R.id.blacklist_open_directory_icon);
        RecyclerView recyclerView = customLayout.findViewById(R.id.blacklist_recycler);
        TextView textView = customLayout.findViewById(R.id.text_no_folders);

        ArrayList<String> blacklist = settingsStorage.loadBlackList();

        BlockFolderListAdapter adapter = new BlockFolderListAdapter(blacklist, requireContext());


        if (blacklist.isEmpty()) {
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
        recyclerView.setLayoutManager(new LinearLayoutManagerWrapper(getContext()));
        recyclerView.setAdapter(adapter);
        // adapter to load in spinner dropdown

        // start "SELECT FOLDER" activity when we click on directory icon
        directory_icon.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            mGetTreeLauncher.launch(intent);
        });
        builder.setNegativeButton("OK", null);
        //finally show the blacklistDialog
        blacklistDialog = builder.create();
        blacklistDialog.show();
    }

    private void showToast(String s) {
        ((ApplicationClass) requireContext().getApplicationContext()).showToast(s);
    }

    private void hideExtra(boolean v) {
        assert mainActivity.bottomSheetPlayerFragment.miniNext != null;
        if (v) {
            mainActivity.bottomSheetPlayerFragment.miniNext.setVisibility(View.VISIBLE);
        } else {
            mainActivity.bottomSheetPlayerFragment.miniNext.setVisibility(View.GONE);
        }
    }


    private void hideArtist(boolean v) {
        assert mainActivity.bottomSheetPlayerFragment.miniArtistText != null;
        if (v) {
            mainActivity.bottomSheetPlayerFragment.miniArtistText.setVisibility(View.VISIBLE);
        } else {
            mainActivity.bottomSheetPlayerFragment.miniArtistText.setVisibility(View.GONE);
        }
    }

    private void hideInfo(boolean v) {
        assert mainActivity.bottomSheetPlayerFragment.infoLayout != null;
        if (v) {
            mainActivity.bottomSheetPlayerFragment.infoLayout.setVisibility(View.VISIBLE);
        } else {
            mainActivity.bottomSheetPlayerFragment.infoLayout.setVisibility(View.GONE);
        }
    }

    /**
     * set switches when the activity start
     */
    private void setButtonState() {

        songInfoSwi.setChecked(showInfo);
        artistSwi.setChecked(showArtist);
        extraSwi.setChecked(showExtra);
        autoPlaySwi.setChecked(autoPlay);
        keepShuffleSwi.setChecked(keepShuffle);
        lowerVolSwi.setChecked(lowerVol);
        selfStopSwi.setChecked(selfStop);
        keepScreenOnSwi.setChecked(keepScreenOn);
        oneClickSkipSwi.setChecked(oneClickSkip);
        beautifySwi.setChecked(beautify);
        scanAllSwi.setChecked(scanAll);
        hideSbSwi.setChecked(hideSb);
        hideNbSwi.setChecked(hideNb);

        if (!dark) {
            light_theme_btn.setChecked(true);
        } else {
            dark_theme_btn.setChecked(true);
        }
    }

    /**
     * converts tree path uri to usable path uri (WORKS WITH BOTH INTERNAL AND EXTERNAL STORAGE)
     *
     * @param treePath tree path to be converted
     * @return returns usable path uri
     */
    private String convertTreeUriToPathUri(String treePath) {
        if (treePath.contains("/tree/primary:")) {
            treePath = treePath.replace("/tree/primary:", "/storage/emulated/0/");
        } else {
            int colonIndex = treePath.indexOf(":");
            String sdCard;
            String folders;
            try {
                sdCard = treePath.substring(6, colonIndex);
                folders = treePath.substring(colonIndex + 1);
            } catch (IndexOutOfBoundsException e) {
                showToast("Cannot recognise path");
                return null;
            }
            treePath = "/storage/" + sdCard + "/" + folders;
        }
        return treePath;
    }

    @SuppressLint("NonConstantResourceId")
    private void setDark(int checkedId) {
        switch (checkedId) {
            case R.id.light_button -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                dark = false;
            }
            case R.id.dark_button -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                dark = true;
            }
        }
        settingsStorage.saveThemeDark(dark);
    }
}