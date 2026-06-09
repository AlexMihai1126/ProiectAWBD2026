package ro.fmi.awbd.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.fmi.awbd.exception.ResourceNotFoundException;
import ro.fmi.awbd.model.dto.mapper.GearMapper;
import ro.fmi.awbd.model.dto.request.GearCreateRequest;
import ro.fmi.awbd.model.dto.request.GearUpdateRequest;
import ro.fmi.awbd.model.dto.response.GearResponse;
import ro.fmi.awbd.model.entity.GearItemEntity;
import ro.fmi.awbd.model.entity.security.User;
import ro.fmi.awbd.repository.GearItemRepository;
import ro.fmi.awbd.repository.security.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GearService {

    private final GearItemRepository gearItemRepository;
    private final UserRepository userRepository;
    private final GearMapper gearMapper;

    @Transactional(readOnly = true)
    public List<GearResponse> getAllGear() {
        return gearItemRepository.findAll().stream().map(gearMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<GearResponse> getGear(Pageable pageable) {
        return gearItemRepository.findAll(pageable).map(gearMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public GearResponse getGearById(Long id) {
        return gearMapper.toResponse(findEntity(id));
    }

    @Transactional
    public GearResponse createGear(GearCreateRequest request) {
        GearItemEntity gear = gearMapper.toEntity(request);
        gear.setOwner(resolveOwner(request.getOwnerId()));
        GearItemEntity saved = gearItemRepository.save(gear);
        log.info("Created gear item id={}", saved.getId());
        return gearMapper.toResponse(saved);
    }

    @Transactional
    public GearResponse updateGear(Long id, GearUpdateRequest request) {
        GearItemEntity gear = findEntity(id);
        gearMapper.updateEntity(request, gear);
        if (request.getOwnerId() != null) {
            gear.setOwner(resolveOwner(request.getOwnerId()));
        }
        GearItemEntity saved = gearItemRepository.save(gear);
        log.info("Updated gear item id={}", saved.getId());
        return gearMapper.toResponse(saved);
    }

    @Transactional
    public void deleteGear(Long id) {
        GearItemEntity gear = findEntity(id);
        gearItemRepository.delete(gear);
        log.info("Deleted gear item id={}", id);
    }

    private GearItemEntity findEntity(Long id) {
        return gearItemRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Gear item", id));
    }

    private User resolveOwner(Long ownerId) {
        return userRepository.findById(ownerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Owner user", ownerId));
    }
}
