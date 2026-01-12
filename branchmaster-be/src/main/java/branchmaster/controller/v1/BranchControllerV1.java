package branchmaster.controller.v1;

import branchmaster.controller.v1.mapper.BranchV1Mapper;
import branchmaster.controller.v1.model.BranchMinimalResponse;
import branchmaster.controller.v1.model.BranchResponse;
import branchmaster.service.BranchService;
import branchmaster.service.model.BranchDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/branch")
@Slf4j
@RequiredArgsConstructor
@RestController
public class BranchControllerV1 {

  private final BranchService branchService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<BranchMinimalResponse>> getBranchListMinimal() {
    try {
      List<BranchDto> response = branchService.getAllOpenBranches();

      return ResponseEntity.status(HttpStatus.OK)
          .body(BranchV1Mapper.INSTANCE.mapMinimal(response));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw e;
    }
  }

  @GetMapping(path = "/full", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<BranchResponse>> getBranchList() {
    try {
      List<BranchDto> response = branchService.getAllOpenBranches();

      return ResponseEntity.status(HttpStatus.OK).body(BranchV1Mapper.INSTANCE.map(response));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw e;
    }
  }
}
