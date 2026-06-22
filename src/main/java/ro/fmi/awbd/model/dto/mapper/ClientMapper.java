package ro.fmi.awbd.model.dto.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import ro.fmi.awbd.model.dto.request.ClientCreateRequest;
import ro.fmi.awbd.model.dto.request.ClientUpdateRequest;
import ro.fmi.awbd.model.dto.response.ClientResponse;
import ro.fmi.awbd.model.entity.ClientEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    ClientEntity toEntity(ClientCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntity(ClientUpdateRequest request, @MappingTarget ClientEntity entity);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    ClientResponse toResponse(ClientEntity entity);
}
