package com.example.bustimer;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.AdapterView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bustimer.databinding.TimeToNextBusConfigureBinding;
import com.example.bustimer.ToPlaceAdapter;

import java.util.ArrayList;

/**
 * The configuration screen for the {@link TimeToNextBus TimeToNextBus} AppWidget.
 */
public class TimeToNextBusConfigureActivity extends Activity {
    Switch StaticSwitch;
    TextView To, From;
    Spinner FromSpinner;
    RecyclerView TorecyclerView;
    Button AddWidget;
    Locations locations;
    private static final String PREFS_NAME = "com.example.bustimer.TimeToNextBus";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = TimeToNextBusConfigureActivity.this;

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            TimeToNextBus.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };
    private TimeToNextBusConfigureBinding binding;

    public TimeToNextBusConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveTitlePref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return context.getString(R.string.appwidget_text);
        }
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        binding = TimeToNextBusConfigureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        // Initialize the Locations instance
        locations = Locations.getInstance(this);

        // Get the list of stops
        ArrayList<String> stops = locations.getAllStops();

        // Populate the Spinner
        FromSpinner = findViewById(R.id.FromSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stops);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        FromSpinner.setAdapter(adapter);

        // Set up the RecyclerView
        TorecyclerView = findViewById(R.id.TorecyclerView);
        TorecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Add listener to FromSpinner to update TorecyclerView based on selected place
        FromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedPlace = stops.get(position);
                ArrayList<String> uniqueToEntries = locations.getUniqueToEntries(selectedPlace);
                ToPlaceAdapter toPlaceAdapter = new ToPlaceAdapter(TimeToNextBusConfigureActivity.this, uniqueToEntries);
                TorecyclerView.setAdapter(toPlaceAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
}