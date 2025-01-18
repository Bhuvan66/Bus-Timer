package com.example.bustimer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class AddStop extends AppCompatActivity {

    Button addStop;
    EditText Place, latLongText;
    RadioGroup modeGroup;
    RadioButton manualMode, autoMode;
    Locations locations;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_stop);
        Place = findViewById(R.id.StopText);
        addStop = findViewById(R.id.AddStopButton);
        latLongText = findViewById(R.id.latLongText);
        modeGroup = findViewById(R.id.modeGroup);
        manualMode = findViewById(R.id.manualMode);
        autoMode = findViewById(R.id.autoMode);
        locations = Locations.getInstance(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        modeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.manualMode) {
                    latLongText.setVisibility(View.VISIBLE);
                } else if (checkedId == R.id.autoMode) {
                    latLongText.setVisibility(View.GONE);
                }
            }
        });

        addStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Place.getText().toString().isEmpty()) {
                    Toast.makeText(AddStop.this, "Please Enter a Place", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (manualMode.isChecked()) {
                    String latLongStr = latLongText.getText().toString();
                    if (latLongStr.isEmpty()) {
                        Toast.makeText(AddStop.this, "Please Enter Coordinates", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String[] latLong = latLongStr.split(",");
                    if (latLong.length != 2) {
                        Toast.makeText(AddStop.this, "Please Enter Coordinates in the format: Latitude, Longitude", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        double latitude = Double.parseDouble(latLong[0].trim());
                        double longitude = Double.parseDouble(latLong[1].trim());
                        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                            Toast.makeText(AddStop.this, "Please Enter Valid Coordinates", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        addStopToDatabase(latitude, longitude);
                    } catch (NumberFormatException e) {
                        Toast.makeText(AddStop.this, "Please Enter Valid Numbers for Coordinates", Toast.LENGTH_SHORT).show();
                    }
                } else if (autoMode.isChecked()) {
                    if (ActivityCompat.checkSelfPermission(AddStop.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(AddStop.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        fusedLocationClient.getLastLocation()
                                .addOnSuccessListener(AddStop.this, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        if (location != null) {
                                            double latitude = location.getLatitude();
                                            double longitude = location.getLongitude();
                                            addStopToDatabase(latitude, longitude);
                                        }
                                    }
                                });
                    }
                }
            }
        });
    }

    private void addStopToDatabase(double latitude, double longitude) {
        boolean createdPlaceTable = locations.CreateStopTable(Place.getText().toString());
        if (createdPlaceTable) {
            Toast.makeText(AddStop.this, "Stop " + Place.getText().toString() + " Added at Latitude: " + latitude + ", Longitude: " + longitude, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(AddStop.this, "Stop Already Exists", Toast.LENGTH_SHORT).show();
        }
    }
}