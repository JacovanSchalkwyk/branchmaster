package branchmaster.service.model;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;

@Builder
public record ResourceUnavailabilityDto(
    Long id,
    Long availableResourceId,
    Long branchId,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    String reason) {}
