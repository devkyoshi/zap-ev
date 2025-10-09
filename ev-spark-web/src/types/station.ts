import type { User } from "./user";

export interface Station {
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

export interface StationWithOperators extends Station {
  assignedOperators?: User[];
}