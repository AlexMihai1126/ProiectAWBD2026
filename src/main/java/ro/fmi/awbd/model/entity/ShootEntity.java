package ro.fmi.awbd.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ro.fmi.awbd.model.entity.security.User;
import ro.fmi.awbd.model.enums.ShootStatus;

import java.time.OffsetDateTime;
import java.util.*;

@Entity
@Table(name = "shoot")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShootEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShootStatus status = ShootStatus.PLANNED;

    @NotNull
    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "end_at")
    private OffsetDateTime endAt;

    @Column(length = 2000)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private LocationEntity location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private ClientEntity client;

    @OneToOne(mappedBy = "shoot", cascade = CascadeType.ALL, orphanRemoval = true)
    private InvoiceEntity invoice;

    @ManyToMany
    @JoinTable(
            name = "shoot_gear",
            joinColumns = @JoinColumn(name = "shoot_id"),
            inverseJoinColumns = @JoinColumn(name = "gear_item_id")
    )
    private Set<GearItemEntity> gearItems = new HashSet<>();

    @OneToMany(mappedBy = "shoot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MediaEntity> mediaItems = new ArrayList<>();

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
