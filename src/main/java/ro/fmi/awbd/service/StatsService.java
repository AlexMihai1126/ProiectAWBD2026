package ro.fmi.awbd.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ro.fmi.awbd.model.entity.InvoiceEntity;
import ro.fmi.awbd.model.entity.ShootEntity;
import ro.fmi.awbd.model.enums.InvoiceStatus;
import ro.fmi.awbd.model.enums.ShootStatus;
import ro.fmi.awbd.repository.InvoiceRepository;
import ro.fmi.awbd.repository.ShootRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final ShootRepository shootRepository;
    private final InvoiceRepository invoiceRepository;

    @Transactional(readOnly = true)
    public StatsResponse getStats(OffsetDateTime from, OffsetDateTime to) {
        if (from == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from is required");
        }
        if (to == null) {
            to = OffsetDateTime.now();
        }
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must be before to");
        }

        List<ShootEntity> shoots = shootRepository.findByStartAtBetween(from, to);
        long planned = shoots.stream().filter(shoot -> shoot.getStatus() == ShootStatus.PLANNED).count();
        long done = shoots.stream().filter(shoot -> shoot.getStatus() == ShootStatus.DONE).count();

        List<InvoiceEntity> invoices = invoiceRepository.findByShootStartAtBetween(from, to);
        BigDecimal paid = invoices.stream()
                .filter(invoice -> invoice.getStatus() == InvoiceStatus.PAID)
                .map(InvoiceEntity::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal receivable = invoices.stream()
                .filter(invoice -> invoice.getStatus() != InvoiceStatus.PAID)
                .map(InvoiceEntity::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return StatsResponse.builder()
                .paidAmount(paid)
                .receivableAmount(receivable)
                .plannedShoots(planned)
                .doneShoots(done)
                .build();
    }
}
