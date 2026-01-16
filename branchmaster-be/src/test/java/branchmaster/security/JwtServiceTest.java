package branchmaster.security;

import static org.junit.jupiter.api.Assertions.*;

import branchmaster.security.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  private static final String SECRET =
      "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF";
  private static final String ISSUER = "branchmaster";
  private static final String EMAIL = "staff@test.com";
  private static final long STAFF_ID = 42L;

  @Test
  void createToken_and_parse_roundTrip_containsExpectedClaims() {
    JwtService jwtService = new JwtService(SECRET, ISSUER, 60);

    Set<Role> roles = EnumSet.of(Role.ADMIN);

    String token = jwtService.createToken(STAFF_ID, EMAIL, roles);
    assertNotNull(token);
    assertFalse(token.isBlank());

    var jws = jwtService.parse(token);
    Claims c = jws.getBody();

    assertEquals(ISSUER, c.getIssuer());
    assertEquals(String.valueOf(STAFF_ID), c.getSubject());

    assertEquals(EMAIL, c.get("email", String.class));
    assertEquals(STAFF_ID, c.get("staffId", Number.class).longValue());

    @SuppressWarnings("unchecked")
    List<String> parsedRoles = (List<String>) c.get("roles");
    assertNotNull(parsedRoles);

    assertTrue(parsedRoles.contains("ADMIN"));
    assertEquals(1, parsedRoles.size());

    assertNotNull(c.getIssuedAt());
    assertNotNull(c.getExpiration());
    assertTrue(c.getExpiration().after(c.getIssuedAt()));
  }

  @Test
  void parse_throws_whenIssuerIsWrong() {
    JwtService jwtService = new JwtService(SECRET, ISSUER, 60);

    String tokenWithDifferentIssuer =
        new JwtService(SECRET, "different-issuer", 60)
            .createToken(STAFF_ID, EMAIL, EnumSet.of(Role.ADMIN));

    assertThrows(JwtException.class, () -> jwtService.parse(tokenWithDifferentIssuer));
  }

  @Test
  void parse_throws_whenSignatureIsInvalid_wrongSecret() {
    JwtService jwtService = new JwtService(SECRET, ISSUER, 60);

    String tokenSignedWithDifferentSecret =
        new JwtService(
                "DIFFERENT_SECRET_0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF", ISSUER, 60)
            .createToken(STAFF_ID, EMAIL, EnumSet.of(Role.ADMIN));

    assertThrows(JwtException.class, () -> jwtService.parse(tokenSignedWithDifferentSecret));
  }

  @Test
  void parse_throws_whenTokenExpired() {
    JwtService jwtService = new JwtService(SECRET, ISSUER, 0);

    String token = jwtService.createToken(STAFF_ID, EMAIL, EnumSet.of(Role.ADMIN));

    assertThrows(JwtException.class, () -> jwtService.parse(token));
  }

  @Test
  void createToken_withEmptyRoles_stillParses_rolesClaimIsEmptyList() {
    JwtService jwtService = new JwtService(SECRET, ISSUER, 60);

    String token = jwtService.createToken(STAFF_ID, EMAIL, Set.of());

    var jws = jwtService.parse(token);
    Claims c = jws.getBody();

    @SuppressWarnings("unchecked")
    List<String> parsedRoles = (List<String>) c.get("roles");

    assertNotNull(parsedRoles);
    assertTrue(parsedRoles.isEmpty());
  }
}
