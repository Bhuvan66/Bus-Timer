package com.example.bustimer;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class AddBus extends AppCompatActivity {
Button AddBus;
Spinner Tables,Types;
EditText BusName,ArrivalTime,To;
String Type="";

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
        AddBus = findViewById(R.id.BusAddButton);
        Locations locations = Locations.getInstance(this);
        //populate spinner with Type of Buses
        ArrayList<String> busTypes = new ArrayList<>();
        busTypes.add("Express");
        busTypes.add("Local");
        ArrayAdapter<String> Adapterfortypes = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, busTypes);
        Adapterfortypes.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        Types.setAdapter(Adapterfortypes);
        Types.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
               Type = busTypes.get(i);
                //type logic for type extraction
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //populate spinner with Stops
        ArrayList<String> stops = locations.getAllStops();
        ArrayAdapter<String> AdapterforStops = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, stops);
        AdapterforStops.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        Tables.setAdapter(AdapterforStops);
        Tables.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //Table logic for Table extraction
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        AddBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast msgs for empty fields
                if(BusName.getText().toString().isEmpty()){
                    Toast.makeText(AddBus.this, "Please Enter Bus Name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(ArrivalTime.getText().toString().isEmpty()){
                    Toast.makeText(AddBus.this, "Please Enter Arrival Time", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(To.getText().toString().isEmpty()){
                    Toast.makeText(AddBus.this, "Please Enter Destination", Toast.LENGTH_SHORT).show();
                    return;
                }
                //is valid time
                if(!ArrivalTime.getText().toString().matches("([01]?[0-9]|2[0-3]):[0-5][0-9]")){
                    Toast.makeText(AddBus.this, "Please Enter a valid Time", Toast.LENGTH_SHORT).show();
                    return;
                }
                //calling addBus(String Place, String bus_name, String arrival_time, String type, String to) in Locations.java to add Bus
                boolean addedBus = locations.addBus(Tables.getSelectedItem().toString(),BusName.getText().toString(),ArrivalTime.getText().toString(),Type,To.getText().toString());
                if(addedBus){
                    Toast.makeText(AddBus.this, "Bus Added", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(AddBus.this, "Bus Already Exists", Toast.LENGTH_SHORT).show();
                }


            }
        });

    }
}