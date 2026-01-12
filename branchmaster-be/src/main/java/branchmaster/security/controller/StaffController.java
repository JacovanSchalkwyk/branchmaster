package branchmaster.security.controller;

import branchmaster.security.StaffAuthService;
import branchmaster.security.controller.model.StaffLoginRequest;
import branchmaster.security.controller.model.StaffLoginResponse;
import branchmaster.security.controller.model.StaffMeResponse;
import branchmaster.security.model.StaffPrincipal;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff")
@Slf4j
public class StaffController {

  private final StaffAuthService authService;

  public StaffController(StaffAuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public ResponseEntity<StaffLoginResponse> login(@Valid @RequestBody StaffLoginRequest req) {
    try {
      return ResponseEntity.ok(authService.login(req));
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.status(401).build();
    } catch (Exception ex) {
      log.error("Something went wrong on login, error=[{}]", ex.getMessage());
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping("/me")
  public ResponseEntity<StaffMeResponse> me(Authentication authentication) {
    try {
      var p = (StaffPrincipal) authentication.getPrincipal();
      var roles =
          authentication.getAuthorities().stream()
              .map(a -> a.getAuthority().replace("ROLE_", ""))
              .collect(java.util.stream.Collectors.toSet());

      return ResponseEntity.ok(new StaffMeResponse(p.staffUserId(), p.email(), roles));
    } catch (Exception ex) {
      return ResponseEntity.internalServerError().build();
    }
  }
}
