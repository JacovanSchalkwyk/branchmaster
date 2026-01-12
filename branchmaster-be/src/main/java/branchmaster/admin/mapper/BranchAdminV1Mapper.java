package branchmaster.admin.mapper;

import branchmaster.admin.model.BranchAdminResponse;
import branchmaster.service.model.BranchDto;
import branchmaster.util.AddressUtils;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", imports = AddressUtils.class)
public interface BranchAdminV1Mapper {
  BranchAdminV1Mapper INSTANCE = Mappers.getMapper(BranchAdminV1Mapper.class);

  @Mapping(
      target = "friendlyAddress",
      expression = "java(AddressUtils.buildFriendlyAddress(branch))")
  @Mapping(source = "id", target = "branchId")
  BranchAdminResponse map(BranchDto branch);

  List<BranchAdminResponse> map(List<BranchDto> branches);
}
