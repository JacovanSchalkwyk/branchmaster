package branchmaster.admin.model;

import java.time.LocalTime;
import lombok.Builder;

@Builder
public record CreateBranchOperatingHoursRequest(
    Long branchId, LocalTime openingTime, LocalTime closingTime, Integer dayOfWeek) {}
