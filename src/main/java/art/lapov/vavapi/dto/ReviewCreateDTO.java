package art.lapov.vavapi.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link art.lapov.vavapi.model.Review}
 */
@Value
public class ReviewCreateDTO implements Serializable {
    @Positive
    @Max(5)
    Integer rating;
    String comment;
}