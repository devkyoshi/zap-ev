import { Car, Users, Zap, Calendar } from "lucide-react";
import { StatsCard, StatsGrid } from "@/components/dashboard/stats-card";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useEffect, useState } from "react";
import axiosInstance from "@/utils/axiosInstance";

// Types for our API responses
interface ChargingStation {
  id: string;
  name: string;
  location: {
    latitude: number;
    longitude: number;
    address: string;
    city: string;
    province: string;
  };
  type: number;
  totalSlots: number;
  availableSlots: number;
  pricePerHour: number;
  operatingHours: {
    openTime: string;
    closeTime: string;
    operatingDays: number[];
  };
  isActive: boolean;
  amenities: string[];
  createdAt: string;
  updatedAt: string;
}

interface VehicleDetail {
  make: string;
  model: string;
  licensePlate: string;
  year: number;
}

interface EVOwner {
  id: string;
  nic: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  passwordHash: string;
  isActive: boolean;
  vehicleDetails: VehicleDetail[];
  lastLogin: string | null;
  createdAt: string;
  updatedAt: string;
}

interface Booking {
  id: string;
  evOwnerNIC: string;
  chargingStationName: string;
  reservationDateTime: string;
  durationMinutes: number;
  status: number;
  totalAmount: number;
  qrCode: string;
  createdAt: string;
}

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T[];
  errors: string[];
}

const AdminDashboard = () => {
  const [chargingStations, setChargingStations] = useState<ChargingStation[]>(
    []
  );
  const [evOwners, setEvOwners] = useState<EVOwner[]>([]);
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);

        // Fetch all API data in parallel using axiosInstance
        const [stationsRes, ownersRes, bookingsRes] = await Promise.all([
          axiosInstance.get<ApiResponse<ChargingStation>>("/ChargingStations"),
          axiosInstance.get<ApiResponse<EVOwner>>("/EVOwners"),
          axiosInstance.get<ApiResponse<Booking>>("/Bookings"),
        ]);

        if (stationsRes.data.success) {
          setChargingStations(stationsRes.data.data);
        }

        if (ownersRes.data.success) {
          setEvOwners(ownersRes.data.data);
        }

        if (bookingsRes.data.success) {
          setBookings(bookingsRes.data.data);
        }
      } catch (err: any) {
        console.error("Error fetching dashboard data:", err);
        setError(
          err.response?.data?.message ||
            err.message ||
            "An error occurred while fetching dashboard data."
        );
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);
  // Calculate statistics from the API data
  const totalEVOwners = evOwners.length;
  const totalActiveStations = chargingStations.filter(
    (station) => station.isActive
  ).length;
  const pendingBookings = bookings.filter(
    (booking) => booking.status === 2
  ).length; // Assuming status 2 is "pending"
  const totalRegisteredVehicles = evOwners.reduce(
    (total, owner) => total + owner.vehicleDetails.length,
    0
  );

  const stats = [
    {
      title: "Total EV Owners",
      value: totalEVOwners.toString(),
      icon: Users,
      description: "Active registered users",
      trend: {
        value: 12, // You might want to calculate this from historical data
        isPositive: true,
        label: "from last month",
      },
    },
    {
      title: "Total Active Stations",
      value: totalActiveStations.toString(),
      icon: Zap,
      description: "Across all regions",
      trend: {
        value: 7.5, // You might want to calculate this from historical data
        isPositive: true,
        label: "from last month",
      },
    },
    {
      title: "Pending Bookings",
      value: pendingBookings.toString(),
      icon: Calendar,
      description: "Awaiting confirmation",
      trend: {
        value: 4.3, // You might want to calculate this from historical data
        isPositive: false,
        label: "from last week",
      },
    },
    {
      title: "Registered Vehicles",
      value: totalRegisteredVehicles.toString(),
      icon: Car,
      description: "All electric vehicles",
      trend: {
        value: 8.1, // You might want to calculate this from historical data
        isPositive: true,
        label: "from last month",
      },
    },
  ];

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <p className="text-lg">Loading dashboard data...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center h-64">
        <p className="text-lg text-red-500">Error: {error}</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">Dashboard</h2>
        <p className="text-muted-foreground">
          Overview of the EV charging station system
        </p>
      </div>

      <StatsGrid>
        {stats.map((stat, index) => (
          <StatsCard
            key={index}
            title={stat.title}
            value={stat.value}
            description={stat.description}
            icon={stat.icon}
            trend={stat.trend}
          />
        ))}
      </StatsGrid>

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        <Card className="col-span-2 h-[300px] md:col-span-1">
          <CardHeader>
            <CardTitle>Regional Usage</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex h-[220px] items-center justify-center rounded-md border border-dashed p-4">
              <p className="text-sm text-muted-foreground">
                {chargingStations.length > 0
                  ? `Stations in ${chargingStations[0].location.city}`
                  : "No station data available"}
              </p>
            </div>
          </CardContent>
        </Card>

        <Card className="h-[300px]">
          <CardHeader>
            <CardTitle>Recent Activities</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex h-[220px] flex-col space-y-2 overflow-y-auto">
              {bookings.slice(0, 5).map((booking) => (
                <div key={booking.id} className="p-2 border rounded">
                  <p className="text-sm font-medium">
                    {booking.chargingStationName}
                  </p>
                  <p className="text-xs text-muted-foreground">
                    {new Date(booking.reservationDateTime).toLocaleDateString()}
                  </p>
                </div>
              ))}
              {bookings.length === 0 && (
                <p className="text-sm text-muted-foreground">
                  No recent activities
                </p>
              )}
            </div>
          </CardContent>
        </Card>

        <Card className="h-[300px]">
          <CardHeader>
            <CardTitle>Booking Summary</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex h-[220px] items-center justify-center rounded-md border border-dashed p-4">
              <p className="text-sm text-muted-foreground">
                Total Bookings: {bookings.length}
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default AdminDashboard;
