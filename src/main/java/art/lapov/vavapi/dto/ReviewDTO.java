package art.lapov.vavapi.dto;

import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link art.lapov.vavapi.model.Review}
 */
@Value
public class ReviewDTO implements Serializable {
    Integer rating;
    String comment;
    LocalDateTime createdAt;
    String authorFirstName;
    String authorLastName;
}