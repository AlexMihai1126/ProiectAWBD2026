package ro.fmi.awbd.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsResponse {
    private BigDecimal paidAmount;
    private BigDecimal receivableAmount;
    private long plannedShoots;
    private long doneShoots;
}
