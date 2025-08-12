package art.lapov.vavapi.dto;

import lombok.Value;

@Value
public class LoginResponseDTO {
    private String token;
    private UserDTO user;
}
