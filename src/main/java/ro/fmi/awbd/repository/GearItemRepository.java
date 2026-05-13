package ro.fmi.awbd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.fmi.awbd.model.entity.GearItemEntity;

public interface GearItemRepository extends JpaRepository<GearItemEntity, Long> {
}
