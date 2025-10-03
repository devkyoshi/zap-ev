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

import { OwnerForm } from "./OwnerForm";
import { OwnerDeleteConfirmation } from "./OwnerDeleteConfirmation";
import axiosInstance from "@/utils/axiosInstance";

// API Response Types
interface VehicleDetail {
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
  isActive: boolean;
  vehicleDetails: VehicleDetail[];
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

// Local Owner type for the UI
interface Owner {
  id: string;
  name: string;
  email: string;
  phone: string;
  registeredDate: string;
  vehicles: number;
  active: boolean;
  nic: string;
  firstName: string;
  lastName: string;
}

type ActionDialogState = {
  isOpen: boolean;
  action: "create" | "edit" | "delete" | null;
  owner: Owner | null;
};

export default function OwnersPage() {
  const [owners, setOwners] = useState<Owner[]>([]);
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
      const response = await axiosInstance.get<ApiResponse<EVOwner>>(
        "/EVOwners"
      );

      if (response.data.success) {
        // Transform API data to local Owner format
        const transformedOwners: Owner[] = response.data.data.map((owner) => ({
          id: owner.id,
          name: `${owner.firstName} ${owner.lastName}`,
          firstName: owner.firstName,
          lastName: owner.lastName,
          email: owner.email,
          phone: owner.phoneNumber,
          nic: owner.nic,
          registeredDate: owner.createdAt,
          vehicles: owner.vehicleDetails.length,
          active: owner.isActive,
        }));

        setOwners(transformedOwners);
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
    return (
      owner.name.toLowerCase().includes(query) ||
      owner.email.toLowerCase().includes(query) ||
      owner.phone.toLowerCase().includes(query) ||
      owner.nic.toLowerCase().includes(query)
    );
  });

  const handleCreateOwner = async (data: Partial<Owner>) => {
    try {
      // Prepare data for API using the register endpoint structure
      const newOwnerData = {
        nic: data.nic || "",
        firstName: data.firstName || "",
        lastName: data.lastName || "",
        email: data.email || "",
        phoneNumber: data.phone || "",
        password: "defaultPassword123", // You might want to make this configurable in the form
        vehicleDetails: [],
      };

      // Validate required fields
      if (
        !newOwnerData.nic ||
        !newOwnerData.firstName ||
        !newOwnerData.lastName
      ) {
        throw new Error("NIC, First Name, and Last Name are required fields");
      }

      const response = await axiosInstance.post<ApiResponse<EVOwner>>(
        "/EVOwners/register",
        newOwnerData
      );

      if (response.data.success) {
        // Refresh the owners list
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
  const handleUpdateOwner = async (data: Partial<Owner>) => {
    if (!actionDialog.owner) return;

    try {
      const updatedOwnerData = {
        nic: data.nic || actionDialog.owner.nic,
        firstName: data.firstName || actionDialog.owner.firstName,
        lastName: data.lastName || actionDialog.owner.lastName,
        email: data.email || actionDialog.owner.email,
        phoneNumber: data.phone || actionDialog.owner.phone,
        isActive: actionDialog.owner.active,
      };

      // Validate required fields
      if (
        !updatedOwnerData.nic ||
        !updatedOwnerData.firstName ||
        !updatedOwnerData.lastName
      ) {
        throw new Error("NIC, First Name, and Last Name are required fields");
      }

      const response = await axiosInstance.put<ApiResponse<EVOwner>>(
        `/EVOwners/${actionDialog.owner.id}`,
        updatedOwnerData
      );

      if (response.data.success) {
        // Update local state
        setOwners(
          owners.map((owner) =>
            owner.id === actionDialog.owner?.id
              ? {
                  ...owner,
                  ...data,
                  name: `${data.firstName || owner.firstName} ${
                    data.lastName || owner.lastName
                  }`,
                }
              : owner
          )
        );
        closeDialog();
      } else {
        throw new Error(response.data.message || "Failed to update owner");
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update owner");
      console.error("Error updating owner:", err);
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
  const handleToggleStatus = async (ownerId: string, active: boolean) => {
    try {
      const owner = owners.find((o) => o.id === ownerId);
      if (!owner) return;

      const updatedOwnerData = {
        nic: owner.nic,
        firstName: owner.firstName,
        lastName: owner.lastName,
        email: owner.email,
        phoneNumber: owner.phone,
        isActive: active,
      };

      const response = await axiosInstance.put<ApiResponse<EVOwner>>(
        `/EVOwners/${ownerId}`,
        updatedOwnerData
      );

      if (response.data.success) {
        setOwners(
          owners.map((owner) =>
            owner.id === ownerId ? { ...owner, active } : owner
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

  const openCreateDialog = () => {
    setActionDialog({
      isOpen: true,
      action: "create",
      owner: null,
    });
  };

  const openEditDialog = (owner: Owner) => {
    setActionDialog({
      isOpen: true,
      action: "edit",
      owner,
    });
  };

  const openDeleteDialog = (owner: Owner) => {
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
              <TableHead>Vehicles</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredOwners.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} className="h-24 text-center">
                  {owners.length === 0
                    ? "No owners found."
                    : "No matching owners found."}
                </TableCell>
              </TableRow>
            ) : (
              filteredOwners.map((owner) => (
                <TableRow key={owner.id}>
                  <TableCell className="font-medium">{owner.name}</TableCell>
                  <TableCell>{owner.email}</TableCell>
                  <TableCell>{owner.phone}</TableCell>
                  <TableCell>{owner.nic}</TableCell>
                  <TableCell>{formatDate(owner.registeredDate)}</TableCell>
                  <TableCell>
                    <span className="inline-flex items-center rounded-full bg-blue-100 px-2.5 py-0.5 text-xs font-medium text-blue-800 dark:bg-blue-900/30 dark:text-blue-400">
                      {owner.vehicles}
                    </span>
                  </TableCell>
                  <TableCell>
                    <Switch
                      checked={owner.active}
                      onCheckedChange={(checked) =>
                        handleToggleStatus(owner.id, checked)
                      }
                      className="data-[state=checked]:bg-green-500"
                    />
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

      {/* Dialog for create/edit/delete actions */}
      <Dialog
        open={actionDialog.isOpen}
        onOpenChange={(isOpen) => !isOpen && closeDialog()}
      >
        <DialogContent className="sm:max-w-md">
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
                  This action cannot be undone.
                </DialogDescription>
              </DialogHeader>
              <OwnerDeleteConfirmation
                ownerName={actionDialog.owner.name}
                onConfirm={handleDeleteOwner}
                onCancel={closeDialog}
              />
            </>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
