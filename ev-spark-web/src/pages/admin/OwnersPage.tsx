import { useState, useEffect } from "react";
import { Search, Plus, MoreHorizontal, Trash2, Edit } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Switch } from "@/components/ui/switch";
import { Badge } from "@/components/ui/badge";
import axiosInstance from "@/utils/axiosInstance";
import { OwnerForm } from "./OwnerForm";

interface Vehicle {
  make: string;
  model: string;
  licensePlate: string;
  year: number;
}

interface EVOwner {
  id: string;
  nic: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  passwordHash: string;
  password?: string;
  isActive: boolean;
  vehicleDetails: Vehicle[];
  lastLogin: string | null;
  createdAt: string;
  updatedAt: string;
}

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T[];
  errors: string[];
}

type ActionDialogState = {
  isOpen: boolean;
  action: "create" | "edit" | "delete" | null;
  owner: EVOwner | null;
};

export default function EVOwnersPage() {
  const [owners, setOwners] = useState<EVOwner[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [actionDialog, setActionDialog] = useState<ActionDialogState>({
    isOpen: false,
    action: null,
    owner: null,
  });

  // Fetch owners from API
  const fetchOwners = async () => {
    try {
      setLoading(true);
      const response = await axiosInstance.get<ApiResponse<EVOwner[]>>(
        "/EVOwners"
      );

      if (response.data.success) {
        setOwners(response.data.data.flat());
      } else {
        throw new Error(response.data.message || "Failed to fetch owners");
      }
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "An error occurred while fetching owners"
      );
      console.error("Error fetching owners:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOwners();
  }, []);

  // Filter owners by search query
  const filteredOwners = owners.filter((owner) => {
    const query = searchQuery.toLowerCase();
    const fullName = `${owner.firstName} ${owner.lastName}`.toLowerCase();

    return (
      fullName.includes(query) ||
      owner.email.toLowerCase().includes(query) ||
      owner.phoneNumber.toLowerCase().includes(query) ||
      owner.nic.toLowerCase().includes(query)
    );
  });

  const handleToggleStatus = async (ownerId: string, isActive: boolean) => {
    try {
      const owner = owners.find((o) => o.id === ownerId);
      if (!owner) return;

      const response = await axiosInstance.put<ApiResponse<EVOwner>>(
        `/EVOwners/${ownerId}`,
        {
          ...owner,
          isActive,
        }
      );

      if (response.data.success) {
        setOwners(
          owners.map((owner) =>
            owner.id === ownerId ? { ...owner, isActive } : owner
          )
        );
      } else {
        throw new Error(
          response.data.message || "Failed to update owner status"
        );
      }
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Failed to update owner status"
      );
      console.error("Error updating owner status:", err);
    }
  };

  const handleDeleteOwner = async () => {
    if (!actionDialog.owner) return;

    try {
      const response = await axiosInstance.delete<ApiResponse<EVOwner>>(
        `/EVOwners/${actionDialog.owner.id}`
      );

      if (response.data.success) {
        setOwners(
          owners.filter((owner) => owner.id !== actionDialog.owner?.id)
        );
        closeDialog();
      } else {
        throw new Error(response.data.message || "Failed to delete owner");
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete owner");
      console.error("Error deleting owner:", err);
    }
  };

  // In your main component, update the handlers to use EVOwner:

  const handleCreateOwner = async (data: Partial<EVOwner>) => {
    try {
      const response = await axiosInstance.post<ApiResponse<EVOwner>>(
        "/EVOwners/register",
        {
          nic: data.nic,
          firstName: data.firstName,
          lastName: data.lastName,
          email: data.email,
          phoneNumber: data.phoneNumber,
          password: data.password,
          vehicleDetails: data.vehicleDetails || [],
        }
      );

      if (response.data.success) {
        await fetchOwners();
        closeDialog();
      } else {
        throw new Error(response.data.message || "Failed to create owner");
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to create owner");
      console.error("Error creating owner:", err);
    }
  };

  const handleUpdateOwner = async (data: Partial<EVOwner>) => {
    if (!actionDialog.owner) {
      console.error("No owner selected for update");
      return;
    }

    try {
      console.log("Updating owner with data:", data);
      console.log("Owner ID:", actionDialog.owner.id);

      const updateData = {
        nic: data.nic,
        firstName: data.firstName,
        lastName: data.lastName,
        email: data.email,
        phoneNumber: data.phoneNumber,
        isActive: actionDialog.owner.isActive,
        // Only include password if provided
        ...(data.password && { password: data.password }),
        vehicleDetails: data.vehicleDetails,
      };

      console.log("Sending update payload:", updateData);

      const response = await axiosInstance.put<ApiResponse<EVOwner>>(
        `/EVOwners/${actionDialog.owner.id}`,
        updateData
      );

      console.log("Update response:", response);

      if (response.data.success) {
        console.log("Update successful, refreshing data...");
        await fetchOwners();
        closeDialog();
      } else {
        console.error("Update failed:", response.data.message);
        throw new Error(response.data.message || "Failed to update owner");
      }
    } catch (err) {
      console.error("Error in handleUpdateOwner:", err);
      const errorMessage =
        err instanceof Error ? err.message : "Failed to update owner";
      setError(errorMessage);
    }
  };

  const openCreateDialog = () => {
    setActionDialog({
      isOpen: true,
      action: "create",
      owner: null,
    });
  };

  const openEditDialog = (owner: EVOwner) => {
    setActionDialog({
      isOpen: true,
      action: "edit",
      owner,
    });
  };

  const openDeleteDialog = (owner: EVOwner) => {
    setActionDialog({
      isOpen: true,
      action: "delete",
      owner,
    });
  };

  const closeDialog = () => {
    setActionDialog({
      isOpen: false,
      action: null,
      owner: null,
    });
    setError(null);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  };

  const formatLastLogin = (lastLogin: string | null) => {
    if (!lastLogin) return "Never";
    return formatDate(lastLogin);
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <p className="text-lg">Loading EV owners...</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">
          EV Owner Management
        </h2>
        <p className="text-muted-foreground">
          Manage electric vehicle owners registered on the platform
        </p>
      </div>

      {error && (
        <div className="bg-destructive/15 text-destructive px-4 py-3 rounded-md">
          {error}
        </div>
      )}

      <div className="flex justify-between items-center">
        <div className="relative w-72">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Search owners..."
            className="pl-8"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>
        <Button onClick={openCreateDialog}>
          <Plus className="mr-2 h-4 w-4" /> Add Owner
        </Button>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead>Email</TableHead>
              <TableHead>Phone</TableHead>
              <TableHead>NIC</TableHead>
              <TableHead>Registered</TableHead>
              <TableHead>Last Login</TableHead>
              <TableHead>Vehicles</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredOwners.length === 0 ? (
              <TableRow>
                <TableCell colSpan={9} className="h-24 text-center">
                  {owners.length === 0
                    ? "No owners found."
                    : "No matching owners found."}
                </TableCell>
              </TableRow>
            ) : (
              filteredOwners.map((owner) => (
                <TableRow key={owner.id}>
                  <TableCell className="font-medium">
                    {owner.firstName} {owner.lastName}
                  </TableCell>
                  <TableCell>{owner.email}</TableCell>
                  <TableCell>{owner.phoneNumber}</TableCell>
                  <TableCell>{owner.nic}</TableCell>
                  <TableCell>{formatDate(owner.createdAt)}</TableCell>
                  <TableCell>{formatLastLogin(owner.lastLogin)}</TableCell>
                  <TableCell>
                    <Badge variant="secondary">
                      {owner.vehicleDetails.length} vehicles
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center space-x-2">
                      <Switch
                        checked={owner.isActive}
                        onCheckedChange={(checked) =>
                          handleToggleStatus(owner.id, checked)
                        }
                        className="data-[state=checked]:bg-green-500"
                      />
                      <span className="text-sm">
                        {owner.isActive ? "Active" : "Inactive"}
                      </span>
                    </div>
                  </TableCell>
                  <TableCell className="text-right">
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-8 w-8 p-0"
                        >
                          <span className="sr-only">Open menu</span>
                          <MoreHorizontal className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem onClick={() => openEditDialog(owner)}>
                          <Edit className="mr-2 h-4 w-4" /> Edit
                        </DropdownMenuItem>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem
                          onClick={() => openDeleteDialog(owner)}
                          className="text-destructive focus:text-destructive"
                        >
                          <Trash2 className="mr-2 h-4 w-4" /> Delete
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      <Dialog
        open={actionDialog.isOpen}
        onOpenChange={(isOpen) => !isOpen && closeDialog()}
      >
        <DialogContent className="sm:max-w-2xl max-h-[90vh] overflow-y-auto">
          {actionDialog.action === "create" && (
            <>
              <DialogHeader>
                <DialogTitle>Add New Owner</DialogTitle>
                <DialogDescription>
                  Create a new electric vehicle owner account
                </DialogDescription>
              </DialogHeader>
              <OwnerForm
                owner={null}
                onSubmit={handleCreateOwner}
                onCancel={closeDialog}
              />
            </>
          )}

          {actionDialog.action === "edit" && actionDialog.owner && (
            <>
              <DialogHeader>
                <DialogTitle>Edit Owner</DialogTitle>
                <DialogDescription>Update owner information</DialogDescription>
              </DialogHeader>
              <OwnerForm
                owner={actionDialog.owner}
                onSubmit={handleUpdateOwner}
                onCancel={closeDialog}
              />
            </>
          )}

          {actionDialog.action === "delete" && actionDialog.owner && (
            <>
              <DialogHeader>
                <DialogTitle>Delete Owner</DialogTitle>
                <DialogDescription>
                  Are you sure you want to delete {actionDialog.owner.firstName}{" "}
                  {actionDialog.owner.lastName}? This action cannot be undone.
                </DialogDescription>
              </DialogHeader>
              <div className="flex justify-end space-x-2">
                <Button variant="outline" onClick={closeDialog}>
                  Cancel
                </Button>
                <Button variant="destructive" onClick={handleDeleteOwner}>
                  Delete
                </Button>
              </div>
            </>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
