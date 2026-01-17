package branchmaster.service;

import branchmaster.admin.model.CreateBranchRequest;
import branchmaster.admin.model.UpdateBranchRequest;
import branchmaster.audit.AdminActionAuditService;
import branchmaster.audit.entity.ActionType;
import branchmaster.repository.BranchRepository;
import branchmaster.repository.entity.BranchEntity;
import branchmaster.service.mapper.BranchMapper;
import branchmaster.service.model.BranchDto;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BranchService {

  private final BranchRepository branchRepository;
  private final AdminActionAuditService auditService;

  public List<BranchDto> getAllOpenBranches() {
    List<BranchEntity> branchEntities = branchRepository.getAllActiveBranchesSorted();

    if (branchEntities.isEmpty()) {
      log.error("No active branches found");
      throw new RuntimeException("No active branches found");
    }

    return BranchMapper.INSTANCE.map(branchEntities);
  }

  public List<BranchDto> getAllBranches() {
    List<BranchEntity> branchEntities = branchRepository.getAllBranchesSorted();

    if (branchEntities.isEmpty()) {
      log.error("No branches found");
      throw new RuntimeException("No branches found");
    }

    return BranchMapper.INSTANCE.map(branchEntities);
  }

  public BranchDto getBranchDetailsAdmin(Long branchId) {
    Optional<BranchEntity> branchEntity = branchRepository.findById(branchId);

    if (branchEntity.isEmpty()) {
      log.error("No branch found with id {}", branchId);
      throw new RuntimeException("No branch found");
    }

    return BranchMapper.INSTANCE.map(branchEntity.get());
  }

  public BranchDto updateBranchAdmin(UpdateBranchRequest req) {
    BranchEntity branchEntity = branchRepository.findById(req.id()).orElse(null);

    if (branchEntity == null) {
      log.error("No branch found with id {}", req.id());
      throw new RuntimeException("No branch found");
    }

    var beforeSnapshot = getSnapshot(branchEntity);

    branchEntity.setName(req.name());
    branchEntity.setAddress(req.address());
    branchEntity.setSuburb(req.suburb());
    branchEntity.setCity(req.city());
    branchEntity.setProvince(req.province());
    branchEntity.setPostalCode(req.postalCode());
    branchEntity.setActive(req.active());
    branchEntity.setTimeslotLength(req.timeslotLength());
    branchEntity.setLatitude(req.latitude());
    branchEntity.setLongitude(req.longitude());

    branchRepository.save(branchEntity);

    var afterSnapshot = getSnapshot(branchEntity);

    auditService.log(
        ActionType.UPDATE_BRANCH, Map.of("before", beforeSnapshot, "after", afterSnapshot));

    return BranchMapper.INSTANCE.map(branchEntity);
  }

  public BranchDto createBranchAdmin(CreateBranchRequest req) {
    BranchEntity branchEntity = new BranchEntity();

    branchEntity.setName(req.name());
    branchEntity.setAddress(req.address());
    branchEntity.setSuburb(req.suburb());
    branchEntity.setCity(req.city());
    branchEntity.setProvince(req.province());
    branchEntity.setPostalCode(req.postalCode());
    branchEntity.setActive(req.active());
    branchEntity.setTimeslotLength(req.timeslotLength());
    branchEntity.setLatitude(req.latitude());
    branchEntity.setLongitude(req.longitude());
    branchEntity.setCountry(req.country());

    branchRepository.save(branchEntity);

    var afterSnapshot = getSnapshot(branchEntity);

    auditService.log(ActionType.CREATE_BRANCH, Map.of("after", afterSnapshot));

    return BranchMapper.INSTANCE.map(branchEntity);
  }

  private Map<String, Object> getSnapshot(BranchEntity e) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", e.getId());
    m.put("address", e.getAddress());
    m.put("suburb", e.getSuburb());
    m.put("city", e.getCity());
    m.put("province", e.getProvince());
    m.put("postalCode", e.getPostalCode());
    m.put("active", e.getActive());
    m.put("timeslotDuration", e.getTimeslotLength());
    m.put("latitude", e.getLatitude());
    m.put("longitude", e.getLongitude());
    return m;
  }
}
