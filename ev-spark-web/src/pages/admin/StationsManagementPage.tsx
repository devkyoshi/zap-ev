import { useState, useEffect } from "react";
import {
  Search,
  MapPin,
  Clock,
  DollarSign,
  Battery,
  Wifi,
  Coffee,
  Shield,
  Camera,
  Car,
  Plus,
  Trash2,
  Edit,
  MoreHorizontal,
  UserPlus,
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
import { StationForm } from "./StationForm";
import { StationDeleteConfirmation } from "./StationDeleteConfirmation";
interface Location {
  latitude: number;
  longitude: number;
  address: string;
  city: string;
  province: string;
}

interface OperatingHours {
  openTime: string;
  closeTime: string;
  operatingDays: number[];
}

interface Station {
  id: string;
  name: string;
  location: Location;
  type: number;
  totalSlots: number;
  availableSlots: number;
  pricePerHour: number;
  operatingHours: OperatingHours;
  isActive: boolean;
  amenities: string[];
  createdAt: string;
  updatedAt: string;
  assignedOperators?: User[];
}

interface ApiResponse {
  success: boolean;
  message: string;
  data: Station[];
  errors: string[];
}
type ActionDialogState = {
  isOpen: boolean;
  action:
    | "create"
    | "edit"
    | "delete"
    | "updateSlots"
    | "revokeOperator"
    | "assignOperator"
    | null;
  station: Station | null;
};
interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: number;
  isActive: boolean;
}

interface StationWithOperators extends Station {
  assignedOperators?: User[];
}
export default function StationsDisplayPage() {
  const [stations, setStations] = useState<Station[]>([]);
  const [filteredStations, setFilteredStations] = useState<Station[]>([]);
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
  const [users, setUsers] = useState<User[]>([]);
  const [selectedOperatorId, setSelectedOperatorId] = useState<string>("");
  const [assignedUsers, setAssignedUsers] = useState<User[]>([]);
  const [loadingAssignedUsers, setLoadingAssignedUsers] = useState(false);
  useEffect(() => {
    const fetchStations = async () => {
      try {
        setLoading(true);
        setError(null);

        const response = await axiosInstance.get("/ChargingStations");

        const result: ApiResponse = response.data;

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
  // GET ASSIGNED USERS
  const fetchAssignedUsers = async (stationId: string) => {
    try {
      setLoadingAssignedUsers(true);
      const response = await axiosInstance.get(
        `/ChargingStations/${stationId}/assigned-users`
      );

      if (response.data.success) {
        setAssignedUsers(response.data.data || []);
      } else {
        throw new Error(
          response.data.message || "Failed to fetch assigned users"
        );
      }
    } catch (err) {
      console.error("Error fetching assigned users:", err);
      setAssignedUsers([]);
    } finally {
      setLoadingAssignedUsers(false);
    }
  };
  // ASSIGN OPERATOR
  const handleAssignOperator = async (
    stationId: string,
    operatorUserId: string
  ) => {
    try {
      const response = await axiosInstance.post(
        `/ChargingStations/${stationId}/assign-operator?operatorUserId=${operatorUserId}`
      );

      if (response.data.success) {
        await fetchAssignedUsers(stationId);

        // Refresh stations or update local state
        const fetchStations = async () => {
          const response = await axiosInstance.get("/ChargingStations");
          const result: ApiResponse = response.data;
          if (result.success && Array.isArray(result.data)) {
            setStations(result.data);
            setFilteredStations(result.data);
          }
        };
        await fetchStations();
        closeDialog();
      } else {
        throw new Error(response.data.message || "Failed to assign operator");
      }
    } catch (err) {
      console.error("Error assigning operator:", err);
      setError("Failed to assign operator");
    }
  };

  // REVOKE OPERATOR
  const handleRevokeOperator = async (
    stationId: string,
    operatorUserId: string
  ) => {
    try {
      const response = await axiosInstance.post(
        `/ChargingStations/${stationId}/revoke-operator?operatorUserId=${operatorUserId}`
      );

      if (response.data.success) {
        await fetchAssignedUsers(stationId);
        // Refresh stations or update local state
        const fetchStations = async () => {
          const response = await axiosInstance.get("/ChargingStations");
          const result: ApiResponse = response.data;
          if (result.success && Array.isArray(result.data)) {
            setStations(result.data);
            setFilteredStations(result.data);
          }
        };
        await fetchStations();
        closeDialog();
      } else {
        throw new Error(response.data.message || "Failed to revoke operator");
      }
    } catch (err) {
      console.error("Error revoking operator:", err);
      setError("Failed to revoke operator");
    }
  };

  const fetchUsers = async () => {
    try {
      const response = await axiosInstance.get("/Users");
      if (response.data.success) {
        setUsers(response.data.data);
      } else {
        throw new Error(response.data.message || "Failed to fetch users");
      }
    } catch (err) {
      console.error("Error fetching users:", err);
      setError("Failed to fetch users");
    }
  };

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

  // UPDATE STATION STATUS
  const handleUpdateStationStatus = async (
    stationId: string,
    isActive: boolean
  ) => {
    try {
      const response = await axiosInstance.patch(
        `/ChargingStations/${stationId}/status?isActive=${isActive}`
      );

      if (response.data.success) {
        // Update local state
        setStations(
          stations.map((station) =>
            station.id === stationId ? { ...station, isActive } : station
          )
        );
      } else {
        throw new Error(response.data.message || "Failed to update status");
      }
    } catch (err) {
      console.error("Error updating station status:", err);
      setError("Failed to update station status");
    }
  };
  // CREATE
  const handleCreateStation = async (data: Partial<Station>) => {
    try {
      const response = await axiosInstance.post("/ChargingStations", data);

      const newStation = response.data.data;
      setStations([...stations, newStation]);
      closeDialog();
    } catch (err) {
      console.error("Error creating station:", err);
      setError("Failed to create station");
    }
  };

  // UPDATE
  const handleUpdateStation = async (data: Partial<Station>) => {
    if (!actionDialog.station) return;

    try {
      const response = await axiosInstance.put(
        `/ChargingStations/${actionDialog.station.id}`,
        data
      );

      const updatedStation = response.data.data;
      setStations(
        stations.map((station) =>
          station.id === actionDialog.station?.id ? updatedStation : station
        )
      );
      closeDialog();
    } catch (err) {
      console.error("Error updating station:", err);
      setError("Failed to update station");
    }
  };

  // DELETE
  const handleDeleteStation = async () => {
    if (!actionDialog.station) return;

    try {
      await axiosInstance.delete(
        `/ChargingStations/${actionDialog.station.id}`
      );

      setStations(
        stations.filter((station) => station.id !== actionDialog.station?.id)
      );
      closeDialog();
    } catch (err) {
      console.error("Error deleting station:", err);
      setError("Failed to delete station");
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
  // Dialog handlers
  const openCreateDialog = () => {
    setActionDialog({
      isOpen: true,
      action: "create",
      station: null,
    });
  };

  const openEditDialog = (station: Station) => {
    setActionDialog({
      isOpen: true,
      action: "edit",
      station,
    });
  };

  const openDeleteDialog = (station: Station) => {
    setActionDialog({
      isOpen: true,
      action: "delete",
      station,
    });
  };

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
    setSelectedOperatorId("");
  };

  const openAssignOperatorDialog = async (station: Station) => {
    setSelectedOperatorId("");
    setActionDialog({
      isOpen: true,
      action: "assignOperator",
      station,
    });
    await fetchUsers();
    await fetchAssignedUsers(station.id);
  };

  const openRevokeOperatorDialog = async (station: Station) => {
    setSelectedOperatorId("");
    setActionDialog({
      isOpen: true,
      action: "revokeOperator",
      station,
    });
    // Fetch users when opening the dialog
    await Promise.all([fetchAssignedUsers(station.id)]);
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
        <Button onClick={openCreateDialog}>
          <Plus className="mr-2 h-4 w-4" /> Add Station
        </Button>
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
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-8 w-8 p-0"
                        >
                          <MoreHorizontal className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem
                          onClick={() => openEditDialog(station)}
                        >
                          <Edit className="mr-2 h-4 w-4" /> Edit
                        </DropdownMenuItem>
                        <DropdownMenuSeparator />
                        <>
                          <DropdownMenuItem
                            onClick={() => openAssignOperatorDialog(station)}
                          >
                            <UserPlus className="mr-2 h-4 w-4" /> Assign
                            Operator
                          </DropdownMenuItem>
                          <DropdownMenuItem
                            onClick={() => openRevokeOperatorDialog(station)}
                          >
                            <UserMinus className="mr-2 h-4 w-4" /> Revoke
                            Operator
                          </DropdownMenuItem>
                          <DropdownMenuSeparator />
                        </>
                        <DropdownMenuItem
                          onClick={() => openDeleteDialog(station)}
                          className="text-destructive focus:text-destructive"
                        >
                          <Trash2 className="mr-2 h-4 w-4" /> Delete
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
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
                    onCheckedChange={(checked) =>
                      handleUpdateStationStatus(station.id, checked)
                    }
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
          {actionDialog.action === "create" && (
            <>
              <DialogHeader>
                <DialogTitle>Add New Station</DialogTitle>
                <DialogDescription>
                  Create a new charging station
                </DialogDescription>
              </DialogHeader>
              <StationForm
                station={null}
                onSubmit={handleCreateStation}
                onCancel={closeDialog}
              />
            </>
          )}

          {actionDialog.action === "edit" && actionDialog.station && (
            <>
              <DialogHeader>
                <DialogTitle>Edit Station</DialogTitle>
                <DialogDescription>
                  Update station information
                </DialogDescription>
              </DialogHeader>
              <StationForm
                station={actionDialog.station}
                onSubmit={handleUpdateStation}
                onCancel={closeDialog}
              />
            </>
          )}

          {actionDialog.action === "delete" && actionDialog.station && (
            <>
              <DialogHeader>
                <DialogTitle>Delete Station</DialogTitle>
                <DialogDescription>
                  This action cannot be undone.
                </DialogDescription>
              </DialogHeader>
              <StationDeleteConfirmation
                stationName={actionDialog.station.name}
                onConfirm={handleDeleteStation}
                onCancel={closeDialog}
              />
            </>
          )}
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
          {actionDialog.action === "assignOperator" && actionDialog.station && (
            <>
              <DialogHeader>
                <DialogTitle>Assign Station Operator</DialogTitle>
                <DialogDescription>
                  Assign an operator to {actionDialog.station.name}
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4">
                <div>
                  <label
                    htmlFor="operator"
                    className="block text-sm font-medium mb-2"
                  >
                    Select Operator
                  </label>
                  <select
                    id="operator"
                    value={selectedOperatorId}
                    onChange={(e) => setSelectedOperatorId(e.target.value)}
                    className="w-full p-2 border rounded-md"
                  >
                    <option value="">Select an operator</option>
                    {users
                      .filter((user) => user.role === 2 && user.isActive)
                      .map((user) => (
                        <option key={user.id} value={user.id}>
                          {user.firstName} {user.lastName} ({user.email})
                        </option>
                      ))}
                  </select>
                </div>
                <div className="flex justify-end space-x-2">
                  <Button variant="outline" onClick={closeDialog}>
                    Cancel
                  </Button>
                  <Button
                    onClick={() =>
                      handleAssignOperator(
                        actionDialog.station!.id,
                        selectedOperatorId
                      )
                    }
                    disabled={!selectedOperatorId}
                  >
                    Assign Operator
                  </Button>
                </div>
              </div>
            </>
          )}

          {/* Revoke Operator Dialog */}
          {actionDialog.action === "revokeOperator" && actionDialog.station && (
            <>
              <DialogHeader>
                <DialogTitle>Revoke Station Operator</DialogTitle>
                <DialogDescription>
                  Revoke an operator from {actionDialog.station.name}
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4">
                <div>
                  <label
                    htmlFor="operator"
                    className="block text-sm font-medium mb-2"
                  >
                    Select Operator to Revoke
                  </label>
                  {loadingAssignedUsers ? (
                    <div className="text-center py-4">
                      <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary mx-auto"></div>
                      <p className="text-sm text-muted-foreground mt-2">
                        Loading assigned operators...
                      </p>
                    </div>
                  ) : assignedUsers.length === 0 ? (
                    <div className="text-center py-4 text-muted-foreground">
                      No operators assigned to this station
                    </div>
                  ) : (
                    <select
                      id="operator"
                      value={selectedOperatorId}
                      onChange={(e) => setSelectedOperatorId(e.target.value)}
                      className="w-full p-2 border rounded-md"
                    >
                      <option value="">Select an operator</option>
                      {assignedUsers
                        .filter((user) => user.role === 2 && user.isActive)
                        .map((user) => (
                          <option key={user.id} value={user.id}>
                            {user.firstName} {user.lastName} ({user.email})
                          </option>
                        ))}
                    </select>
                  )}
                </div>
                <div className="flex justify-end space-x-2">
                  <Button variant="outline" onClick={closeDialog}>
                    Cancel
                  </Button>
                  <Button
                    onClick={() =>
                      handleRevokeOperator(
                        actionDialog.station!.id,
                        selectedOperatorId
                      )
                    }
                    disabled={!selectedOperatorId || assignedUsers.length === 0}
                    variant="destructive"
                  >
                    Revoke Operator
                  </Button>
                </div>
              </div>
            </>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
