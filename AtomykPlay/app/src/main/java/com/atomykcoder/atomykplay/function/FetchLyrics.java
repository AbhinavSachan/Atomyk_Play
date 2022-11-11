package com.atomykcoder.atomykplay.function;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FetchLyrics {

    public void onPreExecute(ProgressBar progressBar) {
        // pre-execute code goes here
        progressBar.setVisibility(View.VISIBLE);
    }

    public Bundle fetchList (String query) {
        Bundle bundle = new Bundle();
        final ArrayList<String> titles = new ArrayList<>();
        final ArrayList<String> durations = new ArrayList<>();
        final ArrayList<String> urls = new ArrayList<>();
        Element link;
        try {
            final Document document = Jsoup.connect("https://www.megalobiz.com/search/all?qry=" + query).get();
            int i = 0;
            for (Element div : document.select("div.pro_part.mid")) {
                link = div.select("a").first();
                if (link != null) {
                    titles.add(link.text());
                    urls.add(link.attr("href"));
                    durations.add(getLength(link.text()));
                }
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        bundle.putStringArrayList("titles", titles);
        bundle.putStringArrayList("durations", durations);
        bundle.putStringArrayList("urls", urls);
        return bundle;
    }

    public String fetchItem (String href) {
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

    public void onPostExecute(ProgressBar progressBar) {
        progressBar.setVisibility(View.GONE);
    }

    private String getLength(String songName) {
        String o = "[00:00.00]";
        Log.i("match", songName);
        Pattern _pattern = Pattern.compile("\\[\\d\\d:\\d\\d.\\d\\d\\]");
        Matcher _matcher = _pattern.matcher(songName);
        if(_matcher.find()) {
            o = songName.substring(_matcher.start(), _matcher.end());
        }
        Log.i("match", "Match : " +  o);
        return o;
    }
}



