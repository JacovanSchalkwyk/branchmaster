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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import branchmaster.controller.v1.AppointmentControllerV1;
import branchmaster.controller.v1.model.CreateAppointmentRequest;
import branchmaster.repository.entity.BookingStatus;
import branchmaster.security.JwtService;
import branchmaster.service.AppointmentService;
import branchmaster.service.model.AppointmentDto;
import branchmaster.service.model.AvailabilityStatus;
import branchmaster.service.model.Timeslot;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AppointmentControllerV1.class)
@AutoConfigureMockMvc(addFilters = false)
class AppointmentControllerV1Test {

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @MockBean AppointmentService appointmentService;
  @MockBean JwtService jwtService;

  @Test
  void getAvailableAppointments_returns200_andJson() throws Exception {
    when(appointmentService.getAvailableAppointments(anyLong(), any(), any()))
        .thenReturn(
            Map.of(
                LocalDate.of(2026, 1, 10),
                List.of(
                    new Timeslot(
                        LocalTime.of(9, 0), LocalTime.of(9, 30), AvailabilityStatus.AVAILABLE),
                    new Timeslot(
                        LocalTime.of(10, 0), LocalTime.of(10, 30), AvailabilityStatus.AVAILABLE))));

    mockMvc
        .perform(get("/api/v1/appointment/available/1?startDate=2026-01-01&endDate=2026-02-02"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.['2026-01-10'][0].startTime").value("09:00:00"));
  }

  @Test
  void getAvailableAppointments_returns5xx() throws Exception {
    when(appointmentService.getAvailableAppointments(anyLong(), any(), any()))
        .thenThrow(RuntimeException.class);

    mockMvc
        .perform(get("/api/v1/appointment/available/1?startDate=2026-01-01&endDate=2026-02-02"))
        .andExpect(status().is5xxServerError());
  }

  @Test
  void createAppointment_returns200_andJson() throws Exception {
    when(appointmentService.createAppointment(any()))
        .thenReturn(
            AppointmentDto.builder()
                .id(1L)
                .branchId(2L)
                .appointmentDate(LocalDate.of(2026, 1, 10))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .createdAt(LocalDateTime.now())
                .reason("Test reason")
                .email("test@gmail.com")
                .phoneNumber("1234567890")
                .name("test name")
                .status(BookingStatus.BOOKED)
                .build());

    CreateAppointmentRequest request =
        CreateAppointmentRequest.builder()
            .branchId(1L)
            .appointmentDate(LocalDate.of(2027, 1, 10))
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(9, 30))
            .reason("Test reason")
            .email("test@test.com")
            .phoneNumber("123456789")
            .name("test name")
            .build();

    mockMvc
        .perform(
            post("/api/v1/appointment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.appointmentId").value(1L))
        .andExpect(jsonPath("$.status").value("BOOKED"));
  }

  @Test
  void createAppointment_returns5xx() throws Exception {
    when(appointmentService.createAppointment(any())).thenThrow(RuntimeException.class);
    CreateAppointmentRequest request =
        CreateAppointmentRequest.builder()
            .branchId(1L)
            .appointmentDate(LocalDate.of(2027, 1, 10))
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(9, 30))
            .reason("Test reason")
            .email("test@test.com")
            .phoneNumber("123456789")
            .name("test name")
            .build();

    mockMvc
        .perform(
            post("/api/v1/appointment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().is5xxServerError());
  }

  @Test
  void deleteAppointment_returns200_andJson() throws Exception {
    doNothing().when(appointmentService).cancelAppointment(any());

    mockMvc.perform(delete("/api/v1/appointment/1")).andExpect(status().isOk());
    verify(appointmentService).cancelAppointment(1L);
  }

  @Test
  void deleteAppointment_returns5xx() throws Exception {
    doThrow(new RuntimeException("Appointment not found"))
        .when(appointmentService)
        .cancelAppointment(1L);

    mockMvc.perform(delete("/api/v1/appointment/1")).andExpect(status().is5xxServerError());
    verify(appointmentService).cancelAppointment(1L);
  }
}
