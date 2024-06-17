package com.atomykcoder.atomykplay.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.adapters.MusicMainAdapter
import com.atomykcoder.atomykplay.data.BaseFragment
import com.atomykcoder.atomykplay.models.Music
import com.atomykcoder.atomykplay.repository.MusicRepo
import com.atomykcoder.atomykplay.scripts.LinearLayoutManagerWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale


//Search Layout Fragment for Performing Searches and Presenting Results
class SearchFragment : BaseFragment() {
    companion object {
        @JvmStatic
        fun newInstance() = SearchFragment()
    }

    private lateinit var manager: InputMethodManager
    private lateinit var searchView: EditText

    private var adapter: MusicMainAdapter? = null
    private var radioGroup: RadioGroup? = null
    private var searchedMusicList: ArrayList<Music> = ArrayList()
    private var songButton: RadioButton? = null
    private var albumButton: RadioButton? = null
    private var artistButton: RadioButton? = null
    private var genreButton: RadioButton? = null
    private var isSearching = false
    private var noResultAnim: LottieAnimationView? = null
    private val searchList = MutableLiveData<ArrayList<Music>>()
    private var _context: Context? = null
    private val context1: Context?
        get() {
            return _context
        }

    fun removeItems(selectedItem: Music?) {
        selectedItem?.let { adapter?.removeItem(it) }
    }

    private fun setSearchList(list: ArrayList<Music>) {
        searchList.value = list
    }

    private fun getSearchList(): LiveData<ArrayList<Music>> {
        return searchList
    }

    private val handler = Handler(Looper.getMainLooper())
    private var coroutineScope = CoroutineScope(Dispatchers.Default)
    private var coroutineMainScope = CoroutineScope(Dispatchers.Main)
    override fun onDestroy() {
        super.onDestroy()
        radioGroup = null
        songButton = null
        albumButton = null
        artistButton = null
        genreButton = null
        noResultAnim = null
    }

    private fun searchWithFilters(query: String, dataList: ArrayList<Music>) {

        //Check if any radio button is pressed
        radioGroup!!.setOnCheckedChangeListener { _: RadioGroup?, _: Int ->
            //search if any radio button is pressed
            search(query, dataList)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        _context = context
    }

    override fun onDetach() {
        super.onDetach()
        _context = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search_results, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.search_recycler_view)
        searchedMusicList = ArrayList()

        // Filter Buttons Initialization
        songButton = view.findViewById(R.id.song_button)
        albumButton = view.findViewById(R.id.album_button)
        artistButton = view.findViewById(R.id.artist_button)
        genreButton = view.findViewById(R.id.genre_button)
        radioGroup = view.findViewById(R.id.radio_group)
        noResultAnim = view.findViewById(R.id.noResultAnim)
        manager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        searchView = view.findViewById(R.id.search_view_search)
        val closeSearch = view.findViewById<ImageView>(R.id.close_search_btn)

        searchWithFilters("", searchedMusicList)
        setSearchList(searchedMusicList)
        searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(query: CharSequence, start: Int, before: Int, count: Int) {
                if (!isSearching) {
                    handler.postDelayed({
                        isSearching = true
                        handleSearchEvent(
                            query.toString().lowercase(Locale.getDefault()),
                            MusicRepo.instance?.initialMusicList
                        )
                    }, 200)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        closeSearch.setOnClickListener {
            searchView.clearFocus()
            try {
                manager.hideSoftInputFromWindow(
                    searchView.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            requireActivity().onBackPressed()
        }
        recyclerView.setHasFixedSize(true)
        val layoutManager: LinearLayoutManager = LinearLayoutManagerWrapper(context1)
        recyclerView.layoutManager = layoutManager
        adapter = context1?.let { MusicMainAdapter(it, searchedMusicList) }
        adapter?.setHasStableIds(true)
        recyclerView.setItemViewCacheSize(5)
        recyclerView.adapter = adapter
        songButton?.isChecked = true
        getSearchList().observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                noResultAnim?.visibility = View.GONE
                noResultAnim?.pauseAnimation()
            } else {
                noResultAnim?.visibility = View.VISIBLE
                if (!noResultAnim!!.isAnimating) {
                    noResultAnim?.playAnimation()
                }
            }
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        searchView.post(Runnable {
            searchView.requestFocus()
            try {
                manager.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT)
            } catch (_: Exception) {

            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.clearFocus()
        try {
            manager.hideSoftInputFromWindow(
                searchView.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //Function that adds music to an arraylist which is being used to show music in recycler view
    private fun addMusicToSearchList(song: Music) {
        searchedMusicList.add(song)
    }

    //Function that performs searches and if it finds a match we add that song to our arraylist
    //Function also do cleanup from previous search
    //Function also searches based on filters selected
    @SuppressLint("NotifyDataSetChanged")
    fun search(query: String?, dataList: ArrayList<Music>) {
        coroutineScope.launch {
            cleanUp()

            //get id from selected button
            val id = radioID
            when (id) {
                1 -> if (!TextUtils.isEmpty(query)) {
                    for (song in dataList) {
                        if (song.name.lowercase(Locale.getDefault()).contains(query!!)) {
                            addMusicToSearchList(song)
                        }
                    }
                }

                2 -> if (!TextUtils.isEmpty(query)) {
                    for (song in dataList) {
                        if (song.album.lowercase(Locale.getDefault()).contains(query!!)) {
                            addMusicToSearchList(song)
                        }
                    }
                }

                3 -> if (!TextUtils.isEmpty(query)) {
                    for (song in dataList) {
                        if (song.artist.lowercase(Locale.getDefault()).contains(query!!)) {
                            addMusicToSearchList(song)
                        }
                    }
                }

                4 -> if (!TextUtils.isEmpty(query)) {
                    for (song in dataList) {
                        if (song.genre != null) if (song.genre.lowercase(Locale.getDefault())
                                .contains(
                                    query!!
                                )
                        ) {
                            addMusicToSearchList(song)
                        }
                    }
                }

                else -> if (!TextUtils.isEmpty(query)) {
                    for (song in dataList) {
                        if (song.name.lowercase(Locale.getDefault())
                                .contains(query!!) || song.artist.lowercase(
                                Locale.getDefault()
                            ).contains(
                                query
                            )
                        ) {
                            addMusicToSearchList(song)
                        }
                    }
                }
            }
            coroutineMainScope.launch {
                setNumText(id)
                adapter?.notifyDataSetChanged()
                setSearchList(searchedMusicList)
                isSearching = false
            }
        }
    }

    private val listSize: Int
        get() = searchedMusicList.size

    private fun setNumText(id: Int) {
        val song0 = "Song"
        val album0 = "Album"
        val artist0 = "Artist"
        val genre0 = "Genre"
        val space = " "
        val num: String
        when (id) {
            1 -> {
                num = listSize.toString() + space + "Song"
                songButton!!.text = num
                albumButton!!.text = album0
                artistButton!!.text = artist0
                genreButton!!.text = genre0
            }

            2 -> {
                num = listSize.toString() + space + "Album"
                songButton!!.text = song0
                albumButton!!.text = num
                artistButton!!.text = artist0
                genreButton!!.text = genre0
            }

            3 -> {
                num = listSize.toString() + space + "Artist"
                songButton!!.text = song0
                albumButton!!.text = album0
                artistButton!!.text = num
                genreButton!!.text = genre0
            }

            4 -> {
                num = listSize.toString() + space + "Genre"
                songButton!!.text = song0
                albumButton!!.text = album0
                artistButton!!.text = artist0
                genreButton!!.text = num
            }
        }
    }

    fun handleSearchEvent(query: String, dataList: ArrayList<Music>?) {
        if (dataList != null) {
            search(query, dataList)
            searchWithFilters(query, dataList)
        }
    }

    //get radio get based on which radio button is selected
    private val radioID: Int
        get() {
            var radioId = 0
            if (songButton!!.isChecked) {
                radioId = 1
            } else if (albumButton!!.isChecked) {
                radioId = 2
            } else if (artistButton!!.isChecked) {
                radioId = 3
            } else if (genreButton!!.isChecked) {
                radioId = 4
            }
            return radioId
        }

    //Cleaning up any search results left from last search
    //Refreshing list
    private fun cleanUp() {
        searchedMusicList.clear()
    }
}