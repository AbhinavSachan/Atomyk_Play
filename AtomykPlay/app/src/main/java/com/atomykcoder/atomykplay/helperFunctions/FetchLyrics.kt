package com.atomykcoder.atomykplay.helperFunctions

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.IOException

class FetchLyrics {
    /**
     * Toggles Progressbar to View.VISIBLE
     */
    fun onPreExecute(progressBar: ProgressBar) {
        // pre-execute code goes here
        progressBar.visibility = View.VISIBLE
    }

    /**
     * Fetches List of All Songs that match the given query
     *
     * @param query query that needed to be searched (a song name + artist name)
     * @return returns a bundle with song titles, sample lyrics and urls
     */
    fun fetchList(query: String): Bundle? {
        val bundle = Bundle()
        var titleLink: Element?
        var lyricsLink: Element?
        var lyrics: String?
        val titles = ArrayList<String>()
        val sampleLyrics = ArrayList<String>()
        val urls = ArrayList<String>()
        try {
            val document = Jsoup.connect("https://www.megalobiz.com/search/all?qry=$query").get()
            val titleElements = document.select("div.pro_part.mid")
            val lyricsElements = document.select("div.details.mid")
            if (titleElements != null) {
                // Retrieve  Items from the list
                for (i in 0..9) {
                    //get Title and urls
                    if (!titleElements.isEmpty()) {
                        titleLink = titleElements[i].select("a").first()
                        if (titleLink != null) {
                            titles.add(titleLink.text())
                            urls.add(titleLink.attr("href"))
                        }
                    }
                    //get sample Lyrics
                    if (lyricsElements != null && !lyricsElements.isEmpty()) {
                        lyricsLink = lyricsElements[i].select("div")[2]
                        lyricsLink = lyricsLink.select("span").first()
                        if (lyricsLink != null) {
                            lyrics = MusicHelper.splitLyricsByNewLine(lyricsLink.text())
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
        // put all the data in a bundle nicely wrapped
        bundle.putStringArrayList("titles", titles)
        bundle.putStringArrayList("sampleLyrics", sampleLyrics)
        bundle.putStringArrayList("urls", urls)

        //and return the bundle
        return bundle
    }

    /**
     * Fetches the song associated with given weblink
     *
     * @param href weblink associated with a song
     * @return returns the Time Stamps of the song
     */
    fun fetchTimeStamps(href: String): String {
        var lyrics: Element? = null
        try {
            val lyricsDocument = Jsoup.connect("https://www.megalobiz.com$href").get()
            for (div in lyricsDocument.select("div.lyrics_details.entity_more_info")) {
                lyrics = div.select("span").first()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return if (lyrics != null) lyrics.text() else ""
    }

    /**
     * Toggles Progressbar to View.GONE
     */
    fun onPostExecute(progressBar: ProgressBar) {
        progressBar.visibility = View.GONE
    }
    //region get duration method maybe for use later...
    //    private String getLength(String songName) {
    //        String o = "[00:00.00]";
    //        Log.i("match", songName);
    //        Pattern _pattern = Pattern.compile("\\[\\d\\d:\\d\\d.\\d\\d\\]");
    //        Matcher _matcher = _pattern.matcher(songName);
    //        if(_matcher.find()) {
    //            o = songName.substring(_matcher.start(), _matcher.end());
    //        }
    //        Log.i("match", "Match : " +  o);
    //        return o;
    //    }
    //endregion.......
}