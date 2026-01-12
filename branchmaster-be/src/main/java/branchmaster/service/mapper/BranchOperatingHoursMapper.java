package branchmaster.service.mapper;

import branchmaster.repository.entity.BranchOperatingHoursEntity;
import branchmaster.service.model.BranchOperatingHoursDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BranchOperatingHoursMapper {
  BranchOperatingHoursMapper INSTANCE = Mappers.getMapper(BranchOperatingHoursMapper.class);

  List<BranchOperatingHoursDto> map(List<BranchOperatingHoursEntity> branchOperatingHoursEntities);

  BranchOperatingHoursDto map(BranchOperatingHoursEntity save);
}
