package com.example.bustimer;

import android.os.Bundle;
import android.provider.CallLog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class AddStop extends AppCompatActivity {
Button addStop;
EditText Place;
Locations locations;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_stop);
        Place = findViewById(R.id.StopText);
        addStop = findViewById(R.id.AddStopButton);
        locations = Locations.getInstance(this);
    addStop.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //if Place is not empty
            if(Place.getText().toString().isEmpty()){
                Toast.makeText(AddStop.this, "Please Enter a Place", Toast.LENGTH_SHORT).show();
                return;
            }
            //calling CreateStopTable(String Place) in Locations.java to create Table for the stop

            boolean createdPlaceTable = locations.CreateStopTable(Place.getText().toString());
            if(createdPlaceTable){
                Toast.makeText(AddStop.this, "Stop" +Place.getText().toString() +" Added", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(AddStop.this, "Stop Already Exists", Toast.LENGTH_SHORT).show();
            }
        }
    });
    }

}