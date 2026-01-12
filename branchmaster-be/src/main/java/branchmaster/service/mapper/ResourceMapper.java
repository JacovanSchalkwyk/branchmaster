package branchmaster.service.mapper;

import branchmaster.repository.entity.ResourceAvailabilityEntity;
import branchmaster.repository.entity.ResourceUnavailabilityEntity;
import branchmaster.service.model.ResourceAvailabilityDto;
import branchmaster.service.model.ResourceUnavailabilityDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ResourceMapper {
  ResourceMapper INSTANCE = Mappers.getMapper(ResourceMapper.class);

  List<ResourceAvailabilityDto> mapAvailabilities(List<ResourceAvailabilityEntity> entities);

  ResourceAvailabilityDto map(ResourceAvailabilityEntity entity);

  List<ResourceUnavailabilityDto> mapUnavailabilities(List<ResourceUnavailabilityEntity> entities);

  ResourceUnavailabilityDto map(ResourceUnavailabilityEntity entity);
}
