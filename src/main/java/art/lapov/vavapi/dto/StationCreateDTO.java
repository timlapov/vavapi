package art.lapov.vavapi.dto;

import art.lapov.vavapi.model.ConnectorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link art.lapov.vavapi.model.Station}
 */
@Value
public class StationCreateDTO implements Serializable {
    @Positive
    Long maxPowerWatt;
    @NotNull
    ConnectorType connectorType;
    String description;
    @NotBlank
    String locationId;
}