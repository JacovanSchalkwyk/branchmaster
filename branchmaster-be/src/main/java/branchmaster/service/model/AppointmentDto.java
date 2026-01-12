package branchmaster.service.model;

import branchmaster.repository.entity.BookingStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Builder;

@Builder
public record AppointmentDto(
    Long id,
    Long branchId,
    LocalDate appointmentDate,
    LocalTime startTime,
    LocalTime endTime,
    LocalDateTime createdAt,
    String reason,
    String email,
    String phoneNumber,
    String name,
    BookingStatus status) {}
