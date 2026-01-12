package branchmaster.admin.model;

import lombok.Builder;

@Builder
public record BranchAdminResponse(
    Long branchId,
    String name,
    String address,
    String suburb,
    String city,
    String province,
    String postalCode,
    String country,
    Boolean active,
    Double latitude,
    Double longitude,
    Integer timeslotLength,
    String friendlyAddress) {}
