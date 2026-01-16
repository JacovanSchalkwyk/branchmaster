package branchmaster.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import branchmaster.admin.model.CreateBranchOperatingHoursRequest;
import branchmaster.admin.model.UpdateBranchOperatingHoursRequest;
import branchmaster.audit.AdminActionAuditService;
import branchmaster.audit.entity.ActionType;
import branchmaster.repository.BranchOperatingHoursRepository;
import branchmaster.repository.entity.BranchOperatingHoursEntity;
import branchmaster.security.StaffAuthUtil;
import branchmaster.service.model.BranchOperatingHoursDto;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BranchOperatingHoursServiceTest {

  @Mock BranchOperatingHoursRepository repo;
  @Mock AdminActionAuditService auditService;

  @InjectMocks BranchOperatingHoursService service;

  @Test
  void getOperatingHoursForBranch_returnsEmptyList_whenRepoReturnsEmpty() {
    when(repo.findByBranchId(1L)).thenReturn(List.of());

    List<BranchOperatingHoursDto> result = service.getOperatingHoursForBranch(1L);

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
    verify(repo).findByBranchId(1L);
    verifyNoInteractions(auditService);
  }

  @Test
  void getOperatingHoursForBranch_returnsMappedDtos_whenRepoReturnsEntities() {
    BranchOperatingHoursEntity e1 = new BranchOperatingHoursEntity();
    e1.setId(10L);
    e1.setBranchId(1L);
    e1.setDayOfWeek(1);
    e1.setOpeningTime(LocalTime.of(9, 0));
    e1.setClosingTime(LocalTime.of(17, 0));
    e1.setClosed(false);

    when(repo.findByBranchId(1L)).thenReturn(List.of(e1));

    List<BranchOperatingHoursDto> result = service.getOperatingHoursForBranch(1L);

    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().id()).isEqualTo(10L);

    verify(repo).findByBranchId(1L);
    verifyNoInteractions(auditService);
  }

  @Test
  void updateBranchOperatingHour_throws_whenNotFound() {
    UpdateBranchOperatingHoursRequest req =
        UpdateBranchOperatingHoursRequest.builder()
            .id(999L)
            .dayOfWeek(2)
            .openingTime(LocalTime.of(8, 0))
            .closingTime(LocalTime.of(16, 0))
            .closed(false)
            .build();

    when(repo.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.updateBranchOperatingHour(req))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Branch operating hours not found");

    verify(repo).findById(999L);
    verify(repo, never()).save(any());
    verifyNoInteractions(auditService);
  }

  @Test
  void updateBranchOperatingHour_updatesEntity_saves_andAudits() {
    BranchOperatingHoursEntity existing = new BranchOperatingHoursEntity();
    existing.setId(10L);
    existing.setBranchId(1L);
    existing.setDayOfWeek(1);
    existing.setOpeningTime(LocalTime.of(9, 0));
    existing.setClosingTime(LocalTime.of(17, 0));
    existing.setClosed(false);

    UpdateBranchOperatingHoursRequest req =
        UpdateBranchOperatingHoursRequest.builder()
            .id(10L)
            .dayOfWeek(5)
            .openingTime(LocalTime.of(10, 0))
            .closingTime(LocalTime.of(18, 0))
            .closed(true)
            .build();

    when(repo.findById(10L)).thenReturn(Optional.of(existing));
    when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    try (MockedStatic<StaffAuthUtil> mocked = mockStatic(StaffAuthUtil.class)) {
      mocked.when(StaffAuthUtil::getStaffId).thenReturn(777L);

      service.updateBranchOperatingHour(req);

      ArgumentCaptor<BranchOperatingHoursEntity> entityCaptor =
          ArgumentCaptor.forClass(BranchOperatingHoursEntity.class);
      verify(repo).save(entityCaptor.capture());

      BranchOperatingHoursEntity saved = entityCaptor.getValue();
      assertThat(saved.getId()).isEqualTo(10L);
      assertThat(saved.getBranchId()).isEqualTo(1L);
      assertThat(saved.getDayOfWeek()).isEqualTo(5);
      assertThat(saved.getOpeningTime()).isEqualTo(LocalTime.of(10, 0));
      assertThat(saved.getClosingTime()).isEqualTo(LocalTime.of(18, 0));
      assertThat(saved.getClosed()).isTrue();

      @SuppressWarnings("unchecked")
      ArgumentCaptor<Map<String, Object>> auditCaptor = ArgumentCaptor.forClass(Map.class);

      verify(auditService)
          .log(eq(777L), eq(ActionType.OPERATING_HOURS_UPDATED), auditCaptor.capture());

      Map<String, Object> payload = auditCaptor.getValue();
      assertThat(payload.get("operatingHoursId")).isEqualTo(10L);
      assertThat(payload.get("branchId")).isEqualTo(1L);

      assertThat(payload.get("before")).isInstanceOf(Map.class);
      assertThat(payload.get("after")).isInstanceOf(Map.class);

      @SuppressWarnings("unchecked")
      Map<String, Object> before = (Map<String, Object>) payload.get("before");
      @SuppressWarnings("unchecked")
      Map<String, Object> after = (Map<String, Object>) payload.get("after");

      assertThat(before.get("dayOfWeek")).isEqualTo(1);
      assertThat(before.get("openingTime")).isEqualTo("09:00");
      assertThat(before.get("closingTime")).isEqualTo("17:00");
      assertThat(before.get("closed")).isEqualTo(false);

      assertThat(after.get("dayOfWeek")).isEqualTo(5);
      assertThat(after.get("openingTime")).isEqualTo("10:00");
      assertThat(after.get("closingTime")).isEqualTo("18:00");
      assertThat(after.get("closed")).isEqualTo(true);

      mocked.verify(StaffAuthUtil::getStaffId);
    }

    verify(repo).findById(10L);
  }

  @Test
  void createBranchOperatingHour_savesEntity_setsClosedFalse_andReturnsDto() {
    CreateBranchOperatingHoursRequest req =
        CreateBranchOperatingHoursRequest.builder()
            .branchId(1L)
            .dayOfWeek(2)
            .openingTime(LocalTime.of(9, 0))
            .closingTime(LocalTime.of(17, 0))
            .build();

    when(repo.save(any()))
        .thenAnswer(
            inv -> {
              BranchOperatingHoursEntity e = inv.getArgument(0);
              e.setId(99L);
              return e;
            });

    BranchOperatingHoursDto dto = service.createBranchOperatingHour(req);

    ArgumentCaptor<BranchOperatingHoursEntity> captor =
        ArgumentCaptor.forClass(BranchOperatingHoursEntity.class);
    verify(repo).save(captor.capture());

    BranchOperatingHoursEntity saved = captor.getValue();
    assertThat(saved.getBranchId()).isEqualTo(1L);
    assertThat(saved.getDayOfWeek()).isEqualTo(2);
    assertThat(saved.getOpeningTime()).isEqualTo(LocalTime.of(9, 0));
    assertThat(saved.getClosingTime()).isEqualTo(LocalTime.of(17, 0));
    assertThat(saved.getClosed()).isFalse();

    assertThat(dto).isNotNull();
    assertThat(dto.id()).isEqualTo(99L);

    verifyNoInteractions(auditService);
  }
}
