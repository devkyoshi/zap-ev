package com.ead.zap.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ead.zap.R;
import com.ead.zap.models.VehicleDetail;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * Adapter for displaying vehicle list in profile
 */
public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

    private List<VehicleDetail> vehicles;
    private OnVehicleActionListener listener;

    public interface OnVehicleActionListener {
        void onEditVehicle(VehicleDetail vehicle, int position);
        void onDeleteVehicle(VehicleDetail vehicle, int position);
    }

    public VehicleAdapter(List<VehicleDetail> vehicles, OnVehicleActionListener listener) {
        this.vehicles = vehicles;
        this.listener = listener;
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
        VehicleDetail vehicle = vehicles.get(position);
        holder.bind(vehicle, position);
    }

    @Override
    public int getItemCount() {
        return vehicles != null ? vehicles.size() : 0;
    }

    public void updateVehicles(List<VehicleDetail> newVehicles) {
        this.vehicles = newVehicles;
        notifyDataSetChanged();
    }

    public void addVehicle(VehicleDetail vehicle) {
        if (vehicles != null) {
            vehicles.add(vehicle);
            notifyItemInserted(vehicles.size() - 1);
        }
    }

    public void updateVehicle(int position, VehicleDetail vehicle) {
        if (vehicles != null && position >= 0 && position < vehicles.size()) {
            vehicles.set(position, vehicle);
            notifyItemChanged(position);
        }
    }

    public void removeVehicle(int position) {
        if (vehicles != null && position >= 0 && position < vehicles.size()) {
            vehicles.remove(position);
            notifyItemRemoved(position);
            // Notify item range changed to update positions of remaining items
            notifyItemRangeChanged(position, vehicles.size() - position);
        }
    }

    class VehicleViewHolder extends RecyclerView.ViewHolder {
        private TextView tvVehicleName;
        private TextView tvLicensePlate;
        private TextView tvVehicleDetails;
        private MaterialButton btnEdit;
        private MaterialButton btnDelete;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVehicleName = itemView.findViewById(R.id.tvVehicleName);
            tvLicensePlate = itemView.findViewById(R.id.tvLicensePlate);
            tvVehicleDetails = itemView.findViewById(R.id.tvVehicleDetails);
            btnEdit = itemView.findViewById(R.id.btnEditVehicle);
            btnDelete = itemView.findViewById(R.id.btnDeleteVehicle);
        }

        public void bind(VehicleDetail vehicle, int position) {
            if (vehicle != null) {
                // Set vehicle name (year + make + model)
                tvVehicleName.setText(vehicle.getVehicleDisplayName());
                
                // Set license plate
                tvLicensePlate.setText(vehicle.getLicensePlate() != null ? 
                    vehicle.getLicensePlate() : "No license plate");
                
                // Set vehicle details
                String details = "Make: " + (vehicle.getMake() != null ? vehicle.getMake() : "Unknown") +
                               " • Model: " + (vehicle.getModel() != null ? vehicle.getModel() : "Unknown");
                tvVehicleDetails.setText(details);
                
                // Set click listeners using current adapter position
                btnEdit.setOnClickListener(v -> {
                    if (listener != null) {
                        int currentPosition = getAdapterPosition();
                        if (currentPosition != RecyclerView.NO_POSITION) {
                            listener.onEditVehicle(vehicles.get(currentPosition), currentPosition);
                        }
                    }
                });
                
                btnDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        int currentPosition = getAdapterPosition();
                        if (currentPosition != RecyclerView.NO_POSITION) {
                            listener.onDeleteVehicle(vehicles.get(currentPosition), currentPosition);
                        }
                    }
                });
            }
        }
    }
}