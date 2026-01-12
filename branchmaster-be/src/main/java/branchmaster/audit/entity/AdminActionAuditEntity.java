package branchmaster.audit.entity;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "admin_audit_log", schema = "branch_master")
public class AdminActionAuditEntity {
  @Id private Long id;

  private Long staffId;

  private ActionType actionType;

  private JsonNode params;

  private LocalDateTime createdAt;
}
