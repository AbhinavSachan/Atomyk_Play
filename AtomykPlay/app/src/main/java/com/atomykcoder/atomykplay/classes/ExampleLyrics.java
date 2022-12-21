package com.atomykcoder.atomykplay.classes;


import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.jaudiotagger.tag.id3.valuepair.TextEncoding;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;

public class ExampleLyrics {


    /**
     * Constructor method.
     * <p/>
     *
     * @param format   The audio tag format
     * @param encoding The audio tag encoding
     * @param mp3file  The target audio file
     * @param textfile The text file with song lyrics
     * @throws IOException If an I/O error occurs
     * @since 1.6
     */
    public ExampleLyrics(String format, String encoding, File mp3file, File textfile) throws IOException {

        System.out.println("Configuring jaudiotagger:");
        System.out.println("    format             " + format);
        System.out.println("    encoding           " + encoding);
        System.out.println("    mp3file            " + mp3file);
        System.out.println("    textfile           " + textfile);
        System.out.println("");


        // Let's prepare some audio tag data
        Hashtable<FieldKey, String> fieldKeyTable = new Hashtable<>(0);

        // Album data
        fieldKeyTable.put(FieldKey.ALBUM, "Aenima");
        fieldKeyTable.put(FieldKey.ALBUM_ARTIST, "Tool");

        // Title data
        fieldKeyTable.put(FieldKey.TITLE, "Die Eier von Satan");
        fieldKeyTable.put(FieldKey.ARTIST, "Tool");
        fieldKeyTable.put(FieldKey.YEAR, "1996");
        fieldKeyTable.put(FieldKey.TRACK, "10");

        // Load lyrics from text file
        String lyrics = new String(readTextData(textfile));
        fieldKeyTable.put(FieldKey.LYRICS, lyrics);

        // Write tag data to audio file
        System.out.println("Writing audio file " + mp3file + " with new tag data");
        try {
            // Open audio file first, audio data is rewritten
            AudioFile audiofile = AudioFileIO.read(mp3file);

            Tag tag;
            TextEncoding textencoding = TextEncoding.getInstanceOf();
            TagOptionSingleton tagoptions = TagOptionSingleton.getInstance();

            // jaudiotagger supports additional tag formats like MP4 or Vorbis.
            // We will concentrate on the relevant ID3V23 and ID3V24 versions.

            {  // Must be SupportedTagFormat.ID3V24_TAG

                // Supported by ID3V24 (see TagOptionSingleton class):
                // TextEncoding.ISO_8859_1, TextEncoding.UTF_16, TextEncoding.UTF_16BE, TextEncoding.UTF_8
                tagoptions.setId3v24DefaultTextEncoding(textencoding.getIdForValue(encoding).byteValue());

                tag = new ID3v24Tag();
            }

            // Activate the new tag fields based on the encoding set before
            audiofile.setTag(tag);
            Enumeration<FieldKey> en = fieldKeyTable.keys();
            while (en.hasMoreElements()) {
                FieldKey key = en.nextElement();
                tag.setField(key, fieldKeyTable.get(key));
            }

            // One could add some artwork like a JPEG album cover here, too.
            // The encoding must then be changed again to be properly processed.

            // Set ISO-8859-1 charset for images, may be not shown by player with UTF-16
            /*
            byte isoencoding = textencoding.getIdForValue(TextEncoding.CHARSET_ISO_8859_1).byteValue();
            if (format.equals(SupportedTagFormat.ID3V23_TAG.name())) {
                tagoptions.setId3v23DefaultTextEncoding(isoencoding);
            }
            else {  // Must be SupportedTagFormat.ID3V24_TAG
                tagoptions.setId3v24DefaultTextEncoding(isoencoding);
            }
            tag.setField(ArtworkFactory.createArtworkFromFile(new File("C:\\Temp\\cover.jpg")));
            */

            // Finally store new tag data into audio file
            AudioFileIO.write(audiofile);

            System.out.println("    Successful, wrote " + mp3file.length() + " bytes of data");
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.err.println("Sorry, could not write tag data due to errors, exiting");
    }

    /**
     * Functionality for testing and debugging.
     * <p/>
     * Supported arguments:
     * <code>
     * -format [value]    The audio tag format, use "ID3V23_TAG" or "ID3V24_TAG"
     * -encoding [value]  The text encoding, use "ISO-8859-1", "UTF-16", "UTF-16BE", "UTF-8"
     * -mp3file [value]   The target audio file
     * -textfile [value]  The text file with song lyrics
     * </code>
     * <p/>
     *
     * @param args Array of strings with console arguments
     * @since 1.6
     */
    public static void main(String[] args) {

        String format = null, encoding = null;
        File mp3file = null, textfile = null;

        System.out.println("");

        try {
            // Parse arguments
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-format") && (i + 1) < args.length) {
                    format = args[++i];
                } else if (args[i].equals("-encoding") && (i + 1) < args.length) {
                    encoding = args[++i];
                } else if (args[i].equals("-mp3file") && (i + 1) < args.length) {
                    mp3file = new File(args[++i]);
                } else if (args[i].equals("-textfile") && (i + 1) < args.length) {
                    textfile = new File(args[++i]);
                }
            }

            // Check out the given arguments
            if (format != null) {  // Encoding OK?
                TextEncoding textencoding = TextEncoding.getInstanceOf();
                Integer id3encoding = (encoding != null ? textencoding.getIdForValue(encoding) : null);
                if (id3encoding == null) {
                    System.err.println("Sorry, the encoding " + encoding + " is not supported");
                } else {  // Files OK?
                    if (mp3file == null || !mp3file.exists()) {
                        System.err.println("Sorry, no audio file found");
                    } else if (textfile == null || !textfile.exists()) {
                        System.err.println("Sorry, no lyrics file found");
                    } else {
                        // Good, let's do it
                        new ExampleLyrics(format, encoding, mp3file, textfile);
                        System.exit(0);
                    }
                }
            }
        } catch (Exception exc) {
            try {
                Thread.sleep(1000);
            } catch (Exception ignored) {
            }
            exc.printStackTrace();
        }
        System.exit(1);
    }

    /**
     * Reads all character data from the given stream.
     * <p/>
     *
     * @param file The input file
     * @return Character data from stream
     * @throws NullPointerException If a parameter is <code>null</code>
     * @throws IOException          If an I/O error occurs
     * @since 1.6
     */
    private char[] readTextData(File file) throws IOException {

        FileInputStream fileInputStream = new FileInputStream(file);
        InputStreamReader reader = new InputStreamReader(fileInputStream);

        char[] buffer = new char[1024];
        int len;
        StringBuilder builder = new StringBuilder(0);

        while ((len = reader.read(buffer)) != -1) {
            builder.append(buffer, 0, len);
        }
        reader.close();
        fileInputStream.close();

        return builder.toString().toCharArray();
    }
}


