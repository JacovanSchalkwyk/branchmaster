package branchmaster.repository;

import branchmaster.repository.entity.AppointmentEntity;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends CrudRepository<AppointmentEntity, Long> {

  @Query(
      """
                SELECT *
                FROM branch_master.appointment
                WHERE branch_id = :branchId
                  AND appointment_date BETWEEN :startDate AND :endDate
                  AND status = 'BOOKED'
            """)
  List<AppointmentEntity> findForBranchBetweenDates(
      @Param("branchId") Long branchId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  @Query(
      """
                SELECT *
                FROM branch_master.appointment
                WHERE branch_id = :branchId
                  AND appointment_date = :date
                  AND status = 'BOOKED'
            """)
  List<AppointmentEntity> findBookedForBranchOnDate(
      @Param("branchId") Long branchId, @Param("date") LocalDate date);

  @Query(
      """
                SELECT DISTINCT resource_availability_id
                FROM branch_master.appointment
                WHERE branch_id = :branchId
                  AND appointment_date = :date
                  AND status = 'BOOKED'
                  AND start_time < :endTime
                  AND end_time > :startTime
            """)
  List<Long> findBookedResourceIdsForSlot(
      Long branchId, LocalDate date, LocalTime startTime, LocalTime endTime);
}
