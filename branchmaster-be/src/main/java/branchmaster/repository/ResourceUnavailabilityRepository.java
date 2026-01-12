package branchmaster.repository;

import branchmaster.repository.entity.ResourceUnavailabilityEntity;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface ResourceUnavailabilityRepository
    extends CrudRepository<ResourceUnavailabilityEntity, Long> {

  List<ResourceUnavailabilityEntity> findAllByBranchId(Long branchId);

  @Query(
      """
          SELECT DISTINCT available_resource_id
          FROM branch_master.resource_unavailability
          WHERE branch_id = :branchId
            AND date = :date
            AND (
                  (start_time IS NULL AND end_time IS NULL) -- whole day block
               OR (start_time < :endTime AND end_time > :startTime) -- overlaps
            )
      """)
  List<Long> findUnavailableResourceIdsForSlot(
      Long branchId, LocalDate date, LocalTime startTime, LocalTime endTime);

  @Query(
      """
          SELECT *
          FROM branch_master.resource_unavailability
          WHERE branch_id = :branchId
            AND date BETWEEN :startDate AND :endDate
      """)
  List<ResourceUnavailabilityEntity> findForBranchBetweenDates(
      Long branchId, LocalDate startDate, LocalDate endDate);
}
