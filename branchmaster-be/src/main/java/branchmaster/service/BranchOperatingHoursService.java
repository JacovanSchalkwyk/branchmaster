package branchmaster.service;

import branchmaster.admin.model.CreateBranchOperatingHoursRequest;
import branchmaster.admin.model.UpdateBranchOperatingHoursRequest;
import branchmaster.audit.AdminActionAuditService;
import branchmaster.audit.entity.ActionType;
import branchmaster.repository.BranchOperatingHoursRepository;
import branchmaster.repository.entity.BranchOperatingHoursEntity;
import branchmaster.service.mapper.BranchOperatingHoursMapper;
import branchmaster.service.model.BranchOperatingHoursDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BranchOperatingHoursService {

  private final BranchOperatingHoursRepository branchOperatingHoursRepository;
  private final AdminActionAuditService auditService;

  public List<BranchOperatingHoursDto> getOperatingHoursForBranch(Long branchId) {
    List<BranchOperatingHoursEntity> branchOperatingHoursEntities =
        branchOperatingHoursRepository.findByBranchId(branchId);

    if (branchOperatingHoursEntities.isEmpty()) {
      return new ArrayList<>();
    }

    return BranchOperatingHoursMapper.INSTANCE.map(branchOperatingHoursEntities);
  }

  public void updateBranchOperatingHour(UpdateBranchOperatingHoursRequest req) {
    BranchOperatingHoursEntity before =
        branchOperatingHoursRepository.findById(req.id()).orElse(null);

    if (before == null) {
      throw new RuntimeException("Branch operating hours not found");
    }

    var beforeSnapshot = getSnapshot(before);

    before.setOpeningTime(req.openingTime());
    before.setClosingTime(req.closingTime());
    before.setClosed(req.closed());
    before.setDayOfWeek(req.dayOfWeek());

    branchOperatingHoursRepository.save(before);

    var afterSnapshot = getSnapshot(before);

    auditService.log(
        ActionType.UPDATE_BRANCH_OPERATING_HOURS,
        Map.of("before", beforeSnapshot, "after", afterSnapshot));
  }

  public BranchOperatingHoursDto createBranchOperatingHour(CreateBranchOperatingHoursRequest req) {
    BranchOperatingHoursEntity entity = new BranchOperatingHoursEntity();

    entity.setBranchId(req.branchId());
    entity.setClosed(false);
    entity.setDayOfWeek(req.dayOfWeek());
    entity.setOpeningTime(req.openingTime());
    entity.setClosingTime(req.closingTime());

    entity = branchOperatingHoursRepository.save(entity);

    var afterSnapshot = getSnapshot(entity);

    auditService.log(ActionType.CREATE_BRANCH_OPERATING_HOURS, Map.of("after", afterSnapshot));

    return BranchOperatingHoursMapper.INSTANCE.map(entity);
  }

  private Object getSnapshot(BranchOperatingHoursEntity before) {
    return Map.of(
        "id",
        before.getId(),
        "branchId",
        before.getBranchId(),
        "dayOfWeek",
        before.getDayOfWeek(),
        "openingTime",
        before.getOpeningTime(),
        "closingTime",
        before.getClosingTime(),
        "closed",
        before.getClosed());
  }
}
