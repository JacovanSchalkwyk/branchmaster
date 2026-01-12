package branchmaster.security.controller.model;

import jakarta.validation.constraints.NotBlank;

public record StaffLoginRequest(@NotBlank String email, @NotBlank String password) {}
