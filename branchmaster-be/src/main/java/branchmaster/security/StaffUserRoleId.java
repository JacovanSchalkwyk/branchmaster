package branchmaster.security;

import java.io.Serializable;
import java.util.Objects;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class StaffUserRoleId implements Serializable {
  private Integer staffUserId;
  private String role;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StaffUserRoleId that = (StaffUserRoleId) o;
    return Objects.equals(staffUserId, that.staffUserId) && Objects.equals(role, that.role);
  }

  @Override
  public int hashCode() {
    return Objects.hash(staffUserId, role);
  }
}
