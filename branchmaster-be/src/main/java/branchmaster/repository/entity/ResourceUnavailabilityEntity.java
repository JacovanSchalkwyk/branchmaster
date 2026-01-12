package branchmaster.repository.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(value = "resource_unavailability", schema = "branch_master")
public class ResourceUnavailabilityEntity {
  @Id private Long id;

  private Long availableResourceId;

  @NotNull private Long branchId;

  @NotNull private LocalDate date;

  private LocalTime startTime;
  private LocalTime endTime;

  @Size(max = 200)
  private String reason;
}
