package com.example.administrator;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.administrator.data.NoteContract;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int NOTE_LOADER = 0; //identifier for the note data

    NoteCursorAdapter noteCursorAdapter; //adapter for the list view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup the fab to open editorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        //find the list view
        ListView noteListView = (ListView) findViewById(R.id.list);
        //set the empty view
        View emptyView = findViewById(R.id.empty_view);
        noteListView.setEmptyView(emptyView);

        //set up the adapter
        noteCursorAdapter = new NoteCursorAdapter(this, null);
        noteListView.setAdapter(noteCursorAdapter);

        //set up the on item click listener for the list view
        noteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //create new intent to go to the editor activity
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                //generate the uri
                Uri currentNoteUri = ContentUris.withAppendedId(NoteContract.NoteEntry.CONTENT_URI, id);

                //insert the Uri on the data
                intent.setData(currentNoteUri);
                startActivity(intent); //launch the intent
            }
        });

        //kick off the loader
        getLoaderManager().initLoader(NOTE_LOADER, null, this);
    }

    //helper method to insert dummy data
    private void insertNote() {
        //create a dummy values

        String timeString = Calendar.getInstance().getTime().toString();  //get the current time
        StringBuilder stringBuilder = new StringBuilder(timeString);
        timeString = stringBuilder.substring(0,10) +" "+ stringBuilder.substring(30) + "   "+ stringBuilder.substring(11,19); //set the time string

        ContentValues values = new ContentValues();
        values.put(NoteContract.NoteEntry.COLUMN_NOTE_TITLE, "无聊人生");
        values.put(NoteContract.NoteEntry.COLUMN_NOTE_SUBTITLE, "今天好无聊啊，啊啊啊啊啊");
        values.put(NoteContract.NoteEntry.COLUMN_NOTE_TIME, timeString);
        values.put(NoteContract.NoteEntry.COLUMN_NOTE_WEATHER, NoteContract.NoteEntry.WEATHER_SUNNY);
        values.put(NoteContract.NoteEntry.COLUMN_NOTE_MESSAGE, "记得小时候，有次下雨天，老妈坐堂屋门口纳鞋底！\n" +
                "我问老妈为什么要把千层底做这么厚？\n" +
                "老妈：因为拿鞋底抽你的时候，怕太薄了震的手疼，做厚点就不会了！\n" +
                "我。。。");

        Uri newUri = getContentResolver().insert(NoteContract.NoteEntry.CONTENT_URI, values);
    }

    //helper method to delete all notes
    private void deleteAllNotes() {
        int rowsDeleted = getContentResolver().delete(NoteContract.NoteEntry.CONTENT_URI, null, null);
        Log.v("MainAcivity", rowsDeleted+" rows deleted from the note database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // user click on the menu
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertNote();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllNotes();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Define a projection that specifies the column from the table we care about
        String[] projection = {
                NoteContract.NoteEntry._ID,
                NoteContract.NoteEntry.COLUMN_NOTE_TITLE,
                NoteContract.NoteEntry.COLUMN_NOTE_SUBTITLE,
                NoteContract.NoteEntry.COLUMN_NOTE_TIME,
                NoteContract.NoteEntry.COLUMN_NOTE_WEATHER };

        return new CursorLoader(this,
                NoteContract.NoteEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        noteCursorAdapter.swapCursor(data); //update
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        noteCursorAdapter.swapCursor(null);
    }
}
