package ro.fmi.awbd.model.dto.response;

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
public class InvoiceResponse {
    private Long id;
    private Long shootId;
    private Long clientId;
    private String clientName;
    private PaymentType paymentMethod;
    private BigDecimal amount;
    private OffsetDateTime paidAt;
    private InvoiceStatus status;
    private String details;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
