package branchmaster.controller.v1.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;

@Builder
public record CreateAppointmentRequest(
    @NotNull @PositiveOrZero Long branchId,
    @NotNull @FutureOrPresent LocalDate appointmentDate,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime,
    String reason,
    String email,
    String phoneNumber,
    @NotBlank String name) {
  @AssertTrue(message = "endTime must be after startTime")
  public boolean isEndAfterStart() {
    if (startTime == null || endTime == null) return true; // let @NotNull handle it
    return endTime.isAfter(startTime);
  }
}
