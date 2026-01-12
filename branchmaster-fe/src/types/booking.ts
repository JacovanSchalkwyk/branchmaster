export type Booking = {
  appointmentId: number;
  appointmentDate: string;
  startTime: string;
  endTime: string;
  email: string | null;
  phoneNumber: string | null;
  reason: string | null;
};

export type BookingResponse = Booking[];
