package branchmaster.admin.model;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;

@Builder
public record UpdateResourceAvailabilityRequest(
    Long id,
    String name,
    Integer dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    LocalDate startDate,
    LocalDate endDate) {}
