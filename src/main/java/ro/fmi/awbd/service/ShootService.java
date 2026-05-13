package ro.fmi.awbd.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ro.fmi.awbd.model.entity.dto.mapper.ShootMapper;
import ro.fmi.awbd.model.entity.dto.request.ShootCreateRequest;
import ro.fmi.awbd.model.entity.dto.request.ShootUpdateRequest;
import ro.fmi.awbd.model.entity.dto.response.ShootListItemResponse;
import ro.fmi.awbd.model.entity.dto.response.ShootResponse;
import ro.fmi.awbd.model.entity.GearItemEntity;
import ro.fmi.awbd.model.entity.LocationEntity;
import ro.fmi.awbd.model.entity.ShootEntity;
import ro.fmi.awbd.model.entity.UserEntity;
import ro.fmi.awbd.repository.GearItemRepository;
import ro.fmi.awbd.repository.LocationRepository;
import ro.fmi.awbd.repository.ShootRepository;
import ro.fmi.awbd.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShootService {

    private final ShootRepository shootRepository;
    private final LocationRepository locationRepository;
    private final GearItemRepository gearItemRepository;
    private final UserRepository userRepository;
    private final ShootMapper shootMapper;

    @Transactional
    public ShootResponse createShoot(ShootCreateRequest request) {
        ShootEntity shoot = shootMapper.toEntity(request);

        shoot.setOwner(resolveOwner(request.getOwnerId()));
        shoot.setLocation(resolveLocation(request.getLocationId()));
        shoot.setGearItems(resolveGearItems(request.getGearItemIds()));

        ShootEntity saved = shootRepository.save(shoot);
        return shootMapper.toResponse(saved);
    }

    @Transactional
    public ShootResponse updateShoot(Long shootId, ShootUpdateRequest request) {
        ShootEntity shoot = shootRepository.findById(shootId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shoot not found"));

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
        return shootMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ShootResponse getShootById(Long shootId) {
        ShootEntity shoot = shootRepository.findById(shootId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shoot not found"));
        return shootMapper.toResponse(shoot);
    }

    @Transactional(readOnly = true)
    public List<ShootListItemResponse> getShootsForOwner(Long ownerId) {
        if (!userRepository.existsById(ownerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner user not found");
        }

        return shootRepository.findByOwnerId(ownerId).stream()
                .map(shootMapper::toListItemResponse)
                .collect(Collectors.toList());
    }

    private LocationEntity resolveLocation(Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found"));
    }

    private Set<GearItemEntity> resolveGearItems(Set<Long> gearItemIds) {
        if (gearItemIds == null || gearItemIds.isEmpty()) {
            return new HashSet<>();
        }

        List<GearItemEntity> items = gearItemRepository.findAllById(gearItemIds);
        if (items.size() != gearItemIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more gear items not found");
        }
        return new HashSet<>(items);
    }

    private UserEntity resolveOwner(Long ownerId) {
        return userRepository.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner user not found"));
    }
}
