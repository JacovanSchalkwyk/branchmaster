package branchmaster.admin.mapper;

import branchmaster.admin.model.ResourceAvailabilityResponse;
import branchmaster.admin.model.ResourceUnavailabilityResponse;
import branchmaster.service.model.ResourceAvailabilityDto;
import branchmaster.service.model.ResourceUnavailabilityDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ResourceAdminV1Mapper {
  ResourceAdminV1Mapper INSTANCE = Mappers.getMapper(ResourceAdminV1Mapper.class);

  List<ResourceAvailabilityResponse> mapAvailabilities(List<ResourceAvailabilityDto> resources);

  ResourceAvailabilityResponse map(ResourceAvailabilityDto created);

  List<ResourceUnavailabilityResponse> map(List<ResourceUnavailabilityDto> resources);

  ResourceUnavailabilityResponse map(ResourceUnavailabilityDto created);
}
