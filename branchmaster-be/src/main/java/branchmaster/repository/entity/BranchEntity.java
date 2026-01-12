package branchmaster.repository.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "branch", schema = "branch_master")
public class BranchEntity {
  @Id private Long id;

  @NotNull
  @Size(min = 1, max = 100)
  private String name;

  @NotNull private LocalDateTime createdAt;

  @NotNull private Integer timeslotLength;

  @NotNull private Boolean active;

  @NotNull
  @Size(max = 120)
  private String address;

  @Size(max = 80)
  private String suburb;

  @NotNull
  @Size(max = 80)
  private String city;

  @Size(max = 80)
  private String province;

  @NotNull
  @Size(max = 20)
  private String postalCode;

  @NotNull
  @Size(max = 80)
  private String country;

  private Double latitude;

  private Double longitude;
}
