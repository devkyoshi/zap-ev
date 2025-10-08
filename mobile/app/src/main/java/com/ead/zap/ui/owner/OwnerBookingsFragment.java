package com.ead.zap.ui.owner;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

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
import com.ead.zap.models.Booking;
import com.ead.zap.services.BookingService;
import com.ead.zap.ui.owner.modals.CreateBookingActivity;
import com.ead.zap.ui.owner.modals.ModifyReservationActivity;
import com.ead.zap.ui.owner.modals.CancelReservationActivity;
import com.ead.zap.ui.owner.modals.QRCodeActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class OwnerBookingsFragment extends Fragment {

    private RecyclerView bookingsRecyclerView;
    private BookingsAdapter bookingsAdapter;
    private Button btnUpcoming, btnPast, btnCreateBooking;
    
    // Services
    private BookingService bookingService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_owner_bookings, container, false);

        // Initialize services
        bookingService = new BookingService(requireContext());

        // Initialize views
        bookingsRecyclerView = view.findViewById(R.id.bookingsRecyclerView);
        btnUpcoming = view.findViewById(R.id.btnUpcoming);
        btnPast = view.findViewById(R.id.btnPast);
        btnCreateBooking = view.findViewById(R.id.btnCreateBooking);

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

        // Create booking button
        btnCreateBooking.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateBookingActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh bookings when returning to this fragment (e.g., after creating a new booking)
        refreshCurrentBookings();
    }

    private void refreshCurrentBookings() {
        // Refresh based on current active tab
        if (btnUpcoming.getCurrentTextColor() == ContextCompat.getColor(requireContext(), R.color.white)) {
            loadUpcomingBookings();
        } else {
            loadPastBookings();
        }
    }

    private void loadUpcomingBookings() {
        bookingService.getUpcomingBookings(new BookingService.BookingListCallback() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (bookings.isEmpty()) {
                            // Show empty list - adapter will handle empty state
                            bookingsAdapter.submitList(new ArrayList<>());
                            Toast.makeText(getActivity(), "No upcoming bookings found", 
                                Toast.LENGTH_SHORT).show();
                        } else {
                            bookingsAdapter.submitList(bookings);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        bookingsAdapter.submitList(new ArrayList<>());
                        Toast.makeText(getActivity(), "Failed to load upcoming bookings: " + error, 
                            Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void loadPastBookings() {
        bookingService.getBookingHistory(new BookingService.BookingListCallback() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (bookings.isEmpty()) {
                            // Show empty list - adapter will handle empty state
                            bookingsAdapter.submitList(new ArrayList<>());
                            Toast.makeText(getActivity(), "No past bookings found", 
                                Toast.LENGTH_SHORT).show();
                        } else {
                            bookingsAdapter.submitList(bookings);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        bookingsAdapter.submitList(new ArrayList<>());
                        Toast.makeText(getActivity(), "Failed to load booking history: " + error, 
                            Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
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

    // Remove the inner Booking class - we now use the proper model class

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
            private final Button btnQrCode;
            private final Button btnModify;
            private final Button btnCancel;

            public BookingViewHolder(@NonNull View itemView) {
                super(itemView);
                stationName = itemView.findViewById(R.id.stationName);
                bookingStatus = itemView.findViewById(R.id.bookingStatus);
                bookingDate = itemView.findViewById(R.id.bookingDate);
                bookingTime = itemView.findViewById(R.id.bookingTime);
                btnQrCode = itemView.findViewById(R.id.btn_qr_code);
                btnModify = itemView.findViewById(R.id.btn_modify);
                btnCancel = itemView.findViewById(R.id.btn_cancel);
            }

            public void bind(Booking booking) {
                stationName.setText(booking.getStationName());
                bookingStatus.setText(booking.getStatusString());
                bookingDate.setText(booking.getDate());
                bookingTime.setText(booking.getTime());

                // Set status color using the enum
                int colorRes = booking.getStatus().getStatusColorRes();
                bookingStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), colorRes));

                // Configure button visibility and functionality based on booking status and timing
                configureActionButtons(booking);
            }

            private void configureActionButtons(Booking booking) {
                android.util.Log.d("OwnerBookingsFragment", "Configuring buttons for booking: " + 
                    booking.getBookingId() + " Status: " + booking.getStatusString());
                
                // QR Code button - only show if booking is approved and within access window
                boolean showQR = booking.canShowQRCode();
                android.util.Log.d("OwnerBookingsFragment", "Can show QR: " + showQR);
                if (showQR) {
                    btnQrCode.setVisibility(View.VISIBLE);
                    btnQrCode.setOnClickListener(v -> {
                        Intent intent = new Intent(itemView.getContext(), QRCodeActivity.class);
                        intent.putExtra("booking", booking);
                        itemView.getContext().startActivity(intent);
                    });
                } else {
                    btnQrCode.setVisibility(View.GONE);
                }

                // Modify button - only show if booking can be modified
                boolean canModify = booking.canBeModified();
                android.util.Log.d("OwnerBookingsFragment", "Can modify: " + canModify);
                if (canModify) {
                    btnModify.setVisibility(View.VISIBLE);
                    btnModify.setOnClickListener(v -> {
                        Intent intent = new Intent(itemView.getContext(), ModifyReservationActivity.class);
                        intent.putExtra("booking", booking);
                        itemView.getContext().startActivity(intent);
                    });
                } else {
                    btnModify.setVisibility(View.GONE);
                }

                // Cancel button - only show if booking can be cancelled
                boolean canCancel = booking.canBeCancelled();
                android.util.Log.d("OwnerBookingsFragment", "Can cancel: " + canCancel);
                if (canCancel) {
                    btnCancel.setVisibility(View.VISIBLE);
                    btnCancel.setOnClickListener(v -> {
                        Intent intent = new Intent(itemView.getContext(), CancelReservationActivity.class);
                        intent.putExtra("booking", booking);
                        itemView.getContext().startActivity(intent);
                    });
                } else {
                    btnCancel.setVisibility(View.GONE);
                }

                // If no action buttons are visible, hide the container
                LinearLayout actionContainer = itemView.findViewById(R.id.action_buttons_container);
                if (btnQrCode.getVisibility() == View.GONE && 
                    btnModify.getVisibility() == View.GONE && 
                    btnCancel.getVisibility() == View.GONE) {
                    actionContainer.setVisibility(View.GONE);
                    android.util.Log.d("OwnerBookingsFragment", "All buttons hidden, hiding container");
                } else {
                    actionContainer.setVisibility(View.VISIBLE);
                    android.util.Log.d("OwnerBookingsFragment", "At least one button visible, showing container");
                }
            }
        }
    }



    // DiffCallback
    static class DiffCallback extends DiffUtil.ItemCallback<Booking> {
        @Override
        public boolean areItemsTheSame(@NonNull Booking oldItem, @NonNull Booking newItem) {
            return oldItem.getBookingId() != null && oldItem.getBookingId().equals(newItem.getBookingId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Booking oldItem, @NonNull Booking newItem) {
            return oldItem.getStationName().equals(newItem.getStationName()) &&
                   oldItem.getStatus().equals(newItem.getStatus()) &&
                   oldItem.getDate().equals(newItem.getDate()) &&
                   oldItem.getTime().equals(newItem.getTime());
        }
    }
}
