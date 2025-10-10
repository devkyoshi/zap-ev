import { Button } from "@/components/ui/button";
import { AlertTriangle } from "lucide-react";

interface UserDeleteConfirmationProps {
  userName: string;
  onConfirm: () => void;
  onCancel: () => void;
  loading?: boolean; // Add the loading prop
}

export function UserDeleteConfirmation({
  userName,
  onConfirm,
  onCancel,
}: UserDeleteConfirmationProps) {
  return (
    <div className="space-y-4">
      <div className="flex items-center gap-3">
        <div className="rounded-full bg-destructive/10 p-2">
          <AlertTriangle className="h-5 w-5 text-destructive" />
        </div>
        <p>
          Are you sure you want to delete the user <strong>{userName}</strong>?
        </p>
      </div>

      <div className="flex justify-end space-x-2 pt-4">
        <Button type="button" variant="outline" onClick={onCancel}>
          Cancel
        </Button>
        <Button variant="destructive" onClick={onConfirm}>
          Delete User
        </Button>
      </div>
    </div>
  );
}
