package branchmaster.service.model;

import java.time.LocalTime;
import lombok.Builder;

@Builder
public record Timeslot(LocalTime startTime, LocalTime endTime, AvailabilityStatus status) {}
