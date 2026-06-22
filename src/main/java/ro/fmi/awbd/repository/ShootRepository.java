package ro.fmi.awbd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.fmi.awbd.model.entity.ShootEntity;

import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShootRepository extends JpaRepository<ShootEntity, Long> {
    List<ShootEntity> findByOwnerId(Long ownerId);

    List<ShootEntity> findByStartAtBetween(OffsetDateTime from, OffsetDateTime to);

    Page<ShootEntity> findByClientUserUsername(String username, Pageable pageable);
}
