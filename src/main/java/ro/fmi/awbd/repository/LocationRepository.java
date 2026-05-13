package ro.fmi.awbd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.fmi.awbd.model.entity.LocationEntity;

public interface LocationRepository extends JpaRepository<LocationEntity, Long> {
}
