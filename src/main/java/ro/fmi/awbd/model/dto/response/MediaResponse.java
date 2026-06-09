package ro.fmi.awbd.model.dto.response;

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
public class MediaResponse {
    private Long id;
    private Long shootId;
    private MediaType mediaType;
    private String fileRef;
    private OffsetDateTime takenAt;
    private Integer iso;
    private Double aperture;
    private String shutterSpeed;
    private Integer focalLength;
    private Integer focalLength35mm;
    private Integer widthPx;
    private Integer heightPx;
    private Integer rating;
    private String notes;
    private Integer durationSeconds;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
