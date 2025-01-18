package com.example.bustimer;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class AddStop extends AppCompatActivity {

    Button addStop,Maps;
    EditText Place, latLongText;
    CheckBox autoModeCheckBox;
    Locations locations;
    private FusedLocationProviderClient fusedLocationClient;
    TextView LocationText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_stop);
        Place = findViewById(R.id.StopText);
        addStop = findViewById(R.id.AddStopButton);
        latLongText = findViewById(R.id.latLongText);
        LocationText = findViewById(R.id.LocationTextViews);
        autoModeCheckBox = findViewById(R.id.autoModeCheckBox);
        locations = Locations.getInstance(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Maps = findViewById(R.id.GetLocationLatNLong);

        Maps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //send to google maps
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="));
                intent.setPackage("com.google.android.apps.maps");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(AddStop.this, "Google Maps is not installed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        autoModeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                latLongText.setVisibility(View.GONE);
                LocationText.setVisibility(View.GONE);
                animateView(addStop, -100f); // Move up by 100 pixels
                animateView(autoModeCheckBox, -100f); // Move up by 100 pixels
            } else {
                latLongText.setVisibility(View.VISIBLE);
                LocationText.setVisibility(View.VISIBLE);
                animateView(addStop, 0f); // Move back to original position
                animateView(autoModeCheckBox, 0f); // Move back to original position
            }
        });

        addStop.setOnClickListener(view -> {
            if (Place.getText().toString().isEmpty()) {
                Toast.makeText(AddStop.this, "Please Enter a Place", Toast.LENGTH_SHORT).show();
                return;
            }

            if (autoModeCheckBox.isChecked()) {
                if (ActivityCompat.checkSelfPermission(AddStop.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(AddStop.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(AddStop.this, location -> {
                                if (location != null) {
                                    double latitude = location.getLatitude();
                                    double longitude = location.getLongitude();
                                    addStopToDatabase(latitude, longitude);
                                }
                            });
                }
            } else {
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
            }
        });
    }

    private void animateView(View view, float translationY) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", translationY);
        animator.setDuration(300); // Animation duration in milliseconds
        animator.start();
    }

    private void addStopToDatabase(double latitude, double longitude) {
        boolean createdPlaceTable = locations.CreateStopTable(Place.getText().toString(), latitude, longitude);
        if (createdPlaceTable) {
            Toast.makeText(AddStop.this, "Stop " + Place.getText().toString() + " Added at Latitude: " + latitude + ", Longitude: " + longitude, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(AddStop.this, "Stop Already Exists", Toast.LENGTH_SHORT).show();
        }
    }
}