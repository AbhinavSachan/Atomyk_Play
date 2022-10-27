package com.atomykcoder.atomykplay.function;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URL;

public class FetchLyrics extends AsyncTask<String, Void, String> {


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    // takes 3 - 10 seconds to fetch lyrics and return result
    // can be executed once the songs start playing it's async so user won't notice anyway
    // executes when user clicks on lyrics button (currently only logs the lyrics in console)
    @Override
    protected String doInBackground(String... strings) {
            Element lyrics = null;
            Element link = null;
        try{
            final Document document = Jsoup.connect("https://www.megalobiz.com/search/all?qry=" + strings[0]).get();
            for (Element div : document.select("div.pro_part.mid")) {
                link = div.select("a").first();
                break;
            }
            final String href = link.attr("href");
            final Document lyricsDocument = Jsoup.connect("https://www.megalobiz.com" + href).get();
            for (Element div : lyricsDocument.select("div.lyrics_details.entity_more_info")) {
                lyrics = div.select("span").first();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lyrics.text();
    }

    @Override
    protected void onPostExecute(String string) {
        super.onPostExecute(string);
    }
};