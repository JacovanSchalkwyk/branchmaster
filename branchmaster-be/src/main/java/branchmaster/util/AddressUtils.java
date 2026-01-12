package branchmaster.util;

import branchmaster.service.model.BranchDto;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class AddressUtils {

  public static String buildFriendlyAddress(BranchDto b) {
    if (b == null) return "";

    StringBuilder sb = new StringBuilder();

    append(sb, b.address());
    append(sb, b.suburb());
    append(sb, b.city());
    append(sb, b.province());
    append(sb, b.postalCode());
    append(sb, b.country());

    return sb.toString();
  }

  private static void append(StringBuilder sb, String value) {
    if (value == null || value.isBlank()) return;

    if (!sb.isEmpty()) sb.append(", ");
    sb.append(value.trim());
  }
}
