package ro.fmi.awbd.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.PageRequest;
import ro.fmi.awbd.exception.ResourceNotFoundException;
import ro.fmi.awbd.model.dto.mapper.ShootMapper;
import ro.fmi.awbd.model.dto.request.ShootCreateRequest;
import ro.fmi.awbd.model.dto.request.ShootUpdateRequest;
import ro.fmi.awbd.model.dto.response.ShootListItemResponse;
import ro.fmi.awbd.model.dto.response.ShootResponse;
import ro.fmi.awbd.model.entity.GearItemEntity;
import ro.fmi.awbd.model.entity.ClientEntity;
import ro.fmi.awbd.model.entity.LocationEntity;
import ro.fmi.awbd.model.entity.ShootEntity;
import ro.fmi.awbd.model.entity.security.User;
import ro.fmi.awbd.model.enums.ShootStatus;
import ro.fmi.awbd.repository.GearItemRepository;
import ro.fmi.awbd.repository.ClientRepository;
import ro.fmi.awbd.repository.LocationRepository;
import ro.fmi.awbd.repository.ShootRepository;
import ro.fmi.awbd.repository.security.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShootServiceTest {

    @Mock
    private ShootRepository shootRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private GearItemRepository gearItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ShootMapper shootMapper;

    @InjectMocks
    private ShootService shootService;

    @Test
    void createShootLinksOwnerLocationAndGear() {
        ShootCreateRequest request = ShootCreateRequest.builder()
                .title("Portrait")
                .ownerId(1L)
                .locationId(2L)
                .clientId(4L)
                .gearItemIds(Set.of(3L))
                .startAt(OffsetDateTime.now().plusDays(1))
                .build();
        User owner = User.builder().id(1L).username("admin").build();
        LocationEntity location = LocationEntity.builder().id(2L).name("Studio").build();
        GearItemEntity gear = GearItemEntity.builder().id(3L).build();
        ClientEntity client = ClientEntity.builder().id(4L).name("Client").build();
        ShootEntity mapped = ShootEntity.builder().title("Portrait").build();
        ShootEntity saved = ShootEntity.builder().id(10L).title("Portrait").build();
        ShootResponse response = ShootResponse.builder().id(10L).title("Portrait").build();

        when(shootMapper.toEntity(request)).thenReturn(mapped);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(location));
        when(clientRepository.findById(4L)).thenReturn(Optional.of(client));
        when(gearItemRepository.findAllById(Set.of(3L))).thenReturn(List.of(gear));
        when(shootRepository.save(mapped)).thenReturn(saved);
        when(shootMapper.toResponse(saved)).thenReturn(response);

        assertThat(shootService.createShoot(request)).isEqualTo(response);
        assertThat(mapped.getOwner()).isEqualTo(owner);
        assertThat(mapped.getLocation()).isEqualTo(location);
        assertThat(mapped.getClient()).isEqualTo(client);
        assertThat(mapped.getGearItems()).containsExactly(gear);
        assertThat(mapped.getStatus()).isEqualTo(ShootStatus.PLANNED);
    }

    @Test
    void getShootByIdReturnsMappedEntity() {
        ShootEntity entity = ShootEntity.builder().id(11L).title("Event").build();
        ShootResponse response = ShootResponse.builder().id(11L).title("Event").build();
        when(shootRepository.findById(11L)).thenReturn(Optional.of(entity));
        when(shootMapper.toResponse(entity)).thenReturn(response);

        assertThat(shootService.getShootById(11L)).isEqualTo(response);
    }

    @Test
    void getAllShootsReturnsMappedList() {
        ShootEntity entity = ShootEntity.builder().id(1L).title("A").build();
        ShootListItemResponse item = ShootListItemResponse.builder().id(1L).title("A").build();
        when(shootRepository.findAll()).thenReturn(List.of(entity));
        when(shootMapper.toListItemResponse(entity)).thenReturn(item);

        assertThat(shootService.getAllShoots()).containsExactly(item);
    }

    @Test
    void getShootsReturnsPage() {
        ShootEntity entity = ShootEntity.builder().id(2L).build();
        ShootListItemResponse item = ShootListItemResponse.builder().id(2L).build();
        PageRequest pageable = PageRequest.of(0, 10);
        when(shootRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity)));
        when(shootMapper.toListItemResponse(entity)).thenReturn(item);

        assertThat(shootService.getShoots(pageable).getContent()).containsExactly(item);
    }

    @Test
    void getShootsForClientReturnsOnlyMappedClientShoots() {
        ShootEntity entity = ShootEntity.builder().id(30L).build();
        ShootListItemResponse item = ShootListItemResponse.builder().id(30L).build();
        PageRequest pageable = PageRequest.of(0, 10);
        when(shootRepository.findByClientUserUsername("client", pageable))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(shootMapper.toListItemResponse(entity)).thenReturn(item);

        assertThat(shootService.getShootsForClient("client", pageable).getContent())
                .containsExactly(item);
    }

    @Test
    void clientCanOpenOwnShoot() {
        User account = User.builder().username("client").build();
        ClientEntity client = ClientEntity.builder().user(account).build();
        ShootEntity entity = ShootEntity.builder().id(31L).client(client).build();
        ShootResponse response = ShootResponse.builder().id(31L).build();
        when(shootRepository.findById(31L)).thenReturn(Optional.of(entity));
        when(shootMapper.toResponse(entity)).thenReturn(response);

        assertThat(shootService.getShootByIdForClient(31L, "client")).isEqualTo(response);
    }

    @Test
    void clientCannotOpenAnotherClientsShoot() {
        User account = User.builder().username("someone-else").build();
        ClientEntity client = ClientEntity.builder().user(account).build();
        ShootEntity entity = ShootEntity.builder().id(32L).client(client).build();
        when(shootRepository.findById(32L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> shootService.getShootByIdForClient(32L, "client"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getShootsForOwnerReturnsMappedList() {
        ShootEntity entity = ShootEntity.builder().id(3L).build();
        ShootListItemResponse item = ShootListItemResponse.builder().id(3L).build();
        when(userRepository.existsById(5L)).thenReturn(true);
        when(shootRepository.findByOwnerId(5L)).thenReturn(List.of(entity));
        when(shootMapper.toListItemResponse(entity)).thenReturn(item);

        assertThat(shootService.getShootsForOwner(5L)).containsExactly(item);
    }

    @Test
    void getShootsForOwnerThrowsWhenOwnerMissing() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> shootService.getShootsForOwner(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getShootByIdThrowsWhenMissing() {
        when(shootRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shootService.getShootById(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createShootWithoutGearUsesEmptySet() {
        ShootCreateRequest request = ShootCreateRequest.builder()
                .title("No gear")
                .status(ShootStatus.DONE)
                .ownerId(1L)
                .locationId(2L)
                .clientId(4L)
                .startAt(OffsetDateTime.now())
                .build();
        ShootEntity mapped = ShootEntity.builder().title("No gear").status(ShootStatus.DONE).build();
        ShootEntity saved = ShootEntity.builder().id(12L).build();
        when(shootMapper.toEntity(request)).thenReturn(mapped);
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(LocationEntity.builder().id(2L).build()));
        when(clientRepository.findById(4L)).thenReturn(Optional.of(ClientEntity.builder().id(4L).build()));
        when(shootRepository.save(mapped)).thenReturn(saved);
        when(shootMapper.toResponse(saved)).thenReturn(ShootResponse.builder().id(12L).build());

        shootService.createShoot(request);

        assertThat(mapped.getGearItems()).isEmpty();
        assertThat(mapped.getStatus()).isEqualTo(ShootStatus.DONE);
    }

    @Test
    void createShootThrowsWhenGearItemsMissing() {
        ShootCreateRequest request = ShootCreateRequest.builder()
                .title("Bad gear")
                .ownerId(1L)
                .locationId(2L)
                .clientId(4L)
                .gearItemIds(Set.of(3L, 4L))
                .startAt(OffsetDateTime.now())
                .build();
        when(shootMapper.toEntity(request)).thenReturn(ShootEntity.builder().build());
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(LocationEntity.builder().id(2L).build()));
        when(clientRepository.findById(4L)).thenReturn(Optional.of(ClientEntity.builder().id(4L).build()));
        when(gearItemRepository.findAllById(Set.of(3L, 4L))).thenReturn(List.of(GearItemEntity.builder().id(3L).build()));

        assertThatThrownBy(() -> shootService.createShoot(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateShootUpdatesOptionalRelations() {
        ShootEntity existing = ShootEntity.builder().id(20L).title("Old").build();
        ShootUpdateRequest request = ShootUpdateRequest.builder()
                .title("New")
                .ownerId(1L)
                .locationId(2L)
                .clientId(4L)
                .gearItemIds(Set.of(3L))
                .build();
        User owner = User.builder().id(1L).build();
        LocationEntity location = LocationEntity.builder().id(2L).build();
        GearItemEntity gear = GearItemEntity.builder().id(3L).build();
        ClientEntity client = ClientEntity.builder().id(4L).build();
        ShootEntity saved = ShootEntity.builder().id(20L).title("New").build();

        when(shootRepository.findById(20L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(location));
        when(clientRepository.findById(4L)).thenReturn(Optional.of(client));
        when(gearItemRepository.findAllById(Set.of(3L))).thenReturn(List.of(gear));
        when(shootRepository.save(existing)).thenReturn(saved);
        when(shootMapper.toResponse(saved)).thenReturn(ShootResponse.builder().id(20L).title("New").build());

        shootService.updateShoot(20L, request);

        assertThat(existing.getOwner()).isEqualTo(owner);
        assertThat(existing.getLocation()).isEqualTo(location);
        assertThat(existing.getClient()).isEqualTo(client);
        assertThat(existing.getGearItems()).containsExactly(gear);
        verify(shootMapper).updateEntity(request, existing);
    }

    @Test
    void updateShootSkipsNullRelationUpdates() {
        ShootEntity existing = ShootEntity.builder().id(21L).build();
        ShootUpdateRequest request = ShootUpdateRequest.builder().title("Only title").build();
        when(shootRepository.findById(21L)).thenReturn(Optional.of(existing));
        when(shootRepository.save(existing)).thenReturn(existing);
        when(shootMapper.toResponse(existing)).thenReturn(ShootResponse.builder().id(21L).build());

        shootService.updateShoot(21L, request);

        verify(shootMapper).updateEntity(request, existing);
    }

    @Test
    void deleteShootRemovesEntity() {
        ShootEntity existing = ShootEntity.builder().id(22L).build();
        when(shootRepository.findById(22L)).thenReturn(Optional.of(existing));

        shootService.deleteShoot(22L);

        verify(shootRepository).delete(existing);
    }
}
