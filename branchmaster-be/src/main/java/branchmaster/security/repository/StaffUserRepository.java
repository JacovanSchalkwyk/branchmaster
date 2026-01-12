package branchmaster.security.repository;

import branchmaster.security.repository.entity.StaffUserEntity;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface StaffUserRepository extends CrudRepository<StaffUserEntity, Long> {
  Optional<StaffUserEntity> findByEmailIgnoreCase(String email);
}
