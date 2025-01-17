package com.example.bustimer;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddBus extends AppCompatActivity {
Button addbus;
Spinner Tables,Types;
EditText BusName,ArrivalTime,To;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_bus);
        Tables = findViewById(R.id.TableListSpinner);
        Types = findViewById(R.id.BusTypeSpinner);
        BusName = findViewById(R.id.BusNameText);
        ArrivalTime = findViewById(R.id.ArivalTimeText);
        To = findViewById(R.id.busToText);
        addbus = findViewById(R.id.BusAddButton);

    }
}