package branchmaster.repository.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "resource_availability", schema = "branch_master")
public class ResourceAvailabilityEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull private Long branchId;

  @Min(0)
  @Max(6)
  private int dayOfWeek; // 0=Sunday, 6=Saturday

  @NotNull private LocalTime startTime;

  @NotNull private LocalTime endTime;

  private LocalDate startDate;
  private LocalDate endDate;
  private String name;
}
