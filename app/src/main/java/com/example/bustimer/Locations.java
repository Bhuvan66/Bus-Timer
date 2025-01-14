package com.example.bustimer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class Locations extends SQLiteOpenHelper {
    private static Locations instance;

    private Locations(Context context) {
        super(context, "locations.db", null, 1);
    }

    public static synchronized Locations getInstance(Context context) {
        if (instance == null) {
            instance = new Locations(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE locations (id INTEGER PRIMARY KEY, name TEXT, latitude REAL, longitude REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS locations");
        onCreate(db);
    }

    public boolean CreateStopTable(String Place) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("CREATE TABLE " + Place + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "bus_name TEXT, " +
                    "arrival_time TEXT, " +
                    "type TEXT)");


            //add 5 dummy rows for testing
            addBus(Place, "Bus 1", "10:00", "AC");
            addBus(Place, "Bus 2", "10:30", "Non-AC");
            addBus(Place, "Bus 3", "11:00", "AC");
            addBus(Place, "Bus 4", "11:30", "Non-AC");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void addBus(String Place, String bus_name, String arrival_time, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO " + Place + " (bus_name, arrival_time, type) VALUES ('" + bus_name + "', '" + arrival_time + "', '" + type + "')");
    }

    public ArrayList<String> getAllStops() {
        ArrayList<String> tableNames = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                tableNames.add(cursor.getString(0));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return tableNames;
    }

    Cursor getBuses(String Place) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + Place, null);
    }
}