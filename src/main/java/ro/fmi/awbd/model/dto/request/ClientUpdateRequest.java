package ro.fmi.awbd.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientUpdateRequest {

    @NotBlank
    @Size(max = 150)
    private String name;

    @Email
    @Size(max = 200)
    private String email;

    @Size(max = 30)
    private String phone;

    @Size(max = 1000)
    private String notes;
}
