package branchmaster.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import branchmaster.audit.AdminActionAuditService;
import branchmaster.audit.entity.ActionType;
import branchmaster.audit.entity.AdminActionAuditEntity;
import branchmaster.audit.repository.AdminActionAuditRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminActionAuditServiceTest {

  @Mock AdminActionAuditRepository repo;

  @InjectMocks AdminActionAuditService service;
  @Spy ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void log_savesAuditEntity_withSerializedParams() {
    Long staffId = 42L;
    ActionType actionType = ActionType.OPERATING_HOURS_UPDATED;

    Map<String, Object> params =
        Map.of(
            "branchId", 10L,
            "before", Map.of("openingTime", "09:00"),
            "after", Map.of("openingTime", "10:00"));

    ArgumentCaptor<AdminActionAuditEntity> captor =
        ArgumentCaptor.forClass(AdminActionAuditEntity.class);

    service.log(staffId, actionType, params);

    verify(repo).save(captor.capture());

    AdminActionAuditEntity saved = captor.getValue();
    assertThat(saved.getStaffId()).isEqualTo(staffId);
    assertThat(saved.getActionType()).isEqualTo(actionType);
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());

    JsonNode json = saved.getParams();
    assertThat(json).isNotNull();
    assertThat(json.get("branchId").asLong()).isEqualTo(10L);
    assertThat(json.get("before").get("openingTime").asText()).isEqualTo("09:00");
    assertThat(json.get("after").get("openingTime").asText()).isEqualTo("10:00");
  }

  @Test
  void log_allowsNullParams_andStoresNullJson() {
    Long staffId = 7L;
    ActionType actionType = ActionType.BRANCH_UPDATED;

    ArgumentCaptor<AdminActionAuditEntity> captor =
        ArgumentCaptor.forClass(AdminActionAuditEntity.class);

    service.log(staffId, actionType, null);

    verify(repo).save(captor.capture());

    AdminActionAuditEntity saved = captor.getValue();
    assertThat(saved.getStaffId()).isEqualTo(staffId);
    assertThat(saved.getActionType()).isEqualTo(actionType);
    assertThat(saved.getParams()).isNull();
    assertThat(saved.getCreatedAt()).isNotNull();
  }
}
