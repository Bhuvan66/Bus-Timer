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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {


    ArrayList<String> tableNames, busNames, arrivalTimes, types, tos;
    RecyclerView recyclerView;
    CustomAdapter customAdapter;
    SwipeRefreshLayout swipeRefreshLayout ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Locations locations = Locations.getInstance(this);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        //initialze arraylits
        busNames = new ArrayList<>();
        arrivalTimes = new ArrayList<>();
        types = new ArrayList<>();
        tos = new ArrayList<>();
        recyclerView = findViewById(R.id.RecycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddstopNbus.class);
                startActivity(intent);
            }
        });
        tableNames = locations.getAllStops();
        if (tableNames.isEmpty()) {
            // Create tables and add dummy data
            locations.CreateStopTable("karkala");
            tableNames = locations.getAllStops();
        }
        //display all data with table name
        LoadData(locations);

        customAdapter = new CustomAdapter(this, busNames, arrivalTimes, types, tos);
        recyclerView.setAdapter(customAdapter);
        swipeRefreshLayout.setOnRefreshListener(this);

    }

    private void LoadData(Locations locations) {
        tableNames = locations.getAllStops();
        for (String tableName : tableNames) {
            busNames.add("Table: " + tableName);
            arrivalTimes.add("");
            types.add("");
            tos.add("");
            //fetch data from table
            //display data in recycler view
            Cursor cursor = locations.getBuses(tableName);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int busNameIndex = cursor.getColumnIndex("bus_name");
                    int arrivalTimeIndex = cursor.getColumnIndex("arrival_time");
                    int typeIndex = cursor.getColumnIndex("type");
                    int toIndex = cursor.getColumnIndex("to");

                    if (busNameIndex != -1 && arrivalTimeIndex != -1 && typeIndex != -1 && toIndex != -1) {
                        busNames.add(cursor.getString(busNameIndex));
                        arrivalTimes.add(cursor.getString(arrivalTimeIndex));
                        types.add(cursor.getString(typeIndex));
                        tos.add(cursor.getString(toIndex));
                    }
                } while (cursor.moveToNext());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRefresh() {
        // Refresh data in recycler view
        Locations locations = Locations.getInstance(this);
        //empty arraylist
        busNames.clear();
        arrivalTimes.clear();
        types.clear();
        tos.clear();
        LoadData(locations);
        customAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);

    }
}