import { BookingStatus } from "@/utils/bookingStatus";

export const getStatusBadgeClasses = (status: number) => {
  switch (status) {
    case BookingStatus.PENDING:
      return "bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400";
    case BookingStatus.APPROVED:
      return "bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400";
    case BookingStatus.IN_PROGRESS:
      return "bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400";
    case BookingStatus.COMPLETED:
      return "bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400";
    case BookingStatus.CANCELLED:
      return "bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400";
    case BookingStatus.NO_SHOW:
      return "bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300";
    default:
      return "bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300";
  }
};
