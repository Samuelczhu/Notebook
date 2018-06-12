package com.example.administrator;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.data.NoteContract;

import java.util.Calendar;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_NOTE_LOADER = 0; //identifier for the note date loader

    private Uri currentNoteUri; //content uri for existing note

    //input views
    private EditText titleEditText;
    private EditText subtitleEditText;
    private EditText messageEditText;

    private Spinner weatherSpinner; //spinner
    private ImageView weatherImage;

    private int mWeather = NoteContract.NoteEntry.WEATHER_SUNNY; //default weather

    private boolean noteHasChanged = false; //keep track of the change

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            noteHasChanged = true; //assume changed
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        //get and examine the intent
        Intent intent = getIntent();
        currentNoteUri = intent.getData();
        
        if (currentNoteUri == null) { //this is a new note
            setTitle(getString(R.string.add_note));
            invalidateOptionsMenu();
        } else { //edit an existing note
            setTitle(getString(R.string.edit_note));
            
            //initialize the loader
            getLoaderManager().initLoader(EXISTING_NOTE_LOADER, null, this);
        }
        
        //find view ny ids
        titleEditText = (EditText) findViewById(R.id.title_input);
        subtitleEditText = (EditText) findViewById(R.id.subtitle_input);
        messageEditText = (EditText) findViewById(R.id.message_input);
        weatherImage = (ImageView) findViewById(R.id.weather_image);
        weatherSpinner = (Spinner) findViewById(R.id.spinner_weather);
        
        //setup the onTouch listener on these views
        titleEditText.setOnTouchListener(onTouchListener);
        subtitleEditText.setOnTouchListener(onTouchListener);
        messageEditText.setOnTouchListener(onTouchListener);
        weatherSpinner.setOnTouchListener(onTouchListener); //note: no need for image
        
        setupSpinner();
    }
    //setup the spinner
    private void setupSpinner() {
        ArrayAdapter weatherSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.array_weather_options, android.R.layout.simple_spinner_item);

        weatherSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line); //dropdown layout style

        weatherSpinner.setAdapter(weatherSpinnerAdapter); //set the adapter

        weatherSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.weather_sunny))) {
                        mWeather = NoteContract.NoteEntry.WEATHER_SUNNY;
                        weatherImage.setImageResource(R.drawable.sunny);
                    } else if (selection.equals(getString(R.string.weather_cloudy))) {
                        mWeather = NoteContract.NoteEntry.WEATHER_CLOUDY;
                        weatherImage.setImageResource(R.drawable.cloudy);
                    } else if (selection.equals(getString(R.string.weather_rainy))) {
                        mWeather = NoteContract.NoteEntry.WEATHER_RAINY;
                        weatherImage.setImageResource(R.drawable.rainy);
                    } else if (selection.equals(getString(R.string.weather_windy))) {
                        mWeather = NoteContract.NoteEntry.WEATHER_WINDY;
                        weatherImage.setImageResource(R.drawable.windy);
                    } else if (selection.equals(getString(R.string.weather_snowy))) {
                        mWeather = NoteContract.NoteEntry.WEATHER_SNOWY;
                        weatherImage.setImageResource(R.drawable.snowy);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mWeather = NoteContract.NoteEntry.WEATHER_SUNNY; //default
            }
        });
    }

    //ge tuser input and save the note
    private void saveNote() {
        //read from the input field
        String titleString = titleEditText.getText().toString().trim();
        String subtitleString = subtitleEditText.getText().toString().trim();
        String messageString = messageEditText.getText().toString().trim();

        String timeString = Calendar.getInstance().getTime().toString();  //get the current time

        StringBuilder stringBuilder = new StringBuilder(timeString);
        timeString = stringBuilder.substring(0,10) + " " +stringBuilder.substring(30) + "   " + stringBuilder.substring(11,19); //set the time string

        //check if this is supposed to be a new note
        if (currentNoteUri == null && TextUtils.isEmpty(titleString) && TextUtils.isEmpty(subtitleString) && TextUtils.isEmpty(messageString) && mWeather == NoteContract.NoteEntry.WEATHER_SUNNY) {
            //since no fields were modified, we can return directly
            return;
        }

        //create a content values and put value inside
        ContentValues values = new ContentValues();
        values.put(NoteContract.NoteEntry.COLUMN_NOTE_TITLE, titleString);
        values.put(NoteContract.NoteEntry.COLUMN_NOTE_SUBTITLE, subtitleString);
        values.put(NoteContract.NoteEntry.COLUMN_NOTE_MESSAGE, messageString);
        values.put(NoteContract.NoteEntry.COLUMN_NOTE_TIME, timeString);
        values.put(NoteContract.NoteEntry.COLUMN_NOTE_WEATHER, mWeather);

        //determine whether this is an existing note
        if (currentNoteUri == null) { //new note
            Uri newUri = getContentResolver().insert(NoteContract.NoteEntry.CONTENT_URI, values);

            //show a toast message to indicate the state
            if (newUri == null) { //error insertion
                Toast.makeText(this, getString(R.string.error_saving), Toast.LENGTH_SHORT).show();
            } else { //success saved
                Toast.makeText(this, getString(R.string.success_saved),Toast.LENGTH_SHORT).show();
            }
        } else {
            //this is an existing note
            int rowsAffected = getContentResolver().update(currentNoteUri, values, null, null);

            //show a toast message to indicate status
            if (rowsAffected == 0) { //error
                Toast.makeText(this, getString(R.string.error_updating), Toast.LENGTH_SHORT).show();
            } else { //success
                Toast.makeText(this, getString(R.string.success_updated), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentNoteUri == null) { //this is a new pet, hide the delete option
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save: //save
                saveNote();
                finish(); //exit the activity
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog(); //pop up a dialog to confirm
                return true;
            case android.R.id.home:
                if (!noteHasChanged) { //nothing changed, exit directly
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                //otherwise there is unsaved changes, pop up a dialog to warn the user
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this); //user clicked the discard button
                    }
                };

                //show a dialog that the user have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //if the note did't changed, exit directly
        if (!noteHasChanged) {
            super.onBackPressed();
            return;
        }

        //have unsaved changed
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish(); //the user clicked discard button
            }
        };
        showUnsavedChangesDialog(discardButtonClickListener); //show dialog
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        //create a AlertDialog builder and set the message
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.discard_edit_question);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();//user clicked keep editing
                }
            }
        });

        //create and show the alert dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        //create an alert dialog builder, same as above
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_question);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteNote(); //user clicked teh delete
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();//user clicked cancel
                }
            }
        });

        //create and show the dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    //delete the note in the database
    private void deleteNote() {
        //only perform the delete if this is an existing note
        if (currentNoteUri != null) {
            int rowsDeleted = getContentResolver().delete(currentNoteUri, null, null);

            //show a taost message to indicate status
            if (rowsDeleted == 0){ //error
                Toast.makeText(this, getString(R.string.error_delete), Toast.LENGTH_SHORT).show();
            } else { //success
                Toast.makeText(this, getString(R.string.success_delete), Toast.LENGTH_SHORT).show();
            }
        }
        //close the activity
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = { //all columns from the note table
                NoteContract.NoteEntry._ID,
                NoteContract.NoteEntry.COLUMN_NOTE_TITLE,
                NoteContract.NoteEntry.COLUMN_NOTE_SUBTITLE,
                NoteContract.NoteEntry.COLUMN_NOTE_WEATHER,
                NoteContract.NoteEntry.COLUMN_NOTE_TIME,
                NoteContract.NoteEntry.COLUMN_NOTE_MESSAGE };
        
        return new CursorLoader(this,
                currentNoteUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //return if the cursor is null or less than 1 row
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        
        //proceed with moving to the first cursor and reading data from it
        if (cursor.moveToFirst()) { //note that we don't need time
            //find the columns of note attributes
            int titleColumnIndex = cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_NOTE_TITLE);
            int subtitleColumnIndex = cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_NOTE_SUBTITLE);
            int weatherColumnIndex = cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_NOTE_WEATHER);
            int messageColumnIndex = cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_NOTE_MESSAGE);
            
            //extract out the values from the cursor
            String title = cursor.getString(titleColumnIndex);
            String subtitle = cursor.getString(subtitleColumnIndex);
            int weather = cursor.getInt(weatherColumnIndex);
            String message = cursor.getString(messageColumnIndex);
            
            //update the views on the screen
            titleEditText.setText(title);
            subtitleEditText.setText(subtitle);
            messageEditText.setText(message);
            
            //the weather is a drop down spinner
            switch (weather) { //set the image too
                case NoteContract.NoteEntry.WEATHER_SUNNY:
                    weatherSpinner.setSelection(0);
                    weatherImage.setImageResource(R.drawable.sunny);
                    break;
                case NoteContract.NoteEntry.WEATHER_CLOUDY:
                    weatherSpinner.setSelection(1);
                    weatherImage.setImageResource(R.drawable.cloudy);
                    break;
                case NoteContract.NoteEntry.WEATHER_RAINY:
                    weatherSpinner.setSelection(2);
                    weatherImage.setImageResource(R.drawable.rainy);
                    break;
                case NoteContract.NoteEntry.WEATHER_WINDY:
                    weatherSpinner.setSelection(3);
                    weatherImage.setImageResource(R.drawable.windy);
                    break;
                case NoteContract.NoteEntry.WEATHER_SNOWY:
                    weatherSpinner.setSelection(4);
                    weatherImage.setImageResource(R.drawable.snowy);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //if the loader is invalidated, clear out all the data
        titleEditText.setText("");
        subtitleEditText.setText("");
        messageEditText.setText("");
        weatherSpinner.setSelection(0); //select sunny
    }
}
