import { useState, useEffect } from "react";
import {
  User,
  Mail,
  Shield,
  Calendar,
  Clock,
  Activity,
  Building,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import axiosInstance from "@/utils/axiosInstance";
import { RoleLabels, type DecodedToken, type UserProfile } from "@/types/user";
import { formatDate, formatTime, getRoleBadgeVariant, getStatusBadgeVariant } from "./ProfileSupport";

export default function ProfilePage() {
  const [profile, setProfile] = useState<UserProfile | null>(null);
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
        setProfile(response.data.data);
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
      <div className="flex items-center justify-center h-64">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto"></div>
          <p className="mt-2 text-muted-foreground">Loading profile...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-center">
          <div className="text-destructive mb-2">Error loading profile</div>
          <p className="text-muted-foreground mb-4">{error}</p>
          <Button onClick={fetchUserProfile}>Retry</Button>
        </div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-center">
          <div className="text-destructive mb-2">Profile not found</div>
          <Button onClick={fetchUserProfile}>Retry</Button>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">User Profile</h2>
        <p className="text-muted-foreground">
          Manage your account information and preferences
        </p>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        {/* Basic Information Card */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <User className="h-5 w-5" />
              Basic Information
            </CardTitle>
            <CardDescription>
              Your account details and role information
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center justify-between">
              <div className="space-y-1">
                <p className="text-sm font-medium">Username</p>
                <p className="text-sm text-muted-foreground">
                  {profile.username}
                </p>
              </div>
              <User className="h-4 w-4 text-muted-foreground" />
            </div>

            <div className="flex items-center justify-between">
              <div className="space-y-1">
                <p className="text-sm font-medium">Email</p>
                <p className="text-sm text-muted-foreground">{profile.email}</p>
              </div>
              <Mail className="h-4 w-4 text-muted-foreground" />
            </div>

            <div className="flex items-center justify-between">
              <div className="space-y-1">
                <p className="text-sm font-medium">Role</p>
                <Badge className={getRoleBadgeVariant(profile.role)}>
                  {RoleLabels[profile.role] || `Unknown (${profile.role})`}
                </Badge>
              </div>
              <Shield className="h-4 w-4 text-muted-foreground" />
            </div>

            <div className="flex items-center justify-between">
              <div className="space-y-1">
                <p className="text-sm font-medium">Status</p>
                <Badge className={getStatusBadgeVariant(profile.isActive)}>
                  {profile.isActive ? "Active" : "Inactive"}
                </Badge>
              </div>
              <Activity className="h-4 w-4 text-muted-foreground" />
            </div>
          </CardContent>
        </Card>

        {/* Account Activity Card */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Clock className="h-5 w-5" />
              Account Activity
            </CardTitle>
            <CardDescription>Recent login and account timeline</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center justify-between">
              <div className="space-y-1">
                <p className="text-sm font-medium">Last Login</p>
                <div className="flex flex-col">
                  <div className="flex items-center text-xs">
                    <Calendar className="h-3 w-3 mr-1" />
                    <span>{formatDate(profile.lastLogin)}</span>
                  </div>
                  <div className="flex items-center text-xs text-muted-foreground">
                    <Clock className="h-3 w-3 mr-1" />
                    <span>{formatTime(profile.lastLogin)}</span>
                  </div>
                </div>
              </div>
              <Activity className="h-4 w-4 text-muted-foreground" />
            </div>

            <div className="flex items-center justify-between">
              <div className="space-y-1">
                <p className="text-sm font-medium">Account Created</p>
                <div className="flex flex-col">
                  <div className="flex items-center text-xs">
                    <Calendar className="h-3 w-3 mr-1" />
                    <span>{formatDate(profile.createdAt)}</span>
                  </div>
                  <div className="flex items-center text-xs text-muted-foreground">
                    <Clock className="h-3 w-3 mr-1" />
                    <span>{formatTime(profile.createdAt)}</span>
                  </div>
                </div>
              </div>
              <Calendar className="h-4 w-4 text-muted-foreground" />
            </div>

            <div className="flex items-center justify-between">
              <div className="space-y-1">
                <p className="text-sm font-medium">Last Updated</p>
                <div className="flex flex-col">
                  <div className="flex items-center text-xs">
                    <Calendar className="h-3 w-3 mr-1" />
                    <span>{formatDate(profile.updatedAt)}</span>
                  </div>
                  <div className="flex items-center text-xs text-muted-foreground">
                    <Clock className="h-3 w-3 mr-1" />
                    <span>{formatTime(profile.updatedAt)}</span>
                  </div>
                </div>
              </div>
              <Clock className="h-4 w-4 text-muted-foreground" />
            </div>
          </CardContent>
        </Card>

        {/* Charging Stations Card - Only show for Station Operators */}
        {profile.role === 2 && profile.chargingStationIds.length > 0 && (
          <Card className="md:col-span-2">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Building className="h-5 w-5" />
                Managed Charging Stations
              </CardTitle>
              <CardDescription>
                Charging stations under your management
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid gap-2 sm:grid-cols-2 lg:grid-cols-3">
                {profile.chargingStationIds.map((stationId) => (
                  <div
                    key={stationId}
                    className="flex items-center justify-between rounded-lg border p-3"
                  >
                    <div className="space-y-1">
                      <p className="text-sm font-medium">Station ID</p>
                      <p className="text-xs text-muted-foreground font-mono">
                        {stationId}
                      </p>
                    </div>
                    <Building className="h-4 w-4 text-muted-foreground" />
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        )}

        {/* User ID Card */}
        <Card className="md:col-span-2">
          <CardHeader>
            <CardTitle>User Identification</CardTitle>
            <CardDescription>
              Your unique user identifier in the system
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex items-center justify-between rounded-lg border p-3">
              <div className="space-y-1">
                <p className="text-sm font-medium">User ID</p>
                <p className="text-sm text-muted-foreground font-mono">
                  {profile.id}
                </p>
              </div>
              <User className="h-4 w-4 text-muted-foreground" />
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="flex justify-end space-x-2">
        <Button variant="outline" onClick={fetchUserProfile}>
          Refresh
        </Button>
      </div>
    </div>
  );
}
