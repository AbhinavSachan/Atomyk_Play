package com.atomykcoder.atomykplay.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.atomykcoder.atomykplay.BuildConfig
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.classes.ApplicationClass
import com.atomykcoder.atomykplay.classes.GlideBuilt
import com.atomykcoder.atomykplay.data.Music
import com.atomykcoder.atomykplay.databinding.FragmentTagEditorBinding
import com.atomykcoder.atomykplay.helperFunctions.CustomMethods.pickImage
import com.atomykcoder.atomykplay.helperFunctions.Logger
import com.atomykcoder.atomykplay.helperFunctions.MusicHelper
import com.atomykcoder.atomykplay.repository.LoadingStatus
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.exceptions.CannotReadException
import org.jaudiotagger.audio.exceptions.CannotWriteException
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.TagException
import org.jaudiotagger.tag.datatype.Artwork
import java.io.File
import java.io.IOException

class TagEditorFragment : Fragment() {
    private lateinit var mediaScannerConnection: MediaScannerConnection
    private val loadingStatus = MutableLiveData<LoadingStatus>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val coroutineMainScope = CoroutineScope(Dispatchers.Main)
    private var glideBuilt: GlideBuilt? = null
    private var b: FragmentTagEditorBinding? = null
    private var music: Music? = null
    private var imageUri: Uri? = null

    // Registers a photo picker activity launcher in single-select mode.
    private val mediaPicker =
        registerForActivityResult<PickVisualMediaRequest?, Uri>(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            uri?.let { setImageUri(it) }
        }
    private val pickIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data != null) {
                    setImageUri(result.data!!.data)
                }
            }
        }
    private var musicUri: Uri? = null

    override fun onDestroyView() {
        super.onDestroyView()
        b = null
    }

    private fun getLoadingStatus(): LiveData<LoadingStatus> {
        return loadingStatus
    }

    private fun setLoadingStatus(status: LoadingStatus) {
        loadingStatus.value = status
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        b = FragmentTagEditorBinding.inflate(inflater, container, false)
        val decodeMessage =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arguments?.getSerializable("currentMusic", String::class.java)
            } else {
                arguments?.getSerializable("currentMusic") as String
            }
        music = MusicHelper.decode(decodeMessage)
        if (music == null) {
            requireActivity().supportFragmentManager.popBackStack()
            showToast(null)
        }
        b!!.toolbarTagEditor.setNavigationIcon(R.drawable.ic_back)
        b!!.toolbarTagEditor.setNavigationOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireActivity().onBackInvokedDispatcher
            } else {
                requireActivity().onBackPressed()
            }
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            b?.editSongGenreTag?.visibility = View.GONE
        }
        glideBuilt = GlideBuilt(requireContext())
        b!!.editSongNameTag.setText(music?.name)
        b!!.editSongArtistTag.setText(music?.artist)
        b!!.editSongAlbumTag.setText(music?.album)
        b!!.editSongYearTag.setText(music?.year)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            b!!.editSongGenreTag.setText(music?.genre)
        }
        val image = arrayOf<Bitmap?>(null)
        coroutineScope.launch {
            //image decoder
            var art: ByteArray? = ByteArray(0)
            try {
                MediaMetadataRetriever().use { mediaMetadataRetriever ->
                    mediaMetadataRetriever.setDataSource(music?.path)
                    art = mediaMetadataRetriever.embeddedPicture
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                image[0] = art?.size?.let { BitmapFactory.decodeByteArray(art, 0, it) }
            } catch (ignored: Exception) {
            }
            coroutineMainScope.launch {
                glideBuilt!!.glideBitmap(
                    image[0], R.drawable.ic_music, b!!.songImageViewTag, 412, false
                )
            }
        }
        b!!.pickCoverTag.setOnClickListener { pickImage(pickIntent, mediaPicker) }
        b!!.tagEditorSaveButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    requestPermissionAndroid11AndAbove()
                } else {
                    saveMusicChanges(music)
                }
            } else {
                if (ContextCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissionAndroidBelow11()
                } else {
                    saveMusicChanges(music)
                }
            }
        }
        setLoader()
        return b!!.root
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private fun requestPermissionAndroid11AndAbove() {
        try {
            val uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
            startActivity(intent)
        } catch (ex: Exception) {
            val intent = Intent()
            intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            startActivity(intent)
        }
    }

    private fun requestPermissionAndroidBelow11() {
        Dexter.withContext(requireContext())
            .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
                    if (multiplePermissionsReport.areAllPermissionsGranted()) {
                        saveMusicChanges(music)
                    } else {
                        showToast("Permissions denied!")
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    list: List<PermissionRequest>, permissionToken: PermissionToken,
                ) {
                    permissionToken.continuePermissionRequest()
                }
            }).check()
    }

    private fun showToast(s: String?) {
        (requireActivity().application as ApplicationClass).showToast(s)
    }

    private fun saveMusicChanges(music: Music?) {
        val newTitle = b!!.editSongNameTag.text.toString().trim()
        val newArtist = b!!.editSongArtistTag.text.toString().trim()
        val newAlbum = b!!.editSongAlbumTag.text.toString().trim()
        val newYear = b!!.editSongYearTag.text.toString().trim()
        var newGenre = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            newGenre = b!!.editSongGenreTag.text.toString().trim()
        }
        setLoadingStatus(LoadingStatus.LOADING)
        val finalNewGenre = newGenre
        coroutineScope.launch {
            try {
                val musicFile = File(music!!.path)
                val f = AudioFileIO.read(musicFile)
                val tag = f.tag

                if (!TextUtils.isEmpty(newTitle)) {
                    tag.setField(FieldKey.TITLE, newTitle)
                } else {
                    val s = tag.getFirst(FieldKey.TITLE)
                    tag.setField(FieldKey.TITLE, s)
                }

                if (!TextUtils.isEmpty(newArtist)) {
                    tag.setField(FieldKey.ARTIST, newArtist)
                } else {
                    val s = "Unknown Artist"
                    tag.setField(FieldKey.ARTIST, s)
                }

                if (!TextUtils.isEmpty(newAlbum)) {
                    tag.setField(FieldKey.ALBUM, newAlbum)
                } else {
                    tag.setField(FieldKey.ALBUM, "Unknown")
                }
                if (!TextUtils.isEmpty(newYear)) {
                    tag.setField(FieldKey.YEAR, newYear)
                } else {
                    tag.setField(FieldKey.YEAR, "Unknown")
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!TextUtils.isEmpty(finalNewGenre)) {
                        tag.setField(FieldKey.GENRE, finalNewGenre)
                    } else {
                        tag.setField(FieldKey.GENRE, "")
                    }
                }
                if (imageUri != null) {
                    val filePath = getRealPathFromImageURI(requireContext(), imageUri)
                    val imageFile = File(filePath)
                    val artwork = Artwork.createArtworkFromFile(imageFile)
                    tag.addField(artwork)
                    tag.setField(artwork)
                }
                f.tag = tag
                f.commit()
                coroutineMainScope.launch {
                    musicUri = Uri.fromFile(musicFile)
                    setLoadingStatus(LoadingStatus.SUCCESS)
                }
            } catch (e: CannotReadException) {
                Logger.normalLog(e.toString())
                coroutineMainScope.launch { setLoadingStatus(LoadingStatus.FAILURE) }
            } catch (e: InvalidAudioFrameException) {
                Logger.normalLog(e.toString())
                coroutineMainScope.launch { setLoadingStatus(LoadingStatus.FAILURE) }
            } catch (e: ReadOnlyFileException) {
                Logger.normalLog(e.toString())
                coroutineMainScope.launch { setLoadingStatus(LoadingStatus.FAILURE) }
            } catch (e: TagException) {
                Logger.normalLog(e.toString())
                coroutineMainScope.launch { setLoadingStatus(LoadingStatus.FAILURE) }
            } catch (e: IOException) {
                Logger.normalLog(e.toString())
                coroutineMainScope.launch { setLoadingStatus(LoadingStatus.FAILURE) }
            } catch (e: CannotWriteException) {
                Logger.normalLog(e.toString())
                coroutineMainScope.launch { setLoadingStatus(LoadingStatus.FAILURE) }
            }
        }
    }

    private fun setLoader() {
        val fragmentManager = requireActivity().supportFragmentManager
        getLoadingStatus().observe(requireActivity()) {
            when (it) {
                LoadingStatus.LOADING -> {
                    b!!.progressBarTag.visibility = View.VISIBLE
                }
                LoadingStatus.SUCCESS -> {
                    showToast("Change's will be applied after restart")
                    musicUri?.let { it1 ->
                        addToMediaStore(it1)
                    }.also {
                        b!!.progressBarTag.visibility = View.GONE
                        fragmentManager.popBackStack()
                    }
                }
                LoadingStatus.FAILURE -> {
                    showToast("Something went wrong")
                    b!!.progressBarTag.visibility = View.GONE
                    fragmentManager.popBackStack()
                }
                else -> {
                    showToast("Something went wrong")
                    b!!.progressBarTag.visibility = View.GONE
                    fragmentManager.popBackStack()
                }
            }
        }
    }

    private fun addToMediaStore(musicUri: Uri) {
        val file = File(musicUri.path.toString())
        val client = object : MediaScannerConnectionClient {
            override fun onScanCompleted(path: String?, uri: Uri?) {
                mediaScannerConnection.disconnect()
            }

            override fun onMediaScannerConnected() {
                mediaScannerConnection.scanFile(file.absolutePath, null)
            }
        }
        mediaScannerConnection = MediaScannerConnection(context, client)
        mediaScannerConnection.connect()
    }

    private fun setImageUri(album_uri: Uri?) {
        imageUri = album_uri
        glideBuilt!!.glide(imageUri.toString(), 0, b!!.songImageViewTag, 512)
    }

    companion object {
        fun getRealPathFromImageURI(context: Context, contentUri: Uri?): String {
            var cursor: Cursor? = null
            return try {
                val proj = arrayOf(MediaStore.Images.Media.DATA)
                cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)
                cursor!!.moveToFirst()
                val columnIndex = cursor.getColumnIndex(proj[0])
                cursor.getString(columnIndex)
            } finally {
                cursor?.close()
            }
        }
    }
}