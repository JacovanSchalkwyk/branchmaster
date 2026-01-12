// src/api/operatingHours.ts
import type {
  BranchOperatingHours,
  BranchOperatingHoursResponse,
  CreateBranchOperatingHoursRequest,
  UpdateBranchOperatingHoursRequest,
} from "../types/branchOperatingHours";
import { getApi, postApi, putApi } from "./client"; // adjust import to your setup

export async function getBranchOperatingHours(
  branchId: number
): Promise<BranchOperatingHoursResponse> {
  return getApi<BranchOperatingHoursResponse>(`/admin/branch/${branchId}/operating-hours`);
}

export async function updateBranchOperatingHours(
  req: UpdateBranchOperatingHoursRequest
): Promise<void> {
  putApi<UpdateBranchOperatingHoursRequest>(`/admin/branch/operating-hours`, req);
}

export async function createBranchOperatingHours(
  req: CreateBranchOperatingHoursRequest
): Promise<BranchOperatingHours> {
  return postApi<BranchOperatingHours>(`/admin/branch/operating-hours`, req);
}
