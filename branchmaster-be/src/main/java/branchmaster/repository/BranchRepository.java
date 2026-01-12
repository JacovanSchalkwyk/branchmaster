package branchmaster.repository;

import branchmaster.repository.entity.BranchEntity;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface BranchRepository extends CrudRepository<BranchEntity, Long> {

  @Query(
      """
                SELECT *
                FROM branch_master.branch
                WHERE active = true
                ORDER BY name ASC
            """)
  List<BranchEntity> getAllActiveBranchesSorted();

  @Query(
      """
                SELECT *
                FROM branch_master.branch
                ORDER BY name ASC
            """)
  List<BranchEntity> getAllBranchesSorted();
}
