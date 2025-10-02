import {
  createBrowserRouter,
  RouterProvider,
  Navigate,
} from "react-router-dom";
import { Toaster } from "sonner";

import { ThemeProvider } from "./components/theme-provider";
import AdminDashboard from "./pages/admin/Dashboard";
import UsersPage from "./pages/admin/UsersPage";
import OwnersPage from "./pages/admin/OwnersPage";
import StationsManagementPage from "./pages/admin/StationsManagementPage";
import BookingsManagementPage from "./pages/admin/BookingsManagementPage";
import OwnerDashboard from "./pages/owner/Dashboard";
import ActiveBookingsPage from "./pages/owner/ActiveBookingsPage";
import BookingHistoryPage from "./pages/owner/BookingHistoryPage";
import ProfilePage from "./pages/owner/ProfilePage";
import NewBookingPage from "./pages/owner/NewBookingPage";
import FindStationsPage from "./pages/owner/FindStationsPage";
import RecommendedStationsPage from "./pages/owner/RecommendedStationsPage";
import ChargingHistoryPage from "./pages/owner/ChargingHistoryPage";
import StationsPage from "./pages/operator/StationsPage";
import OperatorActiveBookingsPage from "./pages/operator/ActiveBookingsPage";
import AllBookingsPage from "./pages/operator/AllBookingsPage";
import SettingsPage from "./pages/operator/SettingsPage";

// Layouts
import AuthLayout from "./layouts/AuthLayout";
import AdminLayout from "./layouts/AdminLayout";
import OperatorLayout from "./layouts/OperatorLayout";
import OwnerLayout from "./layouts/OwnerLayout";

// Auth Pages
import LoginPage from "./pages/auth/LoginPage";
import RegisterPage from "./pages/auth/RegisterPage";
import ForgotPasswordPage from "./pages/auth/ForgotPasswordPage";
import OTPVerificationPage from "./pages/auth/OTPVerificationPage";
import ErrorPage from "./pages/ErrorPage";
import RegisterPageUser from "./pages/auth/RegisterPageUser";
// Application router configuration

const router = createBrowserRouter([
  {
    path: "/",
    element: (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <h1 className="text-2xl font-bold mb-4">EV Spark Charging System</h1>
          <p className="mb-6">
            Welcome to the EV Charging Station Booking System.
          </p>
          <div className="flex justify-center space-x-4">
            <a
              href="/auth/login"
              className="inline-flex items-center justify-center rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground"
            >
              Sign In
            </a>
            <a
              href="/auth/register"
              className="inline-flex items-center justify-center rounded-md border bg-background px-4 py-2 text-sm font-medium shadow-xs"
            >
              Register
            </a>
            <a
              href="/auth/registerUser"
              className="inline-flex items-center justify-center rounded-md border bg-background px-4 py-2 text-sm font-medium shadow-xs"
            >
              Register as USer
            </a>
          </div>
        </div>
      </div>
    ),
  },
  {
    path: "/auth",
    element: <AuthLayout />,
    children: [
      {
        path: "login",
        element: <LoginPage />,
      },
      {
        path: "register",
        element: <RegisterPage />,
      },
      {
        path: "registerUser",
        element: <RegisterPageUser />,
      },
      {
        path: "forgot-password",
        element: <ForgotPasswordPage />,
      },
      {
        path: "verify-otp",
        element: <OTPVerificationPage />,
      },
    ],
  },
  // Admin routes
  {
    path: "/admin",
    element: <AdminLayout />,
    children: [
      {
        path: "",
        element: <Navigate to="/admin/dashboard" replace />,
      },
      {
        path: "dashboard",
        element: <AdminDashboard />,
      },
      {
        path: "users",
        element: <UsersPage />,
      },
      {
        path: "owners",
        element: <OwnersPage />,
      },
      {
        path: "stations",
        element: <StationsManagementPage />,
      },
      {
        path: "bookings",
        element: <BookingsManagementPage />,
      },
      {
        path: "settings",
        element: <div>Settings</div>,
      },
    ],
  },
  // Station Operator routes
  {
    path: "/operator",
    element: <OperatorLayout />,
    children: [
      {
        path: "",
        element: <Navigate to="/operator/stations" replace />,
      },
      {
        path: "stations",
        element: <StationsPage />,
      },
      {
        path: "bookings",
        element: <AllBookingsPage />,
      },
      {
        path: "bookings/active",
        element: <OperatorActiveBookingsPage />,
      },
      {
        path: "settings",
        element: <SettingsPage />,
      },
    ],
  },
  // EV Owner routes
  {
    path: "/owner",
    element: <OwnerLayout />,
    children: [
      {
        path: "",
        element: <Navigate to="/owner/dashboard" replace />,
      },
      {
        path: "dashboard",
        element: <OwnerDashboard />,
      },
      {
        path: "stations",
        element: <FindStationsPage />,
      },
      {
        path: "stations/recommended",
        element: <RecommendedStationsPage />,
      },
      {
        path: "charging/history",
        element: <ChargingHistoryPage />,
      },
      {
        path: "bookings/upcoming",
        element: <ActiveBookingsPage />,
      },
      {
        path: "bookings/history",
        element: <BookingHistoryPage />,
      },
      {
        path: "bookings/new",
        element: <NewBookingPage />,
      },
      {
        path: "profile",
        element: <ProfilePage />,
      },
    ],
  },
  {
    path: "/unauthorized",
    element: <ErrorPage />,
  },
  {
    path: "*",
    element: (
      <ErrorPage
        title="Page Not Found"
        message="The page you're looking for doesn't exist."
        code={404}
      />
    ),
  },
]);

function App() {
  return (
    <ThemeProvider defaultTheme="system">
      <RouterProvider router={router} />
      <Toaster richColors position="top-center" />
    </ThemeProvider>
  );
}

export default App;
