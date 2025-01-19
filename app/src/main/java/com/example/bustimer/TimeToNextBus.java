// TimeToNextBus.java
package com.example.bustimer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import java.util.ArrayList;

public class TimeToNextBus extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(TimeToNextBusConfigureActivity.getPrefsName(), Context.MODE_PRIVATE);
        String from = prefs.getString(TimeToNextBusConfigureActivity.getPrefPrefixKey() + appWidgetId + "_from", null);
        String to = prefs.getString(TimeToNextBusConfigureActivity.getPrefPrefixKey() + appWidgetId + "_to", null);
        String type = prefs.getString(TimeToNextBusConfigureActivity.getPrefPrefixKey() + appWidgetId + "_type", null);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.time_to_next_bus);

        if (from == null || to == null || type == null) {
            views.setTextViewText(R.id.appwidget_text, "Error: Missing configuration");
        } else {
            Locations locations = Locations.getInstance(context);
            ArrayList<String> validPlaces = locations.getAllStops();

            if (!validPlaces.contains(from)) {
                from = validPlaces.isEmpty() ? "" : validPlaces.get(0);
            }

            String nextBus = locations.getNextAvailableBus(from, to, type);
            views.setTextViewText(R.id.appwidget_text, nextBus);
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
    }

    @Override
    public void onDisabled(Context context) {
    }
}