import type {
  BranchAdmin,
  BranchResponse,
  BranchResponseFull,
  CreateBranchRequest,
  UpdateBranchRequest,
} from "../types/branch";
import { getApi, postApi, putApi } from "./client";

export function getActiveBranchListMinimal() {
  return getApi<BranchResponse>(`/v1/branch`);
}

export function getActiveBranchListFull() {
  return getApi<BranchResponseFull>(`/v1/branch/full`);
}

export function getBranchListAdmin() {
  return getApi<BranchAdmin[]>(`/admin/branch`);
}

export function getBranchDetailsAdmin(branchId: number) {
  return getApi<BranchAdmin>(`/admin/branch/${branchId}`);
}

export async function updateBranchAdmin(req: UpdateBranchRequest): Promise<BranchAdmin> {
  return await putApi<BranchAdmin, UpdateBranchRequest>("/admin/branch", req);
}

export async function createBranchAdmin(req: CreateBranchRequest): Promise<BranchAdmin> {
  return await postApi<BranchAdmin, CreateBranchRequest>("/admin/branch", req);
}
