package ro.fmi.awbd.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ro.fmi.awbd.model.dto.mapper.ClientMapper;
import ro.fmi.awbd.model.dto.request.ClientCreateRequest;
import ro.fmi.awbd.model.dto.request.ClientUpdateRequest;
import ro.fmi.awbd.model.dto.response.ClientResponse;
import ro.fmi.awbd.model.entity.ClientEntity;
import ro.fmi.awbd.repository.ClientRepository;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Transactional
    public ClientResponse createClient(ClientCreateRequest request) {
        ClientEntity client = clientMapper.toEntity(request);
        ClientEntity saved = clientRepository.save(client);
        return clientMapper.toResponse(saved);
    }

    @Transactional
    public ClientResponse updateClient(Long clientId, ClientUpdateRequest request) {
        ClientEntity client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));

        clientMapper.updateEntity(request, client);
        ClientEntity saved = clientRepository.save(client);
        return clientMapper.toResponse(saved);
    }
}
