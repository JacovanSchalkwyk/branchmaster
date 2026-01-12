package branchmaster.controller.v1.model;

import lombok.Builder;

@Builder
public record BranchResponse(
    Long branchId,
    String name,
    String friendlyAddress,
    String suburb,
    String city,
    String province,
    String postalCode,
    String country,
    Double latitude,
    Double longitude) {}
