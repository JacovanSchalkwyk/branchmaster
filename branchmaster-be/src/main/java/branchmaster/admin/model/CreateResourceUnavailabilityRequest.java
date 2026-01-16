package branchmaster.admin.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;

@Builder
public record CreateResourceUnavailabilityRequest(
    @NotNull @PositiveOrZero Long branchId,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime,
    @NotNull LocalDate date,
    @NotNull @PositiveOrZero Long availableResourceId,
    String reason) {}
