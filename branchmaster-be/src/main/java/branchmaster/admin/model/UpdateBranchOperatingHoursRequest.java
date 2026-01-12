package branchmaster.admin.model;

import java.time.LocalTime;
import lombok.Builder;

@Builder
public record UpdateBranchOperatingHoursRequest(
    Long id, LocalTime openingTime, LocalTime closingTime, Integer dayOfWeek, Boolean closed) {}
