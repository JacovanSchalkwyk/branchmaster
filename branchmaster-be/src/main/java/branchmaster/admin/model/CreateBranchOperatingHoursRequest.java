package branchmaster.admin.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalTime;
import lombok.Builder;

@Builder
public record CreateBranchOperatingHoursRequest(
    @NotNull @PositiveOrZero Long branchId,
    @NotNull LocalTime openingTime,
    @NotNull LocalTime closingTime,
    @NotNull @Min(0) @Max(6) Integer dayOfWeek) {
  @AssertTrue(message = "closingTime must be after openingTime")
  public boolean isEndAfterStart() {
    if (openingTime == null || closingTime == null) return true;
    return closingTime.isAfter(openingTime);
  }
}
