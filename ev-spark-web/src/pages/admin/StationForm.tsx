import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Switch } from "@/components/ui/switch";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

// Define the Station interface
interface Station {
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
  availableSlots: number; // ✅ Added this
  pricePerHour: number;
  operatingHours: {
    openTime: string;
    closeTime: string;
    operatingDays: number[];
  };
  amenities: string[];
  isActive: boolean;
}

interface StationFormProps {
  station: Station | null;
  onSubmit: (data: any) => void;
  onCancel: () => void;
}

export function StationForm({ station, onSubmit, onCancel }: StationFormProps) {
  const [formData, setFormData] = useState({
    name: station?.name || "",
    location: {
      latitude: station?.location.latitude || 0,
      longitude: station?.location.longitude || 0,
      address: station?.location.address || "",
      city: station?.location.city || "",
      province: station?.location.province || "",
    },
    type: station?.type || 1,
    totalSlots: station?.totalSlots || 0,
    availableSlots: station?.availableSlots || 0, // ✅ Added this
    pricePerHour: station?.pricePerHour || 0,
    operatingHours: {
      openTime: station?.operatingHours.openTime || "06:00",
      closeTime: station?.operatingHours.closeTime || "22:00",
      operatingDays: station?.operatingHours.operatingDays || [1, 2, 3, 4, 5],
    },
    amenities: station?.amenities || [],
    isActive: station?.isActive ?? true,
  });

  const [newAmenity, setNewAmenity] = useState("");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit(formData);
  };

  const addAmenity = () => {
    if (newAmenity.trim() && !formData.amenities.includes(newAmenity.trim())) {
      setFormData({
        ...formData,
        amenities: [...formData.amenities, newAmenity.trim()],
      });
      setNewAmenity("");
    }
  };

  const removeAmenity = (amenity: string) => {
    setFormData({
      ...formData,
      amenities: formData.amenities.filter((a) => a !== amenity),
    });
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {/* Station Name */}
      <div className="space-y-2">
        <Label htmlFor="name">Station Name</Label>
        <Input
          id="name"
          value={formData.name}
          onChange={(e) => setFormData({ ...formData, name: e.target.value })}
          required
        />
      </div>

      {/* Location */}
      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="latitude">Latitude</Label>
          <Input
            id="latitude"
            type="number"
            step="any"
            value={formData.location.latitude}
            onChange={(e) =>
              setFormData({
                ...formData,
                location: {
                  ...formData.location,
                  latitude: parseFloat(e.target.value),
                },
              })
            }
            required
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="longitude">Longitude</Label>
          <Input
            id="longitude"
            type="number"
            step="any"
            value={formData.location.longitude}
            onChange={(e) =>
              setFormData({
                ...formData,
                location: {
                  ...formData.location,
                  longitude: parseFloat(e.target.value),
                },
              })
            }
            required
          />
        </div>
      </div>

      {/* Address */}
      <div className="space-y-2">
        <Label htmlFor="address">Address</Label>
        <Textarea
          id="address"
          value={formData.location.address}
          onChange={(e) =>
            setFormData({
              ...formData,
              location: { ...formData.location, address: e.target.value },
            })
          }
          required
        />
      </div>

      {/* City + Province */}
      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="city">City</Label>
          <Input
            id="city"
            value={formData.location.city}
            onChange={(e) =>
              setFormData({
                ...formData,
                location: { ...formData.location, city: e.target.value },
              })
            }
            required
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="province">Province</Label>
          <Input
            id="province"
            value={formData.location.province}
            onChange={(e) =>
              setFormData({
                ...formData,
                location: { ...formData.location, province: e.target.value },
              })
            }
            required
          />
        </div>
      </div>

      {/* Charger Type + Total Slots */}
      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="type">Charger Type</Label>
          <Select
            value={formData.type.toString()}
            onValueChange={(value) =>
              setFormData({ ...formData, type: parseInt(value) })
            }
          >
            <SelectTrigger>
              <SelectValue placeholder="Select type" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="1">Type 1</SelectItem>
              <SelectItem value="2">Type 2</SelectItem>
              <SelectItem value="3">Type 3</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label htmlFor="totalSlots">Total Slots</Label>
          <Input
            id="totalSlots"
            type="number"
            value={formData.totalSlots}
            onChange={(e) =>
              setFormData({ ...formData, totalSlots: parseInt(e.target.value) })
            }
            required
          />
        </div>
      </div>

      {/* ✅ Available Slots */}
      <div className="space-y-2">
        <Label htmlFor="availableSlots">Available Slots</Label>
        <Input
          id="availableSlots"
          type="number"
          value={formData.availableSlots}
          onChange={(e) =>
            setFormData({
              ...formData,
              availableSlots: parseInt(e.target.value),
            })
          }
          required
        />
      </div>

      {/* Price per hour */}
      <div className="space-y-2">
        <Label htmlFor="pricePerHour">Price per Hour (LKR)</Label>
        <Input
          id="pricePerHour"
          type="number"
          value={formData.pricePerHour}
          onChange={(e) =>
            setFormData({ ...formData, pricePerHour: parseInt(e.target.value) })
          }
          required
        />
      </div>

      {/* Operating Hours */}
      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="openTime">Open Time</Label>
          <Input
            id="openTime"
            type="time"
            value={formData.operatingHours.openTime}
            onChange={(e) =>
              setFormData({
                ...formData,
                operatingHours: {
                  ...formData.operatingHours,
                  openTime: e.target.value,
                },
              })
            }
            required
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="closeTime">Close Time</Label>
          <Input
            id="closeTime"
            type="time"
            value={formData.operatingHours.closeTime}
            onChange={(e) =>
              setFormData({
                ...formData,
                operatingHours: {
                  ...formData.operatingHours,
                  closeTime: e.target.value,
                },
              })
            }
            required
          />
        </div>
      </div>

      {/* Amenities */}
      <div className="space-y-2">
        <Label>Amenities</Label>
        <div className="flex gap-2">
          <Input
            value={newAmenity}
            onChange={(e) => setNewAmenity(e.target.value)}
            placeholder="Add amenity"
          />
          <Button type="button" onClick={addAmenity}>
            Add
          </Button>
        </div>
        <div className="flex flex-wrap gap-2 mt-2">
          {formData.amenities.map((amenity, index) => (
            <div
              key={index}
              className="flex items-center gap-1 bg-secondary px-2 py-1 rounded text-sm"
            >
              {amenity}
              <button
                type="button"
                onClick={() => removeAmenity(amenity)}
                className="text-destructive hover:text-destructive/80"
              >
                ×
              </button>
            </div>
          ))}
        </div>
      </div>

      {/* ✅ Active Station toggle */}
      <div className="flex items-center space-x-2">
        <Switch
          checked={!!formData.isActive}
          onCheckedChange={(checked: boolean) =>
            setFormData((prev) => ({ ...prev, isActive: checked }))
          }
          id="isActive"
        />
        <Label htmlFor="isActive">Active Station</Label>
      </div>

      {/* Buttons */}
      <div className="flex justify-end gap-2">
        <Button type="button" variant="outline" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit">
          {station ? "Update Station" : "Create Station"}
        </Button>
      </div>
    </form>
  );
}
