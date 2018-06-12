package com.example.administrator;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.data.NoteContract;

public class NoteCursorAdapter extends CursorAdapter {

    public NoteCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //find the individual views
        TextView titleText = (TextView) view.findViewById(R.id.title_text);
        TextView subtitleText = (TextView) view.findViewById(R.id.subtitle_text);
        TextView timeText = (TextView) view.findViewById(R.id.time_text);

        //find the columns of the note attributes that we're interested in
        int titleColumnIndex = cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_NOTE_TITLE);
        int subtitleColumnIndex = cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_NOTE_SUBTITLE);
        int timeColumnIndex = cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_NOTE_TIME);
        int weatherColumnIndex = cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_NOTE_WEATHER);

        //read the note attributes
        String noteTitle = cursor.getString(titleColumnIndex);
        String notSubtitle = cursor.getString(subtitleColumnIndex);
        String noteTime = cursor.getString(timeColumnIndex);
        int weather = cursor.getInt(weatherColumnIndex);

        //set the textView
        titleText.setText(noteTitle);
        subtitleText.setText(notSubtitle);
        timeText.setText(noteTime);

        //find the imageView and set the image
        ImageView weatherImage = (ImageView) view.findViewById(R.id.weather_image);
        switch (weather) {
            case NoteContract.NoteEntry.WEATHER_SUNNY:
                weatherImage.setImageResource(R.drawable.sunny);
                break;
            case NoteContract.NoteEntry.WEATHER_CLOUDY:
                weatherImage.setImageResource(R.drawable.cloudy);
                break;
            case NoteContract.NoteEntry.WEATHER_RAINY:
                weatherImage.setImageResource(R.drawable.rainy);
                break;
            case NoteContract.NoteEntry.WEATHER_WINDY:
                weatherImage.setImageResource(R.drawable.windy);
                break;
            case NoteContract.NoteEntry.WEATHER_SNOWY:
                weatherImage.setImageResource(R.drawable.snowy);
                break;
        }
    }
}
