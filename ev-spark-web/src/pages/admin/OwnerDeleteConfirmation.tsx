import { Button } from "@/components/ui/button"
import { AlertTriangle } from "lucide-react"

interface OwnerDeleteConfirmationProps {
  ownerName: string
  onConfirm: () => void
  onCancel: () => void
}

export function OwnerDeleteConfirmation({
  ownerName,
  onConfirm,
  onCancel,
}: OwnerDeleteConfirmationProps) {
  return (
    <div className="space-y-4">
      <div className="flex items-center gap-3">
        <div className="rounded-full bg-destructive/10 p-2">
          <AlertTriangle className="h-5 w-5 text-destructive" />
        </div>
        <p>
          Are you sure you want to delete the owner <strong>{ownerName}</strong>?
        </p>
      </div>
      
      <div className="flex justify-end space-x-2 pt-4">
        <Button type="button" variant="outline" onClick={onCancel}>
          Cancel
        </Button>
        <Button
          variant="destructive"
          onClick={onConfirm}
        >
          Delete Owner
        </Button>
      </div>
    </div>
  )
}
