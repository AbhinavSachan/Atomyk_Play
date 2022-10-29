package com.atomykcoder.atomykplay.function;

import android.os.AsyncTask;

import org.jmusixmatch.MusixMatch;
import org.jmusixmatch.MusixMatchException;
import org.jmusixmatch.entity.lyrics.Lyrics;
import org.jmusixmatch.entity.track.Track;
import org.jmusixmatch.entity.track.TrackData;
import org.jmusixmatch.subtitle.Subtitle;

public class FetchLyrics extends AsyncTask<String, Void, String> {


    // First Argument Track Name, Second Argument Artist Name => (trackname, artistname)
    @Override
    protected String doInBackground(String... strings) {
        String apiKey = "ed2f271ddd53bb4515dc778ec8144f09";
        MusixMatch musixMatch = new MusixMatch(apiKey);
        Lyrics lyrics = null;

        try {
            Track track = musixMatch.getMatchingTrack(strings[0],strings[1]);
            TrackData data = track.getTrack();
            int trackID = data.getTrackId();
            lyrics = musixMatch.getLyrics(trackID);
        } catch (MusixMatchException e) {
            e.printStackTrace();
        }

        if(lyrics != null) {
            return lyrics.getLyricsBody();
        } else {
            return "Song not found";
        }

    }
};



//region WebScrape Code later use

// takes 3 - 10 seconds to fetch lyrics and return result
// can be executed once the songs start playing it's async so user won't notice anyway
// executes when user clicks on lyrics button (currently only logs the lyrics in console)
/*
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
        if(lyrics != null)
        return lyrics.text();
        else
        return "Song not found";
        }

 */
//endregion