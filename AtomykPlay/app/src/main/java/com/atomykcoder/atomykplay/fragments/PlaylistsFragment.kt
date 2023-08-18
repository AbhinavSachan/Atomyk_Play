package com.atomykcoder.atomykplay.fragments

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.adapters.PlaylistAdapter
import com.atomykcoder.atomykplay.constants.FragmentTags.FAVORITE_FRAGMENT_TAG
import com.atomykcoder.atomykplay.scripts.GridSpacing
import com.atomykcoder.atomykplay.models.Playlist
import com.atomykcoder.atomykplay.utils.StorageUtil

class PlaylistsFragment : Fragment() {

    private var noPlLayout: View? = null
    private var playlistList: ArrayList<Playlist>? = null
    private var playlistAdapter: PlaylistAdapter? = null

    fun removeItems(playlist: Playlist?) {
        playlistList?.remove(playlist)
    }

    fun updateItems(list: ArrayList<Playlist>?) {
        playlistAdapter?.updateView(list)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_playlists, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.playlist_recycler_view)
        noPlLayout = view.findViewById(R.id.no_pl_layout)
        val favBtn = view.findViewById<View>(R.id.pl_favorites_btn)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_playlists)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener { v: View? -> requireActivity().onBackPressed() }
        favBtn.setOnClickListener { v: View? ->
            val fragmentManager = requireActivity().supportFragmentManager
            val fragment1 = fragmentManager.findFragmentByTag(FAVORITE_FRAGMENT_TAG)
            val transaction = fragmentManager.beginTransaction()
            if (fragment1 == null) {
                val fragment = FavoritesFragment()
                fragment.enterTransition = TransitionInflater.from(requireContext())
                    .inflateTransition(android.R.transition.slide_right)
                transaction.add(R.id.sec_container, fragment, FAVORITE_FRAGMENT_TAG)
                    .addToBackStack(null).commit()
            }
        }
        recyclerView.setHasFixedSize(true)
        val layoutManager = GridLayoutManager(context, 2)
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(GridSpacing(2, 8, false))
        recyclerView.itemAnimator = DefaultItemAnimator()
        playlistList = StorageUtil(requireContext()).allPlaylist

        noPlLayout?.visibility = View.GONE
        if (playlistList!!.isEmpty()) {
            noPlLayout?.visibility = View.VISIBLE
        }
        playlistAdapter = PlaylistAdapter(context, playlistList)
        recyclerView.adapter = playlistAdapter

        return view
    }

    override fun onDestroyView() {
        noPlLayout = null
        playlistList = null
        playlistAdapter = null
        super.onDestroyView()
    }
}