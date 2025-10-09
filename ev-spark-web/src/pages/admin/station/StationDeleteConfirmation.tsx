import { Button } from "@/components/ui/button";

interface StationDeleteConfirmationProps {
  stationName: string;
  onConfirm: () => void;
  onCancel: () => void;
}

export function StationDeleteConfirmation({
  stationName,
  onConfirm,
  onCancel,
}: StationDeleteConfirmationProps) {
  return (
    <div className="space-y-4">
      <p>
        Are you sure you want to delete <strong>{stationName}</strong>? This
        action cannot be undone.
      </p>
      <div className="flex justify-end gap-2">
        <Button type="button" variant="outline" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="button" variant="destructive" onClick={onConfirm}>
          Delete Station
        </Button>
      </div>
    </div>
  );
}
