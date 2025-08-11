package art.lapov.vavapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class CostCalculationDTO {
    String stationId;
    LocalDateTime startTime;
    LocalDateTime endTime;

    @JsonProperty("total_cost_in_cents")
    Integer totalCostInCents;

    @JsonProperty("total_cost_euros")
    public Double getTotalCostEuros() {
        return totalCostInCents != null ? totalCostInCents / 100.0 : 0.0;
    }

    @JsonProperty("duration_hours")
    Double durationHours;
}
