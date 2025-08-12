package art.lapov.vavapi.dto;

import art.lapov.vavapi.enums.ConnectorType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link art.lapov.vavapi.model.Station}
 */
@Value
public class StationShortDTO implements Serializable {
    String id;
    @Positive
    Long maxPowerWatt;
    @NotNull
    ConnectorType connectorType;
    String description;
    LocationShortDTO location;
}