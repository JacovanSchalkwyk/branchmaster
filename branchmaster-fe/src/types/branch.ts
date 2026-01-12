export type Branch = {
  branchId: number;
  name: string;
  address: string;
};

export type BranchFull = {
  branchId: number;
  name: string;
  friendlyAddress: string;
  city: string;
  country: string;
  province: string;
  postalCode: string;
  suburb: string;
  longitude: number;
  latitude: number;
};

export type BranchAdmin = {
  branchId: number;
  name: string;
  address: string;
  city: string;
  country: string;
  province: string;
  postalCode: string;
  suburb: string;
  active: boolean;
  longitude: number;
  latitude: number;
  timeslotLength: number;
  friendlyAddress: string;
};

export type UpdateBranchRequest = {
  id: number;
  address: string;
  suburb: string | null;
  city: string;
  province: string;
  postalCode: string;
  active: boolean;
  timeslotLength: number;
  name: string;
  latitude: number;
  longitude: number;
};

export type CreateBranchRequest = {
  address: string;
  suburb: string | null;
  city: string;
  province: string;
  postalCode: string;
  active: boolean;
  timeslotLength: number;
  name: string;
  latitude: number;
  longitude: number;
  country: string;
};

export type BranchResponse = Branch[];
export type BranchResponseFull = BranchFull[];
