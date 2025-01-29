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
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.Calendar;

public class TimeToNextBus extends AppWidgetProvider {
    private static CountDownTimer activeTimer;
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.time_to_next_bus);

        SharedPreferences prefs = context.getSharedPreferences(TimeToNextBusConfigureActivity.getPrefsName(), Context.MODE_PRIVATE);
        boolean isDynamic = prefs.getBoolean(TimeToNextBusConfigureActivity.getPrefPrefixKey() + appWidgetId + "_dynamic", false);

        Locations locations = Locations.getInstance(context);
        String nextBus;

        if (isDynamic) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Location permissions are not granted", Toast.LENGTH_SHORT).show();
                return;
            }

            Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (currentLocation != null) {
                double currentLatitude = currentLocation.getLatitude();
                double currentLongitude = currentLocation.getLongitude();
                String nearestStop = locations.getNearestStop(currentLatitude, currentLongitude);
                String to = prefs.getString(TimeToNextBusConfigureActivity.getPrefPrefixKey() + appWidgetId + "_to", "Default To");
                String type = prefs.getString(TimeToNextBusConfigureActivity.getPrefPrefixKey() + appWidgetId + "_type", "Default Type");

                nextBus = locations.getNextAvailableBus(nearestStop, to, type);
            } else {
                nextBus = "Unable to get current location";
            }
        } else {
            String from = prefs.getString(TimeToNextBusConfigureActivity.getPrefPrefixKey() + appWidgetId + "_from", "Default From");
            String to = prefs.getString(TimeToNextBusConfigureActivity.getPrefPrefixKey() + appWidgetId + "_to", "Default To");
            String type = prefs.getString(TimeToNextBusConfigureActivity.getPrefPrefixKey() + appWidgetId + "_type", "Default Type");

            Log.e("TimeToNextBus", "From: " + from + ", To: " + to + ", Type: " + type);
            nextBus = locations.getNextAvailableBus(from, to, type);
        }

        if (!nextBus.equals("No available buses")) {
            views.setTextViewText(R.id.appwidget_text, nextBus);
            try {
                String[] timeParts = nextBus.split(" at ")[1].split(":");
                int busHour = Integer.parseInt(timeParts[0]);
                int busMinute = Integer.parseInt(timeParts[1]);

                Calendar now = Calendar.getInstance();
                Calendar busTime = Calendar.getInstance();
                busTime.set(Calendar.HOUR_OF_DAY, busHour);
                busTime.set(Calendar.MINUTE, busMinute);
                busTime.set(Calendar.SECOND, 0);
                busTime.set(Calendar.MILLISECOND, 0);

                if (busTime.before(now)) {
                    busTime.add(Calendar.DAY_OF_MONTH, 1);
                }

                long countdownMillis = busTime.getTimeInMillis() - now.getTimeInMillis();

                // Cancel previous timer if it exists
                if (activeTimer != null) {
                    activeTimer.cancel();
                    activeTimer = null;
                }

                if (countdownMillis > 0) {
                    activeTimer = new CountDownTimer(countdownMillis, 1000) {
                        public void onTick(long millisUntilFinished) {
                            long minutes = (millisUntilFinished / 1000) / 60;
                            long seconds = (millisUntilFinished / 1000) % 60;
                            String countdownText = context.getString(R.string.countdown_format, minutes, seconds);
                            views.setTextViewText(R.id.Countdown, countdownText);
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                        }

                        public void onFinish() {
                            views.setTextViewText(R.id.Countdown, context.getString(R.string.bus_arrived));
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                            // Fetch the next bus
                            updateAppWidget(context, appWidgetManager, appWidgetId);
                        }
                    }.start();

                    // Force an immediate update by calling onTick manually
                    activeTimer.onTick(countdownMillis);
                } else {
                    views.setTextViewText(R.id.Countdown, "Invalid countdown");
                }
            } catch (Exception e) {
                Log.e("TimeToNextBus", "Error parsing bus time: " + e.getMessage());
                views.setTextViewText(R.id.Countdown, "Error with bus time");
            }
        } else {
            views.setTextViewText(R.id.appwidget_text, "No available buses");
        }

        // Ensure an immediate update after setting countdown
        appWidgetManager.updateAppWidget(appWidgetId, views);

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