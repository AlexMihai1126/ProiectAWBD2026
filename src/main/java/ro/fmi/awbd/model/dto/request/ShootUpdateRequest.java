package ro.fmi.awbd.model.dto.request;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.fmi.awbd.model.enums.ShootStatus;

import java.time.OffsetDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShootUpdateRequest {

    private String title;

    private ShootStatus status;

    private OffsetDateTime startAt;

    private OffsetDateTime endAt;

    @Size(max = 2000)
    private String notes;

    private Long ownerId;

    private Long locationId;

    private Long clientId;

    private Set<Long> gearItemIds;

    @AssertTrue(message = "End date must be the same as or later than the start date")
    public boolean isDateRangeValid() {
        return startAt == null || endAt == null || !endAt.isBefore(startAt);
    }
}
