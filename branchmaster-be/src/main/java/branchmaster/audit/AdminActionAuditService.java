package branchmaster.audit;

import branchmaster.audit.entity.ActionType;
import branchmaster.audit.entity.AdminActionAuditEntity;
import branchmaster.audit.repository.AdminActionAuditRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminActionAuditService {
  private final AdminActionAuditRepository repo;
  private final ObjectMapper objectMapper;

  public void log(Long staffId, ActionType actionType, Object params) {
    AdminActionAuditEntity entity = new AdminActionAuditEntity();
    entity.setStaffId(staffId);
    entity.setActionType(actionType);
    entity.setParams(writeJson(params));
    entity.setCreatedAt(LocalDateTime.now());
    repo.save(entity);
  }

  private JsonNode writeJson(Object params) {
    if (params == null) return null;
    return objectMapper.valueToTree(params);
  }
}
