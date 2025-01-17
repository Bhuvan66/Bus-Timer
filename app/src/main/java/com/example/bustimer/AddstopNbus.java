package com.example.bustimer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddstopNbus extends AppCompatActivity {
Button addStop, addBus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_addstop_nbus);
        addStop = findViewById(R.id.AddStopButton);
        addBus = findViewById(R.id.AddBusButton);
        addStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              Intent intent = new Intent(AddstopNbus.this, AddStop.class);
                startActivity(intent);
            }
        });
        addBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddstopNbus.this, AddBus.class);
                startActivity(intent);
            }
        });
    }
}