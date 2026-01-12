import type {
  AvailableResource,
  AvailableResourceResponse,
  CreateResourceAvailabilityRequest,
  CreateResourceUnavailabilityRequest,
  UnavailableResource,
  UnavailableResourceResponse,
  UpdateResourceAvailabilityRequest,
  UpdateResourceUnavailabilityRequest,
} from "../types/resources";
import { delApi, getApi, postApi, putApi } from "./client";

export function getAvailableResourcesForBranch(branchId: number) {
  return getApi<AvailableResourceResponse>(`/admin/resource/available/${branchId}`);
}

export function getUnavailableResourcesForBranch(branchId: number) {
  return getApi<UnavailableResourceResponse>(`/admin/resource/unavailable/${branchId}`);
}

export async function createResourceAvailability(
  req: CreateResourceAvailabilityRequest
): Promise<AvailableResource> {
  return await postApi<AvailableResource, CreateResourceAvailabilityRequest>(
    "/admin/resource/available",
    req
  );
}

export async function updateResourceAvailability(
  req: UpdateResourceAvailabilityRequest
): Promise<void> {
  await putApi<void, UpdateResourceAvailabilityRequest>("/admin/resource/available", req);
}

export async function deleteResourceAvailability(id: number): Promise<void> {
  await delApi(`/admin/resource/available/${id}`);
}

export async function createResourceUnavailability(
  req: CreateResourceUnavailabilityRequest
): Promise<UnavailableResource> {
  return await postApi<UnavailableResource, CreateResourceUnavailabilityRequest>(
    "/admin/resource/unavailable",
    req
  );
}

export async function updateResourceUnavailability(
  req: UpdateResourceUnavailabilityRequest
): Promise<void> {
  await putApi<void, UpdateResourceUnavailabilityRequest>("/admin/resource/unavailable", req);
}

export async function deleteResourceUnavailability(id: number): Promise<void> {
  await delApi(`/admin/resource/unavailable/${id}`);
}
