package ro.fmi.awbd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.fmi.awbd.model.entity.InvoiceEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long> {
    Optional<InvoiceEntity> findByShootId(Long shootId);

    List<InvoiceEntity> findByShootStartAtBetween(OffsetDateTime from, OffsetDateTime to);
}
