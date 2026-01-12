export type AvailableResource = {
  id: number;
  dayOfWeek: number;
  startTime: string;
  endTime: string;
  startDate: string;
  endDate: string;
  name: string;
};

export type UnavailableResource = {
  id: number;
  availableResourceId: number;
  startTime: string;
  endTime: string;
  date: string;
  reason: string | null;
};

export type CreateResourceAvailabilityRequest = {
  branchId: number;
  startTime: string;
  endTime: string;
  name: string;
  dayOfWeek: number;
  startDate: string;
  endDate: string;
};

export type UpdateResourceAvailabilityRequest = {
  id: number;
  startTime: string;
  endTime: string;
  name: string;
  dayOfWeek: number;
  startDate: string;
  endDate: string;
};

export type AvailableResourceResponse = AvailableResource[];

export type CreateResourceUnavailabilityRequest = {
  branchId: number;
  availableResourceId: number;
  startTime: string;
  endTime: string;
  date: string;
  reason: string | null;
};

export type UpdateResourceUnavailabilityRequest = {
  id: number;
  availableResourceId: number;
  startTime: string;
  endTime: string;
  date: string;
  reason: string | null;
};

export type UnavailableResourceResponse = UnavailableResource[];
