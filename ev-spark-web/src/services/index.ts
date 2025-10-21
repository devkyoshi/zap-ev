export * from './auth-service'
export * from './user-service'
export * from './owner-service'


// Re-export the API client utilities
export { apiRequest } from './api-client'
export type { ApiResponse } from './api-client'

// Main API services export object
import { AuthService } from './auth-service'
import { UserService } from './user-service'
import { OwnerService } from './owner-service'



const API = {
  auth: AuthService,
  users: UserService,
  owners: OwnerService,
}

export default API
