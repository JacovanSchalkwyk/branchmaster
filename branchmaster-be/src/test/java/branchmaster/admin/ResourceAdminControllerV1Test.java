package branchmaster.admin;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import branchmaster.security.JwtService;
import branchmaster.service.ResourceService;
import branchmaster.service.model.ResourceAvailabilityDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ResourceAdminControllerV1.class)
@AutoConfigureMockMvc(addFilters = false)
class ResourceAdminControllerV1Test {

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @MockBean JwtService jwtService;
  @MockBean ResourceService resourceService;

  @Test
  void getAvailableResources_returns200_andJson() throws Exception {
    when(resourceService.getAvailableResourcesForBranch(1L))
        .thenReturn(
            List.of(
                ResourceAvailabilityDto.builder()
                    .id(10L)
                    .branchId(1L)
                    .dayOfWeek(1)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .startDate(LocalDate.of(2026, 1, 1))
                    .endDate(LocalDate.of(2026, 12, 31))
                    .name("Jaco")
                    .build()));

    mockMvc
        .perform(get("/admin/resource/available/{branchId}", 1L))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].id").value(10))
        .andExpect(jsonPath("$[0].name").value("Jaco"));

    verify(resourceService).getAvailableResourcesForBranch(1L);
  }

  @Test
  void createAvailability_returns200() throws Exception {
    var req = new java.util.LinkedHashMap<String, Object>();
    req.put("branchId", 1);
    req.put("name", "Jaco");
    req.put("dayOfWeek", 1);
    req.put("startTime", "09:00:00");
    req.put("endTime", "17:00:00");
    req.put("startDate", "2026-01-01");
    req.put("endDate", "2026-12-31");

    when(resourceService.createResourceAvailability(
            anyLong(), any(), any(), anyInt(), anyString(), any(), any()))
        .thenReturn(
            ResourceAvailabilityDto.builder()
                .id(10L)
                .branchId(1L)
                .dayOfWeek(1)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .name("Jaco")
                .build());

    mockMvc
        .perform(
            post("/admin/resource/available")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(10));

    verify(resourceService)
        .createResourceAvailability(
            eq(1L),
            eq(LocalTime.of(9, 0)),
            eq(LocalTime.of(17, 0)),
            eq(1),
            eq("Jaco"),
            eq(LocalDate.of(2026, 1, 1)),
            eq(LocalDate.of(2026, 12, 31)));
  }

  @Test
  void createAvailability_returns500_whenServiceThrows() throws Exception {
    var req = new java.util.LinkedHashMap<String, Object>();
    req.put("branchId", 1);
    req.put("name", "Jaco");
    req.put("dayOfWeek", 1);
    req.put("startTime", "09:00:00");
    req.put("endTime", "17:00:00");
    req.put("startDate", "2026-01-01");
    req.put("endDate", "2026-12-31");

    when(resourceService.createResourceAvailability(
            anyLong(), any(), any(), anyInt(), anyString(), any(), any()))
        .thenThrow(new RuntimeException("boom"));

    mockMvc
        .perform(
            post("/admin/resource/available")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().is5xxServerError());
  }
}
