import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Badge } from "@/components/ui/badge";
import { Switch } from "@/components/ui/switch";
import { MapPin, MoreHorizontal, Edit, UserPlus, UserMinus, Trash2, Shield, Clock } from "lucide-react";
import { getStatusColor, getStatusText, getDayNames, getAmenityIcon } from "./StationSupport";
import type { StationWithOperators } from "@/types/station";
import { Button } from "@/components/ui/button";
import { formatTime } from "@/utils/time";

interface StationCardProps {
    station: StationWithOperators;
    openEditDialog: (station: StationWithOperators) => void;
    openAssignOperatorDialog: (station: StationWithOperators) => void;
    openRevokeOperatorDialog: (station: StationWithOperators) => void;
    openDeleteDialog: (station: StationWithOperators) => void;
    openUpdateSlotsDialog: (station: StationWithOperators) => void;
    handleUpdateStationStatus: (stationId: string, checked: boolean) => void;
}

export default function StationCard(stationProps: StationCardProps) {
    const {
        station,
        openEditDialog,
        openAssignOperatorDialog,
        openRevokeOperatorDialog,
        openDeleteDialog,
        openUpdateSlotsDialog,
        handleUpdateStationStatus,
    } = stationProps;
    return (
        <Card key={station.id} className="overflow-hidden">
            <CardHeader className="pb-3">
                <div className="flex justify-between items-start">
                    <div>
                        <CardTitle className="text-lg">{stationProps.station.name}</CardTitle>
                        <CardDescription className="flex items-center mt-1">
                            <MapPin className="h-3.5 w-3.5 mr-1" />
                            {stationProps.station.location.address}, {stationProps.station.location.city}
                        </CardDescription>
                    </div>
                    <div className="flex items-center gap-2">
                        <Badge className={getStatusColor(station)}>
                            {getStatusText(station)}
                        </Badge>
                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <Button
                                    variant="ghost"
                                    size="sm"
                                    className="h-8 w-8 p-0"
                                >
                                    <MoreHorizontal className="h-4 w-4" />
                                </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end">
                                <DropdownMenuItem
                                    onClick={() => openEditDialog(station)}
                                >
                                    <Edit className="mr-2 h-4 w-4" /> Edit
                                </DropdownMenuItem>
                                <DropdownMenuSeparator />
                                <>
                                    <DropdownMenuItem
                                        onClick={() => openAssignOperatorDialog(station)}
                                    >
                                        <UserPlus className="mr-2 h-4 w-4" /> Assign
                                        Operator
                                    </DropdownMenuItem>
                                    <DropdownMenuItem
                                        onClick={() => openRevokeOperatorDialog(station)}
                                    >
                                        <UserMinus className="mr-2 h-4 w-4" /> Revoke
                                        Operator
                                    </DropdownMenuItem>
                                    <DropdownMenuSeparator />
                                </>
                                <DropdownMenuItem
                                    onClick={() => openDeleteDialog(station)}
                                    className="text-destructive focus:text-destructive"
                                >
                                    <Trash2 className="mr-2 h-4 w-4" /> Delete
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    </div>
                </div>
            </CardHeader>

            <CardContent className="pb-4 space-y-4">
                {/* Availability */}
                <div
                    className="flex justify-between items-center p-3 bg-muted/50 rounded-lg cursor-pointer hover:bg-muted/70 transition-colors"
                    onClick={() => openUpdateSlotsDialog(station)}
                >
                    <div>
                        <p className="text-sm font-medium">Available Slots</p>
                        <p className="text-2xl font-bold text-primary">
                            {station.availableSlots}
                        </p>
                    </div>
                    <div className="text-right">
                        <p className="text-sm text-muted-foreground">Total Slots</p>
                        <p className="text-lg font-semibold">
                            {station.totalSlots}
                        </p>
                    </div>
                </div>
                <div className="flex items-center justify-between p-3 border rounded-lg">
                    <div className="flex items-center space-x-2">
                        <Shield className="h-4 w-4 text-muted-foreground" />
                        <span className="text-sm font-medium">Station Status</span>
                    </div>
                    <Switch
                        checked={station.isActive}
                        onCheckedChange={(checked) =>
                            handleUpdateStationStatus(station.id, checked)
                        }
                        className="data-[state=checked]:bg-green-500"
                    />
                </div>
                {/* Pricing */}
                <div className="flex items-center justify-between">
                    <div className="flex items-center">
                        <span className="text-sm">Price per hour</span>
                    </div>
                    <span className="font-semibold">
                        LKR {station.pricePerHour}
                    </span>
                </div>

                {/* Operating Hours */}
                <div className="space-y-2">
                    <div className="flex items-center">
                        <Clock className="h-4 w-4 mr-1 text-muted-foreground" />
                        <span className="text-sm font-medium">Operating Hours</span>
                    </div>
                    <div className="text-sm">
                        <p>
                            {formatTime(station.operatingHours.openTime)} -{" "}
                            {formatTime(station.operatingHours.closeTime)}
                        </p>
                        <p className="text-muted-foreground">
                            {getDayNames(station.operatingHours.operatingDays)}
                        </p>
                    </div>
                </div>

                {/* Amenities */}
                {station.amenities && station.amenities.length > 0 && (
                    <div className="space-y-2">
                        <p className="text-sm font-medium">Amenities</p>
                        <div className="flex flex-wrap gap-2">
                            {station.amenities.map((amenity, index) => (
                                <Badge
                                    key={index}
                                    variant="secondary"
                                    className="flex items-center gap-1"
                                >
                                    {getAmenityIcon(amenity)}
                                    {amenity}
                                </Badge>
                            ))}
                        </div>
                    </div>
                )}

                {/* Assigned Operators */}
                {station.assignedOperators &&
                    station.assignedOperators.length > 0 && (
                        <div className="space-y-2">
                            <p className="text-sm font-medium">Assigned Operators</p>
                            <div className="space-y-2">
                                {station.assignedOperators.map((operator) => (
                                    <div
                                        key={operator.id}
                                        className="flex items-center justify-between p-2 bg-blue-50 dark:bg-blue-900/20 rounded-md border"
                                    >
                                        <div className="flex items-center space-x-2">
                                            <div className="w-6 h-6 bg-blue-100 dark:bg-blue-800 rounded-full flex items-center justify-center">
                                                <span className="text-xs font-medium text-blue-600 dark:text-blue-300">
                                                    {operator.email?.charAt(0)}
                                                </span>
                                            </div>
                                            <div>
                                                <p className="text-sm font-medium">
                                                    {operator.firstName} {operator.lastName}
                                                </p>
                                                <p className="text-xs text-muted-foreground">
                                                    {operator.email}
                                                </p>
                                            </div>
                                        </div>
                                        <Badge
                                            variant="outline"
                                            className="bg-blue-100 text-blue-700 dark:bg-blue-900 dark:text-blue-300"
                                        >
                                            Operator
                                        </Badge>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                {/* If no operators assigned, show a message (optional) */}
                {station.assignedOperators &&
                    station.assignedOperators.length === 0 && (
                        <div className="space-y-2">
                            <p className="text-sm font-medium">Assigned Operators</p>
                            <div className="text-center py-3 border border-dashed rounded-md">
                                <UserMinus className="h-4 w-4 mx-auto text-muted-foreground mb-1" />
                                <p className="text-xs text-muted-foreground">
                                    No operators assigned
                                </p>
                            </div>
                        </div>
                    )}
                {/* Station Type */}
                <div className="pt-2 border-t">
                    <Badge variant="outline">Type {station.type} Charger</Badge>
                </div>
            </CardContent>
        </Card>
    )
}