package com.example.administrator.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NoteDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = NoteDbHelper.class.getSimpleName();

    //name of the database file
    private static final String DATABASE_NAME = "notebook.db";

    // version of the database
    private static final int DATABASE_VERSION = 1;


    //the constructor
    public NoteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //create a String that contains the SQL statement to create the note table
        String SQL_CREATE_NOTES_TABLE = "CREATE TABLE "+ NoteContract.NoteEntry.TABLE_NAME+ " ("
                + NoteContract.NoteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NoteContract.NoteEntry.COLUMN_NOTE_TITLE + " TEXT NOT NULL, "
                + NoteContract.NoteEntry.COLUMN_NOTE_SUBTITLE + " TEXT NOT NULL, "
                + NoteContract.NoteEntry.COLUMN_NOTE_WEATHER + " INTEGER NOT NULL DEFAULT 0, "
                + NoteContract.NoteEntry.COLUMN_NOTE_TIME + " TEXT NOT NULL, "
                + NoteContract.NoteEntry.COLUMN_NOTE_MESSAGE + " TEXT);";

        //Execute the SQL statement
        db.execSQL(SQL_CREATE_NOTES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //the database will be still in version 1
    }
}
