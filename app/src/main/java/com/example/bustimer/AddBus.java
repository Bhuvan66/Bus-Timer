package com.example.bustimer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class AddBus extends AppCompatActivity {
    private static final int PICK_CSV_FILE = 1;

    Button AddBus, addCsv;
    Spinner Tables, Types;
    EditText BusName, ArrivalTime, To;
    String Type = "";
    TextView file;
    Locations locations;

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
        addCsv = findViewById(R.id.Addcsv);
        file = findViewById(R.id.FilenameText);

        locations = Locations.getInstance(this);

        // Populate spinner with Type of Buses
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Populate spinner with Stops
        ArrayList<String> stops = locations.getAllStops();
        ArrayAdapter<String> AdapterforStops = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, stops);
        AdapterforStops.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        Tables.setAdapter(AdapterforStops);
        Tables.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        AddBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BusName.getText().toString().isEmpty()) {
                    Toast.makeText(AddBus.this, "Please Enter Bus Name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (ArrivalTime.getText().toString().isEmpty()) {
                    Toast.makeText(AddBus.this, "Please Enter Arrival Time", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (To.getText().toString().isEmpty()) {
                    Toast.makeText(AddBus.this, "Please Enter Destination", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!ArrivalTime.getText().toString().matches("([01]?[0-9]|2[0-3]):[0-5][0-9]")) {
                    Toast.makeText(AddBus.this, "Please Enter a valid Time", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean addedBus = locations.addBus(Tables.getSelectedItem().toString(), BusName.getText().toString(), ArrivalTime.getText().toString(), Type, To.getText().toString());
                if (addedBus) {
                    Toast.makeText(AddBus.this, "Bus Added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddBus.this, "Bus Already Exists", Toast.LENGTH_SHORT).show();
                }
            }
        });

        addCsv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "Select CSV File"), PICK_CSV_FILE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CSV_FILE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    String fileName = uri.getLastPathSegment();
                    file.setText(fileName);
                    if (isValidCsvFile(uri)) {
                        Toast.makeText(this, "Valid CSV file selected", Toast.LENGTH_SHORT).show();
                        processCsvFile(uri);
                    } else {
                        Toast.makeText(this, "Invalid CSV file", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private boolean isValidCsvFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine();
            if (line != null && line.contains(",")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void processCsvFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length == 4) {
                    String busName = columns[0].trim();
                    String arrivalTime = columns[1].trim();
                    String type = columns[2].trim();
                    String to = columns[3].trim();
                    boolean addedBus = locations.addBus(Tables.getSelectedItem().toString(), busName, arrivalTime, type, to);
                    if (addedBus) {
                        Toast.makeText(this, "Bus " + busName + " added", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Bus " + busName + " already exists", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Invalid CSV format", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error processing CSV file", Toast.LENGTH_SHORT).show();
        }
    }
}