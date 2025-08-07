package art.lapov.vavapi.dto;

import art.lapov.vavapi.model.ConnectorType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link art.lapov.vavapi.model.Station}
 */
@Value
public class StationUpdateDTO implements Serializable {
    @Positive
    Long maxPowerWatt;
    @NotNull
    ConnectorType connectorType;
    @NotNull
    Boolean enabled;
    String description;
    String photoUrl;
}