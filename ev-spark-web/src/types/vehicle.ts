export interface Vehicle {
  make: string;
  model: string;
  licensePlate: string;
  year: number;
}

export interface EVOwner {
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
