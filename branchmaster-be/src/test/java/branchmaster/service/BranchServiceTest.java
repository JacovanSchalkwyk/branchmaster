package branchmaster.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import branchmaster.admin.model.CreateBranchRequest;
import branchmaster.admin.model.UpdateBranchRequest;
import branchmaster.repository.BranchRepository;
import branchmaster.repository.entity.BranchEntity;
import branchmaster.service.model.BranchDto;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

  @Mock BranchRepository branchRepository;

  @InjectMocks BranchService service;

  @Test
  void getAllOpenBranches_throws_whenRepoReturnsEmpty() {
    when(branchRepository.getAllActiveBranchesSorted()).thenReturn(List.of());

    assertThatThrownBy(() -> service.getAllOpenBranches())
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No active branches found");

    verify(branchRepository).getAllActiveBranchesSorted();
  }

  @Test
  void getAllOpenBranches_returnsMappedDtos_whenRepoReturnsEntities() {
    BranchEntity b1 = new BranchEntity();
    b1.setId(1L);
    b1.setName("Branch 1");
    b1.setActive(true);

    BranchEntity b2 = new BranchEntity();
    b2.setId(2L);
    b2.setName("Branch 2");
    b2.setActive(true);

    when(branchRepository.getAllActiveBranchesSorted()).thenReturn(List.of(b1, b2));

    List<BranchDto> result = service.getAllOpenBranches();

    assertThat(result).isNotNull();
    assertThat(result).hasSize(2);
    assertThat(result).extracting(BranchDto::id).containsExactlyInAnyOrder(1L, 2L);

    verify(branchRepository).getAllActiveBranchesSorted();
  }

  @Test
  void getAllBranches_throws_whenRepoReturnsEmpty() {
    when(branchRepository.getAllBranchesSorted()).thenReturn(List.of());

    assertThatThrownBy(() -> service.getAllBranches())
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No branches found");

    verify(branchRepository).getAllBranchesSorted();
  }

  @Test
  void getAllBranches_returnsMappedDtos_whenRepoReturnsEntities() {
    BranchEntity b1 = new BranchEntity();
    b1.setId(1L);
    b1.setName("Branch 1");

    BranchEntity b2 = new BranchEntity();
    b2.setId(2L);
    b2.setName("Branch 2");

    when(branchRepository.getAllBranchesSorted()).thenReturn(List.of(b1, b2));

    List<BranchDto> result = service.getAllBranches();

    assertThat(result).isNotNull();
    assertThat(result).hasSize(2);
    assertThat(result).extracting(BranchDto::id).containsExactlyInAnyOrder(1L, 2L);

    verify(branchRepository).getAllBranchesSorted();
  }

  @Test
  void getBranchDetailsAdmin_throws_whenNotFound() {
    when(branchRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getBranchDetailsAdmin(999L))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No branch found");

    verify(branchRepository).findById(999L);
  }

  @Test
  void getBranchDetailsAdmin_returnsDto_whenFound() {
    BranchEntity b = new BranchEntity();
    b.setId(10L);
    b.setName("Sea Point");
    b.setCity("Cape Town");

    when(branchRepository.findById(10L)).thenReturn(Optional.of(b));

    BranchDto result = service.getBranchDetailsAdmin(10L);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(10L);

    verify(branchRepository).findById(10L);
  }

  @Test
  void updateBranchAdmin_throws_whenNotFound() {
    UpdateBranchRequest req =
        UpdateBranchRequest.builder()
            .id(999L)
            .name("New Name")
            .address("1 Main Rd")
            .suburb("Sea Point")
            .city("Cape Town")
            .province("Western Cape")
            .postalCode("8005")
            .active(true)
            .timeslotLength(30)
            .latitude(-33.9)
            .longitude(18.4)
            .build();

    when(branchRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.updateBranchAdmin(req))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("No branch found");

    verify(branchRepository).findById(999L);
    verify(branchRepository, never()).save(any());
  }

  @Test
  void updateBranchAdmin_updatesExistingRow_andReturnsDto() {
    BranchEntity existing = new BranchEntity();
    existing.setId(10L);
    existing.setName("Old Name");

    UpdateBranchRequest req =
        UpdateBranchRequest.builder()
            .id(10L)
            .name("New Name")
            .address("123 Beach Rd")
            .suburb("Sea Point")
            .city("Cape Town")
            .province("Western Cape")
            .postalCode("8005")
            .active(true)
            .timeslotLength(20)
            .latitude(-33.915)
            .longitude(18.389)
            .build();

    when(branchRepository.findById(10L)).thenReturn(Optional.of(existing));
    when(branchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    BranchDto result = service.updateBranchAdmin(req);

    ArgumentCaptor<BranchEntity> captor = ArgumentCaptor.forClass(BranchEntity.class);
    verify(branchRepository).save(captor.capture());

    BranchEntity saved = captor.getValue();
    assertThat(saved.getId()).isEqualTo(10L);
    assertThat(saved.getName()).isEqualTo("New Name");
    assertThat(saved.getAddress()).isEqualTo("123 Beach Rd");
    assertThat(saved.getSuburb()).isEqualTo("Sea Point");
    assertThat(saved.getCity()).isEqualTo("Cape Town");
    assertThat(saved.getProvince()).isEqualTo("Western Cape");
    assertThat(saved.getPostalCode()).isEqualTo("8005");
    assertThat(saved.getActive()).isEqualTo(true);
    assertThat(saved.getTimeslotLength()).isEqualTo(20);
    assertThat(saved.getLatitude()).isEqualTo(-33.915);
    assertThat(saved.getLongitude()).isEqualTo(18.389);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(10L);

    verify(branchRepository).findById(10L);
  }

  @Test
  void createBranchAdmin_savesEntity_andReturnsDto() {
    CreateBranchRequest req =
        CreateBranchRequest.builder()
            .name("New Branch")
            .address("1 Main Rd")
            .suburb("Claremont")
            .city("Cape Town")
            .province("Western Cape")
            .postalCode("7708")
            .country("South Africa")
            .active(true)
            .timeslotLength(15)
            .latitude(-33.98)
            .longitude(18.46)
            .build();

    when(branchRepository.save(any()))
        .thenAnswer(
            inv -> {
              BranchEntity e = inv.getArgument(0);
              e.setId(99L);
              return e;
            });

    BranchDto dto = service.createBranchAdmin(req);

    ArgumentCaptor<BranchEntity> captor = ArgumentCaptor.forClass(BranchEntity.class);
    verify(branchRepository).save(captor.capture());

    BranchEntity saved = captor.getValue();
    assertThat(saved.getId()).isEqualTo(99L);
    assertThat(saved.getName()).isEqualTo("New Branch");
    assertThat(saved.getAddress()).isEqualTo("1 Main Rd");
    assertThat(saved.getSuburb()).isEqualTo("Claremont");
    assertThat(saved.getCity()).isEqualTo("Cape Town");
    assertThat(saved.getProvince()).isEqualTo("Western Cape");
    assertThat(saved.getPostalCode()).isEqualTo("7708");
    assertThat(saved.getCountry()).isEqualTo("South Africa");
    assertThat(saved.getActive()).isEqualTo(true);
    assertThat(saved.getTimeslotLength()).isEqualTo(15);
    assertThat(saved.getLatitude()).isEqualTo(-33.98);
    assertThat(saved.getLongitude()).isEqualTo(18.46);

    assertThat(dto).isNotNull();
    assertThat(dto.id()).isEqualTo(99L);
  }
}
