package branchmaster.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import branchmaster.security.controller.StaffController;
import branchmaster.security.controller.model.StaffLoginRequest;
import branchmaster.security.controller.model.StaffLoginResponse;
import branchmaster.security.model.StaffPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StaffController.class)
@AutoConfigureMockMvc(addFilters = false)
class StaffControllerTest {

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @MockBean StaffAuthService authService;
  @MockBean JwtService jwtService;

  @Test
  void login_returns200_andJson() throws Exception {
    when(authService.login(any())).thenReturn(new StaffLoginResponse("jwt-token-123"));

    StaffLoginRequest req = new StaffLoginRequest("test@test.com", "password");

    mockMvc
        .perform(
            post("/api/staff/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.token").value("jwt-token-123"));
  }

  @Test
  void login_returns401_whenAuthServiceThrowsIllegalArgumentException() throws Exception {
    when(authService.login(any())).thenThrow(new IllegalArgumentException("bad creds"));

    StaffLoginRequest req = new StaffLoginRequest("test@test.com", "wrong");

    mockMvc
        .perform(
            post("/api/staff/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void login_returns5xx_whenAuthServiceThrowsUnexpectedException() throws Exception {
    when(authService.login(any())).thenThrow(new RuntimeException());

    StaffLoginRequest req = new StaffLoginRequest("test@test.com", "password");

    mockMvc
        .perform(
            post("/api/staff/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().is5xxServerError());
  }

  @Test
  void me_returns200_andJson_withRolesWithoutRolePrefix() throws Exception {
    StaffPrincipal principal = new StaffPrincipal(7, "staff@test.com");

    var auth =
        new TestingAuthenticationToken(
            principal, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

    auth.setAuthenticated(true);

    mockMvc
        .perform(get("/api/staff/me").principal(auth))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(7L))
        .andExpect(jsonPath("$.email").value("staff@test.com"))
        .andExpect(jsonPath("$.roles").isArray())
        .andExpect(jsonPath("$.roles").value(org.hamcrest.Matchers.hasItems("ADMIN")));
  }

  @Test
  void me_returns5xx_whenPrincipalIsWrongType() throws Exception {
    var auth =
        new TestingAuthenticationToken(
            "not-a-staff-principal", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    auth.setAuthenticated(true);

    mockMvc.perform(get("/api/staff/me").principal(auth)).andExpect(status().is5xxServerError());
  }

  @Test
  void me_returns5xx_whenAuthenticationMissing() throws Exception {
    mockMvc.perform(get("/api/staff/me")).andExpect(status().is5xxServerError());
  }
}
