package com.example.administrator.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class NoteProvider extends ContentProvider {

    //tag for the log messages
    public static final String LOG_TAG = NoteProvider.class.getSimpleName();

    //the URI matcher code for the content URI for the notes table
    private static final int NOTES = 888;

    //the URI matcher code for the content URI for a single note in the notes table
    private static final int NOTE_ID = 999;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //static initializer
    static {
        sUriMatcher.addURI(NoteContract.CONTENT_AUTHORITY, NoteContract.PATH_NOTES, NOTES);

        sUriMatcher.addURI(NoteContract.CONTENT_AUTHORITY, NoteContract.PATH_NOTES +"/#", NOTE_ID);
    }

    //database helper object
    private NoteDbHelper noteDbHelper;

    @Override
    public boolean onCreate() {
        noteDbHelper = new NoteDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        //get the readable database
        SQLiteDatabase database = noteDbHelper.getReadableDatabase();

        Cursor cursor; //this cursor will contain the result
        int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                //query the notes table directly
                cursor = database.query(NoteContract.NoteEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case NOTE_ID:
                //query a single row
                selection = NoteContract.NoteEntry._ID +"=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))}; //fill the single integer

                cursor = database.query(NoteContract.NoteEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI "+uri);
        }

        //set the notification URI on the cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        //return the cursor
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                return NoteContract.NoteEntry.CONTENT_LIST_TYPE;
            case NOTE_ID:
                return NoteContract.NoteEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI "+uri+" with match "+match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                return insertNote(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for "+uri);
        }
    }
    //insert a note into teh database with the given content values
    private Uri insertNote(Uri uri, ContentValues values) {
        //check the title is not null
        String title = values.getAsString(NoteContract.NoteEntry.COLUMN_NOTE_TITLE);
        if (title == null) {
            throw new IllegalArgumentException("Note requires a title");
        }
        // check the subtitle
        String subtitle = values.getAsString(NoteContract.NoteEntry.COLUMN_NOTE_SUBTITLE);
        if (subtitle == null) {
            throw new IllegalArgumentException("Note requires a subtitle");
        }
        //check for the time
        String time = values.getAsString(NoteContract.NoteEntry.COLUMN_NOTE_TIME);
        if (time == null) {
            throw new IllegalArgumentException("Note requires a time");
        }
        //check for weather
        Integer weather = values.getAsInteger(NoteContract.NoteEntry.COLUMN_NOTE_WEATHER);
        if (weather != null && !NoteContract.NoteEntry.isValidMood(weather)) {
            throw new IllegalArgumentException("Note require a valid weather");
        }
        //no need for check for message

        //get writable database
        SQLiteDatabase database = noteDbHelper.getWritableDatabase();

        //Insert the new note with the given values
        long id = database.insert(NoteContract.NoteEntry.TABLE_NAME, null, values);
        if (id ==-1) { // insertion failed
            Log.e(LOG_TAG, "failed to insert row for "+uri);
            return null;
        }
        //notify all listener
        getContext().getContentResolver().notifyChange(uri, null);

        //return the new URI
        return ContentUris.withAppendedId(uri,id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        //get writable database
        SQLiteDatabase database = noteDbHelper.getWritableDatabase();

        //track the number of rows deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                //delete all rows that match teh slection
                rowsDeleted = database.delete(NoteContract.NoteEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case NOTE_ID:
                //delete a single row by the id
                selection = NoteContract.NoteEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(NoteContract.NoteEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for "+uri);
        }

        //if 1 or more row is changed, notify listener
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        //return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                return updateNote(uri, contentValues, selection, selectionArgs);
            case NOTE_ID:
                selection = NoteContract.NoteEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateNote(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for "+uri);
        }
    }
    //helper method for the update note
    private int updateNote(Uri uri, ContentValues values, String selection, String[] slectionArgs) {
        //check the title
        if (values.containsKey(NoteContract.NoteEntry.COLUMN_NOTE_TIME)) {
            String title = values.getAsString(NoteContract.NoteEntry.COLUMN_NOTE_TIME);
            if (title == null) {
                throw new IllegalArgumentException("Note requires a title");
            }
        }
        //check subtitle
        if (values.containsKey(NoteContract.NoteEntry.COLUMN_NOTE_SUBTITLE)) {
            String subtitle = values.getAsString(NoteContract.NoteEntry.COLUMN_NOTE_SUBTITLE);
            if (subtitle == null) {
                throw new IllegalArgumentException("Note requires a subtitle");
            }
        }
        //check for time
        if (values.containsKey(NoteContract.NoteEntry.COLUMN_NOTE_TIME)) {
            String time = values.getAsString(NoteContract.NoteEntry.COLUMN_NOTE_TIME);
            if (time == null) {
                throw new IllegalArgumentException("Note requires a time");
            }
        }
        //check for mood
        if (values.containsKey(NoteContract.NoteEntry.COLUMN_NOTE_WEATHER)) {
            Integer weather = values.getAsInteger(NoteContract.NoteEntry.COLUMN_NOTE_WEATHER);
            if (weather != null && !NoteContract.NoteEntry.isValidMood(weather)) {
                throw new IllegalArgumentException("Note requires a valid weather");
            }
        }
        //no need to check for message

        //if no values are updated, then don't update
        if (values.size() == 0) {
            return 0;
        }

        //Otherwise, gte writable database
        SQLiteDatabase database = noteDbHelper.getWritableDatabase();

        //Perform the update
        int rowsUpdated = database.update(NoteContract.NoteEntry.TABLE_NAME, values, selection, slectionArgs);

        //if 1 or more rows are updated, then notify the listener
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        //return the number rows updated
        return rowsUpdated;
    }
}
