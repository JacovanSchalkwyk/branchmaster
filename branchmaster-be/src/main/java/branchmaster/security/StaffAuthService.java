package branchmaster.security;

import branchmaster.security.controller.model.StaffLoginRequest;
import branchmaster.security.controller.model.StaffLoginResponse;
import branchmaster.security.model.Role;
import branchmaster.security.repository.StaffUserRepository;
import branchmaster.security.repository.StaffUserRoleRepository;
import branchmaster.security.repository.entity.StaffUserEntity;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StaffAuthService {

  private final StaffUserRepository userRepo;
  private final StaffUserRoleRepository roleRepo;
  private final PasswordEncoder encoder;
  private final JwtService jwt;

  public StaffLoginResponse login(StaffLoginRequest req) {
    var user =
        userRepo
            .findByEmailIgnoreCase(req.email())
            .filter(StaffUserEntity::isActive)
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

    if (!encoder.matches(req.password(), user.getPasswordHash())) {
      throw new IllegalArgumentException("Invalid credentials");
    }

    Set<Role> roles =
        roleRepo.findAllByStaffUserId(user.getId()).stream()
            .map(r -> Role.valueOf(r.getRole()))
            .collect(Collectors.toSet());

    String token = jwt.createToken(user.getId(), user.getEmail(), roles);
    return new StaffLoginResponse(token);
  }
}
