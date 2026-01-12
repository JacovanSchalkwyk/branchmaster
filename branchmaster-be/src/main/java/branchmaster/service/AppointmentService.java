package branchmaster.service;

import branchmaster.controller.v1.model.CreateAppointmentRequest;
import branchmaster.repository.AppointmentRepository;
import branchmaster.repository.BranchOperatingHoursRepository;
import branchmaster.repository.BranchRepository;
import branchmaster.repository.ResourceAvailabilityRepository;
import branchmaster.repository.ResourceUnavailabilityRepository;
import branchmaster.repository.entity.AppointmentEntity;
import branchmaster.repository.entity.BookingStatus;
import branchmaster.repository.entity.BranchEntity;
import branchmaster.repository.entity.BranchOperatingHoursEntity;
import branchmaster.repository.entity.ResourceAvailabilityEntity;
import branchmaster.repository.entity.ResourceUnavailabilityEntity;
import branchmaster.service.mapper.AppointmentMapper;
import branchmaster.service.model.AppointmentDto;
import branchmaster.service.model.AvailabilityStatus;
import branchmaster.service.model.Timeslot;
import branchmaster.web.exception.NoAvailableResourceException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppointmentService {

  private final ResourceAvailabilityRepository resourceAvailabilityRepository;
  private final BranchRepository branchRepository;
  private final AppointmentRepository appointmentRepository;
  private final BranchOperatingHoursRepository branchOperatingHoursRepository;
  private final ResourceUnavailabilityRepository resourceUnavailabilityRepository;

  public AppointmentDto createAppointment(CreateAppointmentRequest req) {
    Optional<ResourceAvailabilityEntity> openResource =
        pickFreeResource(req.branchId(), req.appointmentDate(), req.startTime(), req.endTime());

    if (openResource.isEmpty()) {
      throw new NoAvailableResourceException("Could not create appointment, duplicate entry.");
    }

    AppointmentEntity entity = new AppointmentEntity();
    entity.setCreatedAt(LocalDateTime.now());
    entity.setAppointmentDate(req.appointmentDate());
    entity.setBranchId(req.branchId());
    entity.setStatus(BookingStatus.BOOKED);
    entity.setStartTime(req.startTime());
    entity.setEndTime(req.endTime());
    entity.setReason(req.reason());
    entity.setName(req.name());
    entity.setEmail(req.email());
    entity.setPhoneNumber(req.phoneNumber());
    entity.setResourceAvailabilityId(openResource.get().getId());

    return AppointmentMapper.INSTANCE.map(appointmentRepository.save(entity));
  }

  public Map<LocalDate, List<Timeslot>> getAvailableAppointments(
      Long branchId, LocalDate startDate, LocalDate endDate) {
    Optional<BranchEntity> branch = branchRepository.findById(branchId);

    if (branch.isEmpty()) {
      log.error("Branch not found for branchId=[{}]", branchId);
      throw new RuntimeException("Branch not found");
    }

    Map<LocalDate, List<Timeslot>> result = new LinkedHashMap<>();

    final int timeslotLength = branch.get().getTimeslotLength();

    List<BranchOperatingHoursEntity> operatingHoursForWeek =
        branchOperatingHoursRepository.getOpenByBranchId(branchId);

    List<ResourceAvailabilityEntity> resourceAvailabilities =
        resourceAvailabilityRepository.getAvailabilitiesForBranchBetweenDates(
            branchId, startDate, endDate);
    List<AppointmentEntity> bookedAppointments =
        appointmentRepository.findForBranchBetweenDates(branchId, startDate, endDate);

    List<ResourceUnavailabilityEntity> resourceUnavailability =
        resourceUnavailabilityRepository.findForBranchBetweenDates(branchId, startDate, endDate);

    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
      int dayOfWeek = date.getDayOfWeek().getValue() - 1;
      Map<LocalDateTime, Integer> availableTimeslots =
          initialiseTimeslots(timeslotLength, operatingHoursForWeek, date);

      Optional<BranchOperatingHoursEntity> operatingHoursForDay =
          operatingHoursForWeek.stream().filter(oh -> oh.getDayOfWeek() == dayOfWeek).findFirst();

      if (operatingHoursForDay.isEmpty()) {
        continue;
      }

      List<ResourceAvailabilityEntity> resourceAvailabilitiesFilteredForDay =
          resourceAvailabilities.stream()
              .filter(availability -> availability.getDayOfWeek() == dayOfWeek)
              .toList();

      LocalDate dateTmp = date;
      List<ResourceUnavailabilityEntity> resourceUnavailabilitiesFilteredForDay =
          resourceUnavailability.stream()
              .filter(availability -> availability.getDate().equals(dateTmp))
              .toList();

      for (ResourceAvailabilityEntity employeeAvailability : resourceAvailabilitiesFilteredForDay) {
        LocalTime time = employeeAvailability.getStartTime();

        while (!time.isAfter(employeeAvailability.getEndTime().minusMinutes(timeslotLength))) {
          LocalDateTime slotStartTime = LocalDateTime.of(date, time);
          availableTimeslots.merge(slotStartTime, 1, Integer::sum);
          time = time.plusMinutes(timeslotLength);
        }
      }

      LocalDate finalDate = date;
      List<AppointmentEntity> bookedForDay =
          bookedAppointments.stream()
              .filter(a -> finalDate.equals(a.getAppointmentDate()))
              .toList();

      for (AppointmentEntity appt : bookedForDay) {
        LocalTime startTime = appt.getStartTime();
        while (!startTime.isAfter(appt.getEndTime().minusMinutes(timeslotLength))) {
          LocalDateTime s = LocalDateTime.of(date, startTime);
          availableTimeslots.computeIfPresent(s, (k, v) -> v - 1);
          startTime = startTime.plusMinutes(timeslotLength);
        }
      }

      for (ResourceUnavailabilityEntity employeeUnavailability :
          resourceUnavailabilitiesFilteredForDay) {
        LocalTime time = employeeUnavailability.getStartTime();

        while (!time.isAfter(employeeUnavailability.getEndTime().minusMinutes(timeslotLength))) {
          LocalDateTime slotStartTime = LocalDateTime.of(date, time);
          availableTimeslots.merge(slotStartTime, -1, Integer::sum);
          time = time.plusMinutes(timeslotLength);
        }
      }

      if (!availableTimeslots.isEmpty()) {
        List<Timeslot> timeslots =
            availableTimeslots.entrySet().stream()
                .<Timeslot>map(
                    slot -> {
                      LocalTime start = slot.getKey().toLocalTime();
                      LocalTime end = start.plusMinutes(timeslotLength);

                      AvailabilityStatus status =
                          slot.getValue() <= 0
                              ? AvailabilityStatus.FULLY_BOOKED
                              : AvailabilityStatus.AVAILABLE;

                      return Timeslot.builder()
                          .startTime(start)
                          .endTime(end)
                          .status(status)
                          .build();
                    })
                .toList();

        result.put(date, timeslots);
      }
    }

    return result;
  }

  private Optional<ResourceAvailabilityEntity> pickFreeResource(
      Long branchId, LocalDate date, LocalTime startTime, LocalTime endTime) {
    int dayOfWeek = date.getDayOfWeek().getValue() - 1;

    List<ResourceAvailabilityEntity> candidates =
        resourceAvailabilityRepository.findForBranchOnDate(
            branchId, dayOfWeek, date, startTime, endTime);

    if (candidates.isEmpty()) return Optional.empty();

    List<Long> unavailableIds =
        resourceUnavailabilityRepository.findUnavailableResourceIdsForSlot(
            branchId, date, startTime, endTime);

    List<Long> bookedIds =
        appointmentRepository.findBookedResourceIdsForSlot(branchId, date, startTime, endTime);

    return candidates.stream()
        .filter(r -> unavailableIds == null || !unavailableIds.contains(r.getId()))
        .filter(r -> bookedIds == null || !bookedIds.contains(r.getId()))
        .findFirst();
  }

  public List<AppointmentDto> getBookingsForBranchDay(Long branchId, LocalDate date) {
    List<AppointmentEntity> appointmentEntities =
        appointmentRepository.findBookedForBranchOnDate(branchId, date);

    if (appointmentEntities.isEmpty()) {
      return new ArrayList<>();
    }

    return AppointmentMapper.INSTANCE.map(appointmentEntities);
  }

  public void cancelAppointment(Long bookingId) {
    AppointmentEntity appointmentEntity = appointmentRepository.findById(bookingId).orElse(null);

    if (appointmentEntity == null) {
      log.error("Appointment with id {} not found", bookingId);
      throw new RuntimeException("Appointment not found");
    }

    if (appointmentEntity.getStatus() != BookingStatus.BOOKED) {
      log.error("Appointment with id {} is already booked", bookingId);
      throw new RuntimeException("Appointment is not in a booked state");
    }

    appointmentEntity.setStatus(BookingStatus.USER_CANCELLED);

    appointmentRepository.save(appointmentEntity);
  }

  private Map<LocalDateTime, Integer> initialiseTimeslots(
      int timeslotLength, List<BranchOperatingHoursEntity> operatingHoursForWeek, LocalDate date) {
    Map<LocalDateTime, Integer> availableTimeslots = new TreeMap<>();

    int dayOfWeek = date.getDayOfWeek().getValue() - 1;
    Optional<BranchOperatingHoursEntity> operatingHours =
        operatingHoursForWeek.stream()
            .filter(entity -> entity.getDayOfWeek() == dayOfWeek)
            .findFirst();

    if (operatingHours.isEmpty()) {
      return availableTimeslots;
    }

    LocalTime time = operatingHours.get().getOpeningTime();
    LocalTime closingTime = operatingHours.get().getClosingTime();

    while (!time.isAfter(closingTime.minusMinutes(timeslotLength))) {

      LocalDateTime slotStart = LocalDateTime.of(date, time);

      availableTimeslots.put(slotStart, 0);

      time = time.plusMinutes(timeslotLength);
    }

    return availableTimeslots;
  }
}
