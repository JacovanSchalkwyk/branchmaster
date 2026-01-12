package branchmaster.audit.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public enum ActionType {
  BRANCH_CREATED,
  OPERATING_HOURS_UPDATED,
  BRANCH_UPDATED
}
