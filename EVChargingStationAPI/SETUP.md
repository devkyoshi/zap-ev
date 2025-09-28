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

Create a .env file (example)

Create a file named .env in the project root (same folder as the .csproj and Program.cs).

Use the double-underscore (__) convention to map hierarchical configuration keys to 
ASP.NET Core configuration (this maps to Configuration.GetSection(...)). Example .env:
# .env (DO NOT COMMIT)
ConnectionStrings__MongoDB=MongodbConnectionStringHere
JWT__SecretKey=(put a long random string / base64 here)
JWT__Issuer=Issuer
JWT__Audience=Audience
JWT__ExpirationMinutes=TimeInMinutes
# optional: override Kestrel urls for local testing
ASPNETCORE_URLS=https://localhost:5001;http://localhost:5000
ASPNETCORE_ENVIRONMENT=Development

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

👥 Users

GET/POST/PUT/DELETE /api/users

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