package branchmaster.security;

import branchmaster.security.model.StaffPrincipal;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@UtilityClass
public final class StaffAuthUtil {
  private static StaffPrincipal requireStaffPrincipal() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof StaffPrincipal p)) {
      throw new IllegalStateException("No authenticated staff user");
    }
    return p;
  }

  public static long getStaffId() {
    return requireStaffPrincipal().staffUserId();
  }
}
