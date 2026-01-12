package branchmaster.admin.model;

import java.time.LocalTime;
import lombok.Builder;

@Builder
public record BranchOperatingHoursResponse(
    Long id, LocalTime openingTime, LocalTime closingTime, Integer dayOfWeek) {}
