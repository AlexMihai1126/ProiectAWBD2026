package ro.fmi.awbd.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ro.fmi.awbd.model.entity.dto.mapper.MediaMapper;
import ro.fmi.awbd.model.entity.dto.request.MediaCreateRequest;
import ro.fmi.awbd.model.entity.dto.request.MediaUpdateRequest;
import ro.fmi.awbd.model.entity.dto.response.MediaResponse;
import ro.fmi.awbd.model.entity.MediaEntity;
import ro.fmi.awbd.model.entity.ShootEntity;
import ro.fmi.awbd.repository.MediaRepository;
import ro.fmi.awbd.repository.ShootRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaRepository mediaRepository;
    private final ShootRepository shootRepository;
    private final MediaMapper mediaMapper;

    @Transactional(readOnly = true)
    public List<MediaResponse> listMedia(Long shootId) {
        ensureShootExists(shootId);
        return mediaRepository.findByShootId(shootId).stream()
                .map(mediaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MediaResponse createMedia(Long shootId, MediaCreateRequest request) {
        ShootEntity shoot = shootRepository.findById(shootId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shoot not found"));

        MediaEntity media = mediaMapper.toEntity(request);
        media.setShoot(shoot);

        MediaEntity saved = mediaRepository.save(media);
        return mediaMapper.toResponse(saved);
    }

    @Transactional
    public MediaResponse updateMedia(Long shootId, Long mediaId, MediaUpdateRequest request) {
        MediaEntity media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found"));

        if (!media.getShoot().getId().equals(shootId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found for shoot");
        }

        mediaMapper.updateEntity(request, media);
        MediaEntity saved = mediaRepository.save(media);
        return mediaMapper.toResponse(saved);
    }

    private void ensureShootExists(Long shootId) {
        if (!shootRepository.existsById(shootId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Shoot not found");
        }
    }
}
