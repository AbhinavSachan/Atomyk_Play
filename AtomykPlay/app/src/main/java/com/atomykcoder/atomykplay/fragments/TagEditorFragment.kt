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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.atomykcoder.atomykplay.BuildConfig
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.activities.MainActivity
import com.atomykcoder.atomykplay.classes.ApplicationClass
import com.atomykcoder.atomykplay.classes.GlideBuilt
import com.atomykcoder.atomykplay.customScripts.ArtworkInfo
import com.atomykcoder.atomykplay.customScripts.AudioTagInfo
import com.atomykcoder.atomykplay.customScripts.TagWriter
import com.atomykcoder.atomykplay.data.Music
import com.atomykcoder.atomykplay.databinding.FragmentTagEditorBinding
import com.atomykcoder.atomykplay.helperFunctions.CustomMethods.pickImage
import com.atomykcoder.atomykplay.helperFunctions.Logger
import com.atomykcoder.atomykplay.helperFunctions.MusicHelper
import com.atomykcoder.atomykplay.repository.LoadingStatus
import com.atomykcoder.atomykplay.utils.MusicUtil
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jaudiotagger.tag.FieldKey
import java.io.File
import java.io.IOException
import java.util.*

class TagEditorFragment : Fragment() {
    private lateinit var launcher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var cacheFiles: List<File>
    private lateinit var mediaScannerConnection: MediaScannerConnection
    private val loadingStatus = MutableLiveData<LoadingStatus>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val coroutineMainScope = CoroutineScope(Dispatchers.Main)
    private var glideBuilt: GlideBuilt? = null
    private lateinit var b: FragmentTagEditorBinding
    private var music: Music? = null
    private var imageUri: Uri? = null
    private var songPaths: List<String>? = null
    private var deleteAlbumArt: Boolean = false


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
        b.toolbarTagEditor.setNavigationIcon(R.drawable.ic_back)
        b.toolbarTagEditor.setNavigationOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireActivity().onBackInvokedDispatcher
            } else {
                requireActivity().onBackPressed()
            }
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            b.editSongGenreTag.visibility = View.GONE
        }
        glideBuilt = GlideBuilt(requireContext())
        b.editSongNameTag.setText(music?.name)
        b.editSongArtistTag.setText(music?.artist)
        b.editSongAlbumTag.setText(music?.album)
        b.editSongYearTag.setText(music?.year)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            b.editSongGenreTag.setText(music?.genre)
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
                    image[0], R.drawable.ic_music, b.songImageViewTag, 412, false
                )
            }
        }
        b.pickCoverTag.setOnClickListener {
            deleteAlbumArt = false
            pickImage(pickIntent, mediaPicker)
        }
        b.deleteCoverTag.setOnClickListener { deleteCoverArt() }
        b.tagEditorSaveButton.setOnClickListener {
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
        launcher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                writeToFiles(
                    mutableListOf(MusicUtil.getSongFileUri(music?.id!!.toLong())),
                    cacheFiles
                )
            }
        }
        return b.root
    }

    private fun writeToFiles(songUris: List<Uri>, cacheFiles: List<File>) {
        if (cacheFiles.size == songUris.size) {
            for (i in cacheFiles.indices) {
                requireContext().contentResolver.openOutputStream(songUris[i])?.use { output ->
                    cacheFiles[i].inputStream().use { input ->
                        input.copyTo(output)
                    }
                }
            }
        }
        lifecycleScope.launch {
            TagWriter.scan(requireContext(), MusicUtil.getSongFileUri(music?.id!!.toLong()))
            setLoadingStatus(LoadingStatus.SUCCESS)
        }
    }

    private fun deleteCoverArt() {
        glideBuilt!!.glide(null, R.drawable.ic_choose_artwork, b.songImageViewTag, 512)
        deleteAlbumArt = true
        imageUri = null
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
        setLoadingStatus(LoadingStatus.LOADING)
        val fieldKeyValueMap = EnumMap<FieldKey, String>(FieldKey::class.java)
        fieldKeyValueMap[FieldKey.TITLE] = b.editSongNameTag.text.toString().trim()
        fieldKeyValueMap[FieldKey.ALBUM] = b.editSongAlbumTag.text.toString().trim()
        fieldKeyValueMap[FieldKey.ARTIST] = b.editSongArtistTag.text.toString().trim()
        fieldKeyValueMap[FieldKey.GENRE] = b.editSongGenreTag.text.toString().trim()
        fieldKeyValueMap[FieldKey.YEAR] = b.editSongYearTag.text.toString()
        songPaths = listOf(music!!.path)
        writeValuesToFiles(
            fieldKeyValueMap, when {
                deleteAlbumArt -> ArtworkInfo(
                    music.albumId.toLong(), null
                )
                imageUri == null -> null
                else -> ArtworkInfo(
                    music.albumId.toLong(),
                    imageUri
                )
            }
        )
    }

    private fun writeValuesToFiles(
        fieldKeyValueMap: Map<FieldKey, String>,
        artworkInfo: ArtworkInfo?,
    ) {

        coroutineScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                cacheFiles = try {
                    TagWriter.writeTagsToFilesR(
                        requireContext(), AudioTagInfo(
                            songPaths,
                            fieldKeyValueMap,
                            artworkInfo
                        )
                    )
                } catch (e: Exception) {
                    setLoadingStatus(LoadingStatus.FAILURE)
                    return@launch
                }

                if (cacheFiles.isNotEmpty()) {
                    val pendingIntent =
                        MediaStore.createWriteRequest(
                            requireContext().contentResolver,
                            mutableListOf(MusicUtil.getSongFileUri(music?.id!!.toLong()))
                        )
                    launcher.launch(IntentSenderRequest.Builder(pendingIntent).build())
                }
            } else {
                try {
                    TagWriter.writeTagsToFiles(
                        requireContext(), AudioTagInfo(
                            songPaths,
                            fieldKeyValueMap,
                            artworkInfo
                        )
                    )
                } catch (e: Exception) {
                    setLoadingStatus(LoadingStatus.FAILURE)
                    return@launch
                }
                setLoadingStatus(LoadingStatus.SUCCESS)

            }
        }
    }


    private fun setLoader() {
        val fragmentManager = requireActivity().supportFragmentManager
        getLoadingStatus().observe(requireActivity()) {
            when (it) {
                LoadingStatus.LOADING -> {
                    b.progressBarTag.visibility = View.VISIBLE
                }
                LoadingStatus.SUCCESS -> {
                    showToast("Change's may be applied after restart")
                    (requireActivity() as MainActivity).checkForUpdateList(true)
                    b.progressBarTag.visibility = View.GONE
                    fragmentManager.popBackStack()
                }
                LoadingStatus.FAILURE -> {
                    showToast("Something went wrong")
                    b.progressBarTag.visibility = View.GONE
                    fragmentManager.popBackStack()
                }
                else -> {
                    showToast("Something went wrong")
                    b.progressBarTag.visibility = View.GONE
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
        glideBuilt!!.glide(imageUri.toString(), 0, b.songImageViewTag, 512)
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