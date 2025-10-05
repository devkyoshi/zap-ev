package com.ead.zap.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ead.zap.R;
import com.ead.zap.models.ChargingStation;

import java.util.List;

public class StationSelectionAdapter extends RecyclerView.Adapter<StationSelectionAdapter.StationViewHolder> {
    
    private Context context;
    private List<ChargingStation> stations;
    private OnStationClickListener listener;

    public interface OnStationClickListener {
        void onStationClick(ChargingStation station);
    }

    public StationSelectionAdapter(Context context, List<ChargingStation> stations, OnStationClickListener listener) {
        this.context = context;
        this.stations = stations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.station_booking_item, parent, false);
        return new StationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StationViewHolder holder, int position) {
        android.util.Log.d("StationSelectionAdapter", "onBindViewHolder called for position: " + position);
        ChargingStation station = stations.get(position);
        holder.bind(station);
    }

    @Override
    public int getItemCount() {
        return stations.size();
    }

    public void updateStations(List<ChargingStation> newStations) {
        android.util.Log.d("StationSelectionAdapter", "updateStations called with " + newStations.size() + " stations");
        this.stations.clear();
        this.stations.addAll(newStations);
        notifyDataSetChanged();
        android.util.Log.d("StationSelectionAdapter", "Adapter updated, current size: " + this.stations.size());
    }

    class StationViewHolder extends RecyclerView.ViewHolder {
        private TextView tvStationName, tvStationAddress, tvDistance, 
                        tvAvailability, tvChargingRate;
        private Button btnBookNow;

        public StationViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvStationName = itemView.findViewById(R.id.tv_station_name);
            tvStationAddress = itemView.findViewById(R.id.tv_station_address);
            tvDistance = itemView.findViewById(R.id.tv_distance);
            tvAvailability = itemView.findViewById(R.id.tv_availability);
            tvChargingRate = itemView.findViewById(R.id.tv_charging_rate);
            btnBookNow = itemView.findViewById(R.id.btn_book_now);
        }

        public void bind(ChargingStation station) {
            tvStationName.setText(station.getName());
            
            if (station.getLocation() != null) {
                tvStationAddress.setText(station.getLocation().getAddress());
            }
            
            // Show distance if available
            if (station.getDistance() != null) {
                tvDistance.setText(station.getDistanceText());
                tvDistance.setVisibility(View.VISIBLE);
            } else {
                tvDistance.setVisibility(View.GONE);
            }
            
            tvAvailability.setText(station.getAvailabilityText());
            tvChargingRate.setText(station.getFormattedPrice());
            
            // Enable/disable book button based on availability
            boolean hasSlots = station.getAvailableSlots() > 0;
            btnBookNow.setEnabled(hasSlots);
            btnBookNow.setText(hasSlots ? "Select Station" : "No Slots Available");
            
            // Set click listener
            btnBookNow.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStationClick(station);
                }
            });
            
            // Also make the whole item clickable
            itemView.setOnClickListener(v -> {
                if (listener != null && hasSlots) {
                    listener.onStationClick(station);
                }
            });
        }
    }
}