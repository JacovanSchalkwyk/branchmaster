package branchmaster.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import branchmaster.controller.v1.BranchControllerV1;
import branchmaster.security.JwtService;
import branchmaster.service.BranchService;
import branchmaster.service.model.BranchDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BranchControllerV1.class)
@AutoConfigureMockMvc(addFilters = false)
class BranchControllerV1Test {

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @MockBean BranchService branchService;
  @MockBean JwtService jwtService;

  @Test
  void getOpenBranches_returns200_andJson() throws Exception {
    when(branchService.getAllOpenBranches())
        .thenReturn(
            List.of(
                BranchDto.builder()
                    .id(10L)
                    .name("Branch 1")
                    .address("12 Long Street")
                    .suburb("Suburb 1")
                    .city("City 1")
                    .postalCode("Postal Code 1")
                    .country("Country 1")
                    .build()));

    mockMvc
        .perform(get("/api/v1/branch/full"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].branchId").value(10))
        .andExpect(jsonPath("$[0].name").value("Branch 1"))
        .andExpect(
            jsonPath("$[0].friendlyAddress")
                .value("12 Long Street, Suburb 1, City 1, Postal Code 1, Country 1"));
  }

  @Test
  void getOpenBranches_returns5xx() throws Exception {
    when(branchService.getAllOpenBranches()).thenThrow(RuntimeException.class);

    mockMvc.perform(get("/api/v1/branch/full")).andExpect(status().is5xxServerError());
  }

  @Test
  void getOpenBranchesMinimal_returns200_andJson() throws Exception {
    when(branchService.getAllOpenBranches())
        .thenReturn(
            List.of(
                BranchDto.builder()
                    .id(10L)
                    .name("Branch 1")
                    .address("12 Long Street")
                    .suburb("Suburb 1")
                    .city("City 1")
                    .postalCode("Postal Code 1")
                    .country("Country 1")
                    .build()));

    mockMvc
        .perform(get("/api/v1/branch"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].branchId").value(10))
        .andExpect(jsonPath("$[0].name").value("Branch 1"))
        .andExpect(
            jsonPath("$[0].friendlyAddress")
                .value("12 Long Street, Suburb 1, City 1, Postal Code 1, Country 1"));
  }

  @Test
  void getOpenBranchesMinimal_returns5xx() throws Exception {
    when(branchService.getAllOpenBranches()).thenThrow(RuntimeException.class);

    mockMvc.perform(get("/api/v1/branch")).andExpect(status().is5xxServerError());
  }
}
