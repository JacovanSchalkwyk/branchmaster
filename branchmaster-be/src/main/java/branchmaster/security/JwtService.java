package branchmaster.security;

import branchmaster.security.model.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final Key key;
  private final String issuer;
  private final long ttlMinutes;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.issuer}") String issuer,
      @Value("${app.jwt.ttlMinutes}") long ttlMinutes) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.issuer = issuer;
    this.ttlMinutes = ttlMinutes;
  }

  public String createToken(long staffUserId, String email, Set<Role> roles) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(ttlMinutes * 60);

    Map<String, Object> claims = new HashMap<>();
    claims.put("email", email);
    claims.put("staffId", staffUserId);
    claims.put("roles", roles.stream().map(Enum::name).toList());

    return Jwts.builder()
        .setIssuer(issuer)
        .setSubject(String.valueOf(staffUserId))
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(exp))
        .addClaims(claims)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public Jws<Claims> parse(String token) {
    return Jwts.parserBuilder()
        .requireIssuer(issuer)
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token);
  }
}
