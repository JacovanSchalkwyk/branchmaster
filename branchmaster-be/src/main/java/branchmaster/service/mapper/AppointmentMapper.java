package branchmaster.service.mapper;

import branchmaster.repository.entity.AppointmentEntity;
import branchmaster.service.model.AppointmentDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AppointmentMapper {
  AppointmentMapper INSTANCE = Mappers.getMapper(AppointmentMapper.class);

  List<AppointmentDto> map(List<AppointmentEntity> appointmentEntities);

  AppointmentDto map(AppointmentEntity appointment);
}
