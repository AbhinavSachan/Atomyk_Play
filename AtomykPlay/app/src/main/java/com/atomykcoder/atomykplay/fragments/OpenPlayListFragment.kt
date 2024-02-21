package com.atomykcoder.atomykplay.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.adapters.OpenPlayListAdapter
import com.atomykcoder.atomykplay.adapters.SimpleTouchCallback
import com.atomykcoder.atomykplay.classes.GlideBuilt
import com.atomykcoder.atomykplay.data.BaseFragment
import com.atomykcoder.atomykplay.data.Music
import com.atomykcoder.atomykplay.events.RemoveFromPlaylistEvent
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener
import com.atomykcoder.atomykplay.models.Playlist
import com.atomykcoder.atomykplay.scripts.LinearLayoutManagerWrapper
import com.google.android.material.appbar.CollapsingToolbarLayout
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

private const val ARG_CURRENT_PLAYLIST = "currentPlaylist"

class OpenPlayListFragment : BaseFragment(), OnDragStartListener {
    companion object {
        @JvmStatic
        fun newInstance(playlist: Playlist?) = OpenPlayListFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_CURRENT_PLAYLIST, playlist)
            }
        }
    }

    private var playlist: Playlist? = null
    private var itemTouchHelper: ItemTouchHelper? = null
    private var openPlayListAdapter: OpenPlayListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.run {
            playlist = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getSerializable(ARG_CURRENT_PLAYLIST, Playlist::class.java)
            } else {
                getSerializable(ARG_CURRENT_PLAYLIST) as Playlist
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_open_play_list, container, false)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        val glideBuilt = GlideBuilt(requireContext().applicationContext)
        val recyclerView = view.findViewById<RecyclerView>(R.id.open_pl_music_recycler)
        val noPlLayout = view.findViewById<View>(R.id.song_not_found_layout_opl)
        val collapsingToolbarLayout =
            view.findViewById<CollapsingToolbarLayout>(R.id.collapse_toolbar_opl)
        val imageView = view.findViewById<ImageView>(R.id.toolbar_cover_opl)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_opl)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener { v: View? -> requireActivity().onBackPressed() }
        recyclerView.setHasFixedSize(true)
        val manager: LinearLayoutManager = LinearLayoutManagerWrapper(context)

        val musicList: ArrayList<Music?>? = playlist?.musicList
        collapsingToolbarLayout.title = playlist?.name
        glideBuilt.loadFromUri(playlist?.coverUri, 0, imageView, 512)

        if (musicList != null) {
            openPlayListAdapter =
                OpenPlayListAdapter(requireContext(), playlist!!.name, musicList, this)
            recyclerView.layoutManager = manager
            recyclerView.adapter = openPlayListAdapter
            noPlLayout.visibility = View.GONE
            if (musicList.isEmpty()) {
                noPlLayout.visibility = View.VISIBLE
            }
            val callback: ItemTouchHelper.Callback = SimpleTouchCallback(openPlayListAdapter)
            itemTouchHelper = ItemTouchHelper(callback)
            itemTouchHelper!!.attachToRecyclerView(recyclerView)
        } else {
            noPlLayout.visibility = View.VISIBLE
        }
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun removeMusicFromList(event: RemoveFromPlaylistEvent) {
        openPlayListAdapter!!.removeItem(event.music)
    }

    override fun onDragStart(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper!!.startDrag(viewHolder)
    }
}