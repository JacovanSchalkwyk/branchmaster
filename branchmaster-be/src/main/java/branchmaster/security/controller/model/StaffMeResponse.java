package branchmaster.security.controller.model;

import java.util.Set;
import lombok.Builder;

@Builder
public record StaffMeResponse(int id, String email, Set<String> roles) {}
