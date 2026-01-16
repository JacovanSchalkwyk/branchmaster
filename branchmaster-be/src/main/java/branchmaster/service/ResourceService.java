package branchmaster.service;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceService {

  private final ResourceAvailabilityRepository resourceAvailabilityRepository;
  private final ResourceUnavailabilityRepository resourceUnavailabilityRepository;

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

    return ResourceMapper.INSTANCE.map(
        resourceAvailabilityRepository.save(resourceAvailabilityEntity));
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

    resourceAvailabilityEntity.setName(name);
    resourceAvailabilityEntity.setStartDate(startDate);
    resourceAvailabilityEntity.setEndDate(endDate);
    resourceAvailabilityEntity.setStartTime(startTime);
    resourceAvailabilityEntity.setEndTime(endTime);
    resourceAvailabilityEntity.setDayOfWeek(dayOfWeek);

    resourceAvailabilityRepository.save(resourceAvailabilityEntity);
  }

  public void deleteResourceAvailability(Long resourceId) {
    try {
      resourceAvailabilityRepository.deleteById(resourceId);
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

    return ResourceMapper.INSTANCE.map(
        resourceUnavailabilityRepository.save(resourceUnavailabilityEntity));
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

    resourceUnavailabilityEntity.setStartTime(startTime);
    resourceUnavailabilityEntity.setEndTime(endTime);
    resourceUnavailabilityEntity.setDate(date);
    resourceUnavailabilityEntity.setAvailableResourceId(availableResourceId);
    resourceUnavailabilityEntity.setReason(reason);

    resourceUnavailabilityRepository.save(resourceUnavailabilityEntity);
  }

  public void deleteResourceUnavailability(Long resourceId) {
    try {

      resourceUnavailabilityRepository.deleteById(resourceId);
    } catch (Exception e) {
      log.error(
          "Something went wrong when deleting resource unavailability, id=[{}]", resourceId, e);
      throw e;
    }
  }
}
