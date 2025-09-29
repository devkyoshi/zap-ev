📘 SETUP.md
⚡ EVChargingStationAPI – Setup Guide

This guide explains how to set up and run the EVChargingStationAPI project.

🛠️ Prerequisites

Visual Studio 2022

MongoDB
 (local installation) or MongoDB Atlas

Git (for version control)

Postman / Swagger (for testing API endpoints)

📦 Install Dependencies

Open Package Manager Console in Visual Studio and run:

Install-Package MongoDB.Driver
Install-Package BCrypt.Net-Next
Install-Package System.IdentityModel.Tokens.Jwt
Install-Package Microsoft.AspNetCore.Authentication.JwtBearer
Install-Package Swashbuckle.AspNetCore
Install-Package QRCoder
Install-Package DotNetEnv

## Environment Variables Setup

Create a `.env` file in the project root with the following keys:

```dotenv
ConnectionStrings__MongoDB=your-mongo-connection-string
JWT__SecretKey=your-secret-key
JWT__Issuer=your-issuer
JWT__Audience=your-audience
JWT__ExpirationMinutes=15
JWT__ExpirationDays=7
ASPNETCORE_URLS=http://localhost:5000;https://localhost:5001
ASPNETCORE_ENVIRONMENT=Development
Frontend__Origin=https://your-frontend-domain.com
```

# Rate limiting
RateLimiting__WindowMinutes=1
RateLimiting__PermitLimit=100

▶️ Running the Application

Open the project in Visual Studio 2022.

Build the solution:

Ctrl + Shift + B


Run the project (F5 or ▶ Run button).

The API will start at:

https://localhost:5001
 (HTTPS)

http://localhost:5000
 (HTTP)

📖 API Documentation (Swagger)

Swagger UI will be available at:

https://localhost:5001/swagger

🌐 RESTful API Endpoints
🔑 Authentication

POST /api/auth/login

POST /api/auth/login/evowner

POST /api/Auth/refresh

POST /api/Auth/logout

👥 Users

POST /api/users/register

GET/PUT/DELETE /api/users

PATCH /api/users/{id}/status

🚗 EV Owners

POST /api/evowners/register

GET/PUT/DELETE /api/evowners/{id}

GET /api/evowners/deactivated

PATCH /api/evowners/{id}/reactivate

GET /api/evowners/dashboard/{nic}

⚡ Charging Stations

GET/POST/PUT/DELETE /api/chargingstations

POST /api/chargingstations/nearby

PATCH /api/chargingstations/{id}/slots

PATCH /api/chargingstations/{id}/status

📅 Bookings

GET/POST/PUT/DELETE /api/bookings

GET /api/bookings/evowner/{nic}

PATCH /api/bookings/{id}/approve

PATCH /api/bookings/{id}/start

PATCH /api/bookings/{id}/complete

POST /api/bookings/verify-qr

GET /api/bookings/evowner/{nic}/upcoming

GET /api/bookings/evowner/{nic}/history