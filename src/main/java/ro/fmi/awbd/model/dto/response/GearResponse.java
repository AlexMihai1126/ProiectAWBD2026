package ro.fmi.awbd.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.fmi.awbd.model.enums.GearType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GearResponse {
    private Long id;
    private GearType type;
    private String brand;
    private String model;
    private String notes;
    private Long ownerId;
    private String ownerUsername;
}
