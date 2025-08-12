package art.lapov.vavapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for payment details
 */
@Value
public class PaymentDetailsDTO implements Serializable {
    @NotBlank
    String cardNumber;

    @NotBlank
    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "Format: MM/YY")
    String expiryDate;

    @NotBlank
    @Size(min = 3, max = 3)
    String cvv;

    @NotBlank
    String cardholderName;

    // For MVP, we can just validate the format
    // In production, this would be sent to payment gateway
}
