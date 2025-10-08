package com.ead.zap.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ead.zap.R;
import com.ead.zap.models.Vehicle;

import java.util.List;

/**
 * Adapter for vehicle selection in booking activities
 * Uses radio buttons for single selection
 */
public class VehicleSelectionAdapter extends RecyclerView.Adapter<VehicleSelectionAdapter.VehicleViewHolder> {

    private List<Vehicle> vehicleList;
    private int selectedPosition = -1;
    private OnVehicleSelectedListener listener;

    public interface OnVehicleSelectedListener {
        void onVehicleSelected(Vehicle vehicle);
    }

    public VehicleSelectionAdapter(List<Vehicle> vehicleList) {
        this.vehicleList = vehicleList;
    }

    public VehicleSelectionAdapter(List<Vehicle> vehicleList, OnVehicleSelectedListener listener) {
        this.vehicleList = vehicleList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehicle_selection, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        Vehicle vehicle = vehicleList.get(position);

        holder.tvVehicleName.setText(vehicle.getName());
        holder.tvPlateNumber.setText(vehicle.getPlateNumber());
        holder.tvVehicleType.setText(vehicle.getType());

        // Radio button selection
        holder.radioButton.setChecked(position == selectedPosition);
        holder.itemView.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            
            // Notify the old and new positions to update radio buttons
            if (oldPosition != -1) {
                notifyItemChanged(oldPosition);
            }
            notifyItemChanged(selectedPosition);
            
            // Notify listener of selection
            if (listener != null && selectedPosition < vehicleList.size()) {
                listener.onVehicleSelected(vehicleList.get(selectedPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return vehicleList != null ? vehicleList.size() : 0;
    }

    public Vehicle getSelectedVehicle() {
        if (selectedPosition >= 0 && selectedPosition < vehicleList.size()) {
            return vehicleList.get(selectedPosition);
        }
        return null;
    }

    public void updateVehicles(List<Vehicle> newVehicles) {
        this.vehicleList = newVehicles;
        selectedPosition = -1; // Reset selection
        notifyDataSetChanged();
    }

    static class VehicleViewHolder extends RecyclerView.ViewHolder {
        TextView tvVehicleName, tvPlateNumber, tvVehicleType;
        RadioButton radioButton;

        VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVehicleName = itemView.findViewById(R.id.tv_vehicle_name);
            tvPlateNumber = itemView.findViewById(R.id.tv_plate_number);
            tvVehicleType = itemView.findViewById(R.id.tv_vehicle_type);
            radioButton = itemView.findViewById(R.id.rb_select_vehicle);
            
            // Prevent radio button from handling clicks independently
            radioButton.setOnClickListener(v -> itemView.performClick());
        }
    }
}