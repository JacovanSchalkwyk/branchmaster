package branchmaster.admin.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

@Builder
public record CreateBranchRequest(
    @NotBlank String name,
    @NotBlank String address,
    String suburb,
    @NotBlank String city,
    @NotBlank String province,
    @NotBlank String postalCode,
    @NotNull Boolean active,
    @PositiveOrZero Integer timeslotLength,
    @NotNull Double latitude,
    @NotNull Double longitude,
    @NotBlank String country) {}
