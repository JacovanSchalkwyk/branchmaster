package branchmaster.service.mapper;

import branchmaster.repository.entity.BranchEntity;
import branchmaster.service.model.BranchDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BranchMapper {
  BranchMapper INSTANCE = Mappers.getMapper(BranchMapper.class);

  List<BranchDto> map(List<BranchEntity> branchEntities);

  BranchDto map(BranchEntity branchEntity);
}
