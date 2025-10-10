export const formatDate = (dateString: string) =>
    new Date(dateString).toLocaleDateString("en-US", {
        year: "numeric",
        month: "long",
        day: "numeric",
    });

export const formatTime = (dateString: string) =>
    new Date(dateString).toLocaleTimeString("en-US", {
        hour: "2-digit",
        minute: "2-digit",
    });

export const getRoleBadgeVariant = (role: number) => {
    switch (role) {
        case 0: // EV Owner
            return "bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400";
        case 1: // BackOffice
            return "bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400";
        case 2: // StationOperator
            return "bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-400";
        default:
            return "bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300";
    }
};

export const getStatusBadgeVariant = (isActive: boolean) => {
    return isActive
        ? "bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400"
        : "bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400";
};