package com.ead.zap.ui.operator.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ead.zap.R;
import com.ead.zap.ui.operator.models.SessionHistoryItem;

import java.util.List;

public class SessionHistoryAdapter extends RecyclerView.Adapter<SessionHistoryAdapter.SessionHistoryViewHolder> {

    private List<SessionHistoryItem> historyItems;
    private OnSessionClickListener clickListener;

    public SessionHistoryAdapter(List<SessionHistoryItem> historyItems) {
        this.historyItems = historyItems;
    }

    public interface OnSessionClickListener {
        void onSessionClick(SessionHistoryItem item);
    }

    public void setOnSessionClickListener(OnSessionClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public SessionHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session_history, parent, false);
        return new SessionHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionHistoryViewHolder holder, int position) {
        SessionHistoryItem item = historyItems.get(position);
        holder.bind(item);
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onSessionClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    static class SessionHistoryViewHolder extends RecyclerView.ViewHolder {

        private TextView tvBookingId, tvCustomerName, tvStationSlot, tvDate, tvTimeRange, tvEnergyDelivered, tvStatus;

        public SessionHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookingId = itemView.findViewById(R.id.tv_booking_id);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvStationSlot = itemView.findViewById(R.id.tv_station_slot);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTimeRange = itemView.findViewById(R.id.tv_time_range);
            tvEnergyDelivered = itemView.findViewById(R.id.tv_energy_delivered);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }

        public void bind(SessionHistoryItem item) {
            // Format booking ID as a reference (show only last 6 characters)
            String bookingRef = "Ref: ";
            if (item.getBookingId() != null && item.getBookingId().length() > 6) {
                bookingRef += "..." + item.getBookingId().substring(item.getBookingId().length() - 6);
            } else {
                bookingRef += item.getBookingId();
            }
            tvBookingId.setText(bookingRef);
            
            tvCustomerName.setText(item.getCustomerName());
            tvStationSlot.setText(item.getStationId() + " / " + item.getSlotNumber());
            tvDate.setText(item.getDate());
            tvTimeRange.setText(item.getTimeRange());
            tvEnergyDelivered.setText(item.getEnergyDelivered());
            tvStatus.setText(item.getStatus());
        }
    }
}