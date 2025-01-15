package com.example.bustimer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<String> busNames, arrivalTimes, types, tos;


    public CustomAdapter(Context context, ArrayList<String> busNames, ArrayList<String> arrivalTimes, ArrayList<String> types, ArrayList<String> tos) {
        this.context = context;
        this.busNames = busNames;
        this.arrivalTimes = arrivalTimes;
        this.types = types;
        this.tos = tos;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.busName.setText(busNames.get(position));
        holder.arrivalTime.setText(arrivalTimes.get(position));
        holder.type.setText(types.get(position));
        holder.to.setText(tos.get(position));
    }

    @Override
    public int getItemCount() {
        return busNames.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView busName, arrivalTime, type, to;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            busName = itemView.findViewById(R.id.textView);
            arrivalTime = itemView.findViewById(R.id.textView2);
            type = itemView.findViewById(R.id.textView3);
            to = itemView.findViewById(R.id.textView6);
        }
    }
}
