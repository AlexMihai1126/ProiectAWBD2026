package ro.fmi.awbd.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.fmi.awbd.exception.ResourceNotFoundException;
import ro.fmi.awbd.model.dto.mapper.ShootMapper;
import ro.fmi.awbd.model.dto.request.ShootCreateRequest;
import ro.fmi.awbd.model.dto.request.ShootUpdateRequest;
import ro.fmi.awbd.model.dto.response.ShootListItemResponse;
import ro.fmi.awbd.model.dto.response.ShootResponse;
import ro.fmi.awbd.model.entity.GearItemEntity;
import ro.fmi.awbd.model.entity.LocationEntity;
import ro.fmi.awbd.model.entity.ShootEntity;
import ro.fmi.awbd.model.entity.security.User;
import ro.fmi.awbd.repository.GearItemRepository;
import ro.fmi.awbd.repository.LocationRepository;
import ro.fmi.awbd.repository.ShootRepository;
import ro.fmi.awbd.repository.security.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShootService {

    private final ShootRepository shootRepository;
    private final LocationRepository locationRepository;
    private final GearItemRepository gearItemRepository;
    private final UserRepository userRepository;
    private final ShootMapper shootMapper;

    @Transactional(readOnly = true)
    public List<ShootListItemResponse> getAllShoots() {
        List<ShootListItemResponse> shoots = shootRepository.findAll().stream().map(shootMapper::toListItemResponse).toList();
        log.debug("Listed all shoots, count={}", shoots.size());
        return shoots;
    }

    @Transactional(readOnly = true)
    public Page<ShootListItemResponse> getShoots(Pageable pageable) {
        Page<ShootListItemResponse> page = shootRepository.findAll(pageable).map(shootMapper::toListItemResponse);
        log.debug("Listed shoots page={}, size={}, total={}",
                pageable.getPageNumber(), pageable.getPageSize(), page.getTotalElements());
        return page;
    }

    @Transactional(readOnly = true)
    public ShootResponse getShootById(Long shootId) {
        log.debug("Fetching shoot id={}", shootId);
        return shootMapper.toResponse(findEntity(shootId));
    }

    @Transactional(readOnly = true)
    public List<ShootListItemResponse> getShootsForOwner(Long ownerId) {
        if (!userRepository.existsById(ownerId)) {
            throw ResourceNotFoundException.of("Owner user", ownerId);
        }
        List<ShootListItemResponse> shoots = shootRepository.findByOwnerId(ownerId).stream()
                .map(shootMapper::toListItemResponse)
                .toList();
        log.debug("Listed {} shoots for owner id={}", shoots.size(), ownerId);
        return shoots;
    }

    @Transactional
    public ShootResponse createShoot(ShootCreateRequest request) {
        ShootEntity shoot = shootMapper.toEntity(request);
        if (shoot.getStatus() == null) {
            shoot.setStatus(ro.fmi.awbd.model.enums.ShootStatus.PLANNED);
        }
        shoot.setOwner(resolveOwner(request.getOwnerId()));
        shoot.setLocation(resolveLocation(request.getLocationId()));
        shoot.setGearItems(resolveGearItems(request.getGearItemIds()));

        ShootEntity saved = shootRepository.save(shoot);
        log.info("Created shoot id={} title='{}'", saved.getId(), saved.getTitle());
        return shootMapper.toResponse(saved);
    }

    @Transactional
    public ShootResponse updateShoot(Long shootId, ShootUpdateRequest request) {
        ShootEntity shoot = findEntity(shootId);
        shootMapper.updateEntity(request, shoot);

        if (request.getLocationId() != null) {
            shoot.setLocation(resolveLocation(request.getLocationId()));
        }
        if (request.getOwnerId() != null) {
            shoot.setOwner(resolveOwner(request.getOwnerId()));
        }
        if (request.getGearItemIds() != null) {
            shoot.setGearItems(resolveGearItems(request.getGearItemIds()));
        }

        ShootEntity saved = shootRepository.save(shoot);
        log.info("Updated shoot id={}", saved.getId());
        return shootMapper.toResponse(saved);
    }

    @Transactional
    public void deleteShoot(Long shootId) {
        ShootEntity shoot = findEntity(shootId);
        shootRepository.delete(shoot);
        log.info("Deleted shoot id={}", shootId);
    }

    private ShootEntity findEntity(Long shootId) {
        return shootRepository.findById(shootId)
                .orElseThrow(() -> ResourceNotFoundException.of("Shoot", shootId));
    }

    private LocationEntity resolveLocation(Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> ResourceNotFoundException.of("Location", locationId));
    }

    private Set<GearItemEntity> resolveGearItems(Set<Long> gearItemIds) {
        if (gearItemIds == null || gearItemIds.isEmpty()) {
            return new HashSet<>();
        }
        List<GearItemEntity> items = gearItemRepository.findAllById(gearItemIds);
        if (items.size() != gearItemIds.size()) {
            throw new ResourceNotFoundException("One or more gear items not found");
        }
        return new HashSet<>(items);
    }

    private User resolveOwner(Long ownerId) {
        return userRepository.findById(ownerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Owner user", ownerId));
    }
}
