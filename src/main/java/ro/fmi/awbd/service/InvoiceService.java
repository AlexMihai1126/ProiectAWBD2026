package ro.fmi.awbd.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ro.fmi.awbd.model.entity.dto.mapper.InvoiceMapper;
import ro.fmi.awbd.model.entity.dto.request.InvoiceCreateRequest;
import ro.fmi.awbd.model.entity.dto.request.InvoiceUpdateRequest;
import ro.fmi.awbd.model.entity.dto.response.InvoiceResponse;
import ro.fmi.awbd.model.entity.ClientEntity;
import ro.fmi.awbd.model.entity.InvoiceEntity;
import ro.fmi.awbd.model.entity.ShootEntity;
import ro.fmi.awbd.model.enums.InvoiceStatus;
import ro.fmi.awbd.repository.ClientRepository;
import ro.fmi.awbd.repository.InvoiceRepository;
import ro.fmi.awbd.repository.ShootRepository;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ShootRepository shootRepository;
    private final ClientRepository clientRepository;
    private final InvoiceMapper invoiceMapper;

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(Long shootId) {
        InvoiceEntity invoice = invoiceRepository.findByShootId(shootId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
        return invoiceMapper.toResponse(invoice);
    }

    @Transactional
    public InvoiceResponse createInvoice(Long shootId, InvoiceCreateRequest request) {
        ShootEntity shoot = shootRepository.findById(shootId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shoot not found"));

        if (invoiceRepository.findByShootId(shootId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invoice already exists for shoot");
        }

        ClientEntity client = resolveClient(request.getClientId());

        InvoiceEntity invoice = invoiceMapper.toEntity(request);
        invoice.setShoot(shoot);
        invoice.setClient(client);

        if (invoice.getPaidAt() != null && invoice.getStatus() == InvoiceStatus.DRAFT) {
            invoice.setStatus(InvoiceStatus.PAID);
        }

        InvoiceEntity saved = invoiceRepository.save(invoice);
        return invoiceMapper.toResponse(saved);
    }

    @Transactional
    public InvoiceResponse updateInvoice(Long shootId, InvoiceUpdateRequest request) {
        InvoiceEntity invoice = invoiceRepository.findByShootId(shootId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));

        invoiceMapper.updateEntity(request, invoice);

        if (request.getClientId() != null) {
            invoice.setClient(resolveClient(request.getClientId()));
        }

        if (request.getPaidAt() != null && request.getStatus() == null && invoice.getStatus() == InvoiceStatus.DRAFT) {
            invoice.setStatus(InvoiceStatus.PAID);
        }

        InvoiceEntity saved = invoiceRepository.save(invoice);
        return invoiceMapper.toResponse(saved);
    }

    private ClientEntity resolveClient(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));
    }
}
