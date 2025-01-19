package com.example.bustimer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Locations extends SQLiteOpenHelper {
    private static Locations instance;

    public Locations(Context context) {
        super(context, "locations.db", null, 1);
    }

    public static synchronized Locations getInstance(Context context) {
        if (instance == null) {
            instance = new Locations(context.getApplicationContext());
            SQLiteDatabase db = instance.getWritableDatabase(); // Ensure the database is created
            instance.checkAndCreateTable(db); // Check and create the table if it does not exist
        }
        return instance;
    }

    private void checkAndCreateTable(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='locations'", null);
        if (cursor.getCount() == 0) {
            onCreate(db);
        }
        cursor.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS locations (id INTEGER PRIMARY KEY, name TEXT, latitude REAL, longitude REAL)");
        } catch (Exception e) {
            Log.e("Locations", "Error creating table: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("CREATE TABLE IF NOT EXISTS locations (id INTEGER PRIMARY KEY, name TEXT, latitude REAL, longitude REAL)");
    }
    public boolean CreateStopTable(String Place, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{Place});
        boolean tableExists = cursor.getCount() > 0;
        cursor.close();

        if (tableExists) {
            return false; // Table already exists
        }

        try {
            db.execSQL("CREATE TABLE " + Place + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "bus_name TEXT, " +
                    "arrival_time TEXT, " +
                    "type TEXT, " +
                    "`to` TEXT)");
            // Add the location to the locations table
            db.execSQL("INSERT INTO locations (name, latitude, longitude) VALUES ('" + Place + "', " + latitude + ", " + longitude + ")");

            // Add 5 dummy rows for testing
            addBus(Place, "Bus 1", "10:00", "AC", "To");
            addBus(Place, "Bus 2", "10:30", "Non-AC", "To");
            addBus(Place, "Bus 3", "11:00", "AC", "To");
            addBus(Place, "Bus 4", "11:30", "Non-AC", "To");

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean addBus(String Place, String bus_name, String arrival_time, String type, String to) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("INSERT INTO " + Place + " (bus_name, arrival_time, type, `to`) VALUES ('" + bus_name + "', '" + arrival_time + "', '" + type + "', '" + to + "')");
            return true; // Insertion successful
        } catch (Exception e) {
            return false; // Insertion failed
        }
    }

    public ArrayList<String> getAllStops() {
        ArrayList<String> tableNames = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT name FROM locations", null);
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    tableNames.add(cursor.getString(0));
                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            Log.e("Locations", "Error fetching stops: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return tableNames;
    }

    Cursor getBuses(String Place) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + Place, null);
    }

    public void clearAllTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String tableName = cursor.getString(0);
                if (!tableName.equals("android_metadata") && !tableName.equals("sqlite_sequence")) {
                    db.execSQL("DROP TABLE IF EXISTS " + tableName);
                }
                cursor.moveToNext();
            }
        }
        cursor.close();
    }

    public ArrayList<String> getUniqueToEntries(String place) {
        ArrayList<String> toEntries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT `to` FROM " + place, null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                toEntries.add(cursor.getString(0));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return toEntries;
    }

    public ArrayList<String> getAllUniqueToEntries() {
        ArrayList<String> allUniqueToEntries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT name FROM locations", null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String place = cursor.getString(0);
                Cursor toCursor = db.rawQuery("SELECT DISTINCT `to` FROM `" + place + "`", null);
                if (toCursor.moveToFirst()) {
                    while (!toCursor.isAfterLast()) {
                        String toEntry = toCursor.getString(0);
                        if (!allUniqueToEntries.contains(toEntry)) {
                            allUniqueToEntries.add(toEntry);
                        }
                        toCursor.moveToNext();
                    }
                }
                toCursor.close();
                cursor.moveToNext();
            }
        }
        cursor.close();
        return allUniqueToEntries;
    }

    public String getNextAvailableBus(String from, String to, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        Cursor cursor = db.rawQuery("SELECT * FROM `" + from + "` WHERE `to` = ? AND type = ? AND arrival_time > ? ORDER BY arrival_time ASC LIMIT 1", new String[]{to, type, currentTime});
        String nextBus = "No available buses";

        if (cursor.moveToFirst()) {
            int busNameIndex = cursor.getColumnIndex("bus_name");
            int arrivalTimeIndex = cursor.getColumnIndex("arrival_time");

            if (busNameIndex != -1 && arrivalTimeIndex != -1) {
                String busName = cursor.getString(busNameIndex);
                String arrivalTime = cursor.getString(arrivalTimeIndex);
                nextBus = busName + " at " + arrivalTime;
            }
        }

        cursor.close();
        return nextBus;
    }

    public String getNearestStop(double latitude, double longitude) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name, latitude, longitude FROM locations", null);
        String nearestStop = "No stops available";
        double minDistance = Double.MAX_VALUE;

        if (cursor.moveToFirst()) {
            int latitudeIndex = cursor.getColumnIndex("latitude");
            int longitudeIndex = cursor.getColumnIndex("longitude");
            int nameIndex = cursor.getColumnIndex("name");

            if (latitudeIndex != -1 && longitudeIndex != -1 && nameIndex != -1) {
                while (!cursor.isAfterLast()) {
                    double stopLatitude = cursor.getDouble(latitudeIndex);
                    double stopLongitude = cursor.getDouble(longitudeIndex);
                    double distance = calculateDistance(latitude, longitude, stopLatitude, stopLongitude);

                    if (distance < minDistance) {
                        minDistance = distance;
                        nearestStop = cursor.getString(nameIndex);
                    }
                    cursor.moveToNext();
                }
            }
        }
        cursor.close();
        return nearestStop;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in km
    }
}