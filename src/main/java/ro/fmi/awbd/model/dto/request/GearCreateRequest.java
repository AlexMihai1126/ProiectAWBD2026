package ro.fmi.awbd.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class GearCreateRequest {

    @NotNull
    private GearType type;

    @NotBlank
    private String brand;

    @NotBlank
    private String model;

    @Size(max = 1000)
    private String notes;

    @NotNull
    private Long ownerId;
}
