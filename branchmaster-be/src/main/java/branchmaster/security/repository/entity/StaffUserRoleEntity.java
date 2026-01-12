package branchmaster.security.repository.entity;

import branchmaster.security.StaffUserRoleId;
import jakarta.persistence.IdClass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(StaffUserRoleId.class)
@Table(schema = "branch_master", name = "staff_user_role")
public class StaffUserRoleEntity {

  @Id private Long staffUserId;

  private String role;
}
