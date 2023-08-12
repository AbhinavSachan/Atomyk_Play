package com.atomykcoder.atomykplay.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.adapters.OpenPlayListAdapter
import com.atomykcoder.atomykplay.adapters.SimpleTouchCallback
import com.atomykcoder.atomykplay.classes.GlideBuilt
import com.atomykcoder.atomykplay.customScripts.LinearLayoutManagerWrapper
import com.atomykcoder.atomykplay.data.Music
import com.atomykcoder.atomykplay.dataModels.Playlist
import com.atomykcoder.atomykplay.events.RemoveFromPlaylistEvent
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener
import com.google.android.material.appbar.CollapsingToolbarLayout
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class OpenPlayListFragment : Fragment(), OnDragStartListener {

    companion object{
        @JvmStatic
        fun newInstance() = OpenPlayListFragment()
    }
    private var itemTouchHelper: ItemTouchHelper? = null
    private var openPlayListAdapter: OpenPlayListAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_open_play_list, container, false)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        val playlist =
            if (arguments != null) if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireArguments().getSerializable("currentPlaylist", Playlist::class.java)
            } else {
                requireArguments().getSerializable("currentPlaylist") as Playlist
            } else null
        val glideBuilt = GlideBuilt(requireContext())
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
        var musicList: ArrayList<Music?>? = null
        if (playlist != null) {
            musicList = playlist.musicList
            collapsingToolbarLayout.title = playlist.name
            glideBuilt.loadFromUri(playlist.coverUri, 0, imageView, 512)
        }
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