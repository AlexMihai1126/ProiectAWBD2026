package ro.fmi.awbd.model.dto.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import ro.fmi.awbd.model.dto.request.ShootCreateRequest;
import ro.fmi.awbd.model.dto.request.ShootUpdateRequest;
import ro.fmi.awbd.model.dto.response.ShootListItemResponse;
import ro.fmi.awbd.model.dto.response.ShootResponse;
import ro.fmi.awbd.model.entity.GearItemEntity;
import ro.fmi.awbd.model.entity.ShootEntity;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ShootMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "gearItems", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "mediaItems", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ShootEntity toEntity(ShootCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "gearItems", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "mediaItems", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(ShootUpdateRequest request, @MappingTarget ShootEntity entity);

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerUsername", source = "owner.username")
    @Mapping(target = "locationId", source = "location.id")
    @Mapping(target = "locationName", source = "location.name")
    @Mapping(target = "invoiceId", source = "invoice.id")
    @Mapping(target = "gearItemIds", expression = "java(mapGearIds(entity.getGearItems()))")
    ShootResponse toResponse(ShootEntity entity);

    @Mapping(target = "locationName", source = "location.name")
    ShootListItemResponse toListItemResponse(ShootEntity entity);

    default Set<Long> mapGearIds(Set<GearItemEntity> items) {
        if (items == null) {
            return java.util.Collections.emptySet();
        }
        return items.stream().map(GearItemEntity::getId).collect(Collectors.toSet());
    }
}
