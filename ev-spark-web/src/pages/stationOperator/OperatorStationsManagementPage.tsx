import { useState, useEffect } from "react";
import {
  Search,
  MapPin,
  Clock,
  Battery,
  Wifi,
  Coffee,
  Shield,
  Camera,
  Car,
  UserMinus,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Switch } from "@/components/ui/switch";
import axiosInstance from "@/utils/axiosInstance";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import type { ApiResponse } from "@/types/response";
import type { Station, StationWithOperators } from "@/types/station";

type ActionDialogState = {
  isOpen: boolean;
  action:
  | "updateSlots"
  | null;
  station: Station | null;
};

export default function StationsDisplayPage() {
  const [stations, setStations] = useState<Station[]>([]);
  const [filteredStations, setFilteredStations] = useState<StationWithOperators[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showOnlyAvailable, setShowOnlyAvailable] = useState(false);
  const [actionDialog, setActionDialog] = useState<ActionDialogState>({
    isOpen: false,
    action: null,
    station: null,
  });
  const [slotUpdateValue, setSlotUpdateValue] = useState<string>("");

  useEffect(() => {
    const fetchStations = async () => {
      try {
        setLoading(true);
        setError(null);

        const response = await axiosInstance.get("/ChargingStations");

        const result: ApiResponse<Station> = response.data;

        if (result.success && Array.isArray(result.data)) {
          const stationsWithOperators = await Promise.all(
            result.data.map(async (station) => {
              try {
                const assignedUsersResponse = await axiosInstance.get(
                  `/ChargingStations/${station.id}/assigned-users`
                );
                if (assignedUsersResponse.data.success) {
                  return {
                    ...station,
                    assignedOperators: assignedUsersResponse.data.data || [],
                  };
                }
              } catch (err) {
                console.error(
                  `Error fetching operators for station ${station.id}:`,
                  err
                );
              }
              return { ...station, assignedOperators: [] };
            })
          );

          setStations(stationsWithOperators);
          setFilteredStations(stationsWithOperators);
        } else {
          throw new Error("Invalid API response format");
        }
      } catch (err) {
        const message =
          err instanceof Error ? err.message : "Failed to load stations";
        setError(message);
        console.error("Error fetching stations:", err);
        setStations([]);
        setFilteredStations([]);
      } finally {
        setLoading(false);
      }
    };

    fetchStations();
  }, []);

  const handleUpdateSlotAvailability = async (
    stationId: string,
    availableSlots: number
  ) => {
    try {
      const response = await axiosInstance.patch(
        `/ChargingStations/${stationId}/slots?availableSlots=${availableSlots}`
      );

      if (response.data.success) {
        // Update local state
        setStations(
          stations.map((station) =>
            station.id === stationId ? { ...station, availableSlots } : station
          )
        );
        closeDialog();
      } else {
        throw new Error(response.data.message || "Failed to update slots");
      }
    } catch (err) {
      console.error("Error updating slot availability:", err);
      setError("Failed to update slot availability");
    }
  };
  
  const handleSlotUpdateSubmit = () => {
    if (!actionDialog.station) return;

    const availableSlots = parseInt(slotUpdateValue);
    if (isNaN(availableSlots) || availableSlots < 0) {
      setError("Please enter a valid number");
      return;
    }

    if (availableSlots > actionDialog.station.totalSlots) {
      setError("Available slots cannot exceed total slots");
      return;
    }

    handleUpdateSlotAvailability(actionDialog.station.id, availableSlots);
  };

  useEffect(() => {
    let filtered = stations;

    // Apply search filter
    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(
        (station) =>
          station.name.toLowerCase().includes(query) ||
          station.location.address.toLowerCase().includes(query) ||
          station.location.city.toLowerCase().includes(query) ||
          station.location.province.toLowerCase().includes(query)
      );
    }

    // Apply availability filter
    if (showOnlyAvailable) {
      filtered = filtered.filter((station) => station.availableSlots > 0);
    }

    setFilteredStations(filtered);
  }, [searchQuery, showOnlyAvailable, stations]);

  const getAmenityIcon = (amenity: string) => {
    const amenityLower = amenity.toLowerCase();

    if (amenityLower.includes("wifi")) return <Wifi className="h-4 w-4" />;
    if (amenityLower.includes("café") || amenityLower.includes("coffee"))
      return <Coffee className="h-4 w-4" />;
    if (amenityLower.includes("security"))
      return <Shield className="h-4 w-4" />;
    if (amenityLower.includes("cctv") || amenityLower.includes("surveillance"))
      return <Camera className="h-4 w-4" />;
    if (amenityLower.includes("ev") || amenityLower.includes("charging"))
      return <Battery className="h-4 w-4" />;

    return <Car className="h-4 w-4" />;
  };

  const formatTime = (timeString: string) => {
    return new Date(`2000-01-01T${timeString}`).toLocaleTimeString("en-US", {
      hour: "numeric",
      minute: "2-digit",
      hour12: true,
    });
  };

  const getDayNames = (days: number[]) => {
    const dayMap: { [key: number]: string } = {
      0: "Sun",
      1: "Mon",
      2: "Tue",
      3: "Wed",
      4: "Thu",
      5: "Fri",
      6: "Sat",
    };

    if (days.length === 7) return "Everyday";

    return days.map((day) => dayMap[day]).join(", ");
  };

  const getStatusColor = (station: Station) => {
    if (!station.isActive)
      return "bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400";
    if (station.availableSlots === 0)
      return "bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400";
    return "bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400";
  };

  const getStatusText = (station: Station) => {
    if (!station.isActive) return "Offline";
    if (station.availableSlots === 0) return "Full";
    return "Available";
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto"></div>
          <p className="mt-2 text-muted-foreground">Loading stations...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-center text-destructive">
          <p>Error: {error}</p>
          <Button
            onClick={() => window.location.reload()}
            className="mt-4"
            variant="outline"
          >
            Retry
          </Button>
        </div>
      </div>
    );
  }

  const openUpdateSlotsDialog = (station: Station) => {
    setSlotUpdateValue(station.availableSlots.toString());
    setActionDialog({
      isOpen: true,
      action: "updateSlots",
      station,
    });
  };

  const closeDialog = () => {
    setActionDialog({
      isOpen: false,
      action: null,
      station: null,
    });
    setSlotUpdateValue("");
  };

  return (
    <div className="space-y-6">
      <div>
        <div>
          <h2 className="text-2xl font-bold tracking-tight">
            EV Charging Stations
          </h2>
          <p className="text-muted-foreground">
            Find and view available charging stations near you
          </p>
        </div>
      </div>

      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div className="relative w-full sm:w-72">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Search by name, address, or city..."
            className="pl-8"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>

        <div className="flex items-center space-x-2">
          <Switch
            checked={showOnlyAvailable}
            onCheckedChange={setShowOnlyAvailable}
            className="data-[state=checked]:bg-green-500"
          />
          <span className="text-sm text-muted-foreground">
            Show only available stations
          </span>
        </div>
      </div>

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        {filteredStations.length === 0 ? (
          <div className="col-span-full text-center py-12">
            <p className="text-muted-foreground">
              {stations.length === 0
                ? "No stations available."
                : "No stations match your search criteria."}
            </p>
          </div>
        ) : (
          filteredStations.map((station) => (
            <Card key={station.id} className="overflow-hidden">
              <CardHeader className="pb-3">
                <div className="flex justify-between items-start">
                  <div>
                    <CardTitle className="text-lg">{station.name}</CardTitle>
                    <CardDescription className="flex items-center mt-1">
                      <MapPin className="h-3.5 w-3.5 mr-1" />
                      {station.location.address}, {station.location.city}
                    </CardDescription>
                  </div>
                  <div className="flex items-center gap-2">
                    <Badge className={getStatusColor(station)}>
                      {getStatusText(station)}
                    </Badge>
                  </div>
                </div>
              </CardHeader>

              <CardContent className="pb-4 space-y-4">
                {/* Availability */}
                <div
                  className="flex justify-between items-center p-3 bg-muted/50 rounded-lg cursor-pointer hover:bg-muted/70 transition-colors"
                  onClick={() => openUpdateSlotsDialog(station)}
                >
                  <div>
                    <p className="text-sm font-medium">Available Slots</p>
                    <p className="text-2xl font-bold text-primary">
                      {station.availableSlots}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm text-muted-foreground">Total Slots</p>
                    <p className="text-lg font-semibold">
                      {station.totalSlots}
                    </p>
                  </div>
                </div>
                <div className="flex items-center justify-between p-3 border rounded-lg">
                  <div className="flex items-center space-x-2">
                    <Shield className="h-4 w-4 text-muted-foreground" />
                    <span className="text-sm font-medium">Station Status</span>
                  </div>
                  <Switch
                    checked={station.isActive}
                    className="data-[state=checked]:bg-green-500"
                  />
                </div>
                {/* Pricing */}
                <div className="flex items-center justify-between">
                  <div className="flex items-center">
                    <span className="text-sm">Price per hour</span>
                  </div>
                  <span className="font-semibold">
                    LKR {station.pricePerHour}
                  </span>
                </div>

                {/* Operating Hours */}
                <div className="space-y-2">
                  <div className="flex items-center">
                    <Clock className="h-4 w-4 mr-1 text-muted-foreground" />
                    <span className="text-sm font-medium">Operating Hours</span>
                  </div>
                  <div className="text-sm">
                    <p>
                      {formatTime(station.operatingHours.openTime)} -{" "}
                      {formatTime(station.operatingHours.closeTime)}
                    </p>
                    <p className="text-muted-foreground">
                      {getDayNames(station.operatingHours.operatingDays)}
                    </p>
                  </div>
                </div>

                {/* Amenities */}
                {station.amenities && station.amenities.length > 0 && (
                  <div className="space-y-2">
                    <p className="text-sm font-medium">Amenities</p>
                    <div className="flex flex-wrap gap-2">
                      {station.amenities.map((amenity, index) => (
                        <Badge
                          key={index}
                          variant="secondary"
                          className="flex items-center gap-1"
                        >
                          {getAmenityIcon(amenity)}
                          {amenity}
                        </Badge>
                      ))}
                    </div>
                  </div>
                )}

                {/* Assigned Operators */}
                {station.assignedOperators &&
                  station.assignedOperators.length > 0 && (
                    <div className="space-y-2">
                      <p className="text-sm font-medium">Assigned Operators</p>
                      <div className="space-y-2">
                        {station.assignedOperators.map((operator) => (
                          <div
                            key={operator.id}
                            className="flex items-center justify-between p-2 bg-blue-50 dark:bg-blue-900/20 rounded-md border"
                          >
                            <div className="flex items-center space-x-2">
                              <div className="w-6 h-6 bg-blue-100 dark:bg-blue-800 rounded-full flex items-center justify-center">
                                <span className="text-xs font-medium text-blue-600 dark:text-blue-300">
                                  {operator.email?.charAt(0)}
                                </span>
                              </div>
                              <div>
                                <p className="text-sm font-medium">
                                  {operator.firstName} {operator.lastName}
                                </p>
                                <p className="text-xs text-muted-foreground">
                                  {operator.email}
                                </p>
                              </div>
                            </div>
                            <Badge
                              variant="outline"
                              className="bg-blue-100 text-blue-700 dark:bg-blue-900 dark:text-blue-300"
                            >
                              Operator
                            </Badge>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                {/* If no operators assigned, show a message (optional) */}
                {station.assignedOperators &&
                  station.assignedOperators.length === 0 && (
                    <div className="space-y-2">
                      <p className="text-sm font-medium">Assigned Operators</p>
                      <div className="text-center py-3 border border-dashed rounded-md">
                        <UserMinus className="h-4 w-4 mx-auto text-muted-foreground mb-1" />
                        <p className="text-xs text-muted-foreground">
                          No operators assigned
                        </p>
                      </div>
                    </div>
                  )}
                {/* Station Type */}
                <div className="pt-2 border-t">
                  <Badge variant="outline">Type {station.type} Charger</Badge>
                </div>
              </CardContent>
            </Card>
          ))
        )}
      </div>

      {/* Summary */}
      {filteredStations.length > 0 && (
        <div className="text-center text-sm text-muted-foreground">
          Showing {filteredStations.length} of {stations.length} stations
        </div>
      )}

      {/* Dialog for CRUD operations */}
      <Dialog
        open={actionDialog.isOpen}
        onOpenChange={(isOpen) => !isOpen && closeDialog()}
      >
        <DialogContent className="sm:max-w-lg">
          {actionDialog.action === "updateSlots" && actionDialog.station && (
            <>
              <DialogHeader>
                <DialogTitle>Update Available Slots</DialogTitle>
                <DialogDescription>
                  Update the number of available charging slots for{" "}
                  {actionDialog.station.name}
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4">
                <div>
                  <label
                    htmlFor="availableSlots"
                    className="block text-sm font-medium mb-2"
                  >
                    Available Slots
                  </label>
                  <Input
                    id="availableSlots"
                    type="number"
                    min="0"
                    max={actionDialog.station.totalSlots}
                    value={slotUpdateValue}
                    onChange={(e) => setSlotUpdateValue(e.target.value)}
                    placeholder="Enter number of available slots"
                  />
                  <p className="text-sm text-muted-foreground mt-1">
                    Total slots: {actionDialog.station.totalSlots}
                  </p>
                </div>
                <div className="flex justify-end space-x-2">
                  <Button variant="outline" onClick={closeDialog}>
                    Cancel
                  </Button>
                  <Button onClick={handleSlotUpdateSubmit}>Update Slots</Button>
                </div>
              </div>
            </>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
