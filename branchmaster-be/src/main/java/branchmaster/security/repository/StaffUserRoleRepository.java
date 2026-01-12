package branchmaster.security.repository;

import branchmaster.security.StaffUserRoleId;
import branchmaster.security.repository.entity.StaffUserRoleEntity;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface StaffUserRoleRepository
    extends CrudRepository<StaffUserRoleEntity, StaffUserRoleId> {
  List<StaffUserRoleEntity> findAllByStaffUserId(long staffUserId);
}
