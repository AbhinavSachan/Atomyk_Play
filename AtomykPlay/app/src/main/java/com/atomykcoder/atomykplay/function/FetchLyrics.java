package com.atomykcoder.atomykplay.function;

import android.view.View;
import android.widget.ProgressBar;

import com.atomykcoder.atomykplay.fragments.AddLyricsFragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class FetchLyrics {

    public void onPreExecute(ProgressBar progressBar) {
        // pre-execute code goes here
        progressBar.setVisibility(View.VISIBLE);
    }

    public String fetch (String query) {
        Element lyrics = null;
        Element link = null;
        try {
            final Document document = Jsoup.connect("https://www.megalobiz.com/search/all?qry=" + query).get();
            for (Element div : document.select("div.pro_part.mid")) {
                link = div.select("a").first();
                break;
            }
            if (link != null) {
                final String href = link.attr("href");
                final Document lyricsDocument = Jsoup.connect("https://www.megalobiz.com" + href).get();
                for (Element div : lyricsDocument.select("div.lyrics_details.entity_more_info")) {
                    lyrics = div.select("span").first();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (lyrics != null)
            return lyrics.text();
        else
            return "";
    }

    public void onPostExecute(ProgressBar progressBar) {
        progressBar.setVisibility(View.GONE);
    }
}



