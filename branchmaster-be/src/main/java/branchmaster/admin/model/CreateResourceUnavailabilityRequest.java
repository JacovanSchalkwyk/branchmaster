package branchmaster.admin.model;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;

@Builder
public record CreateResourceUnavailabilityRequest(
    Long branchId,
    LocalTime startTime,
    LocalTime endTime,
    LocalDate date,
    Long availableResourceId,
    String reason) {}
