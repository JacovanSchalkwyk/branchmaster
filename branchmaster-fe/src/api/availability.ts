import { getApi } from "./client";
import type { AvailabilityResponse } from "../types/availability";

export function getBranchAvailability(branchId: number, startDate: string, endDate: string) {
  return getApi<AvailabilityResponse>(
    `/v1/appointment/available/${branchId}?startDate=${encodeURIComponent(startDate)}&endDate=${encodeURIComponent(endDate)}`
  );
}
