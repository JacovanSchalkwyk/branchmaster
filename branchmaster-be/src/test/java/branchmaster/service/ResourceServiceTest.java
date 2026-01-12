package branchmaster.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import branchmaster.repository.ResourceAvailabilityRepository;
import branchmaster.repository.ResourceUnavailabilityRepository;
import branchmaster.repository.entity.ResourceAvailabilityEntity;
import java.time.LocalDate;
import java.time.LocalTime;
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
    assertThat(saved.getId()).isEqualTo(10L); // <- key point: same id
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
  void deleteResourceAvailability_callsRepoDelete() {
    doNothing().when(availabilityRepo).deleteById(10L);

    service.deleteResourceAvailability(10L);

    verify(availabilityRepo).deleteById(10L);
  }
}
