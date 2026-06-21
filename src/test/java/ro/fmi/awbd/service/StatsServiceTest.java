package ro.fmi.awbd.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.fmi.awbd.exception.BadRequestException;
import ro.fmi.awbd.model.entity.InvoiceEntity;
import ro.fmi.awbd.model.entity.ShootEntity;
import ro.fmi.awbd.model.enums.InvoiceStatus;
import ro.fmi.awbd.model.enums.ShootStatus;
import ro.fmi.awbd.repository.InvoiceRepository;
import ro.fmi.awbd.repository.ShootRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private ShootRepository shootRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private StatsService statsService;

    @Test
    void getStatsAggregatesShootsAndInvoices() {
        OffsetDateTime from = OffsetDateTime.parse("2026-01-01T00:00:00Z");
        OffsetDateTime to = OffsetDateTime.parse("2026-01-31T23:59:59Z");
        ShootEntity planned = ShootEntity.builder().status(ShootStatus.PLANNED).build();
        ShootEntity done = ShootEntity.builder().status(ShootStatus.DONE).build();
        InvoiceEntity paid = InvoiceEntity.builder()
                .status(InvoiceStatus.PAID)
                .amount(new BigDecimal("150.00"))
                .build();
        InvoiceEntity draft = InvoiceEntity.builder()
                .status(InvoiceStatus.DRAFT)
                .amount(new BigDecimal("50.00"))
                .build();

        when(shootRepository.findByStartAtBetween(from, to)).thenReturn(List.of(planned, done));
        when(invoiceRepository.findByShootStartAtBetween(from, to)).thenReturn(List.of(paid, draft));

        var stats = statsService.getStats(from, to);

        assertThat(stats.getPlannedShoots()).isEqualTo(1);
        assertThat(stats.getDoneShoots()).isEqualTo(1);
        assertThat(stats.getPaidAmount()).isEqualByComparingTo("150.00");
        assertThat(stats.getReceivableAmount()).isEqualByComparingTo("50.00");
    }

    @Test
    void getStatsThrowsWhenFromMissing() {
        assertThatThrownBy(() -> statsService.getStats(null, OffsetDateTime.now()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getStatsThrowsWhenRangeInvalid() {
        OffsetDateTime from = OffsetDateTime.parse("2026-02-01T00:00:00Z");
        OffsetDateTime to = OffsetDateTime.parse("2026-01-01T00:00:00Z");

        assertThatThrownBy(() -> statsService.getStats(from, to))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getStatsUsesCurrentTimeWhenToMissing() {
        OffsetDateTime from = OffsetDateTime.parse("2026-01-01T00:00:00Z");
        when(shootRepository.findByStartAtBetween(eq(from), any(OffsetDateTime.class))).thenReturn(List.of());
        when(invoiceRepository.findByShootStartAtBetween(eq(from), any(OffsetDateTime.class))).thenReturn(List.of());

        var stats = statsService.getStats(from, null);

        assertThat(stats.getPlannedShoots()).isZero();
        assertThat(stats.getPaidAmount()).isEqualByComparingTo("0");
    }

    @Test
    void getStatsIgnoresInvoicesWithNullAmounts() {
        OffsetDateTime from = OffsetDateTime.parse("2026-01-01T00:00:00Z");
        OffsetDateTime to = OffsetDateTime.parse("2026-01-31T00:00:00Z");
        when(shootRepository.findByStartAtBetween(from, to)).thenReturn(List.of());
        when(invoiceRepository.findByShootStartAtBetween(from, to)).thenReturn(List.of(
                InvoiceEntity.builder().status(InvoiceStatus.PAID).amount(null).build(),
                InvoiceEntity.builder().status(InvoiceStatus.DRAFT).amount(null).build()
        ));

        var stats = statsService.getStats(from, to);

        assertThat(stats.getPaidAmount()).isEqualByComparingTo("0");
        assertThat(stats.getReceivableAmount()).isEqualByComparingTo("0");
    }
}
