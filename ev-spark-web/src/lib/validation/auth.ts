import { z } from "zod";

export const loginSchema = z.object({
  email: z.string().min(1, { message: "Username is required" }),
  password: z
    .string()
    .min(6, { message: "Password must be at least 6 characters" }),
  role: z.enum(["admin", "operator", "owner"]),
  rememberMe: z.boolean().optional(),
});

export const registerSchemaUser = z.object({
  username: z.string().min(2, {
    message: "Username must be at least 2 characters.",
  }),
  email: z.string().email({
    message: "Please enter a valid email address.",
  }),
  password: z.string().min(6, {
    message: "Password must be at least 6 characters.",
  }),
  role: z.number().min(1).max(3),
});

export type RegisterFormValuesUser = z.infer<typeof registerSchemaUser>;

export type LoginFormValues = z.infer<typeof loginSchema>;
export const registerSchema = z
  .object({
    nic: z.string().min(1, { message: "NIC is required" }),
    name: z.string().min(1, { message: "Name is required" }),
    email: z
      .string()
      .min(1, { message: "Email is required" })
      .email({ message: "Invalid email address" }),
    password: z
      .string()
      .min(6, { message: "Password must be at least 6 characters" }),
    confirmPassword: z
      .string()
      .min(1, { message: "Please confirm your password" }),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Passwords don't match",
    path: ["confirmPassword"],
  });

export type RegisterFormValues = z.infer<typeof registerSchema>;

export const forgotPasswordSchema = z.object({
  email: z
    .string()
    .min(1, { message: "Email is required" })
    .email({ message: "Invalid email address" }),
});

export type ForgotPasswordFormValues = z.infer<typeof forgotPasswordSchema>;

export const otpVerificationSchema = z.object({
  otp: z.string().min(6, { message: "Please enter a valid OTP" }).max(6),
});

export type OtpVerificationFormValues = z.infer<typeof otpVerificationSchema>;
