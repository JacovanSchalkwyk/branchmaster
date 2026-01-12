package branchmaster.security;

import branchmaster.security.model.Role;
import branchmaster.security.model.StaffPrincipal;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwtService;

  public JwtAuthFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws java.io.IOException, jakarta.servlet.ServletException {

    String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (auth == null || !auth.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = auth.substring("Bearer ".length()).trim();

    try {
      var jws = jwtService.parse(token);
      Claims c = jws.getBody();

      int staffUserId = Integer.parseInt(c.getSubject());
      String email = c.get("email", String.class);

      @SuppressWarnings("unchecked")
      List<String> rawRoles = (List<String>) c.get("roles", List.class);
      Set<Role> roles =
          rawRoles == null
              ? Set.of()
              : rawRoles.stream().map(Role::valueOf).collect(Collectors.toSet());

      var principal = new StaffPrincipal(staffUserId, email);

      var authorities =
          roles.stream()
              .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
              .collect(Collectors.toSet());

      var authToken = new UsernamePasswordAuthenticationToken(principal, null, authorities);
      SecurityContextHolder.getContext().setAuthentication(authToken);
    } catch (Exception ex) {
      SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
  }
}
