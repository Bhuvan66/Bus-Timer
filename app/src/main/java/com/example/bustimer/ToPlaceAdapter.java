package com.example.bustimer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ToPlaceAdapter extends RecyclerView.Adapter<ToPlaceAdapter.ViewHolder> {
    private List<String> places;
    private Context context;

    public ToPlaceAdapter(Context context, List<String> places) {
        this.context = context;
        this.places = places;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.torecycelerview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String place = places.get(position);
        holder.placeName.setText(place);
        holder.checkBox.setChecked(false); // Default unchecked
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView placeName;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            placeName = itemView.findViewById(R.id.placeName);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}
