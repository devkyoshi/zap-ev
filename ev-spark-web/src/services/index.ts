export * from './auth-service'
export * from './user-service'
export * from './owner-service'
export * from './station-service'
export * from './booking-service'

// Re-export the API client utilities
export { apiRequest } from './api-client'
export type { ApiResponse } from './api-client'

// Main API services export object
import { AuthService } from './auth-service'
import { UserService } from './user-service'
import { OwnerService } from './owner-service'
import { StationService } from './station-service'
import { BookingService } from './booking-service'

const API = {
  auth: AuthService,
  users: UserService,
  owners: OwnerService,
  stations: StationService,
  bookings: BookingService,
}

export default API
