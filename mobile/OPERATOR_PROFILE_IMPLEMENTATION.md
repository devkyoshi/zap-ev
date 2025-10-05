# Operator Profile Implementation

## Overview
The ProfileFragment has been enhanced to support both EV Owner and Station Operator user types with appropriate UI adaptations and backend integration.

## Key Components

### 1. User Type Detection
- `determineUserType()` - Detects if current user is a Station Operator or EV Owner
- Uses JWT token role information from AuthService
- Fallback to username pattern check if role not available

### 2. UI Configuration
- `configureUIForUserType()` - Adapts UI based on user type
- **For Operators**: Hides vehicle management section, shows simplified profile form
- **For EV Owners**: Shows full profile form with vehicle management

### 3. Profile Loading
- **EV Owners**: Uses existing `ProfileService` for complete profile data
- **Operators**: Uses new `OperatorProfileService` for User model data
- Separate loading methods: `loadEVOwnerProfile()` and `loadOperatorProfile()`

### 4. Form Population
- `populateEVOwnerFields()` - Fills form with ProfileResponse data
- `populateOperatorFields()` - Fills form with User model data (username → first name field, email)

### 5. Profile Saving
- **EV Owners**: Uses `ProfileService.updateUserProfile()` with ProfileUpdateRequest
- **Operators**: Uses `OperatorProfileService.updateOperatorProfile()` with User model
- Separate save methods: `saveEVOwnerProfile()` and `saveOperatorProfile()`

## Backend Integration

### OperatorProfileService
- **Purpose**: Handle Station Operator profile operations via Users API
- **Key Methods**:
  - `getOperatorProfile()` - Load operator profile data
  - `updateOperatorProfile()` - Save operator profile changes
  - `createUpdateRequest()` - Utility for creating update requests
- **Caching**: Implements profile caching with 5-minute expiration
- **Error Handling**: Comprehensive error handling with logging

### UserApiService
- **Purpose**: Retrofit interface for Users API endpoints
- **Endpoints**:
  - `getUserById()` - GET /api/users/{id}
  - `updateUser()` - PUT /api/users/{id}
- **Authentication**: Uses Bearer token authentication
- **Response Format**: Uses ApiResponse&lt;User&gt; wrapper

## UI Behavior

### For Station Operators:
- **Visible Fields**: Username (mapped to first name field), Email
- **Hidden Elements**: Vehicle management section, NIC field, phone field, last name field
- **Save Operation**: Updates User model via Users API

### For EV Owners:
- **Visible Fields**: All profile fields (first name, last name, email, phone, NIC)
- **Vehicle Management**: Full CRUD operations for vehicles
- **Save Operation**: Updates EVOwner model via EVOwners API

## Error Handling
- Authentication validation before API calls
- Network error handling with user-friendly messages
- Form validation adapted for each user type
- Loading state management during operations
- Graceful fallbacks for missing data

## Security Considerations
- JWT token validation for API access
- User ID verification before profile operations
- Role-based access control (operators cannot access EV Owner data)
- Secure password handling (not included in profile updates)

## Testing Scenarios
1. **Operator Login & Profile View**: Login as operator, navigate to profile, verify simplified UI
2. **Operator Profile Edit**: Update username/email, save, verify changes persist
3. **EV Owner Profile**: Login as EV Owner, verify full profile functionality remains intact
4. **Role Switching**: Test switching between user types maintains proper UI state
5. **Network Errors**: Test profile loading/saving with network issues

## Files Modified/Created
- **ProfileFragment.java**: Major refactoring for dual user type support
- **OperatorProfileService.java**: New service for operator profile management
- **UserApiService.java**: New API interface for Users endpoints
- **User.java**: Enhanced with helper methods for role checking

## Future Enhancements
- Password change functionality for operators
- Profile image upload support
- Audit trail for profile changes
- Bulk operator management features