# Multi-stage Dockerfile for building and running the EVChargingStationAPI with .NET 8
# Build stage
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src

# Copy csproj and restore as distinct layers for caching
COPY ["EVChargingStationAPI.csproj", "./"]
RUN dotnet restore "EVChargingStationAPI.csproj"

# Copy everything else and publish
COPY . .


# Create a dev HTTPS certificate and export to PFX for use at runtime
RUN mkdir -p /https \
    && dotnet dev-certs https -ep /https/aspnetapp.pfx -p "devpassword" \
    && dotnet dev-certs https --trust || true

RUN dotnet publish "EVChargingStationAPI.csproj" -c Release -o /app/publish /p:UseAppHost=false


# Runtime stage

FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS runtime

WORKDIR /app
ENV ASPNETCORE_URLS="http://+:80;https://+:443"
ENV ASPNETCORE_Kestrel__Certificates__Default__Path=/https/aspnetapp.pfx
ENV ASPNETCORE_Kestrel__Certificates__Default__Password="devpassword"

EXPOSE 80 443

# Copy published app from build stage
COPY --from=build /app/publish .
COPY --from=build /https/aspnetapp.pfx /https/aspnetapp.pfx
COPY .env.production ./.env

ENTRYPOINT ["dotnet", "EVChargingStationAPI.dll"]

