package com.example.administrator.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class NoteContract {

    private NoteContract() {} //empty constructor

    public static final String CONTENT_AUTHORITY = "com.samuel.notebook.notes";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY);

    public static final String PATH_NOTES = "notes";

    public static class NoteEntry implements BaseColumns {
        //the content URI to access the note data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_NOTES);

        //the MIME type for a list of notes
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE+"/"+CONTENT_AUTHORITY+"/"+PATH_NOTES;

        //teh MIME type for a single note
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE+"/"+CONTENT_AUTHORITY+"/"+PATH_NOTES;

        //the name of the database table for notes
        public final static String TABLE_NAME = "notes";

        // the unique id for the note
        public final static String _ID = BaseColumns._ID;

        //title of the note
        public final static String COLUMN_NOTE_TITLE = "title"; //TEXT

        //subtitle of the note
        public final static String COLUMN_NOTE_SUBTITLE = "subtitle"; //TEXT

        // mood of the note
        public final static String COLUMN_NOTE_WEATHER = "weather"; //INTEGER

        // time of the note
        public final static String COLUMN_NOTE_TIME = "time"; // TEXT

        // message of the note
        public final static String COLUMN_NOTE_MESSAGE = "message"; //TEXT

        // Possible values for the mood of the note
        public static final int WEATHER_SUNNY = 0;
        public static final int WEATHER_CLOUDY = 1;
        public static final int WEATHER_RAINY = 2;
        public static final int WEATHER_WINDY = 3;
        public static final int WEATHER_SNOWY = 4;

        //method to check the valid int for the mood
        public static boolean isValidMood(int mood) {
            if (mood == WEATHER_SUNNY || mood == WEATHER_CLOUDY || mood == WEATHER_RAINY || mood == WEATHER_WINDY || mood == WEATHER_SNOWY) {
                return true;
            }
            return false;
        }
    }
}
