package branchmaster.audit.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public enum ActionType {
  CREATE_BRANCH,
  CREATE_BRANCH_OPERATING_HOURS,
  UPDATE_BRANCH,
  CREATE_RESOURCE_AVAILABILITY,
  UPDATE_RESOURCE_AVAILABILITY,
  DELETE_RESOURCE_AVAILABILITY,
  DELETE_RESOURCE_UNAVAILABILITY,
  CREATE_RESOURCE_UNAVAILABILITY,
  UPDATE_BRANCH_OPERATING_HOURS
}
