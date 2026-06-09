package ro.fmi.awbd.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.fmi.awbd.model.enums.InvoiceStatus;
import ro.fmi.awbd.model.enums.PaymentType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceUpdateRequest {

    private Long clientId;

    private PaymentType paymentMethod;

    @DecimalMin("0.0")
    private BigDecimal amount;

    private OffsetDateTime paidAt;

    private InvoiceStatus status;

    @Size(max = 1000)
    private String details;
}
