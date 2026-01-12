export type AvailabilityStatus = "AVAILABLE" | "FULLY_BOOKED";

export type Timeslot = {
  startTime: string;
  endTime: string;
  status: AvailabilityStatus;
};

export type AvailabilityResponse = Record<string, Timeslot[]>;
