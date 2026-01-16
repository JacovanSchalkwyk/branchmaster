package branchmaster.admin;

import branchmaster.admin.mapper.AppointmentAdminV1Mapper;
import branchmaster.admin.mapper.BranchAdminV1Mapper;
import branchmaster.admin.mapper.BranchOperatingHoursAdminV1Mapper;
import branchmaster.admin.model.AppointmentResponse;
import branchmaster.admin.model.BranchAdminResponse;
import branchmaster.admin.model.BranchOperatingHoursResponse;
import branchmaster.admin.model.CreateBranchOperatingHoursRequest;
import branchmaster.admin.model.CreateBranchRequest;
import branchmaster.admin.model.UpdateBranchOperatingHoursRequest;
import branchmaster.admin.model.UpdateBranchRequest;
import branchmaster.service.AppointmentService;
import branchmaster.service.BranchOperatingHoursService;
import branchmaster.service.BranchService;
import branchmaster.service.model.AppointmentDto;
import branchmaster.service.model.BranchDto;
import branchmaster.service.model.BranchOperatingHoursDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/admin")
@Slf4j
@RequiredArgsConstructor
@RestController
public class BranchAdminControllerV1 {

  private final BranchService branchService;
  private final BranchOperatingHoursService branchOperatingHoursService;
  private final AppointmentService appointmentService;

  @GetMapping(path = "/branch", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<BranchAdminResponse>> getBranchList() {
    try {
      List<BranchDto> response = branchService.getAllBranches();
      return ResponseEntity.status(HttpStatus.OK).body(BranchAdminV1Mapper.INSTANCE.map(response));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping(path = "/branch/{branchId}/appointments", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<AppointmentResponse>> getBookingsForBranchDay(
      @PathVariable @NotNull @PositiveOrZero Long branchId,
      @RequestParam("date") @NotNull LocalDate date) {
    try {
      List<AppointmentDto> response = appointmentService.getBookingsForBranchDay(branchId, date);

      return ResponseEntity.ok(AppointmentAdminV1Mapper.INSTANCE.map(response));

    } catch (Exception e) {
      log.error("Failed to fetch bookings for branch {} on {}", branchId, date, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping(path = "/branch/{branchId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BranchAdminResponse> getBranchDetails(
      @PathVariable @NotNull @PositiveOrZero Long branchId) {
    try {
      BranchDto response = branchService.getBranchDetailsAdmin(branchId);
      return ResponseEntity.status(HttpStatus.OK).body(BranchAdminV1Mapper.INSTANCE.map(response));

    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping(
      path = "/branch/{branchId}/operating-hours",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<BranchOperatingHoursResponse>> getBranchOperatingHours(
      @PathVariable @NotNull @PositiveOrZero Long branchId) {
    try {
      List<BranchOperatingHoursDto> operatingHoursDtos =
          branchOperatingHoursService.getOperatingHoursForBranch(branchId);
      return ResponseEntity.status(HttpStatus.OK)
          .body(BranchOperatingHoursAdminV1Mapper.INSTANCE.map(operatingHoursDtos));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PutMapping(path = "/branch/operating-hours", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> updateBranchOperatingHours(
      @Valid @RequestBody UpdateBranchOperatingHoursRequest req) {
    try {
      branchOperatingHoursService.updateBranchOperatingHour(req);
      return ResponseEntity.status(HttpStatus.OK).build();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PostMapping(path = "/branch/operating-hours", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BranchOperatingHoursResponse> createBranchOperatingHour(
      @Valid @RequestBody CreateBranchOperatingHoursRequest req) {
    try {
      BranchOperatingHoursDto response = branchOperatingHoursService.createBranchOperatingHour(req);
      return ResponseEntity.status(HttpStatus.OK)
          .body(BranchOperatingHoursAdminV1Mapper.INSTANCE.map(response));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PutMapping(path = "/branch", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BranchAdminResponse> updateBranch(
      @Valid @RequestBody UpdateBranchRequest req) {
    try {
      BranchDto response = branchService.updateBranchAdmin(req);
      return ResponseEntity.status(HttpStatus.OK).body(BranchAdminV1Mapper.INSTANCE.map(response));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PostMapping(path = "/branch", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BranchAdminResponse> createBranch(
      @Valid @RequestBody CreateBranchRequest req) {
    try {
      BranchDto response = branchService.createBranchAdmin(req);
      return ResponseEntity.status(HttpStatus.OK).body(BranchAdminV1Mapper.INSTANCE.map(response));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
