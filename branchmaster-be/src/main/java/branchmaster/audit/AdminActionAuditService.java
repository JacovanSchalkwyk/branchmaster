package branchmaster.audit;

import branchmaster.audit.entity.ActionType;
import branchmaster.audit.entity.AdminActionAuditEntity;
import branchmaster.audit.repository.AdminActionAuditRepository;
import branchmaster.security.StaffAuthUtil;
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

  public void log(ActionType actionType, Object params) {
    try {
      AdminActionAuditEntity entity = new AdminActionAuditEntity();
      entity.setStaffId(StaffAuthUtil.getStaffId());
      entity.setActionType(actionType);
      entity.setParams(writeJson(params));
      entity.setCreatedAt(LocalDateTime.now());
      repo.save(entity);
    } catch (Exception e) {
      log.error("Something went wrong when creating admin audit, [{}]", e.getMessage());
    }
  }

  private JsonNode writeJson(Object params) {
    if (params == null) return null;
    return objectMapper.valueToTree(params);
  }
}
