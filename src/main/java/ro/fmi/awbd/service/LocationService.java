package ro.fmi.awbd.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.fmi.awbd.exception.ResourceNotFoundException;
import ro.fmi.awbd.model.dto.mapper.LocationMapper;
import ro.fmi.awbd.model.dto.request.LocationCreateRequest;
import ro.fmi.awbd.model.dto.request.LocationUpdateRequest;
import ro.fmi.awbd.model.dto.response.LocationResponse;
import ro.fmi.awbd.model.entity.LocationEntity;
import ro.fmi.awbd.repository.LocationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    @Transactional(readOnly = true)
    public List<LocationResponse> getAllLocations() {
        List<LocationResponse> locations = locationRepository.findAll().stream().map(locationMapper::toResponse).toList();
        log.debug("Listed all locations, count={}", locations.size());
        return locations;
    }

    @Transactional(readOnly = true)
    public Page<LocationResponse> getLocations(Pageable pageable) {
        Page<LocationResponse> page = locationRepository.findAll(pageable).map(locationMapper::toResponse);
        log.debug("Listed locations page={}, size={}, total={}",
                pageable.getPageNumber(), pageable.getPageSize(), page.getTotalElements());
        return page;
    }

    @Transactional(readOnly = true)
    public LocationResponse getLocation(Long id) {
        log.debug("Fetching location id={}", id);
        return locationMapper.toResponse(findEntity(id));
    }

    @Transactional
    public LocationResponse createLocation(LocationCreateRequest request) {
        LocationEntity saved = locationRepository.save(locationMapper.toEntity(request));
        log.info("Created location id={}", saved.getId());
        return locationMapper.toResponse(saved);
    }

    @Transactional
    public LocationResponse updateLocation(Long locationId, LocationUpdateRequest request) {
        LocationEntity location = findEntity(locationId);
        locationMapper.updateEntity(request, location);
        LocationEntity saved = locationRepository.save(location);
        log.info("Updated location id={}", saved.getId());
        return locationMapper.toResponse(saved);
    }

    @Transactional
    public void deleteLocation(Long id) {
        LocationEntity location = findEntity(id);
        locationRepository.delete(location);
        log.info("Deleted location id={}", id);
    }

    private LocationEntity findEntity(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Location", id));
    }
}
