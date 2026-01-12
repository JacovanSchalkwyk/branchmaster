package branchmaster.repository;

import branchmaster.repository.entity.ResourceAvailabilityEntity;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ResourceAvailabilityRepository
    extends CrudRepository<ResourceAvailabilityEntity, Long> {

  @Query(
      """
                    SELECT * FROM branch_master.resource_availability
                    WHERE
                        branch_id = :branchId AND
                        (start_date IS NULL OR
                        end_date IS NULL OR
                        start_date BETWEEN :startDate AND :endDate OR
                        end_date BETWEEN :startDate AND :endDate OR
                        (start_date < :startDate AND end_date > :endDate))

                    """)
  List<ResourceAvailabilityEntity> getAvailabilitiesForBranchBetweenDates(
      @Param("branchId") Long branchId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  List<ResourceAvailabilityEntity> findAllByBranchId(Long branchId);

  @Query(
      """
                  SELECT *
                  FROM branch_master.resource_availability
                  WHERE branch_id = :branchId
                    AND day_of_week = :dayOfWeek
                    AND start_date <= :date
                    AND end_date >= :date
                    AND start_time <= :startTime
                    AND end_time >= :endTime
          """)
  List<ResourceAvailabilityEntity> findForBranchOnDate(
      Long branchId, int dayOfWeek, LocalDate date, LocalTime startTime, LocalTime endTime);
}
