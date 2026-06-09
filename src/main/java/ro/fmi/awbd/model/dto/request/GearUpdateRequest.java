package ro.fmi.awbd.model.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.fmi.awbd.model.enums.GearType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GearUpdateRequest {

    private GearType type;

    private String brand;

    private String model;

    @Size(max = 1000)
    private String notes;

    private Long ownerId;
}
