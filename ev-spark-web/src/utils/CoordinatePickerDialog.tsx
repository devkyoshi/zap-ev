// components/coordinate-picker-dialog.tsx
import { useState, useEffect } from "react";
import { MapContainer, TileLayer, Marker, useMapEvents } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

// Fix for default markers in react-leaflet
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl:
    "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png",
  iconUrl:
    "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png",
  shadowUrl:
    "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png",
});

// Shadcn UI Components (make sure these are installed)
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

interface CoordinatePickerDialogProps {
  trigger: React.ReactNode;
  onCoordinateSelect: (lat: number, lng: number) => void;
  initialLat?: number;
  initialLng?: number;
}

interface MapClickHandlerProps {
  onMapClick: (lat: number, lng: number) => void;
}

function MapClickHandler({ onMapClick }: MapClickHandlerProps) {
  useMapEvents({
    click: (e) => {
      onMapClick(e.latlng.lat, e.latlng.lng);
    },
  });
  return null;
}

export function CoordinatePickerDialog({
  trigger,
  onCoordinateSelect,
  initialLat = 51.505,
  initialLng = -0.09,
}: CoordinatePickerDialogProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [markerPosition, setMarkerPosition] = useState<[number, number]>([
    initialLat,
    initialLng,
  ]);
  const [manualLat, setManualLat] = useState(initialLat.toString());
  const [manualLng, setManualLng] = useState(initialLng.toString());

  useEffect(() => {
    setMarkerPosition([initialLat, initialLng]);
    setManualLat(initialLat.toString());
    setManualLng(initialLng.toString());
  }, [initialLat, initialLng]);

  const handleMapClick = (lat: number, lng: number) => {
    setMarkerPosition([lat, lng]);
    setManualLat(lat.toString());
    setManualLng(lng.toString());
  };

  const handleManualCoordinateChange = () => {
    const lat = parseFloat(manualLat);
    const lng = parseFloat(manualLng);

    if (
      !isNaN(lat) &&
      !isNaN(lng) &&
      lat >= -90 &&
      lat <= 90 &&
      lng >= -180 &&
      lng <= 180
    ) {
      setMarkerPosition([lat, lng]);
    }
  };

  const handleConfirm = () => {
    const [lat, lng] = markerPosition;
    onCoordinateSelect(lat, lng);
    setIsOpen(false);
  };

  const handleCancel = () => {
    // Reset to initial values
    setMarkerPosition([initialLat, initialLng]);
    setManualLat(initialLat.toString());
    setManualLng(initialLng.toString());
    setIsOpen(false);
  };

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogTrigger asChild>{trigger}</DialogTrigger>
      <DialogContent className="sm:max-w-[800px] max-h-[90vh] overflow-auto hide-scrollbar">
        <DialogHeader>
          <DialogTitle>Select Location on Map</DialogTitle>
          <DialogDescription>
            Click on the map to select coordinates or enter them manually. Click
            confirm when done.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4 ">
          {/* Manual Input Section */}
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="latitude">Latitude</Label>
              <div className="flex gap-2">
                <Input
                  id="latitude"
                  type="number"
                  min="-90"
                  max="90"
                  step="0.000001"
                  value={manualLat}
                  onChange={(e) => setManualLat(e.target.value)}
                  onBlur={handleManualCoordinateChange}
                  onKeyDown={(e) =>
                    e.key === "Enter" && handleManualCoordinateChange()
                  }
                />
                <Button
                  type="button"
                  variant="outline"
                  onClick={handleManualCoordinateChange}
                >
                  Update
                </Button>
              </div>
            </div>
            <div className="space-y-2">
              <Label htmlFor="longitude">Longitude</Label>
              <div className="flex gap-2">
                <Input
                  id="longitude"
                  type="number"
                  min="-180"
                  max="180"
                  step="0.000001"
                  value={manualLng}
                  onChange={(e) => setManualLng(e.target.value)}
                  onBlur={handleManualCoordinateChange}
                  onKeyDown={(e) =>
                    e.key === "Enter" && handleManualCoordinateChange()
                  }
                />
                <Button
                  type="button"
                  variant="outline"
                  onClick={handleManualCoordinateChange}
                >
                  Update
                </Button>
              </div>
            </div>
          </div>

          {/* Current Coordinates Display */}
          <div className="bg-muted p-3 rounded-md text-sm">
            <strong>Selected Coordinates:</strong>
            <br />
            Latitude: {markerPosition[0].toFixed(6)}, Longitude:{" "}
            {markerPosition[1].toFixed(6)}
          </div>

          {/* Map Container */}
          <div className="h-[400px] rounded-md overflow-hidden border">
            <MapContainer
              center={markerPosition}
              zoom={13}
              style={{ height: "100%", width: "100%" }}
            >
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />
              <Marker position={markerPosition} />
              <MapClickHandler onMapClick={handleMapClick} />
            </MapContainer>
          </div>
        </div>

        <DialogFooter>
          <Button type="button" variant="outline" onClick={handleCancel}>
            Cancel
          </Button>
          <Button type="button" onClick={handleConfirm}>
            Confirm Selection
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
