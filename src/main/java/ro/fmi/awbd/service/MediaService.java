package ro.fmi.awbd.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.fmi.awbd.exception.ResourceNotFoundException;
import ro.fmi.awbd.model.dto.mapper.MediaMapper;
import ro.fmi.awbd.model.dto.request.MediaCreateRequest;
import ro.fmi.awbd.model.dto.request.MediaUpdateRequest;
import ro.fmi.awbd.model.dto.response.MediaResponse;
import ro.fmi.awbd.model.entity.MediaEntity;
import ro.fmi.awbd.model.entity.ShootEntity;
import ro.fmi.awbd.repository.MediaRepository;
import ro.fmi.awbd.repository.ShootRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {

    private final MediaRepository mediaRepository;
    private final ShootRepository shootRepository;
    private final MediaMapper mediaMapper;

    @Transactional(readOnly = true)
    public List<MediaResponse> listMedia(Long shootId) {
        ensureShootExists(shootId);
        List<MediaResponse> media = mediaRepository.findByShootId(shootId).stream()
                .map(mediaMapper::toResponse)
                .toList();
        log.debug("Listed {} media items for shoot id={}", media.size(), shootId);
        return media;
    }

    @Transactional(readOnly = true)
    public MediaResponse getMedia(Long shootId, Long mediaId) {
        log.debug("Fetching media id={} for shoot id={}", mediaId, shootId);
        return mediaMapper.toResponse(findForShoot(shootId, mediaId));
    }

    @Transactional
    public MediaResponse createMedia(Long shootId, MediaCreateRequest request) {
        ShootEntity shoot = shootRepository.findById(shootId)
                .orElseThrow(() -> ResourceNotFoundException.of("Shoot", shootId));

        MediaEntity media = mediaMapper.toEntity(request);
        media.setShoot(shoot);

        MediaEntity saved = mediaRepository.save(media);
        log.info("Created media id={} for shoot id={}", saved.getId(), shootId);
        return mediaMapper.toResponse(saved);
    }

    @Transactional
    public MediaResponse updateMedia(Long shootId, Long mediaId, MediaUpdateRequest request) {
        MediaEntity media = findForShoot(shootId, mediaId);
        mediaMapper.updateEntity(request, media);
        MediaEntity saved = mediaRepository.save(media);
        log.info("Updated media id={}", saved.getId());
        return mediaMapper.toResponse(saved);
    }

    @Transactional
    public void deleteMedia(Long shootId, Long mediaId) {
        MediaEntity media = findForShoot(shootId, mediaId);
        mediaRepository.delete(media);
        log.info("Deleted media id={} from shoot id={}", mediaId, shootId);
    }

    private MediaEntity findForShoot(Long shootId, Long mediaId) {
        MediaEntity media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> ResourceNotFoundException.of("Media", mediaId));
        if (!media.getShoot().getId().equals(shootId)) {
            throw new ResourceNotFoundException("Media " + mediaId + " not found for shoot " + shootId);
        }
        return media;
    }

    private void ensureShootExists(Long shootId) {
        if (!shootRepository.existsById(shootId)) {
            throw ResourceNotFoundException.of("Shoot", shootId);
        }
    }
}
