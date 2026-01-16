package branchmaster.admin.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalTime;
import lombok.Builder;

@Builder
public record UpdateBranchOperatingHoursRequest(
    @NotNull @PositiveOrZero Long id,
    @NotNull LocalTime openingTime,
    @NotNull LocalTime closingTime,
    @Min(0) @Max(6) Integer dayOfWeek,
    @NotNull Boolean closed) {
  @AssertTrue(message = "closingTime must be after openingTime")
  public boolean isEndAfterStart() {
    if (openingTime == null || closingTime == null) return true;
    return closingTime.isAfter(openingTime);
  }
}
