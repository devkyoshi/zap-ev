import { useState, useEffect } from "react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Label } from "@/components/ui/label";

import axiosInstance from "@/utils/axiosInstance";

// Add Station interface
interface Station {
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

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T[];
  errors: string[];
}

interface CreateBookingFormProps {
  onSubmit: (data: CreateBookingData) => void;
  onCancel: () => void;
}

interface CreateBookingData {
  chargingStationId: string;
  reservationDateTime: string;
  durationMinutes: number;
  notes: string;
}

export function CreateBookingForm({
  onSubmit,
  onCancel,
}: CreateBookingFormProps) {
  const [formData, setFormData] = useState<CreateBookingData>({
    chargingStationId: "",
    reservationDateTime: "",
    durationMinutes: 60,
    notes: "",
  });
  const [stations, setStations] = useState<Station[]>([]);
  const [loading, setLoading] = useState(false);
  const [stationsLoading, setStationsLoading] = useState(true);

  // Fetch stations on component mount
  useEffect(() => {
    const fetchStations = async () => {
      try {
        setStationsLoading(true);
        const response = await axiosInstance.get<ApiResponse<Station[]>>(
          "/ChargingStations"
        );

        const result = response.data;

        if (result.success && Array.isArray(result.data)) {
          const activeStations = result.data.filter(
            (station) => station.isActive
          );
          setStations(activeStations);
        } else {
          throw new Error("Invalid API response format");
        }
      } catch (err) {
        console.error("Error fetching stations:", err);
        setStations([]);
      } finally {
        setStationsLoading(false);
      }
    };

    fetchStations();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      await onSubmit(formData);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (
    field: keyof CreateBookingData,
    value: string | number
  ) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  // Format station display text
  const getStationDisplayText = (station: Station) => {
    return `${station.name} - ${station.location.address} (${station.availableSlots} available slots)`;
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="chargingStationId">Charging Station *</Label>
        <Select
          value={formData.chargingStationId}
          onValueChange={(value) => handleChange("chargingStationId", value)}
          disabled={stationsLoading}
        >
          <SelectTrigger>
            <SelectValue
              placeholder={
                stationsLoading ? "Loading stations..." : "Select a station"
              }
            />
          </SelectTrigger>
          <SelectContent>
            {stations.map((station) => (
              <SelectItem key={station.id} value={station.id}>
                {getStationDisplayText(station)}
              </SelectItem>
            ))}
          </SelectContent>
          {stations.length === 0 && !stationsLoading && (
            <p className="text-sm text-muted-foreground px-2 py-1">
              No stations available
            </p>
          )}
          {stationsLoading && (
            <p className="text-sm text-muted-foreground px-2 py-1">
              Loading...
            </p>
          )}
        </Select>
        {stations.length > 0 && (
          <p className="text-xs text-muted-foreground">
            {stations.length} active station(s) available
          </p>
        )}
      </div>

      <div className="space-y-2">
        <Label htmlFor="reservationDateTime">Reservation Date & Time *</Label>
        <Input
          id="reservationDateTime"
          type="datetime-local"
          value={formData.reservationDateTime}
          onChange={(e) => handleChange("reservationDateTime", e.target.value)}
          required
        />
      </div>

      <div className="space-y-2">
        <Label htmlFor="durationMinutes">Duration (minutes) *</Label>
        <Select
          value={formData.durationMinutes.toString()}
          onValueChange={(value) =>
            handleChange("durationMinutes", Number(value))
          }
        >
          <SelectTrigger>
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="30">30 minutes</SelectItem>
            <SelectItem value="60">1 hour</SelectItem>
            <SelectItem value="120">2 hours</SelectItem>
            <SelectItem value="180">3 hours</SelectItem>
            <SelectItem value="240">4 hours</SelectItem>
            <SelectItem value="480">8 hours</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div className="space-y-2">
        <Label htmlFor="notes">Notes</Label>
        <Input
          id="notes"
          value={formData.notes}
          onChange={(e) => handleChange("notes", e.target.value)}
          placeholder="Additional notes (optional)"
        />
      </div>

      <div className="flex justify-end space-x-2 pt-4">
        <Button
          type="button"
          variant="outline"
          onClick={onCancel}
          disabled={loading}
        >
          Cancel
        </Button>
        <Button
          type="submit"
          disabled={
            loading ||
            !formData.chargingStationId ||
            !formData.reservationDateTime
          }
        >
          {loading ? "Creating..." : "Create Booking"}
        </Button>
      </div>
    </form>
  );
}
