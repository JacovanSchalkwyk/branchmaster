package branchmaster.audit.repository;

import branchmaster.audit.entity.AdminActionAuditEntity;
import org.springframework.data.repository.CrudRepository;

public interface AdminActionAuditRepository extends CrudRepository<AdminActionAuditEntity, Long> {}
