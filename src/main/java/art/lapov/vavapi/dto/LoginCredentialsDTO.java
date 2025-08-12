package art.lapov.vavapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class LoginCredentialsDTO {
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}
