package branchmaster.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import branchmaster.service.model.AppointmentDto;
import branchmaster.service.model.AvailabilityStatus;
import branchmaster.service.model.Timeslot;
import branchmaster.web.exception.NoAvailableResourceException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

  @Mock ResourceAvailabilityRepository resourceAvailabilityRepository;
  @Mock BranchRepository branchRepository;
  @Mock AppointmentRepository appointmentRepository;
  @Mock BranchOperatingHoursRepository branchOperatingHoursRepository;
  @Mock ResourceUnavailabilityRepository resourceUnavailabilityRepository;

  @InjectMocks AppointmentService service;

  @Test
  void createAppointment_throwsNoAvailableResource_whenPickFreeResourceReturnsEmpty() {
    LocalDate date = LocalDate.of(2026, 1, 12);
    CreateAppointmentRequest req =
        CreateAppointmentRequest.builder()
            .branchId(1L)
            .appointmentDate(date)
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(10, 0))
            .reason("Checkup")
            .name("Jaco")
            .email("jaco@test.com")
            .phoneNumber("0123456789")
            .build();

    when(resourceAvailabilityRepository.findForBranchOnDate(
            eq(1L), anyInt(), eq(date), eq(LocalTime.of(9, 0)), eq(LocalTime.of(10, 0))))
        .thenReturn(List.of());

    assertThatThrownBy(() -> service.createAppointment(req))
        .isInstanceOf(NoAvailableResourceException.class)
        .hasMessageContaining("duplicate");

    verify(resourceAvailabilityRepository)
        .findForBranchOnDate(
            eq(1L), anyInt(), eq(date), eq(LocalTime.of(9, 0)), eq(LocalTime.of(10, 0)));
    verifyNoInteractions(appointmentRepository);
  }

  @Test
  void createAppointment_savesAppointment_withBookedStatus_andChosenResourceId() {
    LocalDate date = LocalDate.of(2026, 1, 12);
    CreateAppointmentRequest req =
        CreateAppointmentRequest.builder()
            .branchId(1L)
            .appointmentDate(date)
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(10, 0))
            .reason("Checkup")
            .name("Jaco")
            .email("jaco@test.com")
            .phoneNumber("0123456789")
            .build();

    ResourceAvailabilityEntity candidate = new ResourceAvailabilityEntity();
    candidate.setId(55L);

    when(resourceAvailabilityRepository.findForBranchOnDate(
            eq(1L), anyInt(), eq(date), eq(LocalTime.of(9, 0)), eq(LocalTime.of(10, 0))))
        .thenReturn(List.of(candidate));

    when(resourceUnavailabilityRepository.findUnavailableResourceIdsForSlot(
            eq(1L), eq(date), eq(LocalTime.of(9, 0)), eq(LocalTime.of(10, 0))))
        .thenReturn(List.of());

    when(appointmentRepository.findBookedResourceIdsForSlot(
            eq(1L), eq(date), eq(LocalTime.of(9, 0)), eq(LocalTime.of(10, 0))))
        .thenReturn(List.of());

    when(appointmentRepository.save(any()))
        .thenAnswer(
            inv -> {
              AppointmentEntity e = inv.getArgument(0);
              e.setId(999L);
              return e;
            });

    AppointmentDto dto = service.createAppointment(req);

    ArgumentCaptor<AppointmentEntity> captor = ArgumentCaptor.forClass(AppointmentEntity.class);
    verify(appointmentRepository).save(captor.capture());

    AppointmentEntity saved = captor.getValue();
    assertThat(saved.getBranchId()).isEqualTo(1L);
    assertThat(saved.getAppointmentDate()).isEqualTo(date);
    assertThat(saved.getStartTime()).isEqualTo(LocalTime.of(9, 0));
    assertThat(saved.getEndTime()).isEqualTo(LocalTime.of(10, 0));
    assertThat(saved.getStatus()).isEqualTo(BookingStatus.BOOKED);
    assertThat(saved.getResourceAvailabilityId()).isEqualTo(55L);
    assertThat(saved.getCreatedAt()).isNotNull();

    assertThat(dto).isNotNull();
  }

  @Test
  void getAvailableAppointments_throws_whenBranchNotFound() {
    when(branchRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                service.getAvailableAppointments(
                    1L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2)))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Branch not found");

    verify(branchRepository).findById(1L);
  }

  @Test
  void
      getAvailableAppointments_buildsTimeslots_marksFullyBooked_whenBookedOrUnavailableConsumesCapacity() {
    Long branchId = 1L;
    LocalDate date = LocalDate.of(2026, 1, 12); // Monday
    LocalDate startDate = date;
    LocalDate endDate = date;

    BranchEntity branch = new BranchEntity();
    branch.setId(branchId);
    branch.setTimeslotLength(60);

    int dayOfWeek = 0;

    BranchOperatingHoursEntity monHours = new BranchOperatingHoursEntity();
    monHours.setBranchId(branchId);
    monHours.setDayOfWeek(dayOfWeek);
    monHours.setOpeningTime(LocalTime.of(9, 0));
    monHours.setClosingTime(LocalTime.of(12, 0));
    monHours.setClosed(false);

    ResourceAvailabilityEntity emp = new ResourceAvailabilityEntity();
    emp.setId(10L);
    emp.setBranchId(branchId);
    emp.setDayOfWeek(dayOfWeek);
    emp.setStartTime(LocalTime.of(9, 0));
    emp.setEndTime(LocalTime.of(12, 0));

    AppointmentEntity booked = new AppointmentEntity();
    booked.setId(200L);
    booked.setBranchId(branchId);
    booked.setAppointmentDate(date);
    booked.setStartTime(LocalTime.of(10, 0));
    booked.setEndTime(LocalTime.of(11, 0));
    booked.setStatus(BookingStatus.BOOKED);

    ResourceUnavailabilityEntity unavail = new ResourceUnavailabilityEntity();
    unavail.setId(300L);
    unavail.setBranchId(branchId);
    unavail.setDate(date);
    unavail.setStartTime(LocalTime.of(11, 0));
    unavail.setEndTime(LocalTime.of(12, 0));
    unavail.setAvailableResourceId(10L);

    when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
    when(branchOperatingHoursRepository.getOpenByBranchId(branchId)).thenReturn(List.of(monHours));

    when(resourceAvailabilityRepository.getAvailabilitiesForBranchBetweenDates(
            branchId, startDate, endDate))
        .thenReturn(List.of(emp));

    when(appointmentRepository.findForBranchBetweenDates(branchId, startDate, endDate))
        .thenReturn(List.of(booked));

    when(resourceUnavailabilityRepository.findForBranchBetweenDates(branchId, startDate, endDate))
        .thenReturn(List.of(unavail));

    Map<LocalDate, List<Timeslot>> result =
        service.getAvailableAppointments(branchId, startDate, endDate);

    assertThat(result).isNotNull();
    assertThat(result).containsKey(date);

    List<Timeslot> slots = result.get(date);
    assertThat(slots).isNotNull();

    assertThat(slots)
        .extracting(Timeslot::startTime)
        .containsExactly(LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0));

    assertThat(slots.get(0).status()).isEqualTo(AvailabilityStatus.AVAILABLE);
    assertThat(slots.get(1).status()).isEqualTo(AvailabilityStatus.FULLY_BOOKED);
    assertThat(slots.get(2).status()).isEqualTo(AvailabilityStatus.FULLY_BOOKED);

    verify(branchRepository).findById(branchId);
    verify(branchOperatingHoursRepository).getOpenByBranchId(branchId);
    verify(resourceAvailabilityRepository)
        .getAvailabilitiesForBranchBetweenDates(branchId, startDate, endDate);
    verify(appointmentRepository).findForBranchBetweenDates(branchId, startDate, endDate);
    verify(resourceUnavailabilityRepository)
        .findForBranchBetweenDates(branchId, startDate, endDate);
  }

  @Test
  void getBookingsForBranchDay_returnsEmptyList_whenRepoReturnsEmpty() {
    when(appointmentRepository.findBookedForBranchOnDate(1L, LocalDate.of(2026, 1, 1)))
        .thenReturn(List.of());

    List<AppointmentDto> result = service.getBookingsForBranchDay(1L, LocalDate.of(2026, 1, 1));

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
    verify(appointmentRepository).findBookedForBranchOnDate(1L, LocalDate.of(2026, 1, 1));
  }

  @Test
  void getBookingsForBranchDay_returnsDtos_whenRepoReturnsEntities() {
    AppointmentEntity a = new AppointmentEntity();
    a.setId(10L);
    a.setBranchId(1L);
    a.setAppointmentDate(LocalDate.of(2026, 1, 1));
    a.setStartTime(LocalTime.of(9, 0));
    a.setEndTime(LocalTime.of(10, 0));
    a.setStatus(BookingStatus.BOOKED);

    when(appointmentRepository.findBookedForBranchOnDate(1L, LocalDate.of(2026, 1, 1)))
        .thenReturn(List.of(a));

    List<AppointmentDto> result = service.getBookingsForBranchDay(1L, LocalDate.of(2026, 1, 1));

    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    verify(appointmentRepository).findBookedForBranchOnDate(1L, LocalDate.of(2026, 1, 1));
  }

  @Test
  void cancelAppointment_throws_whenNotFound() {
    when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.cancelAppointment(999L))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Appointment not found");

    verify(appointmentRepository).findById(999L);
    verify(appointmentRepository, never()).save(any());
  }

  @Test
  void cancelAppointment_throws_whenNotInBookedState() {
    AppointmentEntity existing = new AppointmentEntity();
    existing.setId(10L);
    existing.setStatus(BookingStatus.USER_CANCELLED);

    when(appointmentRepository.findById(10L)).thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> service.cancelAppointment(10L))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("not in a booked state");

    verify(appointmentRepository).findById(10L);
    verify(appointmentRepository, never()).save(any());
  }

  @Test
  void cancelAppointment_setsUserCancelled_andSaves_whenBooked() {
    AppointmentEntity existing = new AppointmentEntity();
    existing.setId(10L);
    existing.setStatus(BookingStatus.BOOKED);

    when(appointmentRepository.findById(10L)).thenReturn(Optional.of(existing));
    when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    service.cancelAppointment(10L);

    ArgumentCaptor<AppointmentEntity> captor = ArgumentCaptor.forClass(AppointmentEntity.class);
    verify(appointmentRepository).save(captor.capture());

    AppointmentEntity saved = captor.getValue();
    assertThat(saved.getStatus()).isEqualTo(BookingStatus.USER_CANCELLED);
  }
}
