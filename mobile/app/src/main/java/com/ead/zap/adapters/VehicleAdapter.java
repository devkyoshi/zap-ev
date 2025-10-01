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

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

    private List<Vehicle> vehicleList;
    private int selectedPosition = -1;

    public VehicleAdapter(List<Vehicle> vehicleList) {
        this.vehicleList = vehicleList;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehicle, parent, false);
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
            selectedPosition = holder.getAdapterPosition();
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    public Vehicle getSelectedVehicle() {
        if (selectedPosition >= 0 && selectedPosition < vehicleList.size()) {
            return vehicleList.get(selectedPosition);
        }
        return null;
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
        }
    }
}