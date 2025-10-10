export const BookingStatus = {
  PENDING: 1,
  APPROVED: 2,
  IN_PROGRESS: 3,
  COMPLETED: 4,
  CANCELLED: 5,
  NO_SHOW: 6,
} as const;

export type BookingStatus = (typeof BookingStatus)[keyof typeof BookingStatus];
export const BookingStatusLabel: {
  [key in keyof typeof BookingStatus]: string;
} = {
  PENDING: "Pending",
  APPROVED: "Approved",
  IN_PROGRESS: "In Progress",
  COMPLETED: "Completed",
  CANCELLED: "Cancelled",
  NO_SHOW: "No Show",
};
