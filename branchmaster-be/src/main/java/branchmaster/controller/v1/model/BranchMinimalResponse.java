package branchmaster.controller.v1.model;

import lombok.Builder;

@Builder
public record BranchMinimalResponse(Long branchId, String name, String friendlyAddress) {}
