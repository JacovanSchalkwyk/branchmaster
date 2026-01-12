package branchmaster.controller.v1;

import branchmaster.controller.v1.mapper.AppointmentV1Mapper;
import branchmaster.controller.v1.model.CreateAppointmentRequest;
import branchmaster.controller.v1.model.CreateAppointmentResponse;
import branchmaster.service.AppointmentService;
import branchmaster.service.model.AppointmentDto;
import branchmaster.service.model.Timeslot;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/appointment")
@Slf4j
@RequiredArgsConstructor
@RestController
public class AppointmentControllerV1 {

  private final AppointmentService appointmentService;

  @GetMapping(path = "available/{branchId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<LocalDate, List<Timeslot>>> getAvailableAppointmentsForBranch(
      @PathVariable Long branchId,
      @RequestParam("startDate") LocalDate startDate,
      @RequestParam("endDate") LocalDate endDate) {
    try {
      Map<LocalDate, List<Timeslot>> response =
          appointmentService.getAvailableAppointments(branchId, startDate, endDate);

      return ResponseEntity.status(HttpStatus.OK).body(response);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw e;
    }
  }

  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<CreateAppointmentResponse> create(
      @Valid @RequestBody CreateAppointmentRequest req) {
    try {
      AppointmentDto created = appointmentService.createAppointment(req);

      return ResponseEntity.status(HttpStatus.OK).body(AppointmentV1Mapper.INSTANCE.map(created));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw e;
    }
  }

  @DeleteMapping(path = "/{bookingId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> cancelBooking(@PathVariable("bookingId") Long bookingId) {
    try {
      appointmentService.cancelAppointment(bookingId);

      return ResponseEntity.status(HttpStatus.OK).build();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw e;
    }
  }
}
