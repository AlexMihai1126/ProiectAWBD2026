package ro.fmi.awbd.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.fmi.awbd.exception.ResourceNotFoundException;
import ro.fmi.awbd.model.dto.mapper.MediaMapper;
import ro.fmi.awbd.model.dto.request.MediaCreateRequest;
import ro.fmi.awbd.model.dto.request.MediaUpdateRequest;
import ro.fmi.awbd.model.dto.response.MediaResponse;
import ro.fmi.awbd.model.entity.MediaEntity;
import ro.fmi.awbd.model.entity.ShootEntity;
import ro.fmi.awbd.model.enums.MediaType;
import ro.fmi.awbd.repository.MediaRepository;
import ro.fmi.awbd.repository.ShootRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private ShootRepository shootRepository;

    @Mock
    private MediaMapper mediaMapper;

    @InjectMocks
    private MediaService mediaService;

    @Test
    void listMediaReturnsItemsForShoot() {
        ShootEntity shoot = ShootEntity.builder().id(1L).build();
        MediaEntity media = MediaEntity.builder().id(2L).shoot(shoot).build();
        MediaResponse response = MediaResponse.builder().id(2L).build();
        when(shootRepository.existsById(1L)).thenReturn(true);
        when(mediaRepository.findByShootId(1L)).thenReturn(List.of(media));
        when(mediaMapper.toResponse(media)).thenReturn(response);

        assertThat(mediaService.listMedia(1L)).containsExactly(response);
    }

    @Test
    void createMediaLinksShoot() {
        ShootEntity shoot = ShootEntity.builder().id(3L).build();
        MediaCreateRequest request = MediaCreateRequest.builder()
                .mediaType(MediaType.EDITED_PHOTO)
                .fileRef("img.jpg")
                .build();
        MediaEntity mapped = MediaEntity.builder().fileRef("img.jpg").build();
        MediaEntity saved = MediaEntity.builder().id(4L).fileRef("img.jpg").shoot(shoot).build();
        MediaResponse response = MediaResponse.builder().id(4L).build();
        when(shootRepository.findById(3L)).thenReturn(Optional.of(shoot));
        when(mediaMapper.toEntity(request)).thenReturn(mapped);
        when(mediaRepository.save(mapped)).thenReturn(saved);
        when(mediaMapper.toResponse(saved)).thenReturn(response);

        assertThat(mediaService.createMedia(3L, request)).isEqualTo(response);
        assertThat(mapped.getShoot()).isEqualTo(shoot);
    }

    @Test
    void getMediaThrowsWhenShootMismatch() {
        ShootEntity shoot = ShootEntity.builder().id(5L).build();
        MediaEntity media = MediaEntity.builder().id(6L).shoot(shoot).build();
        when(mediaRepository.findById(6L)).thenReturn(Optional.of(media));

        assertThatThrownBy(() -> mediaService.getMedia(99L, 6L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteMediaRemovesEntity() {
        ShootEntity shoot = ShootEntity.builder().id(7L).build();
        MediaEntity media = MediaEntity.builder().id(8L).shoot(shoot).build();
        when(mediaRepository.findById(8L)).thenReturn(Optional.of(media));

        mediaService.deleteMedia(7L, 8L);

        verify(mediaRepository).delete(media);
    }

    @Test
    void listMediaThrowsWhenShootMissing() {
        when(shootRepository.existsById(10L)).thenReturn(false);

        assertThatThrownBy(() -> mediaService.listMedia(10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getMediaReturnsMappedEntity() {
        ShootEntity shoot = ShootEntity.builder().id(11L).build();
        MediaEntity media = MediaEntity.builder().id(12L).shoot(shoot).build();
        MediaResponse response = MediaResponse.builder().id(12L).build();
        when(mediaRepository.findById(12L)).thenReturn(Optional.of(media));
        when(mediaMapper.toResponse(media)).thenReturn(response);

        assertThat(mediaService.getMedia(11L, 12L)).isEqualTo(response);
    }

    @Test
    void getMediaThrowsWhenMediaMissing() {
        when(mediaRepository.findById(13L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mediaService.getMedia(11L, 13L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateMediaUpdatesExistingEntity() {
        ShootEntity shoot = ShootEntity.builder().id(14L).build();
        MediaEntity media = MediaEntity.builder().id(15L).shoot(shoot).build();
        MediaUpdateRequest request = MediaUpdateRequest.builder().fileRef("updated.jpg").build();
        when(mediaRepository.findById(15L)).thenReturn(Optional.of(media));
        when(mediaRepository.save(media)).thenReturn(media);
        when(mediaMapper.toResponse(media)).thenReturn(MediaResponse.builder().id(15L).build());

        mediaService.updateMedia(14L, 15L, request);

        verify(mediaMapper).updateEntity(request, media);
    }

    @Test
    void createMediaThrowsWhenShootMissing() {
        when(shootRepository.findById(16L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mediaService.createMedia(16L, MediaCreateRequest.builder()
                .mediaType(MediaType.EDITED_PHOTO)
                .fileRef("x.jpg")
                .build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
