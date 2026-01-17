package branchmaster.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import branchmaster.admin.ResourceAdminControllerV1;
import branchmaster.admin.model.CreateResourceAvailabilityRequest;
import branchmaster.admin.model.CreateResourceUnavailabilityRequest;
import branchmaster.admin.model.UpdateResourceAvailabilityRequest;
import branchmaster.admin.model.UpdateResourceUnavailabilityRequest;
import branchmaster.security.JwtService;
import branchmaster.service.ResourceService;
import branchmaster.service.model.ResourceAvailabilityDto;
import branchmaster.service.model.ResourceUnavailabilityDto;
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

  @MockBean ResourceService resourceService;

  @MockBean JwtService jwtService;

  @Test
  void getAvailableResourcesForBranch_returns200_andJsonArray() throws Exception {
    when(resourceService.getAvailableResourcesForBranch(anyLong()))
        .thenReturn(
            List.of(
                ResourceAvailabilityDto.builder()
                    .id(1L)
                    .branchId(10L)
                    .name("Room A")
                    .dayOfWeek(1)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(10, 0))
                    .startDate(LocalDate.of(2026, 1, 1))
                    .endDate(LocalDate.of(2026, 12, 31))
                    .build(),
                ResourceAvailabilityDto.builder()
                    .id(2L)
                    .branchId(10L)
                    .name("Room B")
                    .dayOfWeek(2)
                    .startTime(LocalTime.of(11, 0))
                    .endTime(LocalTime.of(12, 0))
                    .startDate(LocalDate.of(2026, 1, 1))
                    .endDate(LocalDate.of(2026, 12, 31))
                    .build()));

    mockMvc
        .perform(get("/api/admin/resource/available/10"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].branchId").value(10L))
        .andExpect(jsonPath("$[0].name").value("Room A"));
  }

  @Test
  void getAvailableResourcesForBranch_returns5xx_onException() throws Exception {
    when(resourceService.getAvailableResourcesForBranch(anyLong()))
        .thenThrow(new RuntimeException());

    mockMvc.perform(get("/api/admin/resource/available/10")).andExpect(status().is5xxServerError());
  }

  @Test
  void createResourceAvailability_returns200_andJson() throws Exception {
    when(resourceService.createResourceAvailability(
            anyLong(), any(), any(), any(), any(), any(), any()))
        .thenReturn(
            ResourceAvailabilityDto.builder()
                .id(1L)
                .branchId(10L)
                .name("Room A")
                .dayOfWeek(1)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .build());

    CreateResourceAvailabilityRequest req =
        CreateResourceAvailabilityRequest.builder()
            .branchId(10L)
            .name("Room A")
            .dayOfWeek(1)
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(10, 0))
            .startDate(LocalDate.of(2026, 1, 1))
            .endDate(LocalDate.of(2026, 12, 31))
            .build();

    mockMvc
        .perform(
            post("/api/admin/resource/available")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.branchId").value(10L))
        .andExpect(jsonPath("$.name").value("Room A"));
  }

  @Test
  void createResourceAvailability_returns5xx_onException() throws Exception {
    when(resourceService.createResourceAvailability(
            anyLong(), any(), any(), any(), any(), any(), any()))
        .thenThrow(new RuntimeException());

    CreateResourceAvailabilityRequest req =
        CreateResourceAvailabilityRequest.builder()
            .branchId(10L)
            .name("Room A")
            .dayOfWeek(1)
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(10, 0))
            .startDate(LocalDate.of(2026, 1, 1))
            .endDate(LocalDate.of(2026, 12, 31))
            .build();

    mockMvc
        .perform(
            post("/api/admin/resource/available")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().is5xxServerError());
  }

  @Test
  void updateResourceAvailability_returns200() throws Exception {
    doNothing()
        .when(resourceService)
        .updateResourceAvailability(anyLong(), any(), any(), any(), any(), any(), any());

    UpdateResourceAvailabilityRequest req =
        UpdateResourceAvailabilityRequest.builder()
            .id(1L)
            .name("Room A")
            .dayOfWeek(1)
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(10, 0))
            .startDate(LocalDate.of(2026, 1, 1))
            .endDate(LocalDate.of(2026, 12, 31))
            .build();

    mockMvc
        .perform(
            put("/api/admin/resource/available")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk());

    verify(resourceService)
        .updateResourceAvailability(anyLong(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void updateResourceAvailability_returns5xx_onException() throws Exception {
    doThrow(new RuntimeException())
        .when(resourceService)
        .updateResourceAvailability(anyLong(), any(), any(), any(), any(), any(), any());

    UpdateResourceAvailabilityRequest req =
        UpdateResourceAvailabilityRequest.builder()
            .id(1L)
            .name("Room A")
            .dayOfWeek(1)
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(10, 0))
            .startDate(LocalDate.of(2026, 1, 1))
            .endDate(LocalDate.of(2026, 12, 31))
            .build();

    mockMvc
        .perform(
            put("/api/admin/resource/available")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().is5xxServerError());
  }

  @Test
  void deleteAvailableResourcesForBranch_returns200() throws Exception {
    doNothing().when(resourceService).deleteResourceAvailability(anyLong());

    mockMvc.perform(delete("/api/admin/resource/available/1")).andExpect(status().isOk());

    verify(resourceService).deleteResourceAvailability(1L);
  }

  @Test
  void deleteAvailableResourcesForBranch_returns5xx_onException() throws Exception {
    doThrow(new RuntimeException()).when(resourceService).deleteResourceAvailability(1L);

    mockMvc
        .perform(delete("/api/admin/resource/available/1"))
        .andExpect(status().is5xxServerError());

    verify(resourceService).deleteResourceAvailability(1L);
  }

  @Test
  void getUnavailableResourcesForBranch_returns200_andJsonArray() throws Exception {
    when(resourceService.getUnavailableResourcesForBranch(anyLong()))
        .thenReturn(
            List.of(
                ResourceUnavailabilityDto.builder()
                    .id(100L)
                    .branchId(10L)
                    .availableResourceId(1L)
                    .date(LocalDate.of(2026, 1, 10))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(10, 0))
                    .reason("Maintenance")
                    .build()));

    mockMvc
        .perform(get("/api/admin/resource/unavailable/10"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].id").value(100L))
        .andExpect(jsonPath("$[0].availableResourceId").value(1L))
        .andExpect(jsonPath("$[0].reason").value("Maintenance"));
  }

  @Test
  void getUnavailableResourcesForBranch_returns5xx_onException() throws Exception {
    when(resourceService.getUnavailableResourcesForBranch(anyLong()))
        .thenThrow(new RuntimeException());

    mockMvc
        .perform(get("/api/admin/resource/unavailable/10"))
        .andExpect(status().is5xxServerError());
  }

  @Test
  void createResourceUnavailability_returns200_andJson() throws Exception {
    when(resourceService.createResourceUnavailability(
            anyLong(), any(), any(), any(), anyLong(), any()))
        .thenReturn(
            ResourceUnavailabilityDto.builder()
                .id(100L)
                .branchId(10L)
                .availableResourceId(1L)
                .date(LocalDate.of(2026, 1, 10))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .reason("Maintenance")
                .build());

    CreateResourceUnavailabilityRequest req =
        CreateResourceUnavailabilityRequest.builder()
            .branchId(10L)
            .availableResourceId(1L)
            .date(LocalDate.of(2026, 1, 10))
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(10, 0))
            .reason("Maintenance")
            .build();

    mockMvc
        .perform(
            post("/api/admin/resource/unavailable")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(100L))
        .andExpect(jsonPath("$.availableResourceId").value(1L))
        .andExpect(jsonPath("$.reason").value("Maintenance"));
  }

  @Test
  void createResourceUnavailability_returns5xx_onException() throws Exception {
    when(resourceService.createResourceUnavailability(
            anyLong(), any(), any(), any(), anyLong(), any()))
        .thenThrow(new RuntimeException());

    CreateResourceUnavailabilityRequest req =
        CreateResourceUnavailabilityRequest.builder()
            .branchId(10L)
            .availableResourceId(1L)
            .date(LocalDate.of(2026, 1, 10))
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(10, 0))
            .reason("Maintenance")
            .build();

    mockMvc
        .perform(
            post("/api/admin/resource/unavailable")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().is5xxServerError());
  }

  @Test
  void updateResourceUnavailability_returns200() throws Exception {
    doNothing()
        .when(resourceService)
        .updateResourceUnavailability(anyLong(), any(), any(), any(), anyLong(), any());

    UpdateResourceUnavailabilityRequest req =
        UpdateResourceUnavailabilityRequest.builder()
            .id(100L)
            .availableResourceId(1L)
            .date(LocalDate.of(2026, 1, 10))
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(10, 0))
            .reason("Updated reason")
            .build();

    mockMvc
        .perform(
            put("/api/admin/resource/unavailable")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk());

    verify(resourceService)
        .updateResourceUnavailability(anyLong(), any(), any(), any(), anyLong(), any());
  }

  @Test
  void updateResourceUnavailability_returns5xx_onException() throws Exception {
    doThrow(new RuntimeException())
        .when(resourceService)
        .updateResourceUnavailability(anyLong(), any(), any(), any(), anyLong(), any());

    UpdateResourceUnavailabilityRequest req =
        UpdateResourceUnavailabilityRequest.builder()
            .id(100L)
            .availableResourceId(1L)
            .date(LocalDate.of(2026, 1, 10))
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(10, 0))
            .reason("Updated reason")
            .build();

    mockMvc
        .perform(
            put("/api/admin/resource/unavailable")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().is5xxServerError());
  }

  @Test
  void deleteResourceUnavailability_returns200() throws Exception {
    doNothing().when(resourceService).deleteResourceUnavailability(anyLong());

    mockMvc.perform(delete("/api/admin/resource/unavailable/100")).andExpect(status().isOk());

    verify(resourceService).deleteResourceUnavailability(100L);
  }

  @Test
  void deleteResourceUnavailability_returns5xx_onException() throws Exception {
    doThrow(new RuntimeException()).when(resourceService).deleteResourceUnavailability(100L);

    mockMvc
        .perform(delete("/api/admin/resource/unavailable/100"))
        .andExpect(status().is5xxServerError());

    verify(resourceService).deleteResourceUnavailability(100L);
  }
}
