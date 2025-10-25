import { createBrowserRouter, RouterProvider, Navigate } from "react-router-dom";
import { Toaster } from "sonner";
import { ThemeProvider } from "./components/theme-provider";
import { Button } from "./components/ui/button";
import { Card, CardContent } from "./components/ui/card";
import {
    Zap,
    MapPin,
    Clock,
    Shield,
    Star,
    ArrowRight,
    Car,
    Battery,
    Settings,
    Users,
    CreditCard,
    Navigation
} from "lucide-react";

// Admin Pages
import AdminDashboard from "./pages/admin/dashboard/Dashboard";
import UsersPage from "./pages/admin/users/UsersPage";
import OwnersPage from "./pages/admin/owner/OwnersPage";
import StationsManagementPage from "./pages/admin/station/StationsManagementPage";
import BookingsManagementPage from "./pages/admin/booking/BookingsManagementPage";

// Operator Pages
import OperatorStationsManagementPage from "./pages/stationOperator/OperatorStationsManagementPage";
import OperatorBookingsManagementPage from "./pages/stationOperator/OperatorBookingsManagementPage";
import OperatorProfilePage from "./pages/stationOperator/OperatorProfilePage";

// Layouts
import AuthLayout from "./layouts/AuthLayout";
import AdminLayout from "./layouts/AdminLayout";
import OperatorLayout from "./layouts/OperatorLayout";

// Auth Pages
import LoginPage from "./pages/auth/LoginPage";
import RegisterPage from "./pages/auth/RegisterPage";
import ForgotPasswordPage from "./pages/auth/ForgotPasswordPage";
import OTPVerificationPage from "./pages/auth/OTPVerificationPage";
import ErrorPage from "./pages/ErrorPage";
import RegisterPageUser from "./pages/auth/RegisterPageUser";
import ProfilePage from "./pages/admin/profile/Profile";

const router = createBrowserRouter([
    {
        path: "/",
        element: (
            <div className="min-h-screen bg-background">
                {/* Navigation */}
                <nav className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
                    <div className="container mx-auto px-4 py-4 flex justify-between items-center">
                        <div className="flex items-center space-x-2">
                            <div className="flex items-center justify-center w-8 h-8 bg-primary rounded-lg">
                                <img
                                    src="/app_logo.png"
                                    alt="App Logo"
                                    className="object-cover w-full h-full"
                                />
                            </div>
                            <span className="text-xl font-bold bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent">
                Zap EV
              </span>
                        </div>
                        <div className="flex space-x-6">
                            <a
                                href="/auth/login"
                                className="text-sm font-medium transition-colors hover:text-primary text-foreground/80"
                            >
                                Sign In
                            </a>
                            <a
                                href="/auth/registerUser"
                                className="text-sm font-medium transition-colors hover:text-primary text-foreground/80"
                            >
                                Register
                            </a>
                        </div>
                    </div>
                </nav>

                {/* Hero Section */}
                <section className="relative overflow-hidden">
                    <div className="container mx-auto px-4 py-20 lg:py-28">
                        <div className="grid lg:grid-cols-2 gap-12 items-center">
                            <div className="space-y-8">
                                <div className="space-y-6">
                                    <div className="inline-flex items-center rounded-full border px-3 py-1 text-sm font-medium bg-muted/50">
                                        🚀 The future of EV charging is here
                                    </div>
                                    <h1 className="text-4xl lg:text-6xl font-bold tracking-tight">
                                        Power Your{" "}
                                        <span className="text-primary">Journey</span>{" "}
                                        with Zap EV
                                    </h1>
                                    <p className="text-xl text-muted-foreground max-w-2xl">
                                        Book charging slots instantly at stations near you. Fast, reliable,
                                        and eco-friendly electric vehicle charging solutions for modern drivers.
                                    </p>
                                </div>
                                <div className="flex flex-col sm:flex-row gap-4">
                                    <Button asChild size="lg" className="gap-2 h-12 px-8 text-base">
                                        <a href="/auth/registerUser">
                                            Get Started
                                            <ArrowRight className="h-5 w-5" />
                                        </a>
                                    </Button>
                                    <Button asChild variant="outline" size="lg" className="h-12 px-8 text-base">
                                        <a href="/auth/login">Sign In</a>
                                    </Button>
                                </div>

                                {/* Stats Row */}
                                <div className="grid grid-cols-3 gap-8 pt-8 border-t">
                                    <div>
                                        <div className="text-2xl font-bold text-primary">500+</div>
                                        <div className="text-sm text-muted-foreground">Stations</div>
                                    </div>
                                    <div>
                                        <div className="text-2xl font-bold text-primary">10K+</div>
                                        <div className="text-sm text-muted-foreground">Users</div>
                                    </div>
                                    <div>
                                        <div className="text-2xl font-bold text-primary">50K+</div>
                                        <div className="text-sm text-muted-foreground">Charges</div>
                                    </div>
                                </div>
                            </div>

                            {/* Hero Image */}
                            <div className="relative">
                                <div className="aspect-square rounded-2xl bg-gradient-to-br from-primary/20 to-muted overflow-hidden shadow-2xl">
                                    <img
                                        src="/station-img.jpg"
                                        alt="Modern EV Charging Station"
                                        className="object-cover w-full h-full"
                                    />
                                </div>
                                {/* Floating Cards */}
                                <div className="absolute -bottom-6 -left-6 bg-background rounded-xl shadow-lg border p-4 w-32">
                                    <div className="flex items-center space-x-2">
                                        <MapPin className="h-4 w-4 text-primary" />
                                        <span className="text-sm font-medium">Live Map</span>
                                    </div>
                                </div>
                                <div className="absolute -top-6 -right-6 bg-background rounded-xl shadow-lg border p-4 w-36">
                                    <div className="flex items-center space-x-2">
                                        <Clock className="h-4 w-4 text-primary" />
                                        <span className="text-sm font-medium">Fast Booking</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>

                {/* Features Section */}
                <section className="py-20 bg-muted/30">
                    <div className="container mx-auto px-4">
                        <div className="text-center space-y-4 mb-16">
                            <h2 className="text-3xl lg:text-4xl font-bold">Why Choose Zap Ev?</h2>
                            <p className="text-muted-foreground text-lg max-w-2xl mx-auto">
                                Experience the future of electric vehicle charging with our seamless,
                                user-friendly platform designed for modern EV owners.
                            </p>
                        </div>

                        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
                            <Card className="border-0 shadow-lg hover:shadow-xl transition-all duration-300 hover:translate-y-[-4px]">
                                <CardContent className="p-8 space-y-4">
                                    <div className="p-3 bg-primary/10 rounded-xl w-fit">
                                        <MapPin className="h-6 w-6 text-primary" />
                                    </div>
                                    <h3 className="text-xl font-semibold">Smart Station Finder</h3>
                                    <p className="text-muted-foreground">
                                        Locate available charging stations in real-time with our intelligent
                                        mapping system and live availability updates.
                                    </p>
                                </CardContent>
                            </Card>

                            <Card className="border-0 shadow-lg hover:shadow-xl transition-all duration-300 hover:translate-y-[-4px]">
                                <CardContent className="p-8 space-y-4">
                                    <div className="p-3 bg-primary/10 rounded-xl w-fit">
                                        <Clock className="h-6 w-6 text-primary" />
                                    </div>
                                    <h3 className="text-xl font-semibold">Instant Booking</h3>
                                    <p className="text-muted-foreground">
                                        Reserve your perfect charging slot in seconds with our streamlined,
                                        intuitive booking system.
                                    </p>
                                </CardContent>
                            </Card>

                            <Card className="border-0 shadow-lg hover:shadow-xl transition-all duration-300 hover:translate-y-[-4px]">
                                <CardContent className="p-8 space-y-4">
                                    <div className="p-3 bg-primary/10 rounded-xl w-fit">
                                        <Shield className="h-6 w-6 text-primary" />
                                    </div>
                                    <h3 className="text-xl font-semibold">Secure & Reliable</h3>
                                    <p className="text-muted-foreground">
                                        Your data and payments are protected with enterprise-grade security
                                        and 24/7 monitoring.
                                    </p>
                                </CardContent>
                            </Card>

                            <Card className="border-0 shadow-lg hover:shadow-xl transition-all duration-300 hover:translate-y-[-4px]">
                                <CardContent className="p-8 space-y-4">
                                    <div className="p-3 bg-primary/10 rounded-xl w-fit">
                                        <CreditCard className="h-6 w-6 text-primary" />
                                    </div>
                                    <h3 className="text-xl font-semibold">Easy Payments</h3>
                                    <p className="text-muted-foreground">
                                        Multiple payment options with seamless transactions and instant
                                        confirmation for all your charging sessions.
                                    </p>
                                </CardContent>
                            </Card>

                            <Card className="border-0 shadow-lg hover:shadow-xl transition-all duration-300 hover:translate-y-[-4px]">
                                <CardContent className="p-8 space-y-4">
                                    <div className="p-3 bg-primary/10 rounded-xl w-fit">
                                        <Navigation className="h-6 w-6 text-primary" />
                                    </div>
                                    <h3 className="text-xl font-semibold">Live Navigation</h3>
                                    <p className="text-muted-foreground">
                                        Turn-by-turn navigation to your booked station with real-time
                                        traffic updates and ETA calculations.
                                    </p>
                                </CardContent>
                            </Card>

                            <Card className="border-0 shadow-lg hover:shadow-xl transition-all duration-300 hover:translate-y-[-4px]">
                                <CardContent className="p-8 space-y-4">
                                    <div className="p-3 bg-primary/10 rounded-xl w-fit">
                                        <Users className="h-6 w-6 text-primary" />
                                    </div>
                                    <h3 className="text-xl font-semibold">Dedicated Support</h3>
                                    <p className="text-muted-foreground">
                                        24/7 customer support to assist you with any issues or questions
                                        about your charging experience.
                                    </p>
                                </CardContent>
                            </Card>
                        </div>
                    </div>
                </section>

                {/* Stats Section */}
                <section className="py-20">
                    <div className="container mx-auto px-4">
                        <div className="grid grid-cols-2 lg:grid-cols-4 gap-8 text-center">
                            <div className="space-y-4">
                                <div className="mx-auto w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center">
                                    <Car className="h-8 w-8 text-primary" />
                                </div>
                                <div className="space-y-2">
                                    <div className="text-3xl font-bold text-primary">500+</div>
                                    <div className="text-muted-foreground font-medium">Charging Stations</div>
                                </div>
                            </div>
                            <div className="space-y-4">
                                <div className="mx-auto w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center">
                                    <Battery className="h-8 w-8 text-primary" />
                                </div>
                                <div className="space-y-2">
                                    <div className="text-3xl font-bold text-primary">50K+</div>
                                    <div className="text-muted-foreground font-medium">Charging Sessions</div>
                                </div>
                            </div>
                            <div className="space-y-4">
                                <div className="mx-auto w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center">
                                    <Star className="h-8 w-8 text-primary" />
                                </div>
                                <div className="space-y-2">
                                    <div className="text-3xl font-bold text-primary">4.8/5</div>
                                    <div className="text-muted-foreground font-medium">User Rating</div>
                                </div>
                            </div>
                            <div className="space-y-4">
                                <div className="mx-auto w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center">
                                    <Settings className="h-8 w-8 text-primary" />
                                </div>
                                <div className="space-y-2">
                                    <div className="text-3xl font-bold text-primary">24/7</div>
                                    <div className="text-muted-foreground font-medium">Support</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>

                {/* CTA Section */}
                <section className="py-20 bg-muted/50">
                    <div className="container mx-auto px-4">
                        <Card className="bg-primary/10 text-white border-0 shadow-2xl max-w-4xl mx-auto">
                            <CardContent className="p-12 text-center">
                                <h2 className="text-3xl lg:text-4xl font-bold mb-6">
                                    Ready to Power Your EV Journey?
                                </h2>
                                <p className="text-white/80 text-lg mb-8 max-w-2xl mx-auto">
                                    Join thousands of EV owners who trust Zap EV for their charging needs.
                                    Start booking your charging slots today and experience the future of EV charging.
                                </p>
                                <div className="flex flex-col sm:flex-row gap-4 justify-center">
                                    <Button asChild variant="secondary" size="lg" className="h-12 px-8 text-base font-semibold">
                                        <a href="/auth/registerUser">Create Free Account</a>
                                    </Button>
                                    <Button asChild variant="outline" size="lg" className="h-12 px-8 text-base font-semibold bg-transparent border-white text-white hover:bg-white hover:text-primary">
                                        <a href="/auth/login">Sign In to Dashboard</a>
                                    </Button>
                                </div>
                            </CardContent>
                        </Card>
                    </div>
                </section>

                {/* Footer */}
                <footer className="border-t bg-background py-12">
                    <div className="container mx-auto px-4">
                        <div className="flex flex-col lg:flex-row justify-between items-center">
                            <div className="flex items-center space-x-3 mb-6 lg:mb-0">
                                <div className="flex items-center justify-center w-10 h-10 bg-primary rounded-lg">
                                    <Zap className="h-6 w-6 text-primary-foreground" />
                                </div>
                                <div>
                                    <span className="text-xl font-bold">Zap EV</span>
                                    <p className="text-sm text-muted-foreground">Powering the EV revolution</p>
                                </div>
                            </div>
                            <div className="text-center lg:text-right">
                                <div className="text-sm text-muted-foreground">
                                    © 2025 Zap EV Charging System. All rights reserved.
                                </div>
                                <div className="text-xs text-muted-foreground mt-2">
                                    Built for a sustainable future
                                </div>
                            </div>
                        </div>
                    </div>
                </footer>
            </div>
        ),
    },
    {
        path: "/auth",
        element: <AuthLayout />,
        children: [
            {
                path: "login",
                element: <LoginPage />,
            },
            {
                path: "register",
                element: <RegisterPage />,
            },
            {
                path: "registerUser",
                element: <RegisterPageUser />,
            },
            {
                path: "forgot-password",
                element: <ForgotPasswordPage />,
            },
            {
                path: "verify-otp",
                element: <OTPVerificationPage />,
            },
        ],
    },
    // Admin routes
    {
        path: "/admin",
        element: <AdminLayout />,
        children: [
            {
                path: "",
                element: <Navigate to="/admin/dashboard" replace />,
            },
            {
                path: "dashboard",
                element: <AdminDashboard />,
            },
            {
                path: "users",
                element: <UsersPage />,
            },
            {
                path: "owners",
                element: <OwnersPage />,
            },
            {
                path: "stations",
                element: <StationsManagementPage />,
            },
            {
                path: "bookings",
                element: <BookingsManagementPage />,
            },
            {
                path: "settings",
                element: <div>Settings</div>,
            },
            {
                path: "profile",
                element: <ProfilePage />,
            },
        ],
    },
    // Station Operator Routes
    {
        path: "/operator",
        element: <OperatorLayout />,
        children: [
            {
                path: "",
                element: <Navigate to="/operator/stations" replace />,
            },
            {
                path: "stations",
                element: <OperatorStationsManagementPage />,
            },
            {
                path: "bookings",
                element: <OperatorBookingsManagementPage />,
            },
            {
                path: "profile",
                element: <OperatorProfilePage />,
            },
        ],
    },

    {
        path: "/unauthorized",
        element: <ErrorPage />,
    },
    {
        path: "*",
        element: (
            <ErrorPage
                title="Page Not Found"
                message="The page you're looking for doesn't exist."
                code={404}
            />
        ),
    },
]);

function App() {
    return (
        <ThemeProvider defaultTheme="system">
            <RouterProvider router={router} />
            <Toaster richColors position="top-center" />
        </ThemeProvider>
    );
}

export default App;