package branchmaster.repository.entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "branch_operating_hours", schema = "branch_master")
public class BranchOperatingHoursEntity {
  @Id private Long id;

  @NotNull private Long branchId;

  @NotNull private LocalTime openingTime;

  @NotNull private LocalTime closingTime;

  @Min(0)
  @Max(6)
  private int dayOfWeek; // 0=Sunday, 6=Saturday

  @NotNull private Boolean closed;
}
