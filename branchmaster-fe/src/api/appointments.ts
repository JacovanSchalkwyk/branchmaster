import type { Booking } from "../types/booking";
import { delApi, postApi } from "./client";

export type CreateAppointmentRequest = {
  branchId: number;
  appointmentDate: string; // yyyy-MM-dd
  startTime: string; // HH:mm or HH:mm:ss
  endTime: string;
  name: string;
  email: string;
  phoneNumber: string | null;
  reason: string | null;
};

export async function createAppointment(req: CreateAppointmentRequest): Promise<Booking> {
  return await postApi<Booking, CreateAppointmentRequest>("/v1/appointment", req);
}

export async function cancelAppointment(bookingId: number): Promise<void> {
  await delApi(`/v1/appointment/${bookingId}`);
}
