package ro.fmi.awbd.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ro.fmi.awbd.exception.ResourceNotFoundException;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthorityRepository authorityRepository;

    @InjectMocks
    private ClientService clientService;

    @Test
    void getAllClientsReturnsMappedResults() {
        ClientEntity entity = ClientEntity.builder().id(1L).name("Alice").build();
        ClientResponse response = ClientResponse.builder().id(1L).name("Alice").build();
        when(clientRepository.findAll()).thenReturn(List.of(entity));
        when(clientMapper.toResponse(entity)).thenReturn(response);

        assertThat(clientService.getAllClients()).containsExactly(response);
    }

    @Test
    void getClientsReturnsPage() {
        ClientEntity entity = ClientEntity.builder().id(1L).name("Alice").build();
        ClientResponse response = ClientResponse.builder().id(1L).name("Alice").build();
        PageRequest pageable = PageRequest.of(0, 10);
        when(clientRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity)));
        when(clientMapper.toResponse(entity)).thenReturn(response);

        assertThat(clientService.getClients(pageable).getContent()).containsExactly(response);
    }

    @Test
    void getClientReturnsMappedEntity() {
        ClientEntity entity = ClientEntity.builder().id(2L).name("Bob").build();
        ClientResponse response = ClientResponse.builder().id(2L).name("Bob").build();
        when(clientRepository.findById(2L)).thenReturn(Optional.of(entity));
        when(clientMapper.toResponse(entity)).thenReturn(response);

        assertThat(clientService.getClient(2L)).isEqualTo(response);
    }

    @Test
    void getClientThrowsWhenMissing() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.getClient(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createClientPersistsAndReturnsResponse() {
        ClientCreateRequest request = ClientCreateRequest.builder().name("New").build();
        ClientEntity mapped = ClientEntity.builder().name("New").build();
        ClientEntity saved = ClientEntity.builder().id(3L).name("New").build();
        ClientResponse response = ClientResponse.builder().id(3L).name("New").build();
        when(clientMapper.toEntity(request)).thenReturn(mapped);
        when(clientRepository.save(mapped)).thenReturn(saved);
        when(clientMapper.toResponse(saved)).thenReturn(response);

        assertThat(clientService.createClient(request)).isEqualTo(response);
    }

    @Test
    void createClientLinksAccountAndGrantsClientRole() {
        ClientCreateRequest request = ClientCreateRequest.builder().name("Linked").userId(8L).build();
        ClientEntity mapped = ClientEntity.builder().name("Linked").build();
        User user = User.builder().id(8L).username("client").build();
        Authority role = Authority.builder().id(3L).role("ROLE_CLIENT").build();
        ClientEntity saved = ClientEntity.builder().id(9L).name("Linked").user(user).build();
        ClientResponse response = ClientResponse.builder().id(9L).userId(8L).username("client").build();
        when(clientMapper.toEntity(request)).thenReturn(mapped);
        when(clientRepository.existsByUserId(8L)).thenReturn(false);
        when(userRepository.findById(8L)).thenReturn(Optional.of(user));
        when(authorityRepository.findByRole("ROLE_CLIENT")).thenReturn(Optional.of(role));
        when(clientRepository.save(mapped)).thenReturn(saved);
        when(clientMapper.toResponse(saved)).thenReturn(response);

        assertThat(clientService.createClient(request)).isEqualTo(response);
        assertThat(mapped.getUser()).isEqualTo(user);
        assertThat(user.getAuthorities()).extracting(Authority::getRole).containsExactly("ROLE_CLIENT");
        verify(userRepository).save(user);
    }

    @Test
    void updateClientUpdatesExistingEntity() {
        ClientUpdateRequest request = ClientUpdateRequest.builder().name("Updated").build();
        ClientEntity existing = ClientEntity.builder().id(4L).name("Old").build();
        ClientEntity saved = ClientEntity.builder().id(4L).name("Updated").build();
        ClientResponse response = ClientResponse.builder().id(4L).name("Updated").build();
        when(clientRepository.findById(4L)).thenReturn(Optional.of(existing));
        when(clientRepository.save(existing)).thenReturn(saved);
        when(clientMapper.toResponse(saved)).thenReturn(response);

        assertThat(clientService.updateClient(4L, request)).isEqualTo(response);
        verify(clientMapper).updateEntity(request, existing);
    }

    @Test
    void deleteClientRemovesEntity() {
        ClientEntity existing = ClientEntity.builder().id(5L).name("Delete me").build();
        when(clientRepository.findById(5L)).thenReturn(Optional.of(existing));

        clientService.deleteClient(5L);

        verify(clientRepository).delete(existing);
    }
}
