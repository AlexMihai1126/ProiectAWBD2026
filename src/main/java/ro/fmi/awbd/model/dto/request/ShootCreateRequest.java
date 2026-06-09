package ro.fmi.awbd.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.fmi.awbd.model.enums.ShootStatus;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShootCreateRequest {

    @NotBlank
    private String title;

    private ShootStatus status;

    @NotNull
    private OffsetDateTime startAt;

    private OffsetDateTime endAt;

    @Size(max = 2000)
    private String notes;

    @NotNull
    private Long ownerId;

    @NotNull
    private Long locationId;

    @Builder.Default
    private Set<Long> gearItemIds = new HashSet<>();
}
