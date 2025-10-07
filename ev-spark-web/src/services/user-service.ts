import { apiRequest } from "./api-client";
import type { ApiResponse } from "./api-client";

// Types
export interface User {
  username: any;
  id: string;
  name: string;
  email: string;
  role: any;
  createdAt: string;
  active: boolean;
}

export interface CreateUserData {
  name: string;
  email: string;
  password: string;
  role: string;
}

export interface UpdateUserData {
  name?: string;
  email?: string;
  role?: string;
  active?: boolean;
}

// User API functions
export const UserService = {
  getUsers: async (page = 1, limit = 10): Promise<ApiResponse<User[]>> => {
    return apiRequest({
      method: "GET",
      url: "/users",
      params: { page, limit },
    });
  },

  getUser: async (id: string): Promise<ApiResponse<User>> => {
    return apiRequest({
      method: "GET",
      url: `/users/${id}`,
    });
  },

  createUser: async (data: CreateUserData): Promise<ApiResponse<User>> => {
    return apiRequest({
      method: "POST",
      url: "/users",
      data,
    });
  },

  updateUser: async (
    id: string,
    data: UpdateUserData
  ): Promise<ApiResponse<User>> => {
    return apiRequest({
      method: "PUT",
      url: `/users/${id}`,
      data,
    });
  },

  deleteUser: async (id: string): Promise<ApiResponse> => {
    return apiRequest({
      method: "DELETE",
      url: `/users/${id}`,
    });
  },

  // For admins to manage operators and other admins
  toggleUserStatus: async (
    id: string,
    active: boolean
  ): Promise<ApiResponse> => {
    return apiRequest({
      method: "PATCH",
      url: `/users/${id}/status`,
      data: { active },
    });
  },
};
