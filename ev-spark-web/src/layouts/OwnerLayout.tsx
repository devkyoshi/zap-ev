import { 
  DashboardLayout 
} from "@/components/dashboard/dashboard-components"
import { 
  LayoutDashboard, 
  Zap, 
  Calendar, 
  History,
  Clock,
  UserCircle,
  ThumbsUp,
  BatteryCharging
} from "lucide-react"
import { Outlet } from "react-router-dom"

const ownerNavItems = [
  {
    label: "Dashboard",
    href: "/owner/dashboard",
    icon: LayoutDashboard,
  },
  {
    label: "Find Stations",
    href: "/owner/stations",
    icon: Zap,
  },
  {
    label: "Recommended Stations",
    href: "/owner/stations/recommended",
    icon: ThumbsUp,
  },
  {
    label: "Charging History",
    href: "/owner/charging/history",
    icon: BatteryCharging,
  },
  {
    label: "Upcoming Bookings",
    href: "/owner/bookings/upcoming",
    icon: Clock,
  },
  {
    label: "Booking History",
    href: "/owner/bookings/history",
    icon: History,
  },
  {
    label: "New Booking",
    href: "/owner/bookings/new",
    icon: Calendar,
  },
  {
    label: "Profile",
    href: "/owner/profile",
    icon: UserCircle,
  },
]

// Mock data - would come from authentication context in real app
const ownerUser = {
  name: "EV Owner",
  email: "owner@example.com",
  role: "EV Owner",
}

const OwnerLayout = () => {
  return (
    <DashboardLayout
      navItems={ownerNavItems}
      title="EV Owner Dashboard"
      userName={ownerUser.name}
      userEmail={ownerUser.email}
      userRole={ownerUser.role}
    >
      <Outlet />
    </DashboardLayout>
  )
}

export default OwnerLayout
