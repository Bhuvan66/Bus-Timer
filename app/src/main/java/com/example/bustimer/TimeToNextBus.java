package com.example.bustimer;

import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.bustimer.UpdateWidgetReceiver;

public class TimeToNextBus extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.time_to_next_bus);

        SharedPreferences prefs = context.getSharedPreferences(TimeToNextBusConfigureActivity.getPrefsName(), Context.MODE_PRIVATE);
        boolean isDynamic = prefs.getBoolean(TimeToNextBusConfigureActivity.getPrefPrefixKey() + appWidgetId + "_dynamic", false);

        if (isDynamic) {
            // Get the current location
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Request location permissions if not granted
                Toast.makeText(context, "Location permissions are not granted", Toast.LENGTH_SHORT).show();
                return;
            }

            Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (currentLocation != null) {
                double currentLatitude = currentLocation.getLatitude();
                double currentLongitude = currentLocation.getLongitude();

                // Find the nearest bus stop
                Locations locations = Locations.getInstance(context);
                String nearestStop = locations.getNearestStop(currentLatitude, currentLongitude);
                String to = prefs.getString(TimeToNextBusConfigureActivity.getPrefPrefixKey() + appWidgetId + "_to", "Default To");
                String type = prefs.getString(TimeToNextBusConfigureActivity.getPrefPrefixKey() + appWidgetId + "_type", "Default Type");

                String nextBus = locations.getNextAvailableBus(nearestStop, to, type);
                if (!nextBus.equals("No available buses")) {
                    views.setTextViewText(R.id.appwidget_text, nextBus);
                } else {
                    views.setTextViewText(R.id.appwidget_text, "No available buses");
                }
            } else {
                views.setTextViewText(R.id.appwidget_text, "Unable to get current location");
            }
        } else {
            String from = prefs.getString(TimeToNextBusConfigureActivity.getPrefPrefixKey() + appWidgetId + "_from", "Default From");
            String to = prefs.getString(TimeToNextBusConfigureActivity.getPrefPrefixKey() + appWidgetId + "_to", "Default To");
            String type = prefs.getString(TimeToNextBusConfigureActivity.getPrefPrefixKey() + appWidgetId + "_type", "Default Type");

            Locations locations = Locations.getInstance(context);
            String nextBus = locations.getNextAvailableBus(from, to, type);
            if (!nextBus.equals("No available buses")) {
                views.setTextViewText(R.id.appwidget_text, nextBus);
            } else {
                views.setTextViewText(R.id.appwidget_text, "No available buses");
            }
        }

        Intent intent = new Intent(context, TimeToNextBusConfigureActivity.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            TimeToNextBusConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        UpdateWidgetReceiver.setUpdateAlarm(context);
    }

    @Override
    public void onDisabled(Context context) {
        UpdateWidgetReceiver.cancelUpdateAlarm(context);
    }
}