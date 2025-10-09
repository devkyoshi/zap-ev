import { z } from "zod";

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: number;
  isActive: boolean;
}

export const userFormSchema = z.object({
  username: z
    .string()
    .min(2, { message: "Username must be at least 2 characters" }),
  email: z.string().email({ message: "Please enter a valid email address" }),
  role: z.number().min(1).max(2),
  password: z
    .string()
    .min(6, { message: "Password must be at least 6 characters" })
    .optional()
    .or(z.literal("")),
});

export type UserFormValues = z.infer<typeof userFormSchema>;

export interface UserProfile {
  username: string;
  email: string;
  passwordHash: string;
  role: number;
  chargingStationIds: string[];
  isActive: boolean;
  lastLogin: string;
  id: string;
  createdAt: string;
  updatedAt: string;
}

export interface DecodedToken {
  nameid: string;
  role: string;
  UserType: string;
  jti: string;
  iat: number;
  nbf: number;
  exp: number;
  iss: string;
  aud: string;
}

export const RoleLabels: { [key: number]: string } = {
  1: "BackOffice",
  2: "StationOperator",
};