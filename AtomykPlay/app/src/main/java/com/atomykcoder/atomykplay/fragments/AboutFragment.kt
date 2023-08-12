package com.atomykcoder.atomykplay.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.palette.graphics.Palette
import com.atomykcoder.atomykplay.BuildConfig
import com.atomykcoder.atomykplay.R
import com.google.android.material.card.MaterialCardView
import de.hdodenhof.circleimageview.CircleImageView

class AboutFragment : Fragment() {
    private val flatIconUri = Uri.parse("https://www.flaticon.com/")
    private val svgRepoUri = Uri.parse("https://www.svgrepo.com/")
    private val lottieFilesUri = Uri.parse("https://lottiefiles.com/")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_about, container, false)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_about)
        val abh_img = view.findViewById<CircleImageView>(R.id.abhinav_profile)
        val abhinav_card = view.findViewById<MaterialCardView>(R.id.abhinav_card)
        val flatIcon = view.findViewById<TextView>(R.id.flat_icon)
        val svgRepo = view.findViewById<TextView>(R.id.svg_repo)
        val lottie = view.findViewById<TextView>(R.id.lottie_files)
        val verTv = view.findViewById<TextView>(R.id.version_name)
        val rateUs = view.findViewById<TextView>(R.id.rate_us)
        val changelog = view.findViewById<TextView>(R.id.changelog)
        val openLicense = view.findViewById<TextView>(R.id.open_license)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
        val abh = BitmapFactory.decodeResource(resources, R.drawable.abhinav)
        abh_img.setImageBitmap(abh)
        abhinav_card.setCardBackgroundColor(getColor(abh))
        flatIcon.setOnClickListener { v: View? -> openLinkInChrome(flatIconUri) }
        svgRepo.setOnClickListener { v: View? -> openLinkInChrome(svgRepoUri) }
        lottie.setOnClickListener { v: View? -> openLinkInChrome(lottieFilesUri) }
        rateUs.setOnClickListener { v: View? -> rateUs() }
        changelog.setOnClickListener { v: View? -> changes() }
        openLicense.setOnClickListener { v: View? -> changes() }
        verTv.text = BuildConfig.VERSION_NAME
        return view
    }

    private fun changes() {
        Toast.makeText(requireContext(), "Coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun getColor(bitmap: Bitmap): Int {
        val palette = Palette.from(bitmap).generate()
        return palette.getLightVibrantColor(resources.getColor(R.color.secondary_bg, null))
    }

    private fun openLinkInChrome(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            ex.printStackTrace()
        }
    }

    private fun rateUs() {
        val rateIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=" + requireContext().packageName)
        )
        startActivity(rateIntent)
    }
    companion object{
        @JvmStatic
        fun newInstance() = AboutFragment()
    }
}