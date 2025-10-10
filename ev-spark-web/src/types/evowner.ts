import { z } from "zod";

export const vehicleSchema = z.object({
  make: z.string().min(1, { message: "Make is required" }),
  model: z.string().min(1, { message: "Model is required" }),
  licensePlate: z.string().min(1, { message: "License plate is required" }),
  year: z.coerce
    .number()
    .min(1900, { message: "Enter a valid year" })
    .max(new Date().getFullYear() + 1, {
      message: "Year cannot be in the future",
    }),
});

// Schema for creating new owner (with password)
export const createOwnerSchema = z.object({
  nic: z.string().min(1, { message: "NIC is required" }),
  firstName: z.string().min(1, { message: "First name is required" }),
  lastName: z.string().min(1, { message: "Last name is required" }),
  email: z.string().email({ message: "Please enter a valid email address" }),
  phoneNumber: z.string().min(1, { message: "Phone number is required" }),
  password: z
    .string()
    .min(6, { message: "Password must be at least 6 characters" }),
  vehicleDetails: z
    .array(vehicleSchema)
    .min(1, { message: "At least one vehicle is required" }),
});

// Schema for updating owner (password optional)
export const updateOwnerSchema = z.object({
  nic: z.string().min(1, { message: "NIC is required" }),
  firstName: z.string().min(1, { message: "First name is required" }),
  lastName: z.string().min(1, { message: "Last name is required" }),
  email: z.string().email({ message: "Please enter a valid email address" }),
  phoneNumber: z.string().min(1, { message: "Phone number is required" }),
  password: z
    .string()
    .min(6, { message: "Password must be at least 6 characters" })
    .optional()
    .or(z.literal("")),
  vehicleDetails: z
    .array(vehicleSchema)
    .min(1, { message: "At least one vehicle is required" }),
});

export type CreateOwnerFormValues = z.infer<typeof createOwnerSchema>;
export type UpdateOwnerFormValues = z.infer<typeof updateOwnerSchema>;
export type OwnerFormValues = CreateOwnerFormValues | UpdateOwnerFormValues;