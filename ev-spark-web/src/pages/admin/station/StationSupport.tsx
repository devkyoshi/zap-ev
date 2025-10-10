import type { Station } from "@/types/station";
import { Battery, Camera, Car, Coffee, Shield, Wifi } from "lucide-react";

export const getAmenityIcon = (amenity: string) => {
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

export const getDayNames = (days: number[]) => {
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

export const getStatusColor = (station: Station) => {
    if (!station.isActive)
        return "bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400";
    if (station.availableSlots === 0)
        return "bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400";
    return "bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400";
};

export const getStatusText = (station: Station) => {
    if (!station.isActive) return "Offline";
    if (station.availableSlots === 0) return "Full";
    return "Available";
};