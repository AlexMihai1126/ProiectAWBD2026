package ro.fmi.awbd.model.dto.response;

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
public class ShootResponse {
    private Long id;
    private String title;
    private ShootStatus status;
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private String notes;
    private Long ownerId;
    private String ownerUsername;
    private Long locationId;
    private String locationName;
    private Set<Long> gearItemIds;
    private Long invoiceId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
