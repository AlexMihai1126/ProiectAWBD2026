package ro.fmi.awbd.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.fmi.awbd.model.enums.MediaType;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaUpdateRequest {

    private MediaType mediaType;

    @Size(max = 500)
    private String fileRef;

    private OffsetDateTime takenAt;

    @Min(50)
    @Max(204800)
    private Integer iso;

    private Double aperture;

    private String shutterSpeed;

    private Integer focalLength;

    private Integer focalLength35mm;

    private Integer widthPx;

    private Integer heightPx;

    @Min(1)
    @Max(5)
    private Integer rating;

    @Size(max = 1000)
    private String notes;

    @Min(1)
    private Integer durationSeconds;
}
