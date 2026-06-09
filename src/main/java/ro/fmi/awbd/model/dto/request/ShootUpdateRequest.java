package ro.fmi.awbd.model.dto.request;

import jakarta.validation.constraints.Size;
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

    private Set<Long> gearItemIds;
}
