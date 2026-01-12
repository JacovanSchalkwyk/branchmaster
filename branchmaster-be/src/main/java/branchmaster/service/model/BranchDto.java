package branchmaster.service.model;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record BranchDto(
    Long id,
    String name,
    LocalDateTime createdAt,
    Integer timeslotLength,
    Boolean active,
    String address,
    String suburb,
    String city,
    String province,
    String postalCode,
    String country,
    Double latitude,
    Double longitude) {}
