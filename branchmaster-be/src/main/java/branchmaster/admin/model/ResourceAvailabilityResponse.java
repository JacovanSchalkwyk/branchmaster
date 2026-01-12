package branchmaster.admin.model;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;

@Builder
public record ResourceAvailabilityResponse(
    Long id,
    Long branchId,
    int dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    LocalDate startDate,
    LocalDate endDate,
    String name) {}
