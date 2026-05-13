package ro.fmi.awbd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.fmi.awbd.model.entity.MediaEntity;

import java.util.List;

public interface MediaRepository extends JpaRepository<MediaEntity, Long> {
    List<MediaEntity> findByShootId(Long shootId);
}
