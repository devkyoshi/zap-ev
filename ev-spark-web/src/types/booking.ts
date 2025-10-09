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


export interface Booking {
  id: string;
  evOwnerNIC: string;
  chargingStationName: string;
  reservationDateTime: string;
  durationMinutes: number;
  status: number;
  totalAmount: number;
  qrCode: string;
  createdAt: string;
}