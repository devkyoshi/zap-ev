import { useState, useEffect } from "react";
import {
  Search,
  MoreHorizontal,
  CheckCircle,
  XCircle,
  Calendar,
  Clock,
} from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

import { BookingDetailView } from "./BookingDetailView";
import axiosInstance from "@/utils/axiosInstance";
import { BookingStatus, BookingStatusLabel } from "@/utils/bookingStatus";

// Booking type definition matching API
interface Booking {
  id: string;
  stationName: string;
  stationLocation: string;
  ownerName: string;
  vehicleInfo: string;
  startTime: string;
  endTime: string;
  duration: number; // in minutes
  status: BookingStatus;
  paymentStatus: "pending" | "paid" | "refunded";
  totalAmount: number;
}

// API request types
interface CreateBookingRequest {
  chargingStationId: string;
  reservationDateTime: string;
  durationMinutes: number;
  notes: string;
}

type ActionDialogState = {
  isOpen: boolean;
  action: "view" | "cancel" | "create" | null;
  booking: Booking | null;
};

export default function BookingsManagementPage() {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState<BookingStatus>(
    BookingStatus.COMPLETED
  );
  const [actionDialog, setActionDialog] = useState<ActionDialogState>({
    isOpen: false,
    action: null,
    booking: null,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const fetchBookings = async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await axiosInstance.get("/bookings");
      // ✅ Ensure the data is an array
      const bookingsData = Array.isArray(response.data)
        ? response.data
        : response.data.data;

      setBookings(bookingsData || []);
    } catch (err) {
      const message = err instanceof Error ? err.message : "An error occurred";
      setError(message);
      console.error("Error fetching bookings:", err);
    } finally {
      setLoading(false);
    }
  };

  // Load bookings on component mount
  useEffect(() => {
    fetchBookings();
  }, []);

  const openViewDialog = (booking: Booking) => {
    setActionDialog({
      isOpen: true,
      action: "view",
      booking,
    });
  };

  const openCancelDialog = (booking: Booking) => {
    setActionDialog({
      isOpen: true,
      action: "cancel",
      booking,
    });
  };

  const closeDialog = () => {
    setActionDialog({
      isOpen: false,
      action: null,
      booking: null,
    });
  };

  const retryFetch = () => {
    fetchBookings();
  };

  // Filter bookings by search query and status
  const filteredBookings = bookings.filter((booking) => {
    const matchesSearch =
      searchQuery === "" ||
      booking.id.toLowerCase().includes(searchQuery.toLowerCase()) ||
      booking.ownerName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      booking.stationName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      booking.vehicleInfo.toLowerCase().includes(searchQuery.toLowerCase());

    const matchesStatus = booking.status === statusFilter;

    return matchesSearch && matchesStatus;
  });

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  };

  const formatTime = (dateString: string) => {
    return new Date(dateString).toLocaleTimeString("en-US", {
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const getStatusBadgeClasses = (status: BookingStatus) => {
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

  if (loading && bookings.length === 0) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto"></div>
          <p className="mt-2 text-muted-foreground">Loading bookings...</p>
        </div>
      </div>
    );
  }

  if (error && bookings.length === 0) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-center">
          <div className="text-destructive mb-2">Error loading bookings</div>
          <p className="text-muted-foreground mb-4">{error}</p>
          <Button onClick={retryFetch}>Retry</Button>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">
          Booking Management
        </h2>
        <p className="text-muted-foreground">
          Manage all charging station bookings
        </p>
      </div>

      {error && (
        <div className="bg-destructive/15 text-destructive px-4 py-3 rounded-md">
          <div className="flex justify-between items-center">
            <span>{error}</span>
            <Button variant="outline" size="sm" onClick={() => setError(null)}>
              Dismiss
            </Button>
          </div>
        </div>
      )}

      <div className="flex justify-between items-center">
        <div className="flex space-x-2 items-center">
          <div className="relative w-72">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search bookings..."
              className="pl-8"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>

          <Select
            value={statusFilter as unknown as string}
            onValueChange={setStatusFilter}
          >
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="Filter by status" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Statuses</SelectItem>
              <SelectItem value={String(BookingStatus.PENDING)}>
                {BookingStatusLabel.PENDING}
              </SelectItem>
              <SelectItem value={String(BookingStatus.APPROVED)}>
                {BookingStatusLabel.APPROVED}
              </SelectItem>
              <SelectItem value={String(BookingStatus.IN_PROGRESS)}>
                {BookingStatusLabel.IN_PROGRESS}
              </SelectItem>
              <SelectItem value={String(BookingStatus.COMPLETED)}>
                {BookingStatusLabel.COMPLETED}
              </SelectItem>
              <SelectItem value={String(BookingStatus.CANCELLED)}>
                {BookingStatusLabel.CANCELLED}
              </SelectItem>
              <SelectItem value={String(BookingStatus.NO_SHOW)}>
                {BookingStatusLabel.NO_SHOW}
              </SelectItem>
            </SelectContent>
          </Select>
        </div>

        <Button
          onClick={() =>
            setActionDialog({ isOpen: true, action: "create", booking: null })
          }
        >
          Create Booking
        </Button>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Booking ID</TableHead>
              <TableHead>Station</TableHead>
              <TableHead>User</TableHead>
              <TableHead>Vehicle</TableHead>
              <TableHead>Date & Time</TableHead>
              <TableHead>Duration</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Amount</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredBookings.length === 0 ? (
              <TableRow>
                <TableCell colSpan={9} className="h-24 text-center">
                  {bookings.length === 0
                    ? "No bookings available."
                    : "No bookings match your filters."}
                </TableCell>
              </TableRow>
            ) : (
              filteredBookings.map((booking) => (
                <TableRow key={booking.id}>
                  <TableCell className="font-medium">{booking.id}</TableCell>
                  <TableCell>
                    <div>
                      <div className="font-medium">{booking.stationName}</div>
                      <div className="text-xs text-muted-foreground">
                        {booking.stationLocation}
                      </div>
                    </div>
                  </TableCell>
                  <TableCell>{booking.ownerName}</TableCell>
                  <TableCell>{booking.vehicleInfo}</TableCell>
                  <TableCell>
                    <div className="flex flex-col">
                      <div className="flex items-center text-xs">
                        <Calendar className="h-3 w-3 mr-1" />
                        <span>{formatDate(booking.startTime)}</span>
                      </div>
                      <div className="flex items-center text-xs text-muted-foreground">
                        <Clock className="h-3 w-3 mr-1" />
                        <span>
                          {formatTime(booking.startTime)} -{" "}
                          {formatTime(booking.endTime)}
                        </span>
                      </div>
                    </div>
                  </TableCell>
                  <TableCell>{booking.duration} mins</TableCell>
                  <TableCell>
                    <span
                      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${getStatusBadgeClasses(
                        booking.status
                      )}`}
                    >
                      {
                        BookingStatusLabel[
                          Object.keys(BookingStatus)[
                            Object.values(BookingStatus).indexOf(booking.status)
                          ] as keyof typeof BookingStatus
                        ]
                      }
                    </span>
                  </TableCell>
                  <TableCell className="text-right">
                    ${booking.totalAmount}
                    <div className="text-xs text-muted-foreground">
                      {booking.paymentStatus}
                    </div>
                  </TableCell>
                  <TableCell className="text-right">
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-8 w-8 p-0"
                        >
                          <span className="sr-only">Open menu</span>
                          <MoreHorizontal className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem
                          onClick={() => openViewDialog(booking)}
                        >
                          <CheckCircle className="mr-2 h-4 w-4" /> View Details
                        </DropdownMenuItem>
                        {booking.status === BookingStatus.PENDING && (
                          <>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem
                              onClick={() => openCancelDialog(booking)}
                              className="text-destructive focus:text-destructive"
                            >
                              <XCircle className="mr-2 h-4 w-4" /> Cancel
                              Booking
                            </DropdownMenuItem>
                          </>
                        )}
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {/* Dialog for view/cancel actions */}
      <Dialog
        open={actionDialog.isOpen}
        onOpenChange={(isOpen) => !isOpen && closeDialog()}
      >
        <DialogContent className="sm:max-w-lg">
          {actionDialog.action === "view" && actionDialog.booking && (
            <>
              <DialogHeader>
                <DialogTitle>Booking Details</DialogTitle>
                <DialogDescription>
                  Information for booking {actionDialog.booking.id}
                </DialogDescription>
              </DialogHeader>
              <BookingDetailView booking={actionDialog.booking} />
              <div className="flex justify-end">
                <Button variant="outline" onClick={closeDialog}>
                  Close
                </Button>
              </div>
            </>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
