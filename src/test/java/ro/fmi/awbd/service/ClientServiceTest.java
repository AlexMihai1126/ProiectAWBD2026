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
import ro.fmi.awbd.repository.ClientRepository;

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
