package branchmaster.service.model;

import java.time.LocalTime;
import lombok.Builder;

@Builder
public record BranchOperatingHoursDto(
    Long id,
    Long branchId,
    LocalTime openingTime,
    LocalTime closingTime,
    int dayOfWeek,
    Boolean closed) {}
