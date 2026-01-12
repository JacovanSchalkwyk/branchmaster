package branchmaster.security.controller.model;

import lombok.Builder;

@Builder
public record StaffLoginResponse(String token) {}
