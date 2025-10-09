export interface CreateBookingFormProps {
  onSubmit: (data: CreateBookingData) => void;
  onCancel: () => void;
}

export interface CreateBookingData {
  chargingStationId: string;
  reservationDateTime: string;
  durationMinutes: number;
  notes: string;
}