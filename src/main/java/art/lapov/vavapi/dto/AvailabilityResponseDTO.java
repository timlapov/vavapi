package art.lapov.vavapi.dto;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class AvailabilityResponseDTO {
    Boolean available;
    String stationId;
    LocalDateTime startDate;
    LocalDateTime endDate;
}
