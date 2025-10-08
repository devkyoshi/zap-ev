import { apiRequest } from "./api-client";
import type { ApiResponse } from "./api-client";

// API Response Types
export interface VehicleDetail {
  make: string;
  model: string;
  licensePlate: string;
  year: number;
}

export interface EVOwner {
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

// Local Owner type for the UI
export interface Owner {
  id: string;
  nic: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string; // ✅ not phone
  password: string;
  vehicleDetails: Vehicle[];
  registeredDate: string;
  active: boolean;
}

// Types
export interface EVOwner {
  id: string;
  nic: string;
  name: string;
  email: string;
  createdAt: string;
  active: boolean;
  vehicles?: Vehicle[];
}

export interface Vehicle {
  id: string;
  ownerId: string;
  registrationNumber: string;
  make: string;
  model: string;
  batteryCapacity: number;
}

export interface CreateOwnerData {
  nic: string;
  name: string;
  email: string;
  password: string;
}

export interface UpdateOwnerData {
  name?: string;
  email?: string;
  active?: boolean;
}

export interface AddVehicleData {
  ownerId: string;
  registrationNumber: string;
  make: string;
  model: string;
  batteryCapacity: number;
}

// EV Owner API functions
export const OwnerService = {
  getOwners: async (page = 1, limit = 10): Promise<ApiResponse<EVOwner[]>> => {
    return apiRequest({
      method: "GET",
      url: "/owners",
      params: { page, limit },
    });
  },

  getOwner: async (id: string): Promise<ApiResponse<EVOwner>> => {
    return apiRequest({
      method: "GET",
      url: `/owners/${id}`,
    });
  },

  createOwner: async (data: CreateOwnerData): Promise<ApiResponse<EVOwner>> => {
    return apiRequest({
      method: "POST",
      url: "/owners",
      data,
    });
  },

  updateOwner: async (
    id: string,
    data: UpdateOwnerData
  ): Promise<ApiResponse<EVOwner>> => {
    return apiRequest({
      method: "PUT",
      url: `/owners/${id}`,
      data,
    });
  },

  deleteOwner: async (id: string): Promise<ApiResponse> => {
    return apiRequest({
      method: "DELETE",
      url: `/owners/${id}`,
    });
  },

  toggleOwnerStatus: async (
    id: string,
    active: boolean
  ): Promise<ApiResponse> => {
    return apiRequest({
      method: "PATCH",
      url: `/owners/${id}/status`,
      data: { active },
    });
  },

  // Vehicle management
  getOwnerVehicles: async (
    ownerId: string
  ): Promise<ApiResponse<Vehicle[]>> => {
    return apiRequest({
      method: "GET",
      url: `/owners/${ownerId}/vehicles`,
    });
  },

  addVehicle: async (data: AddVehicleData): Promise<ApiResponse<Vehicle>> => {
    return apiRequest({
      method: "POST",
      url: `/vehicles`,
      data,
    });
  },

  updateVehicle: async (
    id: string,
    data: Partial<AddVehicleData>
  ): Promise<ApiResponse<Vehicle>> => {
    return apiRequest({
      method: "PUT",
      url: `/vehicles/${id}`,
      data,
    });
  },

  deleteVehicle: async (id: string): Promise<ApiResponse> => {
    return apiRequest({
      method: "DELETE",
      url: `/vehicles/${id}`,
    });
  },
};
