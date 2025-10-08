import { 
  DashboardLayout 
} from "@/components/dashboard/dashboard-components"
import { 
  LayoutDashboard, 
  Users, 
  Car, 
  Zap, 
  Calendar, 
  Settings 
} from "lucide-react"
import { Outlet } from "react-router-dom"

const adminNavItems = [
  {
    label: "Dashboard",
    href: "/admin/dashboard",
    icon: LayoutDashboard,
  },
  {
    label: "User Management",
    href: "/admin/users",
    icon: Users,
  },
  {
    label: "EV Owners",
    href: "/admin/owners",
    icon: Car,
  },
  {
    label: "Stations",
    href: "/admin/stations",
    icon: Zap,
  },
  {
    label: "Bookings",
    href: "/admin/bookings",
    icon: Calendar,
  },
  {
    label: "Settings",
    href: "/admin/settings",
    icon: Settings,
  },
]

// Mock data - would come from authentication context in real app
const adminUser = {
  name: "Admin User",
  email: "admin@evspark.com",
  role: "System Administrator",
}

const AdminLayout = () => {
  return (
    <DashboardLayout
      navItems={adminNavItems}
      title="Admin Dashboard"
      userName={adminUser.name}
      userEmail={adminUser.email}
      userRole={adminUser.role}
    >
      <Outlet />
    </DashboardLayout>
  )
}

export default AdminLayout
