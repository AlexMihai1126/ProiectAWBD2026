package ro.fmi.awbd.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import ro.fmi.awbd.model.enums.MediaType;

import java.time.OffsetDateTime;

@Entity
@Table(name = "media")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shoot_id", nullable = false)
    private ShootEntity shoot;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 30)
    private MediaType mediaType;

    @NotBlank
    @Column(name = "file_ref", nullable = false, length = 500)
    private String fileRef;

    private OffsetDateTime takenAt;

    @Min(50) @Max(204800)
    private Integer iso;

    private Double aperture;
    private String shutterSpeed;
    private Integer focalLength;
    private Integer focalLength35mm;

    @Column(name = "width_px")
    private Integer widthPx;

    @Column(name = "height_px")
    private Integer heightPx;

    @Min(1) @Max(5)
    private Integer rating;

    @Column(length = 1000)
    private String notes;

    @Min(1)
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
