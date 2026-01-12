package branchmaster.security.repository.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "branch_master", name = "staff_user")
public class StaffUserEntity {

  @Id private Long id;

  private String email;

  private String passwordHash;

  private boolean active = true;

  private LocalDateTime createdAt;
}
