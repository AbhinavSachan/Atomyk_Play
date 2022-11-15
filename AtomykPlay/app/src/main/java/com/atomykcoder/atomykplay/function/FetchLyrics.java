package com.atomykcoder.atomykplay.function;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class FetchLyrics {

    /**
     * Toggles Progressbar to View.VISIBLE
     * @param progressBar
     */
    public void onPreExecute(ProgressBar progressBar) {
        // pre-execute code goes here
        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Fetches List of All Songs that match the given query
     * @param query query that needed to be searched (a song name + artist name)
     * @return returns a bundle with song titles, samplelyrics and urls
     */
    public Bundle fetchList (String query) {
        Bundle bundle = new Bundle();
        Element titleLink;
        Element lyricsLink;
        String lyrics;
         ArrayList<String> titles = new ArrayList<>();
         ArrayList<String> sampleLyrics = new ArrayList<>();
         ArrayList<String> urls = new ArrayList<>();
        try {
            final Document document = Jsoup.connect("https://www.megalobiz.com/search/all?qry=" + query).get();
            Elements titleElements = document.select("div.pro_part.mid");
            Elements lyricsElements = document.select("div.details.mid");

            // Retrieve 10 Items from the list
            for ( int i = 0; i < 20; i++ ) {
                //get Title and urls
                if(titleElements != null && !titleElements.isEmpty()) {
                    titleLink = titleElements.get(i).select("a").first();
                    if (titleLink != null) {
                            titles.add(titleLink.text());
                        urls.add(titleLink.attr("href"));
                    }
                }
                //get sample Lyrics
                if(lyricsElements != null && !lyricsElements.isEmpty()) {
                    lyricsLink = lyricsElements.get(i).select("div").get(2);
                    lyricsLink = lyricsLink.select("span").first();
                    if (lyricsLink != null) {
                        lyrics = MusicHelper.splitLyricsByNewLine(lyricsLink.text());
                        sampleLyrics.add(lyrics);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // put all the data in a bundle nicely wrapped
        bundle.putStringArrayList("titles", titles);
        bundle.putStringArrayList("sampleLyrics", sampleLyrics);
        bundle.putStringArrayList("urls", urls);

        //and return the bundle
        return bundle;
    }

    /**
     * Fetches the song associated with given weblink
     * @param href weblink associated with a song
     * @return returns the Time Stamps of the song
     */
    public String fetchTimeStamps (String href) {
        Element lyrics = null;
        try {
            final Document lyricsDocument = Jsoup.connect("https://www.megalobiz.com" + href).get();
            for (Element div : lyricsDocument.select("div.lyrics_details.entity_more_info")) {
                lyrics = div.select("span").first();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (lyrics != null)
            return lyrics.text();
        else
            return "";
    }

    /**
     * Toggles Progressbar to View.GONE
     * @param progressBar
     */
    public void onPostExecute(ProgressBar progressBar) {
        progressBar.setVisibility(View.GONE);
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



