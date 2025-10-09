import type { Booking } from "@/types/booking";
import { BookingStatus, BookingStatusLabel } from "@/utils/bookingStatus";
import { Calendar, Clock, MapPin, User, CreditCard } from "lucide-react";

interface BookingDetailViewProps {
  booking: Booking;
}

export function BookingDetailView({ booking }: BookingDetailViewProps) {
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
      weekday: "long",
    });
  };

  const formatTime = (dateString: string) => {
    return new Date(dateString).toLocaleTimeString("en-US", {
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const getStatusBadgeClasses = (status: number) => {
    switch (status) {
      case BookingStatus.PENDING:
        return "bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400";
      case BookingStatus.APPROVED:
        return "bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400";
      case BookingStatus.IN_PROGRESS:
        return "bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400";
      case BookingStatus.COMPLETED:
        return "bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400";
      case BookingStatus.CANCELLED:
        return "bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400";
      case BookingStatus.NO_SHOW:
        return "bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300";
      default:
        return "bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300";
    }
  };

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
