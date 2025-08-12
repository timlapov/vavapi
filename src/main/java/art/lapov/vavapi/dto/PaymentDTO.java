package art.lapov.vavapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link art.lapov.vavapi.model.Payment}
 */
@Value
public class PaymentDTO implements Serializable {
    String id;
    Integer amountInCents;
    LocalDateTime paidAt;
    String transactionId;
    LocalDateTime refundedAt;
    Integer refundedAmountInCents;

    @JsonProperty("amount_euros")
    public Double getAmountEuros() {
        return amountInCents != null ? amountInCents / 100.0 : 0.0;
    }

    @JsonProperty("refunded_amount_euros")
    public Double getRefundedAmountEuros() {
        return refundedAmountInCents != null ? refundedAmountInCents / 100.0 : 0.0;
    }

    @JsonProperty("is_refunded")
    public boolean isRefunded() {
        return refundedAt != null;
    }

    @JsonProperty("is_partially_refunded")
    public boolean isPartiallyRefunded() {
        return refundedAmountInCents != null &&
                refundedAmountInCents > 0 &&
                refundedAmountInCents < amountInCents;
    }
}