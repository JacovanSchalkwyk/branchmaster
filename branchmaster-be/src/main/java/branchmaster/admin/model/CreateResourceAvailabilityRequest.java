package branchmaster.admin.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;

@Builder
public record CreateResourceAvailabilityRequest(
    @PositiveOrZero Long branchId,
    @NotBlank String name,
    @NotNull @Min(0) @Max(6) Integer dayOfWeek,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate) {
  @AssertTrue(message = "endTime must be after startTime")
  public boolean isEndAfterStart() {
    if (startTime == null || endTime == null) return true;
    return endTime.isAfter(startTime);
  }

  @AssertTrue(message = "endDate must be after startDate")
  public boolean isEndAfterStartDate() {
    if (startDate == null || endDate == null) return true;
    return endDate.isAfter(startDate);
  }
}
