import { Calendar, Clock, Zap } from "lucide-react"
import { StatsCard, StatsGrid } from "@/components/dashboard/stats-card"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"

const OwnerDashboard = () => {
  // Mock data - would come from API in a real app
  const stats = [
    {
      title: "Available Stations Nearby",
      value: "12",
      icon: Zap,
      description: "Within 10km radius",
    },
    {
      title: "Upcoming Bookings",
      value: "3",
      icon: Calendar,
      description: "Next 7 days",
    },
    {
      title: "Usage This Month",
      value: "8.5 hrs",
      icon: Clock,
      description: "Total charging time",
      trend: {
        value: 12,
        isPositive: true,
        label: "from last month",
      },
    },
  ]

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">My Dashboard</h2>
        <p className="text-muted-foreground">
          Welcome back to your EV charging dashboard
        </p>
      </div>
      
      <StatsGrid>
        {stats.map((stat, index) => (
          <StatsCard
            key={index}
            title={stat.title}
            value={stat.value}
            description={stat.description}
            icon={stat.icon}
            trend={stat.trend}
          />
        ))}
      </StatsGrid>
      
      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Recommended Stations</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex h-[220px] items-center justify-center rounded-md border border-dashed p-4">
              <p className="text-sm text-muted-foreground">Station list placeholder</p>
            </div>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader>
            <CardTitle>Charging History</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex h-[220px] items-center justify-center rounded-md border border-dashed p-4">
              <p className="text-sm text-muted-foreground">History chart placeholder</p>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

export default OwnerDashboard
