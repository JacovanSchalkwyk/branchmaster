package branchmaster.admin;

import branchmaster.admin.mapper.ResourceAdminV1Mapper;
import branchmaster.admin.model.CreateResourceAvailabilityRequest;
import branchmaster.admin.model.CreateResourceUnavailabilityRequest;
import branchmaster.admin.model.ResourceAvailabilityResponse;
import branchmaster.admin.model.ResourceUnavailabilityResponse;
import branchmaster.admin.model.UpdateResourceAvailabilityRequest;
import branchmaster.admin.model.UpdateResourceUnavailabilityRequest;
import branchmaster.service.ResourceService;
import branchmaster.service.model.ResourceAvailabilityDto;
import branchmaster.service.model.ResourceUnavailabilityDto;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/admin")
@Slf4j
@RequiredArgsConstructor
@RestController
public class ResourceAdminControllerV1 {

  private final ResourceService resourceService;

  @GetMapping(path = "/resource/available/{branchId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<ResourceAvailabilityResponse>> getAvailableResourcesForBranch(
      @PathVariable Long branchId) {
    try {
      List<ResourceAvailabilityDto> resources =
          resourceService.getAvailableResourcesForBranch(branchId);
      return ResponseEntity.status(HttpStatus.OK)
          .body(ResourceAdminV1Mapper.INSTANCE.mapAvailabilities(resources));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PostMapping(path = "/resource/available", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ResourceAvailabilityResponse> createResourceAvailability(
      @Valid @RequestBody CreateResourceAvailabilityRequest req) {
    try {
      ResourceAvailabilityDto created =
          resourceService.createResourceAvailability(
              req.branchId(),
              req.startTime(),
              req.endTime(),
              req.dayOfWeek(),
              req.name(),
              req.startDate(),
              req.endDate());

      return ResponseEntity.status(HttpStatus.OK).body(ResourceAdminV1Mapper.INSTANCE.map(created));

    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PutMapping(path = "/resource/available", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> updateResourceAvailability(
      @Valid @RequestBody UpdateResourceAvailabilityRequest req) {
    try {
      resourceService.updateResourceAvailability(
          req.id(),
          req.startTime(),
          req.endTime(),
          req.dayOfWeek(),
          req.name(),
          req.startDate(),
          req.endDate());

      return ResponseEntity.status(HttpStatus.OK).build();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @DeleteMapping(
      path = "/resource/available/{resourceId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> deleteAvailableResourcesForBranch(@PathVariable Long resourceId) {
    try {
      resourceService.deleteResourceAvailability(resourceId);
      return ResponseEntity.status(HttpStatus.OK).build();

    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping(
      path = "/resource/unavailable/{branchId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<ResourceUnavailabilityResponse>> getUnavailableResourcesForBranch(
      @PathVariable Long branchId) {
    try {
      List<ResourceUnavailabilityDto> resources =
          resourceService.getUnavailableResourcesForBranch(branchId);
      return ResponseEntity.status(HttpStatus.OK)
          .body(ResourceAdminV1Mapper.INSTANCE.map(resources));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PostMapping(path = "/resource/unavailable", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ResourceUnavailabilityResponse> createResourceUnavailability(
      @Valid @RequestBody CreateResourceUnavailabilityRequest req) {
    try {
      ResourceUnavailabilityDto created =
          resourceService.createResourceUnavailability(
              req.branchId(),
              req.startTime(),
              req.endTime(),
              req.date(),
              req.availableResourceId(),
              req.reason());

      return ResponseEntity.status(HttpStatus.OK).body(ResourceAdminV1Mapper.INSTANCE.map(created));

    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PutMapping(path = "/resource/unavailable", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> updateResourceUnavailability(
      @Valid @RequestBody UpdateResourceUnavailabilityRequest req) {
    try {
      resourceService.updateResourceUnavailability(
          req.id(),
          req.startTime(),
          req.endTime(),
          req.date(),
          req.availableResourceId(),
          req.reason());

      return ResponseEntity.status(HttpStatus.OK).build();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @DeleteMapping(
      path = "/resource/unavailable/{resourceId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> deleteResourceUnavailability(@PathVariable Long resourceId) {
    try {
      resourceService.deleteResourceUnavailability(resourceId);
      return ResponseEntity.status(HttpStatus.OK).build();

    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
