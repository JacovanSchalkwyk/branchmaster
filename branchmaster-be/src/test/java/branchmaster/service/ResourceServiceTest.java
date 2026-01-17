package branchmaster.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import branchmaster.audit.AdminActionAuditService;
import branchmaster.repository.ResourceAvailabilityRepository;
import branchmaster.repository.ResourceUnavailabilityRepository;
import branchmaster.repository.entity.ResourceAvailabilityEntity;
import branchmaster.repository.entity.ResourceUnavailabilityEntity;
import branchmaster.service.model.ResourceAvailabilityDto;
import branchmaster.service.model.ResourceUnavailabilityDto;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

  @Mock ResourceAvailabilityRepository availabilityRepo;
  @Mock ResourceUnavailabilityRepository unavailabilityRepo;

  @InjectMocks ResourceService service;
  @Mock AdminActionAuditService auditService;

  @Test
  void getAvailableResourcesForBranch_returnsEmptyList_whenRepoReturnsEmpty() {
    when(availabilityRepo.findAllByBranchId(1L)).thenReturn(List.of());

    List<ResourceAvailabilityDto> result = service.getAvailableResourcesForBranch(1L);

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
    verify(availabilityRepo).findAllByBranchId(1L);
  }

  @Test
  void getUnavailableResourcesForBranch_returnsMappedDtos_whenRepoReturnsEntities() {
    ResourceUnavailabilityEntity e1 = new ResourceUnavailabilityEntity();
    e1.setId(10L);
    e1.setBranchId(1L);
    e1.setAvailableResourceId(1L);
    e1.setDate(LocalDate.of(2026, 1, 1));
    e1.setStartTime(LocalTime.of(9, 0));
    e1.setEndTime(LocalTime.of(17, 0));

    when(unavailabilityRepo.findAllByBranchId(1L)).thenReturn(List.of(e1));

    List<ResourceUnavailabilityDto> result = service.getUnavailableResourcesForBranch(1L);

    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);

    assertThat(result.getFirst().id()).isIn(10L);
    assertThat(result)
        .extracting(ResourceUnavailabilityDto::availableResourceId)
        .containsExactlyInAnyOrder(1L);

    verify(unavailabilityRepo).findAllByBranchId(1L);
  }

  @Test
  void getUnavailableResourcesForBranch_returnsEmptyList_whenRepoReturnsEmpty() {
    when(unavailabilityRepo.findAllByBranchId(1L)).thenReturn(List.of());

    List<ResourceUnavailabilityDto> result = service.getUnavailableResourcesForBranch(1L);

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
    verify(unavailabilityRepo).findAllByBranchId(1L);
  }

  @Test
  void getAvailableResourcesForBranch_returnsMappedDtos_whenRepoReturnsEntities() {
    ResourceAvailabilityEntity e1 = new ResourceAvailabilityEntity();
    e1.setId(10L);
    e1.setBranchId(1L);
    e1.setName("Jaco");
    e1.setDayOfWeek(2);
    e1.setStartTime(LocalTime.of(9, 0));
    e1.setEndTime(LocalTime.of(17, 0));
    e1.setStartDate(LocalDate.of(2026, 1, 1));
    e1.setEndDate(LocalDate.of(2026, 12, 31));

    ResourceAvailabilityEntity e2 = new ResourceAvailabilityEntity();
    e2.setId(11L);
    e2.setBranchId(1L);
    e2.setName("Amy");
    e2.setDayOfWeek(3);
    e2.setStartTime(LocalTime.of(8, 0));
    e2.setEndTime(LocalTime.of(16, 0));
    e2.setStartDate(LocalDate.of(2026, 2, 1));
    e2.setEndDate(LocalDate.of(2026, 11, 30));

    when(availabilityRepo.findAllByBranchId(1L)).thenReturn(List.of(e1, e2));

    List<ResourceAvailabilityDto> result = service.getAvailableResourcesForBranch(1L);

    assertThat(result).isNotNull();
    assertThat(result).hasSize(2);

    assertThat(result.getFirst().id()).isIn(10L, 11L);
    assertThat(result)
        .extracting(ResourceAvailabilityDto::name)
        .containsExactlyInAnyOrder("Jaco", "Amy");

    verify(availabilityRepo).findAllByBranchId(1L);
  }

  @Test
  void updateResourceAvailability_updatesExistingRow_notCreatesNew() {
    var existing = new ResourceAvailabilityEntity();
    existing.setId(10L);
    existing.setBranchId(1L);

    when(availabilityRepo.findById(10L)).thenReturn(Optional.of(existing));
    when(availabilityRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    service.updateResourceAvailability(
        10L,
        LocalTime.of(9, 0),
        LocalTime.of(17, 0),
        2,
        "Jaco",
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 12, 31));

    ArgumentCaptor<ResourceAvailabilityEntity> captor =
        ArgumentCaptor.forClass(ResourceAvailabilityEntity.class);
    verify(availabilityRepo).save(captor.capture());

    var saved = captor.getValue();
    assertThat(saved.getId()).isEqualTo(10L);
    assertThat(saved.getDayOfWeek()).isEqualTo(2);
    assertThat(saved.getStartTime()).isEqualTo(LocalTime.of(9, 0));
    assertThat(saved.getEndTime()).isEqualTo(LocalTime.of(17, 0));
    assertThat(saved.getName()).isEqualTo("Jaco");
    assertThat(saved.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
    assertThat(saved.getEndDate()).isEqualTo(LocalDate.of(2026, 12, 31));
  }

  @Test
  void updateResourceAvailability_throws_whenNotFound() {
    when(availabilityRepo.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                service.updateResourceAvailability(
                    999L,
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0),
                    2,
                    "Jaco",
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 12, 31)))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Resource not found");

    verify(availabilityRepo, never()).save(any());
  }

  @Test
  void createResourceAvailability_savesEntity_andReturnsDto() {
    when(availabilityRepo.save(any()))
        .thenAnswer(
            inv -> {
              ResourceAvailabilityEntity e = inv.getArgument(0);
              e.setId(99L);
              return e;
            });

    ResourceAvailabilityDto dto =
        service.createResourceAvailability(
            1L,
            LocalTime.of(9, 0),
            LocalTime.of(10, 0),
            1,
            "test name",
            LocalDate.of(2026, 1, 10),
            LocalDate.of(2027, 1, 10));

    ArgumentCaptor<ResourceAvailabilityEntity> captor =
        ArgumentCaptor.forClass(ResourceAvailabilityEntity.class);
    verify(availabilityRepo).save(captor.capture());

    ResourceAvailabilityEntity saved = captor.getValue();
    assertThat(saved.getBranchId()).isEqualTo(1L);
    assertThat(saved.getStartTime()).isEqualTo(LocalTime.of(9, 0));
    assertThat(saved.getEndTime()).isEqualTo(LocalTime.of(10, 0));

    assertThat(dto).isNotNull();
  }

  @Test
  void deleteResourceAvailability_callsRepoDelete() {
    doNothing().when(availabilityRepo).deleteById(10L);

    service.deleteResourceAvailability(10L);

    verify(availabilityRepo).deleteById(10L);
  }

  @Test
  void createResourceUnavailability_savesEntity_andReturnsDto() {
    when(unavailabilityRepo.save(any()))
        .thenAnswer(
            inv -> {
              ResourceUnavailabilityEntity e = inv.getArgument(0);
              e.setId(99L);
              return e;
            });

    ResourceUnavailabilityDto dto =
        service.createResourceUnavailability(
            1L,
            LocalTime.of(9, 0),
            LocalTime.of(10, 0),
            LocalDate.of(2026, 1, 10),
            10L,
            "Sick leave");

    ArgumentCaptor<ResourceUnavailabilityEntity> captor =
        ArgumentCaptor.forClass(ResourceUnavailabilityEntity.class);
    verify(unavailabilityRepo).save(captor.capture());

    ResourceUnavailabilityEntity saved = captor.getValue();
    assertThat(saved.getBranchId()).isEqualTo(1L);
    assertThat(saved.getStartTime()).isEqualTo(LocalTime.of(9, 0));
    assertThat(saved.getEndTime()).isEqualTo(LocalTime.of(10, 0));
    assertThat(saved.getDate()).isEqualTo(LocalDate.of(2026, 1, 10));
    assertThat(saved.getAvailableResourceId()).isEqualTo(10L);
    assertThat(saved.getReason()).isEqualTo("Sick leave");

    assertThat(dto).isNotNull();
  }

  @Test
  void updateResourceUnavailability_updatesExistingRow() {
    ResourceUnavailabilityEntity existing = new ResourceUnavailabilityEntity();
    existing.setId(10L);
    existing.setBranchId(1L);
    existing.setAvailableResourceId(5L);

    when(unavailabilityRepo.findById(10L)).thenReturn(Optional.of(existing));
    when(unavailabilityRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    service.updateResourceUnavailability(
        10L, LocalTime.of(11, 0), LocalTime.of(12, 0), LocalDate.of(2026, 2, 2), 77L, "Training");

    ArgumentCaptor<ResourceUnavailabilityEntity> captor =
        ArgumentCaptor.forClass(ResourceUnavailabilityEntity.class);
    verify(unavailabilityRepo).save(captor.capture());

    ResourceUnavailabilityEntity saved = captor.getValue();
    assertThat(saved.getId()).isEqualTo(10L);
    assertThat(saved.getStartTime()).isEqualTo(LocalTime.of(11, 0));
    assertThat(saved.getEndTime()).isEqualTo(LocalTime.of(12, 0));
    assertThat(saved.getDate()).isEqualTo(LocalDate.of(2026, 2, 2));
    assertThat(saved.getAvailableResourceId()).isEqualTo(77L);
    assertThat(saved.getReason()).isEqualTo("Training");
  }

  @Test
  void deleteResourceUnavailability_callsDelete() {
    doNothing().when(unavailabilityRepo).deleteById(10L);

    service.deleteResourceUnavailability(10L);

    verify(unavailabilityRepo).deleteById(10L);
  }

  @Test
  void deleteResourceUnavailability_throws_whenRepoThrows() {
    doThrow(new RuntimeException("DB down")).when(unavailabilityRepo).deleteById(11L);

    assertThatThrownBy(() -> service.deleteResourceUnavailability(11L))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("DB down");

    verify(unavailabilityRepo).deleteById(11L);
  }
}
