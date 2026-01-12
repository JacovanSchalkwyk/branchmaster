package branchmaster.admin.mapper;

import branchmaster.admin.model.AppointmentResponse;
import branchmaster.service.model.AppointmentDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AppointmentAdminV1Mapper {
  AppointmentAdminV1Mapper INSTANCE = Mappers.getMapper(AppointmentAdminV1Mapper.class);

  List<AppointmentResponse> map(List<AppointmentDto> response);
}
