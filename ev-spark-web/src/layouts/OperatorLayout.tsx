import { 
  DashboardLayout 
} from "@/components/dashboard/dashboard-components"
import { 
  Zap, 
  Calendar, 
  Settings,
  Clock
} from "lucide-react"
import { Outlet } from "react-router-dom"

const operatorNavItems = [
  {
    label: "My Stations",
    href: "/operator/stations",
    icon: Zap,
  },
  {
    label: "Active Bookings",
    href: "/operator/bookings/active",
    icon: Clock,
  },
  {
    label: "All Bookings",
    href: "/operator/bookings",
    icon: Calendar,
  },
  {
    label: "Settings",
    href: "/operator/settings",
    icon: Settings,
  },
]

// Mock data - would come from authentication context in real app
const operatorUser = {
  name: "Station Operator",
  email: "operator@evspark.com",
  role: "Station Operator",
}

const OperatorLayout = () => {
  return (
    <DashboardLayout
      navItems={operatorNavItems}
      title="Station Management"
      userName={operatorUser.name}
      userEmail={operatorUser.email}
      userRole={operatorUser.role}
    >
      <Outlet />
    </DashboardLayout>
  )
}

export default OperatorLayout
