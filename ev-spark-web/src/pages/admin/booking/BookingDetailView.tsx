import type { Booking } from "@/types/booking";
import { BookingStatus, BookingStatusLabel } from "@/utils/bookingStatus";
import { Calendar, Clock, MapPin, User, CreditCard } from "lucide-react";
import { getStatusBadgeClasses } from "./BookingSupport";
import { formatDate, formatTime } from "@/utils/time";

interface BookingDetailViewProps {
  booking: Booking;
}

export function BookingDetailView({ booking }: BookingDetailViewProps) {
  return (
    <div className="space-y-6 py-2">
      {/* Booking ID and Status */}
      <div className="flex justify-between items-center">
        <div>
          <h3 className="font-semibold">Booking </h3>
        </div>
        <span
          className={`inline-flex items-center rounded-full px-2.5 py-1 text-xs font-medium ${getStatusBadgeClasses(
            booking.status
          )}`}
        >
          {BookingStatusLabel[
            Object.keys(BookingStatus).find(
              (key) =>
                BookingStatus[key as keyof typeof BookingStatus] ===
                booking.status
            ) as keyof typeof BookingStatus
          ] ?? "Unknown"}
        </span>
      </div>

      {/* Date and Time */}
      <div className="space-y-2">
        <h4 className="text-sm font-medium text-muted-foreground">
          Date and Time
        </h4>
        <div className="bg-muted/50 p-3 rounded-md space-y-2">
          <div className="flex items-center">
            <Calendar className="h-4 w-4 mr-2 text-muted-foreground" />
            {formatDate(booking.reservationDateTime)}
          </div>
          <div className="flex items-center">
            <Clock className="h-4 w-4 mr-2 text-muted-foreground" />
            {formatTime(booking.reservationDateTime)} ({booking.durationMinutes}{" "}
            minutes)
          </div>
        </div>
      </div>

      {/* Booking Details */}
      <div className="space-y-2">
        <h4 className="text-sm font-medium text-muted-foreground">
          Booking Details
        </h4>
        <div className="bg-muted/50 p-3 rounded-md space-y-2">
          <div className="flex items-center">
            <User className="h-4 w-4 mr-2 text-muted-foreground" />
            EV Owner NIC: {booking.evOwnerNIC}
          </div>
          <div className="flex items-center">
            <MapPin className="h-4 w-4 mr-2 text-muted-foreground" />
            Station: {booking.chargingStationName}
          </div>
          <div className="flex items-center">
            <CreditCard className="h-4 w-4 mr-2 text-muted-foreground" />
            Total Amount: Rs. {booking.totalAmount.toFixed(2)}
          </div>
        </div>
      </div>

      {/* Created At */}
      <div className="text-xs text-muted-foreground text-right">
        Created on {formatDate(booking.createdAt)} at{" "}
        {formatTime(booking.createdAt)}
      </div>
    </div>
  );
}
