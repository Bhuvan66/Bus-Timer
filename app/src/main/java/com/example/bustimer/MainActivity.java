package com.example.bustimer;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
ArrayList<String> tableNames, busNames, arrivalTimes, types;
RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Locations locations = Locations.getInstance(this);
        CustomAdapter customAdapter;

        //initialze arraylits
        busNames = new ArrayList<>();
        arrivalTimes = new ArrayList<>();
        types = new ArrayList<>();
        recyclerView = findViewById(R.id.RecycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddStop.class);
                startActivity(intent);
            }
        });
        tableNames = locations.getAllStops();
        //display all data with table name
        for (String tableName : tableNames) {
            busNames.add("Table: " + tableName);
            arrivalTimes.add("");
            types.add("");
            //fetch data from table
            //display data in recycler view
            Cursor cursor = locations.getBuses(tableName);
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    int busNameIndex = cursor.getColumnIndex("bus_name");
                    int arrivalTimeIndex = cursor.getColumnIndex("arrival_time");
                    int typeIndex = cursor.getColumnIndex("type");

                    if (busNameIndex != -1 && arrivalTimeIndex != -1 && typeIndex != -1) {
                        busNames.add(cursor.getString(busNameIndex));
                        arrivalTimes.add(cursor.getString(arrivalTimeIndex));
                        types.add(cursor.getString(typeIndex));
                    }
                    cursor.moveToNext();
                }
            }
        }

        customAdapter = new CustomAdapter(this, busNames, arrivalTimes, types);
        recyclerView.setAdapter(customAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
}