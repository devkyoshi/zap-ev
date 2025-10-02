import { apiRequest } from "./api-client"
import type { ApiResponse } from "./api-client"

// Types
export interface AuthCredentials {
  email: string
  password: string
  role: "admin" | "operator" | "owner"
  rememberMe?: boolean
}

export interface RegisterData {
  nic: string
  name: string
  email: string
  password: string
}

export interface ResetPasswordRequest {
  email: string
}

export interface VerifyOtpRequest {
  email: string
  otp: string
}

export interface AuthResponse {
  token: string
  user: {
    id: string
    name: string
    email: string
    role: string
  }
}

// Auth API functions
export const AuthService = {
  login: async (credentials: AuthCredentials): Promise<ApiResponse<AuthResponse>> => {
    return apiRequest({
      method: "POST",
      url: "/auth/login",
      data: credentials,
    })
  },

  register: async (data: RegisterData): Promise<ApiResponse> => {
    return apiRequest({
      method: "POST",
      url: "/auth/register",
      data,
    })
  },

  resetPassword: async (data: ResetPasswordRequest): Promise<ApiResponse> => {
    return apiRequest({
      method: "POST",
      url: "/auth/reset-password",
      data,
    })
  },

  verifyOtp: async (data: VerifyOtpRequest): Promise<ApiResponse> => {
    return apiRequest({
      method: "POST",
      url: "/auth/verify-otp",
      data,
    })
  },

  logout: async (): Promise<void> => {
    localStorage.removeItem("token")
  }
}
