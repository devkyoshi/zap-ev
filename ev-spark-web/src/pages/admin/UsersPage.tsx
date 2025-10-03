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

import { UserForm } from "./UserForm";
import { UserDeleteConfirmation } from "./UserDeleteConfirmation";
import axiosInstance from "@/utils/axiosInstance";
import axios from "axios";

// Update the User type to match API response
export interface User {
  id: string;
  username: string;
  email: string;
  role: number; // 0 = user, 1 = admin, 2 = operator
  isActive: boolean;
  lastLogin: string | null;
  createdAt: string;
  updatedAt: string;
  passwordHash?: string;
  name: string;
  active: boolean;
  password?: string;
}

// API response type
interface ApiResponse {
  success: boolean;
  message: string;
  data: User[];
  errors: string[];
}

type ActionDialogState = {
  isOpen: boolean;
  action: "create" | "edit" | "delete" | null;
  user: User | null;
};

export default function UsersPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [actionDialog, setActionDialog] = useState<ActionDialogState>({
    isOpen: false,
    action: null,
    user: null,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await axiosInstance.get<ApiResponse>("/Users");

      if (response.data.success) {
        setUsers(response.data.data);
      } else {
        throw new Error(response.data.message || "Failed to fetch users");
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "An error occurred");
      console.error("Error fetching users:", err);
    } finally {
      setLoading(false);
    }
  };

  // Filter users by search query
  const filteredUsers = users.filter((user) => {
    const query = searchQuery.toLowerCase();
    return (
      user.username.toLowerCase().includes(query) ||
      user.email.toLowerCase().includes(query)
    );
  });

  const handleCreateUser = async (data: Partial<User>) => {
    try {
      setLoading(true);
      setError(null);

      const payload = {
        username: data.username,
        email: data.email,
        password: data.password,
        role: data.role ?? 2,
      };

      const response = await axios.post("/api/Users/register", payload);

      if (response.data.success) {
        const newUser = response.data.data as unknown as User;
        setUsers((prev) => [...prev, newUser]);
        closeDialog();
      } else {
        throw new Error(response.data.message || "User creation failed");
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "An error occurred");
      console.error("Error creating user:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateUser = async (data: Partial<User>) => {
    if (!actionDialog.user) return;

    try {
      setLoading(true);
      setError(null);

      const payload: any = {
        username: data.username,
        email: data.email,
        role: data.role,
      };

      // Only include password if it's provided (for edit)
      if (data.password && data.password.trim() !== "") {
        payload.password = data.password;
      }

      const response = await axiosInstance.put(
        `/Users/${actionDialog.user.id}`,
        payload
      );

      if (response.data.success) {
        const updatedUser = response.data.data as User;
        setUsers((prev) =>
          prev.map((user) =>
            user.id === actionDialog.user?.id
              ? { ...user, ...updatedUser }
              : user
          )
        );
        closeDialog();
      } else {
        throw new Error(response.data.message || "User update failed");
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "An error occurred");
      console.error("Error updating user:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteUser = async () => {
    if (!actionDialog.user) return;

    try {
      setLoading(true);
      setError(null);

      const response = await axiosInstance.delete(
        `/Users/${actionDialog.user.id}`
      );

      if (response.data.success) {
        setUsers((prev) =>
          prev.filter((user) => user.id !== actionDialog.user?.id)
        );
        closeDialog();
      } else {
        throw new Error(response.data.message || "User deletion failed");
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "An error occurred");
      console.error("Error deleting user:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleToggleStatus = async (userId: string, isActive: boolean) => {
    try {
      const user = users.find((u) => u.id === userId);
      if (!user) return;

      const response = await axiosInstance.put(`/Users/${userId}`, {
        username: user.username,
        email: user.email,
        role: user.role,
        isActive: isActive,
      });

      if (response.data.success) {
        setUsers((prev) =>
          prev.map((user) =>
            user.id === userId ? { ...user, isActive } : user
          )
        );
      } else {
        throw new Error(response.data.message || "Status update failed");
        // Revert the UI change if the API call fails
        setUsers((prev) =>
          prev.map((user) =>
            user.id === userId ? { ...user, isActive: !isActive } : user
          )
        );
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "An error occurred");
      console.error("Error updating user status:", err);
      // Revert the UI change if the API call fails
      setUsers((prev) =>
        prev.map((user) =>
          user.id === userId ? { ...user, isActive: !isActive } : user
        )
      );
    }
  };

  const openCreateDialog = () => {
    setActionDialog({
      isOpen: true,
      action: "create",
      user: null,
    });
  };

  const openEditDialog = (user: User) => {
    setActionDialog({
      isOpen: true,
      action: "edit",
      user,
    });
  };

  const openDeleteDialog = (user: User) => {
    setActionDialog({
      isOpen: true,
      action: "delete",
      user,
    });
  };

  const closeDialog = () => {
    setActionDialog({
      isOpen: false,
      action: null,
      user: null,
    });
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  };

  // Convert role number to display name
  const getRoleDisplayName = (role: number): string => {
    switch (role) {
      case 0:
        return "User";
      case 1:
        return "Administrator";
      case 2:
        return "Station Operator";
      default:
        return "Unknown";
    }
  };

  const getRoleBadgeClasses = (role: number) => {
    switch (role) {
      case 1: // admin
        return "bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-400";
      case 2: // operator
        return "bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400";
      case 0: // user
      default:
        return "bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300";
    }
  };

  if (loading && users.length === 0) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-lg">Loading users...</div>
      </div>
    );
  }

  if (error && users.length === 0) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-lg text-destructive">Error: {error}</div>
        <Button onClick={fetchUsers} className="ml-4">
          Retry
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">User Management</h2>
        <p className="text-muted-foreground">
          Manage system administrators and station operators
        </p>
      </div>

      {error && (
        <div className="bg-destructive/15 text-destructive px-4 py-3 rounded-md flex justify-between items-center">
          <span>{error}</span>
          <Button variant="outline" size="sm" onClick={() => setError(null)}>
            Dismiss
          </Button>
        </div>
      )}

      <div className="flex justify-between items-center">
        <div className="relative w-72">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Search users..."
            className="pl-8"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>
        <Button onClick={openCreateDialog} disabled={loading}>
          <Plus className="mr-2 h-4 w-4" /> Add User
        </Button>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Username</TableHead>
              <TableHead>Email</TableHead>
              <TableHead>Role</TableHead>
              <TableHead>Created</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="w-[100px]">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredUsers.length > 0 ? (
              filteredUsers.map((user) => (
                <TableRow key={user.id}>
                  <TableCell className="font-medium">{user.username}</TableCell>
                  <TableCell>{user.email}</TableCell>
                  <TableCell>
                    <span
                      className={`inline-flex items-center rounded-md px-2 py-1 text-xs font-medium ring-1 ring-inset ${getRoleBadgeClasses(
                        user.role
                      )}`}
                    >
                      {getRoleDisplayName(user.role)}
                    </span>
                  </TableCell>
                  <TableCell>{formatDate(user.createdAt)}</TableCell>
                  <TableCell>
                    <Switch
                      checked={user.isActive}
                      onCheckedChange={(checked) =>
                        handleToggleStatus(user.id, checked)
                      }
                      aria-label="Toggle user status"
                      disabled={loading}
                    />
                  </TableCell>
                  <TableCell>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button
                          variant="ghost"
                          className="h-8 w-8 p-0"
                          disabled={loading}
                        >
                          <span className="sr-only">Open menu</span>
                          <MoreHorizontal className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem
                          onClick={() => openEditDialog(user)}
                          disabled={loading}
                        >
                          <Edit className="mr-2 h-4 w-4" />
                          Edit
                        </DropdownMenuItem>
                        {user.role !== 1 && ( // Don't show delete for admins
                          <>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem
                              onClick={() => openDeleteDialog(user)}
                              className="text-destructive focus:text-destructive"
                              disabled={loading}
                            >
                              <Trash2 className="mr-2 h-4 w-4" />
                              Delete
                            </DropdownMenuItem>
                          </>
                        )}
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={6} className="h-24 text-center">
                  {searchQuery
                    ? "No users found matching your search."
                    : "No users found."}
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      {/* Create/Edit User Dialog */}
      <Dialog
        open={
          actionDialog.isOpen &&
          ["create", "edit"].includes(actionDialog.action || "")
        }
        onOpenChange={(open) => !open && closeDialog()}
      >
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>
              {actionDialog.action === "create" ? "Add New User" : "Edit User"}
            </DialogTitle>
            <DialogDescription>
              {actionDialog.action === "create"
                ? "Create a new system user with the appropriate role."
                : "Update user details."}
            </DialogDescription>
          </DialogHeader>

          <UserForm
            user={actionDialog.user}
            onSubmit={(data) => {
              const userData: Partial<User> = {
                username: data.username,
                email: data.email,
                password: data.password || undefined, // Only include if provided
                role: Number(data.role),
              };
              if (actionDialog.action === "create") {
                handleCreateUser(userData);
              } else {
                handleUpdateUser(userData);
              }
            }}
            onCancel={closeDialog}
            loading={loading}
          />
        </DialogContent>
      </Dialog>

      {/* Delete User Confirmation Dialog */}
      <Dialog
        open={actionDialog.isOpen && actionDialog.action === "delete"}
        onOpenChange={(open) => !open && closeDialog()}
      >
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Delete User</DialogTitle>
            <DialogDescription>
              This action cannot be undone. This will permanently delete the
              user account.
            </DialogDescription>
          </DialogHeader>

          <UserDeleteConfirmation
            userName={actionDialog.user?.username || ""}
            onConfirm={handleDeleteUser}
            onCancel={closeDialog}
            loading={loading}
          />
        </DialogContent>
      </Dialog>
    </div>
  );
}
