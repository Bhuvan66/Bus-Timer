package com.example.bustimer;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bustimer.databinding.TimeToNextBusConfigureBinding;

import java.util.ArrayList;
import java.util.List;

public class TimeToNextBusConfigureActivity extends Activity {
    Switch DynamicSwitch;
    TextView To, From, Type;
    Spinner FromSpinner, TypeSpinner;
    RecyclerView TorecyclerView;
    Button AddWidget;
    Locations locations;
    private static final String PREFS_NAME = "com.example.bustimer.TimeToNextBus";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    ToPlaceAdapter toPlaceAdapter;
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = TimeToNextBusConfigureActivity.this;

            String from = FromSpinner.getSelectedItem().toString();
            String type = TypeSpinner.getSelectedItem().toString();
            List<String> checkedTos = toPlaceAdapter.getCheckedItems();

            if (checkedTos.isEmpty()) {
                Toast.makeText(context, "Please select at least one 'To' location", Toast.LENGTH_SHORT).show();
                return;
            }

            String to = String.join(",", checkedTos);

            saveWidgetDetails(mAppWidgetId, from, to, type);

            updateWidget(context, mAppWidgetId);

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

    static void saveTitlePref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.apply();
    }

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

    public static String getPrefsName() {
        return PREFS_NAME;
    }

    public static String getPrefPrefixKey() {
        return PREF_PREFIX_KEY;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setResult(RESULT_CANCELED);

        binding = TimeToNextBusConfigureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        From = findViewById(R.id.FromTextView);
        To = findViewById(R.id.ToTextView);
        DynamicSwitch = findViewById(R.id.DynamicSwitch);
        Type = findViewById(R.id.TypeTextView);
        AddWidget = findViewById(R.id.AddWidgetButton);
        TypeSpinner = findViewById(R.id.TypeSpinner);
        FromSpinner = findViewById(R.id.FromSpinner);
        TorecyclerView = findViewById(R.id.TorecyclerView);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        locations = Locations.getInstance(this);

        ArrayList<String> stops = locations.getAllStops();

        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.types, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        TypeSpinner.setAdapter(adapter1);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stops);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        FromSpinner.setAdapter(adapter);

        TorecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set an empty adapter initially to avoid the "No adapter attached" error
        toPlaceAdapter = new ToPlaceAdapter(this, new ArrayList<>());
        TorecyclerView.setAdapter(toPlaceAdapter);

        FromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedPlace = stops.get(position);
                ArrayList<String> uniqueToEntries = locations.getUniqueToEntries(selectedPlace);
                toPlaceAdapter = new ToPlaceAdapter(TimeToNextBusConfigureActivity.this, uniqueToEntries);
                TorecyclerView.setAdapter(toPlaceAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        DynamicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                From.setVisibility(View.GONE);
                FromSpinner.setVisibility(View.GONE);
                animateComponentsUp();

                // Get the current location
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                    return;
                }

                Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (currentLocation != null) {
                    double currentLatitude = currentLocation.getLatitude();
                    double currentLongitude = currentLocation.getLongitude();

                    // Find the nearest bus stop
                    String nearestStop = locations.getNearestStop(currentLatitude, currentLongitude);
                    ArrayList<String> uniqueToEntries = locations.getAllUniqueToEntries();
                    toPlaceAdapter = new ToPlaceAdapter(TimeToNextBusConfigureActivity.this, uniqueToEntries);
                    TorecyclerView.setAdapter(toPlaceAdapter);
                } else {
                    Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            } else {
                From.setVisibility(View.VISIBLE);
                FromSpinner.setVisibility(View.VISIBLE);
                animateComponentsDown();
                String selectedPlace = (String) FromSpinner.getSelectedItem();
                if (selectedPlace != null) {
                    ArrayList<String> uniqueToEntries = locations.getUniqueToEntries(selectedPlace);
                    toPlaceAdapter = new ToPlaceAdapter(TimeToNextBusConfigureActivity.this, uniqueToEntries);
                    TorecyclerView.setAdapter(toPlaceAdapter);
                }
            }
        });

        AddWidget.setOnClickListener(mOnClickListener);
    }
    private void animateComponentsUp() {
        To.animate().translationYBy(-From.getHeight()).setDuration(300).start();
        Type.animate().translationYBy(-From.getHeight()).setDuration(300).start();
        TypeSpinner.animate().translationYBy(-From.getHeight()).setDuration(300).start();
        TorecyclerView.animate().translationYBy(-From.getHeight()).setDuration(300).start();
        AddWidget.animate().translationYBy(-From.getHeight()).setDuration(300).start();
    }

    private void animateComponentsDown() {
        To.animate().translationYBy(From.getHeight()).setDuration(300).start();
        Type.animate().translationYBy(From.getHeight()).setDuration(300).start();
        TypeSpinner.animate().translationYBy(From.getHeight()).setDuration(300).start();
        TorecyclerView.animate().translationYBy(From.getHeight()).setDuration(300).start();
        AddWidget.animate().translationYBy(From.getHeight()).setDuration(300).start();
    }

    private void saveWidgetDetails(int appWidgetId, String from, String to, String type) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_PREFIX_KEY + appWidgetId + "_from", from);
        editor.putString(PREF_PREFIX_KEY + appWidgetId + "_to", to);
        editor.putString(PREF_PREFIX_KEY + appWidgetId + "_type", type);
        editor.apply();
    }

    private void updateWidget(Context context, int appWidgetId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.time_to_next_bus);

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String from = prefs.getString(PREF_PREFIX_KEY + appWidgetId + "_from", "Default From");
        String to = prefs.getString(PREF_PREFIX_KEY + appWidgetId + "_to", "Default To");
        String type = prefs.getString(PREF_PREFIX_KEY + appWidgetId + "_type", "Default Type");

        Locations locations = Locations.getInstance(context);
        String nextBus = locations.getNextAvailableBus(from, to, type);

        views.setTextViewText(R.id.appwidget_text, nextBus);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}