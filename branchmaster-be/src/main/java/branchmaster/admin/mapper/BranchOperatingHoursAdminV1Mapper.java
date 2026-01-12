package branchmaster.admin.mapper;

import branchmaster.admin.model.BranchOperatingHoursResponse;
import branchmaster.service.model.BranchOperatingHoursDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BranchOperatingHoursAdminV1Mapper {
  BranchOperatingHoursAdminV1Mapper INSTANCE =
      Mappers.getMapper(BranchOperatingHoursAdminV1Mapper.class);

  List<BranchOperatingHoursResponse> map(List<BranchOperatingHoursDto> operatingHoursDtos);

  BranchOperatingHoursResponse map(BranchOperatingHoursDto response);
}
