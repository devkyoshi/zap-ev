import { Outlet } from "react-router-dom"

const AuthLayout = () => {
  return (
    <div className="flex min-h-screen items-center justify-center bg-background px-4 py-12 sm:px-6 lg:px-8">
      <div className="w-full max-w-md space-y-8">
        <div className="text-center">
          <h2 className="mt-6 text-3xl font-bold tracking-tight">
            EV Spark Charging
          </h2>
          <p className="mt-2 text-sm text-muted-foreground">
            Electric Vehicle Charging Station Booking System
          </p>
        </div>
        <Outlet />
      </div>
    </div>
  )
}

export default AuthLayout
