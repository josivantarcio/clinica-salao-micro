package com.clinicsalon.appointment.mapper;

import com.clinicsalon.appointment.dto.ServiceRequest;
import com.clinicsalon.appointment.dto.ServiceResponse;
import com.clinicsalon.appointment.model.ServiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ServiceMapper {

    ServiceResponse toResponse(ServiceEntity service);
    
    ServiceEntity toEntity(ServiceRequest request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(ServiceRequest request, @MappingTarget ServiceEntity service);
}
