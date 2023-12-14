package com.example.challenge3.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DataOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "data.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_TEMPERATURE = "temperature";
    public static final String TABLE_HUMIDITY = "humidity";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_VALUE = "value";

    private static final String CREATE_TABLE_TEMPERATURE =
            "CREATE TABLE " + TABLE_TEMPERATURE + " (" +
                    COLUMN_TIMESTAMP + " TEXT PRIMARY KEY," +
                    COLUMN_VALUE + " TEXT);";

    private static final String CREATE_TABLE_HUMIDITY =
            "CREATE TABLE " + TABLE_HUMIDITY + " (" +
                    COLUMN_TIMESTAMP + " TEXT PRIMARY KEY," +
                    COLUMN_VALUE + " TEXT);";

    public DataOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_TABLE_TEMPERATURE);
        db.execSQL(CREATE_TABLE_HUMIDITY);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public String getCurrentTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS", Locale.getDefault());
        Date currentDate = new Date();
        return dateFormat.format(currentDate);
    }
    public String getLastTemperatureValue() {
        SQLiteDatabase db = getReadableDatabase();

        // Define the query to get the last temperature value, ordered by timestamp in descending order
        String query = "SELECT * FROM " + TABLE_TEMPERATURE +
                " ORDER BY datetime(" + COLUMN_TIMESTAMP + ") DESC LIMIT 1";


        Cursor cursor = db.rawQuery(query, null);

        String lastTemperatureValue = null;

        if (cursor.moveToFirst()) {
            // Retrieve the temperature value from the cursor
            lastTemperatureValue = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VALUE));
        }


        // Close the cursor and database
        cursor.close();
        db.close();

        return lastTemperatureValue;
    }

    public String getLastHumidityValue() {
        SQLiteDatabase db = getReadableDatabase();

        // Define the query to get the last humidity value, ordered by timestamp in descending order
        String query = "SELECT " + COLUMN_VALUE + " FROM " + TABLE_HUMIDITY +
                " ORDER BY datetime(" + COLUMN_TIMESTAMP + ") DESC LIMIT 1";

        Cursor cursor = db.rawQuery(query, null);

        String lastHumidityValue = null;

        if (cursor.moveToFirst()) {
            // Retrieve the humidity value from the cursor
            lastHumidityValue = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VALUE));
        }

        // Close the cursor and database
        cursor.close();
        db.close();

        return lastHumidityValue;
    }


    public long insertTemperature(String value) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TIMESTAMP, getCurrentTimestamp());
        values.put(COLUMN_VALUE, value);
        return db.insert(TABLE_TEMPERATURE, null, values);
    }

    public long insertHumidity(String value) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TIMESTAMP, getCurrentTimestamp());
        values.put(COLUMN_VALUE, value);
        return db.insert(TABLE_HUMIDITY, null, values);
    }
}
