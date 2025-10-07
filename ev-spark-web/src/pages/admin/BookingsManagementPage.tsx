import { useState } from "react"
import { Search, MoreHorizontal, CheckCircle, XCircle, Calendar, Clock } from "lucide-react"

import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"

import { BookingDetailView } from "./BookingDetailView"
import { BookingCancellationConfirmation } from "./BookingCancellationConfirmation"

// Booking type definition
interface Booking {
  id: string
  stationName: string
  stationLocation: string
  ownerName: string
  vehicleInfo: string
  startTime: string
  endTime: string
  duration: number // in minutes
  status: "upcoming" | "in-progress" | "completed" | "cancelled"
  paymentStatus: "pending" | "paid" | "refunded"
  amount: number
}

// Sample data - would come from API in real application
const sampleBookings: Booking[] = [
  {
    id: "B-1001",
    stationName: "Downtown Charging Hub",
    stationLocation: "123 Main St, City Center",
    ownerName: "John Smith",
    vehicleInfo: "Tesla Model 3",
    startTime: "2025-12-15T10:00:00Z",
    endTime: "2025-12-15T11:30:00Z",
    duration: 90,
    status: "upcoming",
    paymentStatus: "paid",
    amount: 25.50,
  },
  {
    id: "B-1002",
    stationName: "Westside Mall Station",
    stationLocation: "456 Market Ave, Westside",
    ownerName: "Emily Johnson",
    vehicleInfo: "Nissan Leaf",
    startTime: "2025-12-15T09:00:00Z",
    endTime: "2025-12-15T10:30:00Z",
    duration: 90,
    status: "in-progress",
    paymentStatus: "paid",
    amount: 25.50,
  },
  {
    id: "B-1003",
    stationName: "Downtown Charging Hub",
    stationLocation: "123 Main St, City Center",
    ownerName: "Michael Brown",
    vehicleInfo: "Chevrolet Bolt",
    startTime: "2025-12-14T14:00:00Z",
    endTime: "2025-12-14T15:00:00Z",
    duration: 60,
    status: "completed",
    paymentStatus: "paid",
    amount: 18.00,
  },
  {
    id: "B-1004",
    stationName: "East End Quick Charge",
    stationLocation: "789 Park Road, East District",
    ownerName: "Sarah Wilson",
    vehicleInfo: "Ford Mustang Mach-E",
    startTime: "2025-12-16T11:00:00Z",
    endTime: "2025-12-16T12:30:00Z",
    duration: 90,
    status: "cancelled",
    paymentStatus: "refunded",
    amount: 25.50,
  },
]

type ActionDialogState = {
  isOpen: boolean
  action: "view" | "cancel" | null
  booking: Booking | null
}

export default function BookingsManagementPage() {
  const [bookings, setBookings] = useState<Booking[]>(sampleBookings)
  const [searchQuery, setSearchQuery] = useState("")
  const [statusFilter, setStatusFilter] = useState<string>("all")
  const [actionDialog, setActionDialog] = useState<ActionDialogState>({
    isOpen: false,
    action: null,
    booking: null,
  })

  // Filter bookings by search query and status
  const filteredBookings = bookings.filter((booking) => {
    const matchesSearch = searchQuery === "" || 
      booking.id.toLowerCase().includes(searchQuery.toLowerCase()) ||
      booking.ownerName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      booking.stationName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      booking.vehicleInfo.toLowerCase().includes(searchQuery.toLowerCase())
    
    const matchesStatus = statusFilter === "all" || booking.status === statusFilter
    
    return matchesSearch && matchesStatus
  })

  const handleCancelBooking = () => {
    if (!actionDialog.booking) return
    
    setBookings(
      bookings.map((booking) =>
        booking.id === actionDialog.booking?.id 
          ? { ...booking, status: "cancelled", paymentStatus: "refunded" } 
          : booking
      )
    )
    closeDialog()
  }

  const openViewDialog = (booking: Booking) => {
    setActionDialog({
      isOpen: true,
      action: "view",
      booking,
    })
  }

  const openCancelDialog = (booking: Booking) => {
    setActionDialog({
      isOpen: true,
      action: "cancel",
      booking,
    })
  }

  const closeDialog = () => {
    setActionDialog({
      isOpen: false,
      action: null,
      booking: null,
    })
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    })
  }

  const formatTime = (dateString: string) => {
    return new Date(dateString).toLocaleTimeString("en-US", {
      hour: "2-digit",
      minute: "2-digit",
    })
  }

  const getStatusBadgeClasses = (status: string) => {
    switch (status) {
      case "upcoming":
        return "bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400"
      case "in-progress":
        return "bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400"
      case "completed":
        return "bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400"
      case "cancelled":
        return "bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400"
      default:
        return "bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300"
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">Booking Management</h2>
        <p className="text-muted-foreground">
          Manage all charging station bookings
        </p>
      </div>

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
          
          <Select value={statusFilter} onValueChange={setStatusFilter}>
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="Filter by status" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Statuses</SelectItem>
              <SelectItem value="upcoming">Upcoming</SelectItem>
              <SelectItem value="in-progress">In Progress</SelectItem>
              <SelectItem value="completed">Completed</SelectItem>
              <SelectItem value="cancelled">Cancelled</SelectItem>
            </SelectContent>
          </Select>
        </div>
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
                  No bookings found.
                </TableCell>
              </TableRow>
            ) : (
              filteredBookings.map((booking) => (
                <TableRow key={booking.id}>
                  <TableCell className="font-medium">{booking.id}</TableCell>
                  <TableCell>
                    <div>
                      <div className="font-medium">{booking.stationName}</div>
                      <div className="text-xs text-muted-foreground">{booking.stationLocation}</div>
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
                        <span>{formatTime(booking.startTime)} - {formatTime(booking.endTime)}</span>
                      </div>
                    </div>
                  </TableCell>
                  <TableCell>{booking.duration} mins</TableCell>
                  <TableCell>
                    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${getStatusBadgeClasses(booking.status)}`}>
                      {booking.status.charAt(0).toUpperCase() + booking.status.slice(1)}
                    </span>
                  </TableCell>
                  <TableCell className="text-right">
                    ${booking.amount.toFixed(2)}
                    <div className="text-xs text-muted-foreground">{booking.paymentStatus}</div>
                  </TableCell>
                  <TableCell className="text-right">
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="sm" className="h-8 w-8 p-0">
                          <span className="sr-only">Open menu</span>
                          <MoreHorizontal className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem onClick={() => openViewDialog(booking)}>
                          <CheckCircle className="mr-2 h-4 w-4" /> View Details
                        </DropdownMenuItem>
                        {booking.status === "upcoming" && (
                          <>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem 
                              onClick={() => openCancelDialog(booking)}
                              className="text-destructive focus:text-destructive"
                            >
                              <XCircle className="mr-2 h-4 w-4" /> Cancel Booking
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
      <Dialog open={actionDialog.isOpen} onOpenChange={(isOpen) => !isOpen && closeDialog()}>
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
          
          {actionDialog.action === "cancel" && actionDialog.booking && (
            <>
              <DialogHeader>
                <DialogTitle>Cancel Booking</DialogTitle>
                <DialogDescription>
                  This will cancel the booking and refund the payment.
                </DialogDescription>
              </DialogHeader>
              <BookingCancellationConfirmation
                bookingId={actionDialog.booking.id}
                onConfirm={handleCancelBooking}
                onCancel={closeDialog}
              />
            </>
          )}
        </DialogContent>
      </Dialog>
    </div>
  )
}
