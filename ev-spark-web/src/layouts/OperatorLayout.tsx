import { DashboardLayout } from "@/components/dashboard/dashboard-components";
import {
  LayoutDashboard,
  Users,
  Car,
  Zap,
  Calendar,
} from "lucide-react";
import { Outlet } from "react-router-dom";
import { useState, useEffect } from "react";
import axiosInstance from "@/utils/axiosInstance";

interface DecodedToken {
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

interface UserProfile {
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

const RoleLabels: { [key: number]: string } = {
  0: "EV Owner",
  1: "BackOffice",
  2: "StationOperator",
};

const adminNavItems = [
  {
    label: "Stations",
    href: "/operator/stations",
    icon: Zap,
  },
  {
    label: "Bookings",
    href: "/operator/bookings",
    icon: Calendar,
  },
];

const OperatorLayout = () => {
  const [userProfile, setUserProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const getUserIdFromToken = (): string | null => {
    try {
      const token = localStorage.getItem("authToken");
      if (!token) return null;

      // Decode JWT token payload
      const payload = JSON.parse(atob(token.split(".")[1])) as DecodedToken;
      return payload.nameid;
    } catch (err) {
      console.error("Error decoding token:", err);
      return null;
    }
  };

  const fetchUserProfile = async () => {
    try {
      setLoading(true);
      setError(null);

      const userId = getUserIdFromToken();
      if (!userId) {
        throw new Error("User ID not found in token");
      }

      const response = await axiosInstance.get(`/Users/${userId}`);

      if (response.data.success) {
        setUserProfile(response.data.data);
      } else {
        throw new Error(
          response.data.message || "Failed to fetch user profile"
        );
      }
    } catch (err) {
      const message =
        err instanceof Error
          ? err.message
          : "An error occurred while fetching profile";
      setError(message);
      console.error("Error fetching user profile:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUserProfile();
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto"></div>
          <p className="mt-2 text-muted-foreground">
            Loading station operator dashboard...
          </p>
        </div>
      </div>
    );
  }

  // Show error state
  if (error || !userProfile) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="text-destructive mb-2">Error loading dashboard</div>
          <p className="text-muted-foreground mb-4">
            {error || "Failed to load user profile"}
          </p>
          <button
            onClick={fetchUserProfile}
            className="px-4 py-2 bg-primary text-primary-foreground rounded-md hover:bg-primary/90"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  // Prepare user data for DashboardLayout
  const adminUser = {
    name: userProfile.username,
    email: userProfile.email,
    role: RoleLabels[userProfile.role] || "User",
  };

  return (
    <DashboardLayout
      navItems={adminNavItems}
      title="Station Operator Dashboard"
      userName={adminUser.name}
      userEmail={adminUser.email}
      userRole={adminUser.role}
    >
      <Outlet />
    </DashboardLayout>
  );
};

export default OperatorLayout;
