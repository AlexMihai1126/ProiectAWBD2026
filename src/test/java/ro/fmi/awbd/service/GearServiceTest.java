package ro.fmi.awbd.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.fmi.awbd.exception.ResourceNotFoundException;
import ro.fmi.awbd.model.dto.mapper.GearMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ro.fmi.awbd.model.dto.request.GearCreateRequest;
import ro.fmi.awbd.model.dto.request.GearUpdateRequest;
import ro.fmi.awbd.model.dto.response.GearResponse;
import ro.fmi.awbd.model.entity.GearItemEntity;
import ro.fmi.awbd.model.entity.security.User;
import ro.fmi.awbd.model.enums.GearType;
import ro.fmi.awbd.repository.GearItemRepository;
import ro.fmi.awbd.repository.security.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GearServiceTest {

    @Mock
    private GearItemRepository gearItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GearMapper gearMapper;

    @InjectMocks
    private GearService gearService;

    @Test
    void createGearResolvesOwner() {
        GearCreateRequest request = GearCreateRequest.builder()
                .type(GearType.CAMERA_BODY)
                .brand("Canon")
                .model("R5")
                .ownerId(7L)
                .build();
        User owner = User.builder().id(7L).username("admin").build();
        GearItemEntity mapped = GearItemEntity.builder().brand("Canon").build();
        GearItemEntity saved = GearItemEntity.builder().id(1L).brand("Canon").owner(owner).build();
        GearResponse response = GearResponse.builder().id(1L).brand("Canon").build();
        when(gearMapper.toEntity(request)).thenReturn(mapped);
        when(userRepository.findById(7L)).thenReturn(Optional.of(owner));
        when(gearItemRepository.save(mapped)).thenAnswer(invocation -> {
            assertThat(((GearItemEntity) invocation.getArgument(0)).getOwner()).isEqualTo(owner);
            return saved;
        });
        when(gearMapper.toResponse(saved)).thenReturn(response);

        assertThat(gearService.createGear(request)).isEqualTo(response);
    }

    @Test
    void createGearThrowsWhenOwnerMissing() {
        GearCreateRequest request = GearCreateRequest.builder().ownerId(8L).build();
        GearItemEntity mapped = GearItemEntity.builder().build();
        when(gearMapper.toEntity(request)).thenReturn(mapped);
        when(userRepository.findById(8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gearService.createGear(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteGearRemovesEntity() {
        GearItemEntity entity = GearItemEntity.builder().id(2L).build();
        when(gearItemRepository.findById(2L)).thenReturn(Optional.of(entity));

        gearService.deleteGear(2L);

        verify(gearItemRepository).delete(entity);
    }

    @Test
    void getAllGearReturnsMappedList() {
        GearItemEntity entity = GearItemEntity.builder().id(3L).build();
        GearResponse response = GearResponse.builder().id(3L).build();
        when(gearItemRepository.findAll()).thenReturn(List.of(entity));
        when(gearMapper.toResponse(entity)).thenReturn(response);

        assertThat(gearService.getAllGear()).containsExactly(response);
    }

    @Test
    void getGearReturnsPage() {
        GearItemEntity entity = GearItemEntity.builder().id(4L).build();
        GearResponse response = GearResponse.builder().id(4L).build();
        PageRequest pageable = PageRequest.of(0, 5);
        when(gearItemRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity)));
        when(gearMapper.toResponse(entity)).thenReturn(response);

        assertThat(gearService.getGear(pageable).getTotalElements()).isEqualTo(1);
    }

    @Test
    void getGearByIdReturnsMappedEntity() {
        GearItemEntity entity = GearItemEntity.builder().id(5L).build();
        GearResponse response = GearResponse.builder().id(5L).build();
        when(gearItemRepository.findById(5L)).thenReturn(Optional.of(entity));
        when(gearMapper.toResponse(entity)).thenReturn(response);

        assertThat(gearService.getGearById(5L)).isEqualTo(response);
    }

    @Test
    void getGearByIdThrowsWhenMissing() {
        when(gearItemRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gearService.getGearById(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateGearUpdatesOwnerWhenProvided() {
        GearItemEntity entity = GearItemEntity.builder().id(6L).build();
        User owner = User.builder().id(9L).build();
        GearUpdateRequest request = GearUpdateRequest.builder().brand("Nikon").ownerId(9L).build();
        when(gearItemRepository.findById(6L)).thenReturn(Optional.of(entity));
        when(userRepository.findById(9L)).thenReturn(Optional.of(owner));
        when(gearItemRepository.save(entity)).thenReturn(entity);
        when(gearMapper.toResponse(entity)).thenReturn(GearResponse.builder().id(6L).build());

        gearService.updateGear(6L, request);

        assertThat(entity.getOwner()).isEqualTo(owner);
        verify(gearMapper).updateEntity(request, entity);
    }

    @Test
    void updateGearSkipsOwnerWhenNotProvided() {
        GearItemEntity entity = GearItemEntity.builder().id(7L).build();
        GearUpdateRequest request = GearUpdateRequest.builder().brand("Sony").build();
        when(gearItemRepository.findById(7L)).thenReturn(Optional.of(entity));
        when(gearItemRepository.save(entity)).thenReturn(entity);
        when(gearMapper.toResponse(entity)).thenReturn(GearResponse.builder().id(7L).build());

        gearService.updateGear(7L, request);

        verify(gearMapper).updateEntity(request, entity);
    }
}
