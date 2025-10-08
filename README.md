# ⚡ ZapEV - Electric Vehicle Charging Station Management System

![ZapEV Banner](https://img.shields.io/badge/ZapEV-Electric%20Vehicle%20Charging-green?style=for-the-badge)
![.NET](https://img.shields.io/badge/.NET-9.0-512BD4?style=flat&logo=.net)
![React](https://img.shields.io/badge/React-19.1-61DAFB?style=flat&logo=react)
![Android](https://img.shields.io/badge/Android-24+-3DDC84?style=flat&logo=android)
![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=flat&logo=mongodb)

ZapEV is a comprehensive Electric Vehicle (EV) charging station management system that provides seamless booking, management, and operation of EV charging stations. The platform consists of three integrated applications: a web-based admin dashboard, a mobile app for EV owners and operators, and a robust REST API backend.

## 🌟 Features

### 🌐 Web Application (Admin Dashboard)
- **User Management**: Comprehensive user account management with role-based access control
- **EV Owner Management**: Registration, profile management, and account activation/deactivation
- **Charging Station Management**: Full CRUD operations for charging stations with slot management
- **Booking Management**: Real-time booking oversight, approval workflow, and status tracking
- **Dashboard Analytics**: Real-time statistics and system overview
- **Authentication**: Secure JWT-based authentication with role-based access

### 📱 Mobile Application
- **User Registration & Login**: Secure account creation and authentication for EV owners and operators
- **Reservation System**: Create, modify, and cancel charging reservations with 7-day advance booking
- **QR Code Generation**: Generate QR codes for approved bookings
- **Map Integration**: Interactive maps showing nearby charging stations using OpenStreetMap
- **Booking History**: View upcoming reservations and past charging history
- **Operator Functions**: QR code scanning, booking verification, and session finalization
- **Offline Capability**: Local SQLite database for user management

### 🔧 Backend API
- **RESTful API**: Comprehensive REST API with Swagger documentation
- **Authentication & Authorization**: JWT-based security with refresh token support
- **Real-time Updates**: Live booking status updates and notifications
- **QR Code Management**: Generate and verify QR codes for bookings
- **Database Management**: MongoDB integration with comprehensive data models
- **Rate Limiting**: Built-in API rate limiting for security and performance

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Web App       │    │   Mobile App    │    │   Backend API   │
│   (React)       │◄──►│   (Android)     │◄──►│   (.NET Core)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                        ┌─────────────────┐
                        │    MongoDB      │
                        │   Database      │
                        └─────────────────┘
```

## 🛠️ Tech Stack

### 🌐 Web Application (ev-spark-web)
| Technology | Version | Purpose |
|------------|---------|---------|
| **React** | 19.1.1 | Frontend framework |
| **TypeScript** | 5.8.3 | Type-safe JavaScript |
| **Vite** | 7.1.2 | Build tool and dev server |
| **Tailwind CSS** | 4.1.13 | Utility-first CSS framework |
| **Radix UI** | Various | Accessible UI components |
| **React Router** | 7.9.1 | Client-side routing |
| **React Hook Form** | 7.62.0 | Form handling and validation |
| **Zod** | 4.1.8 | Schema validation |
| **Axios** | 1.12.1 | HTTP client |
| **Lucide React** | 0.544.0 | Icon library |
| **JWT Decode** | 4.0.0 | JWT token handling |
| **Date-fns** | 4.1.0 | Date manipulation |

### 📱 Mobile Application (Android)
| Technology | Version | Purpose |
|------------|---------|---------|
| **Android SDK** | API 24+ (min) / 36 (target) | Mobile platform |
| **Java** | 11 | Programming language |
| **Material Design** | 1.13.0 | UI design system |
| **Retrofit** | 2.9.0 | HTTP client library |
| **Room Database** | 2.6.1 | Local SQLite database |
| **Gson** | 2.10.1 | JSON serialization |
| **ZXing** | 3.5.2 / 4.3.0 | QR code scanning |
| **OkHttp** | 4.12.0 | HTTP client and logging |
| **OSMDroid** | 6.1.17 | OpenStreetMap integration |
| **Data Binding** | - | Android data binding |

### 🔧 Backend API (.NET Core)
| Technology | Version | Purpose |
|------------|---------|---------|
| **.NET Core** | 9.0 | Backend framework |
| **C#** | Latest | Programming language |
| **MongoDB Driver** | 3.5.0 | Database connectivity |
| **JWT Bearer** | 9.0.9 | Authentication |
| **BCrypt.Net** | 4.0.3 | Password hashing |
| **Swagger/OpenAPI** | 9.0.4 | API documentation |
| **QRCoder** | 1.6.0 | QR code generation |
| **DotNetEnv** | 3.1.1 | Environment configuration |

### 🗄️ Database
- **MongoDB**: NoSQL database for flexible data storage and scalability

## 🚀 Getting Started

### Prerequisites
- **Development Environment**:
  - Visual Studio 2022 or VS Code
  - Android Studio (for mobile development)
  - Node.js 18+ and npm/yarn
  - .NET 9.0 SDK
  
- **Database**:
  - MongoDB (local installation or MongoDB Atlas)
  
- **Tools**:
  - Git
  - Postman (for API testing)

### 🔧 Backend API Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/devkyoshi/zap-ev.git
   cd zap-ev/EVChargingStationAPI
   ```

2. **Install dependencies**
   ```bash
   dotnet restore
   ```

3. **Create environment configuration**
   Create a `.env` file in the API root:
   ```env
   ConnectionStrings__MongoDB=mongodb://localhost:27017/zapev
   JWT__SecretKey=your-super-secret-jwt-key-here
   JWT__Issuer=ZapEV
   JWT__Audience=ZapEV-Users
   JWT__ExpirationMinutes=15
   JWT__ExpirationDays=7
   ASPNETCORE_URLS=http://localhost:5000;https://localhost:5001
   ASPNETCORE_ENVIRONMENT=Development
   Frontend__Origin=http://localhost:5173
   RateLimiting__WindowMinutes=1
   RateLimiting__PermitLimit=100
   ```

4. **Run the API**
   ```bash
   dotnet run
   ```

5. **Access Swagger Documentation**
   Navigate to: `https://localhost:5001/swagger`

### 🌐 Web Application Setup

1. **Navigate to web directory**
   ```bash
   cd ../ev-spark-web
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Start development server**
   ```bash
   npm run dev
   ```

4. **Access the application**
   Navigate to: `http://localhost:5173`

### 📱 Mobile Application Setup

1. **Open in Android Studio**
   ```bash
   cd ../mobile
   # Open the project in Android Studio
   ```

2. **Sync Gradle**
   - Let Android Studio sync the project dependencies

3. **Configure API Base URL**
   - Update the API base URL in the network configuration

4. **Build and Run**
   - Connect an Android device or start an emulator
   - Build and run the application

## 📱 Mobile App Features

### For EV Owners
- **Account Management**: Registration, login, profile updates, account deactivation
- **Reservation System**:
  - Book charging slots up to 7 days in advance
  - Modify bookings (minimum 12 hours before)
  - Cancel reservations (minimum 12 hours before)
  - View booking summary before confirmation
- **QR Code Access**: Generate QR codes for approved bookings
- **Booking Management**:
  - View upcoming reservations
  - Access charging history with costs and details
- **Dashboard**: 
  - Pending reservation count
  - Approved reservation count
  - Interactive map with nearby charging stations

### For Station Operators
- **Operator Authentication**: Secure login for station operators
- **QR Code Operations**:
  - Scan customer QR codes
  - Verify booking details from server
  - Finalize charging sessions after completion

## 🌐 Web Dashboard Features

### Admin Functions
- **User Management**: Create, update, delete users with role assignments
- **EV Owner Management**: Handle owner registrations and account status
- **Station Management**: Comprehensive charging station CRUD operations
- **Booking Oversight**: Monitor, approve, and manage all bookings
- **System Dashboard**: Real-time analytics and system status

### Authentication & Security
- **Role-Based Access**: Admin, Operator, and Owner roles
- **JWT Authentication**: Secure token-based authentication
- **Form Validation**: Comprehensive input validation using Zod schemas
- **Responsive Design**: Mobile-first responsive interface

## 🔌 API Endpoints

### Authentication
- `POST /api/auth/login` - User authentication
- `POST /api/auth/login/evowner` - EV owner authentication
- `POST /api/auth/refresh` - Refresh JWT tokens
- `POST /api/auth/logout` - User logout

### User Management
- `POST /api/users/register` - Register new users
- `GET /api/users` - Retrieve all users
- `PUT /api/users/{id}` - Update user details
- `DELETE /api/users/{id}` - Delete user account
- `PATCH /api/users/{id}/status` - Update user status

### EV Owners
- `POST /api/evowners/register` - EV owner registration
- `GET /api/evowners/{id}` - Get owner details
- `PUT /api/evowners/{id}` - Update owner profile
- `PATCH /api/evowners/{id}/reactivate` - Reactivate account
- `GET /api/evowners/dashboard/{nic}` - Owner dashboard data

### Charging Stations
- `GET /api/chargingstations` - List all stations
- `POST /api/chargingstations` - Create new station
- `PUT /api/chargingstations/{id}` - Update station
- `DELETE /api/chargingstations/{id}` - Remove station
- `POST /api/chargingstations/nearby` - Find nearby stations
- `PATCH /api/chargingstations/{id}/slots` - Update available slots

### Bookings
- `GET /api/bookings` - List all bookings
- `POST /api/bookings` - Create new booking
- `PUT /api/bookings/{id}` - Update booking
- `DELETE /api/bookings/{id}` - Cancel booking
- `PATCH /api/bookings/{id}/approve` - Approve booking
- `PATCH /api/bookings/{id}/start` - Start charging session
- `PATCH /api/bookings/{id}/complete` - Complete session
- `POST /api/bookings/verify-qr` - Verify QR code
- `GET /api/bookings/evowner/{nic}/upcoming` - Upcoming bookings
- `GET /api/bookings/evowner/{nic}/history` - Booking history

## 🏛️ System Architecture

### Data Models

#### User
- Basic user information with role-based access
- Supports Admin, Operator, and Owner roles
- JWT-based authentication with refresh tokens

#### EV Owner
- Extended user profile for electric vehicle owners
- NIC as primary key for unique identification
- Account activation/deactivation workflow

#### Charging Station
- Complete station information with location data
- Available slots and real-time status tracking
- Operator assignment capabilities

#### Booking
- Comprehensive reservation system
- Status workflow: Pending → Approved → In Progress → Completed
- QR code integration for seamless check-in

### Security Features
- **Password Hashing**: BCrypt for secure password storage
- **JWT Tokens**: Secure authentication with automatic refresh
- **Rate Limiting**: API protection against abuse
- **Input Validation**: Comprehensive validation on all endpoints
- **CORS Configuration**: Secure cross-origin resource sharing

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request



## 👥 Team

- SayunHetti (Sayun Hettiarachchi)
- yashodalasith (Yashodha Jayasinghe)
- Silverviles (Tharindu Siriwardhana)
- devKyoshi (Ashan Tharindu)

## 🆘 Support

For support and questions:
1. Check the [API Documentation](https://localhost:5001/swagger) when running the backend
2. Review the setup guides in each component directory
3. Open an issue in the GitHub repository

## 🔄 Version History

- **v1.0.0** - Initial release with core functionality
  - Web dashboard with admin features
  - Mobile app for EV owners and operators
  - Complete REST API with authentication
  - MongoDB integration
  - QR code system for bookings

---

⚡ **ZapEV** - Powering the future of electric vehicle charging! 🚗💚