package com.atomykcoder.atomykplay.helperFunctions

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import com.atomykcoder.atomykplay.interfaces.ApiService
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class FetchLyrics {

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.megalobiz.com/")
            .client(createOkHttpClient())
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }

    private val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    /**
     * Create and configure OkHttpClient with logging interceptor
     */
    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(45, TimeUnit.SECONDS)
            .callTimeout(45, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Toggles Progressbar to View.VISIBLE
     */
    fun onPreExecute(progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE
    }

    /**
     * Fetches List of All Songs that match the given query
     *
     * @param name query that needed to be searched (song name)
     * @param artistName query that needed to be searched (artist name)
     * @return returns a bundle with song titles, sample lyrics, and URLs
     */
    suspend fun fetchList(name: String?, artistName: String?): Bundle? {
        val bundle = Bundle()
        val titles = ArrayList<String>()
        val sampleLyrics = ArrayList<String>()
        val urls = ArrayList<String>()
        try {
            val response = apiService.search("$name $artistName")

            val document = Jsoup.parse(response.string())

            val titleElements = document.select("div.pro_part.mid")
            val lyricsElements = document.select("div.details.mid")

            if (titleElements != null) {
                // Retrieve items from the list
                for (i in 0..9) {
                    // Get Title and URLs
                    if (!titleElements.isEmpty()) {
                        val titleLink = titleElements[i].select("a").first()
                        if (titleLink != null) {
                            titles.add(titleLink.text())
                            urls.add(titleLink.attr("href"))
                        }
                    }
                    // Get sample Lyrics
                    if (lyricsElements != null && !lyricsElements.isEmpty()) {
                        var lyricsLink = lyricsElements[i].select("div")[2]
                        lyricsLink = lyricsLink.select("span").first()
                        if (lyricsLink != null) {
                            val lyrics = MusicHelper.splitLyricsByNewLine(lyricsLink.text())
                            sampleLyrics.add(lyrics)
                        }
                    }
                }
            } else {
                return null
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Put all the data in a bundle nicely wrapped
        bundle.putStringArrayList("titles", titles)
        bundle.putStringArrayList("sampleLyrics", sampleLyrics)
        bundle.putStringArrayList("urls", urls)

        // And return the bundle
        return bundle
    }

    /**
     * Fetches the song associated with the given weblink
     *
     * @param href weblink associated with a song
     * @return returns the Time Stamps of the song
     */
    suspend fun fetchTimeStamps(href: String): String {
        var lyrics: Element? = null
        try {
            val response: ResponseBody = apiService.getSong(href)
            val lyricsDocument = Jsoup.parse(response.string())
            for (div in lyricsDocument.select("div.lyrics_details.entity_more_info")) {
                lyrics = div.select("span").first()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return lyrics?.text() ?: ""
    }

    /**
     * Toggles Progressbar to View.GONE
     */
    fun onPostExecute(progressBar: ProgressBar) {
        progressBar.visibility = View.GONE
    }
}
