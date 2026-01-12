package branchmaster.repository;

import branchmaster.repository.entity.BranchOperatingHoursEntity;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface BranchOperatingHoursRepository
    extends CrudRepository<BranchOperatingHoursEntity, Long> {

  @Query(
      """
              SELECT * FROM branch_master.branch_operating_hours
              WHERE
                  branch_id = :branchId AND
                  closed = false
      """)
  List<BranchOperatingHoursEntity> getOpenByBranchId(@Param("branchId") Long branchId);

  List<BranchOperatingHoursEntity> findByBranchId(Long branchId);
}
