import type { BookingResponse } from "../types/booking";
import { getApi } from "./client";

export async function getBookingsForBranchDay(
  branchId: number,
  date: string
): Promise<BookingResponse> {
  return getApi<BookingResponse>(
    `/admin/branch/${branchId}/appointments?date=${encodeURIComponent(date)}`
  );
}
