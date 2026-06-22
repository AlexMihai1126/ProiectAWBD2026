package ro.fmi.awbd.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.fmi.awbd.model.enums.ShootStatus;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShootListItemResponse {
    private Long id;
    private String title;
    private ShootStatus status;
    private OffsetDateTime startAt;
    private String locationName;
    private String clientName;
}
