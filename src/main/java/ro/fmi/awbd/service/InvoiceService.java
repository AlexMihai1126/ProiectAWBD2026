package ro.fmi.awbd.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ShootRepository shootRepository;
    private final ClientRepository clientRepository;
    private final InvoiceMapper invoiceMapper;

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(Long shootId) {
        log.debug("Fetching invoice for shoot id={}", shootId);
        InvoiceEntity invoice = invoiceRepository.findByShootId(shootId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for shoot " + shootId));
        return invoiceMapper.toResponse(invoice);
    }

    @Transactional(readOnly = true)
    public boolean existsForShoot(Long shootId) {
        boolean exists = invoiceRepository.findByShootId(shootId).isPresent();
        log.debug("Invoice exists for shoot id={}: {}", shootId, exists);
        return exists;
    }

    @Transactional
    public InvoiceResponse createInvoice(Long shootId, InvoiceCreateRequest request) {
        ShootEntity shoot = shootRepository.findById(shootId)
                .orElseThrow(() -> ResourceNotFoundException.of("Shoot", shootId));

        if (invoiceRepository.findByShootId(shootId).isPresent()) {
            log.warn("Duplicate invoice creation attempted for shoot id={}", shootId);
            throw new DuplicateResourceException("Invoice already exists for shoot " + shootId);
        }

        ClientEntity client = resolveClient(request.getClientId());

        InvoiceEntity invoice = invoiceMapper.toEntity(request);
        invoice.setShoot(shoot);
        invoice.setClient(client);

        if (invoice.getStatus() == null) {
            invoice.setStatus(InvoiceStatus.DRAFT);
        }
        if (invoice.getPaidAt() != null && invoice.getStatus() == InvoiceStatus.DRAFT) {
            invoice.setStatus(InvoiceStatus.PAID);
        }

        InvoiceEntity saved = invoiceRepository.save(invoice);
        log.info("Created invoice id={} for shoot id={}", saved.getId(), shootId);
        return invoiceMapper.toResponse(saved);
    }

    @Transactional
    public InvoiceResponse updateInvoice(Long shootId, InvoiceUpdateRequest request) {
        InvoiceEntity invoice = invoiceRepository.findByShootId(shootId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for shoot " + shootId));

        invoiceMapper.updateEntity(request, invoice);

        if (request.getClientId() != null) {
            invoice.setClient(resolveClient(request.getClientId()));
        }

        if (request.getPaidAt() != null && request.getStatus() == null && invoice.getStatus() == InvoiceStatus.DRAFT) {
            invoice.setStatus(InvoiceStatus.PAID);
        }

        InvoiceEntity saved = invoiceRepository.save(invoice);
        log.info("Updated invoice id={}", saved.getId());
        return invoiceMapper.toResponse(saved);
    }

    @Transactional
    public void deleteInvoice(Long shootId) {
        InvoiceEntity invoice = invoiceRepository.findByShootId(shootId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for shoot " + shootId));
        invoiceRepository.delete(invoice);
        log.info("Deleted invoice for shoot id={}", shootId);
    }

    private ClientEntity resolveClient(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> ResourceNotFoundException.of("Client", clientId));
    }
}
