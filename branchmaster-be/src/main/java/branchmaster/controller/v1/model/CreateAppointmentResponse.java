package branchmaster.controller.v1.model;

import branchmaster.repository.entity.BookingStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;

@Builder
public record CreateAppointmentResponse(
    Long appointmentId,
    BookingStatus status,
    LocalDate appointmentDate,
    LocalTime startTime,
    LocalTime endTime,
    String email,
    String phoneNumber,
    String reason) {}
