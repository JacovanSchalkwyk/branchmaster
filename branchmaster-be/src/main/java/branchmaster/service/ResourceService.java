package branchmaster.service;

import branchmaster.audit.AdminActionAuditService;
import branchmaster.audit.entity.ActionType;
import branchmaster.repository.ResourceAvailabilityRepository;
import branchmaster.repository.ResourceUnavailabilityRepository;
import branchmaster.repository.entity.ResourceAvailabilityEntity;
import branchmaster.repository.entity.ResourceUnavailabilityEntity;
import branchmaster.service.mapper.ResourceMapper;
import branchmaster.service.model.ResourceAvailabilityDto;
import branchmaster.service.model.ResourceUnavailabilityDto;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceService {

  private final ResourceAvailabilityRepository resourceAvailabilityRepository;
  private final ResourceUnavailabilityRepository resourceUnavailabilityRepository;
  private final AdminActionAuditService auditService;

  public List<ResourceAvailabilityDto> getAvailableResourcesForBranch(Long branchId) {
    List<ResourceAvailabilityEntity> branchEntities =
        resourceAvailabilityRepository.findAllByBranchId(branchId);

    if (branchEntities.isEmpty()) {
      return new ArrayList<>();
    }

    return ResourceMapper.INSTANCE.mapAvailabilities(branchEntities);
  }

  public ResourceAvailabilityDto createResourceAvailability(
      Long branchId,
      LocalTime startTime,
      LocalTime endTime,
      Integer dayOfWeek,
      String name,
      LocalDate startDate,
      LocalDate endDate) {
    ResourceAvailabilityEntity resourceAvailabilityEntity = new ResourceAvailabilityEntity();
    resourceAvailabilityEntity.setBranchId(branchId);
    resourceAvailabilityEntity.setName(name);
    resourceAvailabilityEntity.setStartDate(startDate);
    resourceAvailabilityEntity.setEndDate(endDate);
    resourceAvailabilityEntity.setStartTime(startTime);
    resourceAvailabilityEntity.setEndTime(endTime);
    resourceAvailabilityEntity.setDayOfWeek(dayOfWeek);

    resourceAvailabilityRepository.save(resourceAvailabilityEntity);

    var afterSnapshot = getSnapshot(resourceAvailabilityEntity);

    auditService.log(ActionType.CREATE_RESOURCE_AVAILABILITY, Map.of("after", afterSnapshot));

    return ResourceMapper.INSTANCE.map(resourceAvailabilityEntity);
  }

  public void updateResourceAvailability(
      Long id,
      LocalTime startTime,
      LocalTime endTime,
      Integer dayOfWeek,
      String name,
      LocalDate startDate,
      LocalDate endDate) {
    ResourceAvailabilityEntity resourceAvailabilityEntity =
        resourceAvailabilityRepository.findById(id).orElse(null);

    if (resourceAvailabilityEntity == null) {
      log.error("ResourceAvailabilityEntity with id {} not found", id);
      throw new RuntimeException("Resource not found");
    }

    var beforeSnapshot = getSnapshot(resourceAvailabilityEntity);

    resourceAvailabilityEntity.setName(name);
    resourceAvailabilityEntity.setStartDate(startDate);
    resourceAvailabilityEntity.setEndDate(endDate);
    resourceAvailabilityEntity.setStartTime(startTime);
    resourceAvailabilityEntity.setEndTime(endTime);
    resourceAvailabilityEntity.setDayOfWeek(dayOfWeek);

    resourceAvailabilityRepository.save(resourceAvailabilityEntity);

    var afterSnapshot = getSnapshot(resourceAvailabilityEntity);

    auditService.log(
        ActionType.UPDATE_RESOURCE_AVAILABILITY,
        Map.of("before", beforeSnapshot, "after", afterSnapshot));
  }

  public void deleteResourceAvailability(Long resourceId) {
    try {
      resourceAvailabilityRepository.deleteById(resourceId);

      auditService.log(ActionType.DELETE_RESOURCE_AVAILABILITY, Map.of("id", resourceId));
    } catch (Exception e) {
      log.error("Something went wrong when deleting resource availability, id=[{}]", resourceId, e);
    }
  }

  public List<ResourceUnavailabilityDto> getUnavailableResourcesForBranch(Long branchId) {
    List<ResourceUnavailabilityEntity> entities =
        resourceUnavailabilityRepository.findAllByBranchId(branchId);

    if (entities.isEmpty()) {
      return new ArrayList<>();
    }

    return ResourceMapper.INSTANCE.mapUnavailabilities(entities);
  }

  public ResourceUnavailabilityDto createResourceUnavailability(
      Long branchId,
      LocalTime startTime,
      LocalTime endTime,
      LocalDate date,
      Long availableResourceId,
      String reason) {

    ResourceUnavailabilityEntity resourceUnavailabilityEntity = new ResourceUnavailabilityEntity();
    resourceUnavailabilityEntity.setBranchId(branchId);
    resourceUnavailabilityEntity.setStartTime(startTime);
    resourceUnavailabilityEntity.setEndTime(endTime);
    resourceUnavailabilityEntity.setDate(date);
    resourceUnavailabilityEntity.setAvailableResourceId(availableResourceId);
    resourceUnavailabilityEntity.setReason(reason);

    resourceUnavailabilityRepository.save(resourceUnavailabilityEntity);

    var afterSnapshot = getSnapshot(resourceUnavailabilityEntity);

    auditService.log(ActionType.CREATE_RESOURCE_UNAVAILABILITY, Map.of("after", afterSnapshot));

    return ResourceMapper.INSTANCE.map(resourceUnavailabilityEntity);
  }

  public void updateResourceUnavailability(
      Long id,
      LocalTime startTime,
      LocalTime endTime,
      LocalDate date,
      Long availableResourceId,
      String reason) {
    ResourceUnavailabilityEntity resourceUnavailabilityEntity =
        resourceUnavailabilityRepository.findById(id).orElse(null);

    if (resourceUnavailabilityEntity == null) {
      log.error("ResourceUnavailabilityEntity with id {} not found", id);
      throw new RuntimeException("Resource not found");
    }

    var beforeSnapshot = getSnapshot(resourceUnavailabilityEntity);

    resourceUnavailabilityEntity.setStartTime(startTime);
    resourceUnavailabilityEntity.setEndTime(endTime);
    resourceUnavailabilityEntity.setDate(date);
    resourceUnavailabilityEntity.setAvailableResourceId(availableResourceId);
    resourceUnavailabilityEntity.setReason(reason);

    resourceUnavailabilityRepository.save(resourceUnavailabilityEntity);

    var afterSnapshot = getSnapshot(resourceUnavailabilityEntity);

    auditService.log(
        ActionType.CREATE_RESOURCE_UNAVAILABILITY,
        Map.of("before", beforeSnapshot, "after", afterSnapshot));
  }

  public void deleteResourceUnavailability(Long resourceId) {
    try {
      resourceUnavailabilityRepository.deleteById(resourceId);

      auditService.log(ActionType.DELETE_RESOURCE_UNAVAILABILITY, Map.of("id", resourceId));
    } catch (Exception e) {
      log.error(
          "Something went wrong when deleting resource unavailability, id=[{}]", resourceId, e);
      throw e;
    }
  }

  private Object getSnapshot(ResourceAvailabilityEntity resourceAvailabilityEntity) {
    return Map.of(
        "id", resourceAvailabilityEntity.getId(),
        "branchId", resourceAvailabilityEntity.getBranchId(),
        "name", resourceAvailabilityEntity.getName(),
        "startDate", resourceAvailabilityEntity.getStartDate(),
        "endDate", resourceAvailabilityEntity.getEndDate(),
        "startTime", resourceAvailabilityEntity.getStartTime(),
        "endTime", resourceAvailabilityEntity.getEndTime(),
        "dayOfWeek", resourceAvailabilityEntity.getDayOfWeek());
  }

  private Object getSnapshot(ResourceUnavailabilityEntity resourceUnavailabilityEntity) {
    return Map.of(
        "id", resourceUnavailabilityEntity.getId(),
        "branchId", resourceUnavailabilityEntity.getBranchId(),
        "date", resourceUnavailabilityEntity.getDate(),
        "startTime", resourceUnavailabilityEntity.getStartTime(),
        "endTime", resourceUnavailabilityEntity.getEndTime(),
        "availableResourceId", resourceUnavailabilityEntity.getAvailableResourceId(),
        "reason", resourceUnavailabilityEntity.getReason());
  }
}
