export type BranchOperatingHours = {
  id: number;
  openingTime: string | null;
  closingTime: string | null;
  dayOfWeek: number;
  closed: boolean;
};

export type UpdateBranchOperatingHoursRequest = {
  id: number;
  openingTime: string;
  closingTime: string;
  dayOfWeek: number;
  closed: boolean;
};

export type CreateBranchOperatingHoursRequest = {
  branchId: number;
  dayOfWeek: number;
  openingTime: string;
  closingTime: string;
};

export type BranchOperatingHoursResponse = BranchOperatingHours[];
