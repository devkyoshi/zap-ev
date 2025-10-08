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

public class StationMapListAdapter extends RecyclerView.Adapter<StationMapListAdapter.StationViewHolder> {
    
    private Context context;
    private List<ChargingStation> stations;
    private OnStationClickListener listener;

    public interface OnStationClickListener {
        void onStationClick(ChargingStation station);
        void onNavigateClick(ChargingStation station);
    }

    public StationMapListAdapter(Context context, List<ChargingStation> stations, OnStationClickListener listener) {
        this.context = context;
        this.stations = stations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_station_map_list, parent, false);
        return new StationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StationViewHolder holder, int position) {
        android.util.Log.d("StationMapListAdapter", "Binding station at position: " + position);
        ChargingStation station = stations.get(position);
        android.util.Log.d("StationMapListAdapter", "Station name: " + station.getName());
        holder.bind(station);
    }

    @Override
    public int getItemCount() {
        int count = stations.size();
        android.util.Log.d("StationMapListAdapter", "getItemCount: " + count);
        return count;
    }

    public void updateStations(List<ChargingStation> newStations) {
        // Create a new list to avoid reference issues
        this.stations.clear();
        if (newStations != null) {
            this.stations.addAll(newStations);
        }
        notifyDataSetChanged();
    }

    class StationViewHolder extends RecyclerView.ViewHolder {
        private TextView tvStationName, tvStationAddress, tvDistance, 
                        tvAvailability, tvPrice, tvStatus;
        private Button btnNavigate;
        private View statusIndicator;

        public StationViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvStationName = itemView.findViewById(R.id.tv_station_name);
            tvStationAddress = itemView.findViewById(R.id.tv_station_address);
            tvDistance = itemView.findViewById(R.id.tv_distance);
            tvAvailability = itemView.findViewById(R.id.tv_availability);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnNavigate = itemView.findViewById(R.id.btn_navigate);
            statusIndicator = itemView.findViewById(R.id.status_indicator);
        }

        public void bind(ChargingStation station) {
            android.util.Log.d("StationViewHolder", "Binding station: " + station.getName());
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
            tvPrice.setText(station.getFormattedPrice());
            
            // Set status and availability
            boolean hasSlots = station.getAvailableSlots() > 0;
            if (hasSlots) {
                tvStatus.setText("Available");
                tvStatus.setTextColor(context.getColor(R.color.primary_light));
                statusIndicator.setBackgroundTintList(
                    context.getColorStateList(R.color.primary_light));
            } else {
                tvStatus.setText("Full");
                tvStatus.setTextColor(context.getColor(R.color.red_600));
                statusIndicator.setBackgroundTintList(
                    context.getColorStateList(R.color.red_600));
            }
            
            // Set click listeners
            btnNavigate.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNavigateClick(station);
                }
            });
            
            // Make the whole item clickable
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStationClick(station);
                }
            });
        }
    }
}