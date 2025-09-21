package com.ead.zap.ui.owner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.TextView;

import com.ead.zap.R;

import java.util.ArrayList;
import java.util.List;

public class OwnerBookingsFragment extends Fragment {

    private RecyclerView bookingsRecyclerView;
    private BookingsAdapter bookingsAdapter;
    private Button btnUpcoming, btnPast;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_owner_bookings, container, false);

        // Initialize views
        bookingsRecyclerView = view.findViewById(R.id.bookingsRecyclerView);
        btnUpcoming = view.findViewById(R.id.btnUpcoming);
        btnPast = view.findViewById(R.id.btnPast);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup RecyclerView
        bookingsAdapter = new BookingsAdapter();
        bookingsRecyclerView.setAdapter(bookingsAdapter);
        bookingsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Default load
        loadUpcomingBookings();
        setActiveTab(true);

        // Handle toggle buttons
        btnUpcoming.setOnClickListener(v -> {
            loadUpcomingBookings();
            setActiveTab(true);
        });

        btnPast.setOnClickListener(v -> {
            loadPastBookings();
            setActiveTab(false);
        });
    }

    private void loadUpcomingBookings() {
        List<Booking> bookings = new ArrayList<>();
        bookings.add(new Booking("EcoCharge Hub - Central", "Approved", "2024-07-25", "14:00 - 15:00"));
        bookings.add(new Booking("PowerUp Point - West", "Pending", "2024-07-26", "09:30 - 10:30"));
        bookings.add(new Booking("Volt Oasis - North", "Approved", "2024-07-27", "18:00 - 19:00"));

        bookingsAdapter.submitList(bookings);
    }

    private void loadPastBookings() {
        List<Booking> pastBookings = new ArrayList<>();
        pastBookings.add(new Booking("GreenCharge - East", "Completed", "2024-06-15", "11:00 - 12:00"));
        pastBookings.add(new Booking("ChargePoint - South", "Cancelled", "2024-06-10", "16:00 - 17:00"));

        bookingsAdapter.submitList(pastBookings);
    }

    private void setActiveTab(boolean isUpcoming) {
        if (isUpcoming) {
            btnUpcoming.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.primary_light));
            btnUpcoming.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

            btnPast.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.accent));
            btnPast.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        } else {
            btnPast.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.primary_light));
            btnPast.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

            btnUpcoming.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.accent));
            btnUpcoming.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        }
    }

    // Booking data class
    public static class Booking {
        private String stationName;
        private String status;
        private String date;
        private String time;

        public Booking(String stationName, String status, String date, String time) {
            this.stationName = stationName;
            this.status = status;
            this.date = date;
            this.time = time;
        }

        public String getStationName() { return stationName; }
        public String getStatus() { return status; }
        public String getDate() { return date; }
        public String getTime() { return time; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Booking booking = (Booking) o;
            return stationName.equals(booking.stationName) &&
                    date.equals(booking.date);
        }

        @Override
        public int hashCode() {
            int result = stationName.hashCode();
            result = 31 * result + date.hashCode();
            return result;
        }
    }

    // BookingsAdapter
    public static class BookingsAdapter extends ListAdapter<Booking, BookingsAdapter.BookingViewHolder> {

        protected BookingsAdapter() {
            super(new DiffCallback());
        }

        @NonNull
        @Override
        public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.booking_item, parent, false);
            return new BookingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
            Booking booking = getItem(position);
            holder.bind(booking);
        }

        static class BookingViewHolder extends RecyclerView.ViewHolder {
            private final TextView stationName;
            private final TextView bookingStatus;
            private final TextView bookingDate;
            private final TextView bookingTime;

            public BookingViewHolder(@NonNull View itemView) {
                super(itemView);
                stationName = itemView.findViewById(R.id.stationName);
                bookingStatus = itemView.findViewById(R.id.bookingStatus);
                bookingDate = itemView.findViewById(R.id.bookingDate);
                bookingTime = itemView.findViewById(R.id.bookingTime);
            }

            public void bind(Booking booking) {
                stationName.setText(booking.getStationName());
                bookingStatus.setText(booking.getStatus());
                bookingDate.setText(booking.getDate());
                bookingTime.setText(booking.getTime());

                // Set status color based on approval state
                switch (booking.getStatus()) {
                    case "Approved":
                        bookingStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.primary_light));
                        break;
                    case "Pending":
                        bookingStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.amber_600));
                        break;
                    case "Completed":
                        bookingStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.gray_600));
                        break;
                    case "Cancelled":
                        bookingStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.red_600));
                        break;
                    default:
                        bookingStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.black));
                        break;
                }
            }
        }
    }

    // DiffCallback
    static class DiffCallback extends DiffUtil.ItemCallback<Booking> {
        @Override
        public boolean areItemsTheSame(@NonNull Booking oldItem, @NonNull Booking newItem) {
            return oldItem.getStationName().equals(newItem.getStationName()) &&
                    oldItem.getDate().equals(newItem.getDate());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Booking oldItem, @NonNull Booking newItem) {
            return oldItem.equals(newItem);
        }
    }
}
