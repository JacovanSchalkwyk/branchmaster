package branchmaster.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import branchmaster.admin.BranchAdminControllerV1;
import branchmaster.admin.model.CreateBranchOperatingHoursRequest;
import branchmaster.admin.model.CreateBranchRequest;
import branchmaster.admin.model.UpdateBranchOperatingHoursRequest;
import branchmaster.admin.model.UpdateBranchRequest;
import branchmaster.repository.entity.BookingStatus;
import branchmaster.security.JwtService;
import branchmaster.service.AppointmentService;
import branchmaster.service.BranchOperatingHoursService;
import branchmaster.service.BranchService;
import branchmaster.service.model.AppointmentDto;
import branchmaster.service.model.BranchDto;
import branchmaster.service.model.BranchOperatingHoursDto;
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

@WebMvcTest(BranchAdminControllerV1.class)
@AutoConfigureMockMvc(addFilters = false)
class BranchAdminControllerV1Test {

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @MockBean BranchService branchService;
  @MockBean BranchOperatingHoursService branchOperatingHoursService;
  @MockBean AppointmentService appointmentService;

  @MockBean JwtService jwtService;

  @Test
  void getBranchList_returns200_andJsonArray() throws Exception {
    when(branchService.getAllBranches())
        .thenReturn(
            List.of(
                BranchDto.builder()
                    .id(1L)
                    .name("Branch 1")
                    .suburb("Suburb")
                    .city("Cape Town")
                    .province("WC")
                    .postalCode("8000")
                    .country("South Africa")
                    .build(),
                BranchDto.builder()
                    .id(2L)
                    .name("Branch 2")
                    .suburb("Suburb2")
                    .city("Stellenbosch")
                    .province("WC")
                    .postalCode("7600")
                    .country("South Africa")
                    .build()));

    mockMvc
        .perform(get("/api/admin/branch"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].branchId").value(1L))
        .andExpect(jsonPath("$[0].name").value("Branch 1"))
        .andExpect(jsonPath("$[1].branchId").value(2L));
  }

  @Test
  void getBranchList_returns5xx_onException() throws Exception {
    when(branchService.getAllBranches()).thenThrow(new RuntimeException());

    mockMvc.perform(get("/api/admin/branch")).andExpect(status().is5xxServerError());
  }

  @Test
  void getBranchDetails_returns200_andJson() throws Exception {
    when(branchService.getBranchDetailsAdmin(anyLong()))
        .thenReturn(
            BranchDto.builder()
                .id(1L)
                .name("Branch 1")
                .suburb("Suburb")
                .city("Cape Town")
                .province("WC")
                .postalCode("8000")
                .country("South Africa")
                .build());

    mockMvc
        .perform(get("/api/admin/branch/1"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.branchId").value(1L))
        .andExpect(jsonPath("$.name").value("Branch 1"));
  }

  @Test
  void getBranchDetails_returns5xx_onException() throws Exception {
    when(branchService.getBranchDetailsAdmin(anyLong())).thenThrow(new RuntimeException());

    mockMvc.perform(get("/api/admin/branch/1")).andExpect(status().is5xxServerError());
  }

  @Test
  void getBranchOperatingHours_returns200_andJsonArray() throws Exception {
    when(branchOperatingHoursService.getOperatingHoursForBranch(anyLong()))
        .thenReturn(
            List.of(
                BranchOperatingHoursDto.builder()
                    .id(10L)
                    .branchId(1L)
                    .dayOfWeek(1)
                    .openingTime(LocalTime.of(9, 0))
                    .closingTime(LocalTime.of(17, 0))
                    .build(),
                BranchOperatingHoursDto.builder()
                    .id(11L)
                    .branchId(1L)
                    .dayOfWeek(2)
                    .openingTime(LocalTime.of(9, 0))
                    .closingTime(LocalTime.of(17, 0))
                    .build()));

    mockMvc
        .perform(get("/api/admin/branch/1/operating-hours"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].id").value(10L));
  }

  @Test
  void getBranchOperatingHours_returns5xx_onException() throws Exception {
    when(branchOperatingHoursService.getOperatingHoursForBranch(anyLong()))
        .thenThrow(new RuntimeException());

    mockMvc
        .perform(get("/api/admin/branch/1/operating-hours"))
        .andExpect(status().is5xxServerError());
  }

  @Test
  void updateBranchOperatingHours_returns200() throws Exception {
    doNothing().when(branchOperatingHoursService).updateBranchOperatingHour(any());

    UpdateBranchOperatingHoursRequest req =
        UpdateBranchOperatingHoursRequest.builder()
            // adjust these fields to match your real record/builder
            .id(10L)
            .dayOfWeek(1)
            .openingTime(LocalTime.of(9, 0))
            .closingTime(LocalTime.of(17, 0))
            .build();

    mockMvc
        .perform(
            put("/api/admin/branch/operating-hours")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk());

    verify(branchOperatingHoursService).updateBranchOperatingHour(any());
  }

  @Test
  void updateBranchOperatingHours_returns5xx_onException() throws Exception {
    doThrow(new RuntimeException())
        .when(branchOperatingHoursService)
        .updateBranchOperatingHour(any());

    UpdateBranchOperatingHoursRequest req =
        UpdateBranchOperatingHoursRequest.builder()
            .id(10L)
            .dayOfWeek(1)
            .openingTime(LocalTime.of(9, 0))
            .closingTime(LocalTime.of(17, 0))
            .build();

    mockMvc
        .perform(
            put("/api/admin/branch/operating-hours")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().is5xxServerError());
  }

  @Test
  void createBranchOperatingHour_returns200_andJson() throws Exception {
    when(branchOperatingHoursService.createBranchOperatingHour(any()))
        .thenReturn(
            BranchOperatingHoursDto.builder()
                .id(10L)
                .branchId(1L)
                .dayOfWeek(1)
                .openingTime(LocalTime.of(9, 0))
                .closingTime(LocalTime.of(17, 0))
                .build());

    CreateBranchOperatingHoursRequest req =
        CreateBranchOperatingHoursRequest.builder()
            .branchId(1L)
            .dayOfWeek(1)
            .openingTime(LocalTime.of(9, 0))
            .closingTime(LocalTime.of(17, 0))
            .build();

    mockMvc
        .perform(
            post("/api/admin/branch/operating-hours")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(10L));
  }

  @Test
  void createBranchOperatingHour_returns5xx_onException() throws Exception {
    when(branchOperatingHoursService.createBranchOperatingHour(any()))
        .thenThrow(new RuntimeException());

    CreateBranchOperatingHoursRequest req =
        CreateBranchOperatingHoursRequest.builder()
            .branchId(1L)
            .dayOfWeek(1)
            .openingTime(LocalTime.of(9, 0))
            .closingTime(LocalTime.of(17, 0))
            .build();

    mockMvc
        .perform(
            post("/api/admin/branch/operating-hours")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().is5xxServerError());
  }

  @Test
  void updateBranch_returns200_andJson() throws Exception {
    when(branchService.updateBranchAdmin(any()))
        .thenReturn(
            BranchDto.builder()
                .id(1L)
                .name("Updated Branch")
                .suburb("Suburb")
                .city("Cape Town")
                .province("WC")
                .postalCode("8000")
                .country("South Africa")
                .build());

    UpdateBranchRequest req =
        UpdateBranchRequest.builder()
            .id(1L)
            .name("Updated Branch")
            .suburb("Suburb")
            .city("Cape Town")
            .province("WC")
            .postalCode("8000")
            .build();

    mockMvc
        .perform(
            put("/api/admin/branch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.branchId").value(1L))
        .andExpect(jsonPath("$.name").value("Updated Branch"));
  }

  @Test
  void updateBranch_returns5xx_onException() throws Exception {
    when(branchService.updateBranchAdmin(any())).thenThrow(new RuntimeException());

    UpdateBranchRequest req =
        UpdateBranchRequest.builder()
            .id(1L)
            .name("Updated Branch")
            .suburb("Suburb")
            .city("Cape Town")
            .province("WC")
            .postalCode("8000")
            .build();

    mockMvc
        .perform(
            put("/api/admin/branch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().is5xxServerError());
  }

  @Test
  void createBranch_returns200_andJson() throws Exception {
    when(branchService.createBranchAdmin(any()))
        .thenReturn(
            BranchDto.builder()
                .id(1L)
                .name("New Branch")
                .suburb("Suburb")
                .city("Cape Town")
                .province("WC")
                .postalCode("8000")
                .country("South Africa")
                .build());

    CreateBranchRequest req =
        CreateBranchRequest.builder()
            .name("New Branch")
            .suburb("Suburb")
            .city("Cape Town")
            .province("WC")
            .postalCode("8000")
            .country("South Africa")
            .build();

    mockMvc
        .perform(
            post("/api/admin/branch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.branchId").value(1L))
        .andExpect(jsonPath("$.name").value("New Branch"));
  }

  @Test
  void createBranch_returns5xx_onException() throws Exception {
    when(branchService.createBranchAdmin(any())).thenThrow(new RuntimeException());

    CreateBranchRequest req =
        CreateBranchRequest.builder()
            .name("New Branch")
            .suburb("Suburb")
            .city("Cape Town")
            .province("WC")
            .postalCode("8000")
            .country("South Africa")
            .build();

    mockMvc
        .perform(
            post("/api/admin/branch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().is5xxServerError());
  }

  @Test
  void getBookingsForBranchDay_returns200_andJsonArray() throws Exception {
    // If your AppointmentAdminV1Mapper changes field names, adjust jsonPath accordingly.
    when(appointmentService.getBookingsForBranchDay(anyLong(), any()))
        .thenReturn(
            List.of(
                AppointmentDto.builder()
                    .id(1L)
                    .branchId(1L)
                    .appointmentDate(LocalDate.of(2026, 1, 10))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(9, 30))
                    .email("test@test.com")
                    .phoneNumber("123")
                    .name("Test")
                    .reason("Reason")
                    .status(BookingStatus.BOOKED)
                    .build()));

    mockMvc
        .perform(get("/api/admin/branch/1/appointments?date=2026-01-10"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        // Adjust these to your AppointmentResponse fields from the mapper
        .andExpect(jsonPath("$[0].appointmentDate").value("2026-01-10"));
  }

  @Test
  void getBookingsForBranchDay_returns5xx_onException() throws Exception {
    when(appointmentService.getBookingsForBranchDay(anyLong(), any()))
        .thenThrow(new RuntimeException());

    mockMvc
        .perform(get("/api/admin/branch/1/appointments?date=2026-01-10"))
        .andExpect(status().is5xxServerError());
  }
}
