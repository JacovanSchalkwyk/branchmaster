package branchmaster.admin.model;

import lombok.Builder;

@Builder
public record CreateBranchRequest(
    String name,
    String address,
    String suburb,
    String city,
    String province,
    String postalCode,
    Boolean active,
    Integer timeslotLength,
    Double latitude,
    Double longitude,
    String country) {}
