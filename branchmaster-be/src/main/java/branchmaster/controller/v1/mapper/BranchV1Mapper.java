package branchmaster.controller.v1.mapper;

import branchmaster.controller.v1.model.BranchMinimalResponse;
import branchmaster.controller.v1.model.BranchResponse;
import branchmaster.service.model.BranchDto;
import branchmaster.util.AddressUtils;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", imports = AddressUtils.class)
public interface BranchV1Mapper {
  BranchV1Mapper INSTANCE = Mappers.getMapper(BranchV1Mapper.class);

  List<BranchMinimalResponse> mapMinimal(List<BranchDto> response);

  List<BranchResponse> map(List<BranchDto> response);

  @Mapping(
      target = "friendlyAddress",
      expression = "java(AddressUtils.buildFriendlyAddress(branch))")
  @Mapping(source = "id", target = "branchId")
  BranchResponse map(BranchDto branch);

  @Mapping(
      target = "friendlyAddress",
      expression = "java(AddressUtils.buildFriendlyAddress(branch))")
  @Mapping(source = "id", target = "branchId")
  BranchMinimalResponse mapMinimal(BranchDto branch);
}
