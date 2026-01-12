package branchmaster.controller.v1.mapper;

import branchmaster.controller.v1.model.CreateAppointmentResponse;
import branchmaster.service.model.AppointmentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AppointmentV1Mapper {
  AppointmentV1Mapper INSTANCE = Mappers.getMapper(AppointmentV1Mapper.class);

  @Mapping(source = "id", target = "appointmentId")
  CreateAppointmentResponse map(AppointmentDto created);
}
