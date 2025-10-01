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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_owner_bookings, container, false);

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

    private void loadUpcomingBookings() {
        List<Booking> bookings = new ArrayList<>();
        
        // Create sample upcoming bookings with proper dates
        Calendar future1 = Calendar.getInstance();
        future1.add(Calendar.DAY_OF_MONTH, 2);
        future1.set(Calendar.HOUR_OF_DAY, 14);
        future1.set(Calendar.MINUTE, 0);
        
        Calendar future2 = Calendar.getInstance();
        future2.add(Calendar.DAY_OF_MONTH, 3);
        future2.set(Calendar.HOUR_OF_DAY, 9);
        future2.set(Calendar.MINUTE, 30);
        
        Calendar future3 = Calendar.getInstance();
        future3.add(Calendar.DAY_OF_MONTH, 4);
        future3.set(Calendar.HOUR_OF_DAY, 18);
        future3.set(Calendar.MINUTE, 0);
        
        Booking booking1 = new Booking("USER001", "ST001", "EcoCharge Hub - Central", 
                                     "123 Central Ave, Colombo", future1.getTime(), future1.getTime(), 60, 150.0);
        booking1.setStatus("APPROVED");
        booking1.setBookingId("BOOK001");
        
        Booking booking2 = new Booking("USER001", "ST002", "PowerUp Point - West", 
                                     "456 West Rd, Gampaha", future2.getTime(), future2.getTime(), 90, 225.0);
        booking2.setStatus("APPROVED");  // Changed to APPROVED to show QR code
        booking2.setBookingId("BOOK002");
        
        Booking booking3 = new Booking("USER001", "ST003", "Volt Oasis - North", 
                                     "789 North St, Kandy", future3.getTime(), future3.getTime(), 120, 300.0);
        booking3.setStatus("APPROVED");
        booking3.setBookingId("BOOK003");
        
        // Add a booking that's close to current time for QR code testing
        Calendar nearFuture = Calendar.getInstance();
        nearFuture.add(Calendar.HOUR, 2); // 2 hours from now
        
        Booking booking4 = new Booking("USER001", "ST004", "QuickCharge - City Center", 
                                     "456 Center Plaza, Colombo", nearFuture.getTime(), nearFuture.getTime(), 90, 225.0);
        booking4.setStatus("APPROVED");
        booking4.setBookingId("BOOK004");
        
        bookings.add(booking1);
        bookings.add(booking2);
        bookings.add(booking3);
        bookings.add(booking4);
        
        bookingsAdapter.submitList(bookings);
    }

    private void loadPastBookings() {
        List<Booking> pastBookings = new ArrayList<>();
        
        // Create sample past bookings
        Calendar past1 = Calendar.getInstance();
        past1.add(Calendar.DAY_OF_MONTH, -15);
        past1.set(Calendar.HOUR_OF_DAY, 11);
        past1.set(Calendar.MINUTE, 0);
        
        Calendar past2 = Calendar.getInstance();
        past2.add(Calendar.DAY_OF_MONTH, -20);
        past2.set(Calendar.HOUR_OF_DAY, 16);
        past2.set(Calendar.MINUTE, 0);
        
        Booking pastBooking1 = new Booking("USER001", "ST004", "GreenCharge - East", 
                                         "321 East Ave, Battaramulla", past1.getTime(), past1.getTime(), 75, 187.5);
        pastBooking1.setStatus("COMPLETED");
        pastBooking1.setBookingId("BOOK004");
        
        Booking pastBooking2 = new Booking("USER001", "ST005", "ChargePoint - South", 
                                         "654 South Lane, Panadura", past2.getTime(), past2.getTime(), 60, 150.0);
        pastBooking2.setStatus("CANCELLED");
        pastBooking2.setBookingId("BOOK005");
        
        pastBookings.add(pastBooking1);
        pastBookings.add(pastBooking2);
        
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
                bookingStatus.setText(booking.getStatus());
                bookingDate.setText(booking.getDate());
                bookingTime.setText(booking.getTime());

                // Set status color based on approval state
                switch (booking.getStatus()) {
                    case "APPROVED":
                        bookingStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.primary_light));
                        break;
                    case "PENDING":
                        bookingStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.amber_600));
                        break;
                    case "COMPLETED":
                        bookingStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.gray_600));
                        break;
                    case "CANCELLED":
                        bookingStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.red_600));
                        break;
                    default:
                        bookingStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.black));
                        break;
                }

                // Configure button visibility and functionality based on booking status and timing
                configureActionButtons(booking);
            }

            private void configureActionButtons(Booking booking) {
                // QR Code button - only show if booking is approved and within access window
                if (booking.canShowQRCode()) {
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
                if (booking.canBeModified()) {
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
                if (booking.canBeCancelled()) {
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
                } else {
                    actionContainer.setVisibility(View.VISIBLE);
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
