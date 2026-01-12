package branchmaster.admin.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Builder;

@Builder
public record AppointmentResponse(
    Long branchId,
    LocalDate appointmentDate,
    LocalTime startTime,
    LocalTime endTime,
    LocalDateTime createdAt,
    String reason,
    String email,
    String phoneNumber,
    String name) {}
