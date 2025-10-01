# Mobile Application – Requirements & Screens

The mobile app is intended for EV Owners and Station Operators, with functionalities grouped accordingly.

## 1. User Management
### Requirements

- EV Owners can create, update, and deactivate their accounts (NIC as PK).

- Deactivated accounts can only be reactivated by Backoffice users.

- App must maintain a local SQLite database for user management.

### Screens

- Registration Screen – Create account (NIC, name, email, password, etc.).

- Login Screen – Login with credentials.

- Profile Screen – Update or deactivate account.

- Reactivation Info Screen – Show message when account is deactivated ("Contact Backoffice to reactivate").

## 2. Reservation Management
### Requirements

EV Owners can:

- Create new reservations (within 7 days).

- Modify reservations (at least 12 hours before).

- Cancel reservations (at least 12 hours before).

- View booking summary before confirmation.

- Generate a QR code after booking approval.

### Screens

- Reservation Form Screen – Select date, time, and station slot.

- Reservation Summary Screen – Preview details before confirming.

- Modify Reservation Screen – Edit booking (if within allowed time).

- Cancel Reservation Confirmation – Prompt before cancellation.

- QR Code Screen – Display QR code after confirmation.

## 3. View Bookings
### Requirements

EV Owners can view:

- Upcoming reservations.

- Past charging history.

### Screens

- Upcoming Bookings Screen – List of future reservations.

- Past History Screen – Completed bookings with date, time, station, cost, etc.

## 4. Dashboard (Home Screen)
### Requirements

- Show pending reservation count.

- Show approved future reservations count.

- Display nearby charging stations on Google Maps API.

### Screens

- Dashboard Screen – Main home view with:

- Pending reservations count.

- Approved reservations count.

- Map with nearby charging stations (Google Maps integration).

## 5. Station Operator Functions
### Requirements

Station Operators can log in with their credentials.

They can:

- Scan QR codes of bookings.

- Verify booking details retrieved from the server.

- Finalize charging session once EV operation is completed.

### Screens

- Operator Login Screen – Login with operator credentials.

- QR Scanner Screen – Scan customer’s QR code.

- Booking Verification Screen – Show booking details after scan.

- Finalize Session Screen – Confirm charging completion.

## Summary of Mobile Screens

- Registration Screen

- Login Screen

- Profile Screen

- Reactivation Info Screen

- Reservation Form Screen

- Reservation Summary Screen

- Modify Reservation Screen

- Cancel Reservation Confirmation

- QR Code Screen

- Upcoming Bookings Screen

- Past History Screen

- Dashboard Screen (with Map)

- Operator Login Screen

- QR Scanner Screen

- Booking Verification Screen

- Finalize Session Screen