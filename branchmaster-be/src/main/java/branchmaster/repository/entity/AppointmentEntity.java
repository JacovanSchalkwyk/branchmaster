package branchmaster.repository.entity;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "appointment", schema = "branch_master")
public class AppointmentEntity {
  @Id private Long id;

  @NotNull private Long branchId;

  @NotNull private LocalDate appointmentDate;

  @NotNull private LocalTime startTime;

  @NotNull private LocalTime endTime;

  @NotNull private BookingStatus status;

  @NotNull private LocalDateTime createdAt;

  private String reason;
  private String email;
  private String phoneNumber;
  private String name;
  private Long resourceAvailabilityId;
}
