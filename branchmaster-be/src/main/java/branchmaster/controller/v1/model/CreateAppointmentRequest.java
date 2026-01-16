package branchmaster.controller.v1.model;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;

@Builder
public record CreateAppointmentRequest(
    @NotNull Long branchId,
    @NotNull LocalDate appointmentDate,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime,
    String reason,
    String email,
    String phoneNumber,
    @NotNull String name) {}
