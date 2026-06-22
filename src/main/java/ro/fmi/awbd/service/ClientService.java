package ro.fmi.awbd.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.fmi.awbd.exception.ResourceNotFoundException;
import ro.fmi.awbd.exception.DuplicateResourceException;
import ro.fmi.awbd.model.dto.mapper.ClientMapper;
import ro.fmi.awbd.model.dto.request.ClientCreateRequest;
import ro.fmi.awbd.model.dto.request.ClientUpdateRequest;
import ro.fmi.awbd.model.dto.response.ClientResponse;
import ro.fmi.awbd.model.entity.ClientEntity;
import ro.fmi.awbd.model.entity.security.Authority;
import ro.fmi.awbd.model.entity.security.User;
import ro.fmi.awbd.repository.ClientRepository;
import ro.fmi.awbd.repository.security.AuthorityRepository;
import ro.fmi.awbd.repository.security.UserRepository;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;

    @Transactional(readOnly = true)
    public List<ClientResponse> getAllClients() {
        List<ClientResponse> clients = clientRepository.findAll().stream().map(clientMapper::toResponse).toList();
        log.debug("Listed all clients, count={}", clients.size());
        return clients;
    }

    @Transactional(readOnly = true)
    public Page<ClientResponse> getClients(Pageable pageable) {
        Page<ClientResponse> page = clientRepository.findAll(pageable).map(clientMapper::toResponse);
        log.debug("Listed clients page={}, size={}, total={}",
                pageable.getPageNumber(), pageable.getPageSize(), page.getTotalElements());
        return page;
    }

    @Transactional(readOnly = true)
    public ClientResponse getClient(Long id) {
        log.debug("Fetching client id={}", id);
        return clientMapper.toResponse(findEntity(id));
    }

    @Transactional
    public ClientResponse createClient(ClientCreateRequest request) {
        ClientEntity client = clientMapper.toEntity(request);
        client.setUser(resolveAvailableUser(request.getUserId(), null));
        ClientEntity saved = clientRepository.save(client);
        log.info("Created client id={}", saved.getId());
        return clientMapper.toResponse(saved);
    }

    @Transactional
    public ClientResponse updateClient(Long clientId, ClientUpdateRequest request) {
        ClientEntity client = findEntity(clientId);
        clientMapper.updateEntity(request, client);
        client.setUser(resolveAvailableUser(request.getUserId(), clientId));
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

    private User resolveAvailableUser(Long userId, Long currentClientId) {
        if (userId == null) {
            return null;
        }
        boolean alreadyLinked = currentClientId == null
                ? clientRepository.existsByUserId(userId)
                : clientRepository.existsByUserIdAndIdNot(userId, currentClientId);
        if (alreadyLinked) {
            throw new DuplicateResourceException("Login account is already linked to another client");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        Authority clientRole = authorityRepository.findByRole("ROLE_CLIENT")
                .orElseThrow(() -> new ResourceNotFoundException("ROLE_CLIENT is not configured"));
        HashSet<Authority> roles = new HashSet<>();
        if (user.getAuthorities() != null) {
            roles.addAll(user.getAuthorities());
        }
        boolean hasClientRole = roles.stream().anyMatch(a -> "ROLE_CLIENT".equals(a.getRole()));
        if (!hasClientRole) {
            roles.add(clientRole);
            user.setAuthorities(roles);
            userRepository.save(user);
        }
        return user;
    }
}
