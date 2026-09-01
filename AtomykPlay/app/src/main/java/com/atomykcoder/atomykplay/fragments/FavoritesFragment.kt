package com.atomykcoder.atomykplay.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.adapters.FavoriteListAdapter
import com.atomykcoder.atomykplay.adapters.SimpleTouchCallback
import com.atomykcoder.atomykplay.data.BaseFragment
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener
import com.atomykcoder.atomykplay.models.Music
import com.atomykcoder.atomykplay.scripts.LinearLayoutManagerWrapper
import com.atomykcoder.atomykplay.state.StateHolder
import com.atomykcoder.atomykplay.utils.StorageUtil
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest

class FavoritesFragment : BaseFragment(), OnDragStartListener {
    companion object {
        @JvmStatic
        fun newInstance() = FavoritesFragment()
    }

    private var itemTouchHelper: ItemTouchHelper? = null
    private var playListAdapter: FavoriteListAdapter? = null
    private var storageUtil: StorageUtil? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)
        storageUtil = StorageUtil(requireContext())
        val recyclerView = view.findViewById<RecyclerView>(R.id.favorite_music_recycler)
        val noPlLayout = view.findViewById<View>(R.id.song_not_found_layout_favorite)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_favorites)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
        recyclerView.setHasFixedSize(true)
        val manager: LinearLayoutManager = LinearLayoutManagerWrapper(context)
        val favList: ArrayList<Music> = storageUtil!!.favouriteList

        playListAdapter = FavoriteListAdapter(requireContext(), favList, this)
        recyclerView.layoutManager = manager
        recyclerView.adapter = playListAdapter
        noPlLayout.visibility = View.GONE
        if (favList.isEmpty()) {
            noPlLayout.visibility = View.VISIBLE
        }
        val callback: ItemTouchHelper.Callback = SimpleTouchCallback(playListAdapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper!!.attachToRecyclerView(recyclerView)

        // Keep the list in sync with FavoriteStateManager - e.g. when a song is removed
        // from favorites via the option sheet elsewhere in the app while this list is open.
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            StateHolder.favoriteStateManager.favoriteIds.collectLatest { ids ->
                playListAdapter?.removeItemsNotIn(ids)
            }
        }
        return view
    }

    override fun onDragStart(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper!!.startDrag(viewHolder)
    }

}