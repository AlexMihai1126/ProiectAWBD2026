package ro.fmi.awbd.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.fmi.awbd.exception.ResourceNotFoundException;
import ro.fmi.awbd.model.dto.mapper.ClientMapper;
import ro.fmi.awbd.model.dto.request.ClientCreateRequest;
import ro.fmi.awbd.model.dto.request.ClientUpdateRequest;
import ro.fmi.awbd.model.dto.response.ClientResponse;
import ro.fmi.awbd.model.entity.ClientEntity;
import ro.fmi.awbd.repository.ClientRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Transactional(readOnly = true)
    public List<ClientResponse> getAllClients() {
        return clientRepository.findAll().stream().map(clientMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<ClientResponse> getClients(Pageable pageable) {
        return clientRepository.findAll(pageable).map(clientMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ClientResponse getClient(Long id) {
        return clientMapper.toResponse(findEntity(id));
    }

    @Transactional
    public ClientResponse createClient(ClientCreateRequest request) {
        ClientEntity saved = clientRepository.save(clientMapper.toEntity(request));
        log.info("Created client id={}", saved.getId());
        return clientMapper.toResponse(saved);
    }

    @Transactional
    public ClientResponse updateClient(Long clientId, ClientUpdateRequest request) {
        ClientEntity client = findEntity(clientId);
        clientMapper.updateEntity(request, client);
        ClientEntity saved = clientRepository.save(client);
        log.info("Updated client id={}", saved.getId());
        return clientMapper.toResponse(saved);
    }

    @Transactional
    public void deleteClient(Long id) {
        ClientEntity client = findEntity(id);
        clientRepository.delete(client);
        log.info("Deleted client id={}", id);
    }

    private ClientEntity findEntity(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Client", id));
    }
}
