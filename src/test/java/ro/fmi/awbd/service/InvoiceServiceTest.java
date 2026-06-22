package ro.fmi.awbd.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.fmi.awbd.exception.DuplicateResourceException;
import ro.fmi.awbd.exception.ResourceNotFoundException;
import ro.fmi.awbd.model.dto.mapper.InvoiceMapper;
import ro.fmi.awbd.model.dto.request.InvoiceCreateRequest;
import ro.fmi.awbd.model.dto.request.InvoiceUpdateRequest;
import ro.fmi.awbd.model.dto.response.InvoiceResponse;
import ro.fmi.awbd.model.entity.ClientEntity;
import ro.fmi.awbd.model.entity.InvoiceEntity;
import ro.fmi.awbd.model.entity.ShootEntity;
import ro.fmi.awbd.model.enums.InvoiceStatus;
import ro.fmi.awbd.repository.ClientRepository;
import ro.fmi.awbd.repository.InvoiceRepository;
import ro.fmi.awbd.repository.ShootRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ShootRepository shootRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private InvoiceMapper invoiceMapper;

    @InjectMocks
    private InvoiceService invoiceService;

    @Test
    void createInvoicePersistsDraftInvoice() {
        ShootEntity shoot = ShootEntity.builder().id(1L).build();
        ClientEntity client = ClientEntity.builder().id(2L).name("Client").build();
        InvoiceCreateRequest request = InvoiceCreateRequest.builder()
                .clientId(2L)
                .amount(new BigDecimal("100.00"))
                .build();
        InvoiceEntity mapped = InvoiceEntity.builder().amount(new BigDecimal("100.00")).build();
        InvoiceEntity saved = InvoiceEntity.builder().id(3L).status(InvoiceStatus.DRAFT).build();
        InvoiceResponse response = InvoiceResponse.builder().id(3L).build();

        when(shootRepository.findById(1L)).thenReturn(Optional.of(shoot));
        when(invoiceRepository.findByShootId(1L)).thenReturn(Optional.empty());
        when(clientRepository.findById(2L)).thenReturn(Optional.of(client));
        when(invoiceMapper.toEntity(request)).thenReturn(mapped);
        when(invoiceRepository.save(mapped)).thenReturn(saved);
        when(invoiceMapper.toResponse(saved)).thenReturn(response);

        assertThat(invoiceService.createInvoice(1L, request)).isEqualTo(response);
        assertThat(mapped.getShoot()).isEqualTo(shoot);
        assertThat(mapped.getClient()).isEqualTo(client);
        assertThat(mapped.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
    }

    @Test
    void createInvoiceThrowsWhenAlreadyExists() {
        when(shootRepository.findById(1L)).thenReturn(Optional.of(ShootEntity.builder().id(1L).build()));
        when(invoiceRepository.findByShootId(1L)).thenReturn(Optional.of(InvoiceEntity.builder().id(9L).build()));

        assertThatThrownBy(() -> invoiceService.createInvoice(1L, InvoiceCreateRequest.builder().clientId(1L).build()))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void getInvoiceThrowsWhenMissing() {
        when(invoiceRepository.findByShootId(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.getInvoice(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createInvoiceMarksPaidWhenPaidAtProvided() {
        ShootEntity shoot = ShootEntity.builder().id(6L).build();
        ClientEntity client = ClientEntity.builder().id(7L).build();
        InvoiceCreateRequest request = InvoiceCreateRequest.builder()
                .clientId(7L)
                .paidAt(OffsetDateTime.now())
                .build();
        InvoiceEntity mapped = InvoiceEntity.builder().paidAt(request.getPaidAt()).build();
        InvoiceEntity saved = InvoiceEntity.builder().id(8L).status(InvoiceStatus.PAID).build();
        InvoiceResponse response = InvoiceResponse.builder().id(8L).build();

        when(shootRepository.findById(6L)).thenReturn(Optional.of(shoot));
        when(invoiceRepository.findByShootId(6L)).thenReturn(Optional.empty());
        when(clientRepository.findById(7L)).thenReturn(Optional.of(client));
        when(invoiceMapper.toEntity(request)).thenReturn(mapped);
        when(invoiceRepository.save(mapped)).thenReturn(saved);
        when(invoiceMapper.toResponse(saved)).thenReturn(response);

        assertThat(invoiceService.createInvoice(6L, request)).isEqualTo(response);
        assertThat(mapped.getStatus()).isEqualTo(InvoiceStatus.PAID);
    }

    @Test
    void getInvoiceReturnsMappedEntity() {
        InvoiceEntity invoice = InvoiceEntity.builder().id(10L).build();
        InvoiceResponse response = InvoiceResponse.builder().id(10L).build();
        when(invoiceRepository.findByShootId(1L)).thenReturn(Optional.of(invoice));
        when(invoiceMapper.toResponse(invoice)).thenReturn(response);

        assertThat(invoiceService.getInvoice(1L)).isEqualTo(response);
    }

    @Test
    void existsForShootReturnsTrueWhenPresent() {
        when(invoiceRepository.findByShootId(2L)).thenReturn(Optional.of(InvoiceEntity.builder().id(1L).build()));

        assertThat(invoiceService.existsForShoot(2L)).isTrue();
    }

    @Test
    void existsForShootReturnsFalseWhenMissing() {
        when(invoiceRepository.findByShootId(3L)).thenReturn(Optional.empty());

        assertThat(invoiceService.existsForShoot(3L)).isFalse();
    }

    @Test
    void updateInvoiceUpdatesClientAndMarksPaid() {
        InvoiceEntity invoice = InvoiceEntity.builder().id(4L).status(InvoiceStatus.DRAFT).build();
        ClientEntity client = ClientEntity.builder().id(8L).build();
        InvoiceUpdateRequest request = InvoiceUpdateRequest.builder()
                .clientId(8L)
                .paidAt(OffsetDateTime.now())
                .build();
        InvoiceEntity saved = InvoiceEntity.builder().id(4L).status(InvoiceStatus.PAID).build();
        when(invoiceRepository.findByShootId(7L)).thenReturn(Optional.of(invoice));
        when(clientRepository.findById(8L)).thenReturn(Optional.of(client));
        when(invoiceRepository.save(invoice)).thenReturn(saved);
        when(invoiceMapper.toResponse(saved)).thenReturn(InvoiceResponse.builder().id(4L).build());

        invoiceService.updateInvoice(7L, request);

        assertThat(invoice.getClient()).isEqualTo(client);
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        verify(invoiceMapper).updateEntity(request, invoice);
    }

    @Test
    void deleteInvoiceRemovesEntity() {
        InvoiceEntity invoice = InvoiceEntity.builder().id(5L).build();
        when(invoiceRepository.findByShootId(9L)).thenReturn(Optional.of(invoice));

        invoiceService.deleteInvoice(9L);

        verify(invoiceRepository).delete(invoice);
    }

    @Test
    void createInvoiceThrowsWhenShootMissing() {
        when(shootRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.createInvoice(100L, InvoiceCreateRequest.builder().clientId(1L).build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createInvoiceThrowsWhenClientMissing() {
        when(shootRepository.findById(1L)).thenReturn(Optional.of(ShootEntity.builder().id(1L).build()));
        when(invoiceRepository.findByShootId(1L)).thenReturn(Optional.empty());
        when(clientRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.createInvoice(1L, InvoiceCreateRequest.builder().clientId(2L).build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
