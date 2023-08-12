package com.atomykcoder.atomykplay.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.atomykcoder.atomykplay.R

class HelpFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_help, container, false)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_help)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
        return view
    }

    companion object{
        @JvmStatic
        fun newInstance() = HelpFragment()
    }
}