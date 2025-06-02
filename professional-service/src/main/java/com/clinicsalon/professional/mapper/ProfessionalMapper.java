package com.clinicsalon.professional.mapper;

import com.clinicsalon.professional.dto.ProfessionalRequest;
import com.clinicsalon.professional.dto.ProfessionalResponse;
import com.clinicsalon.professional.model.Professional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProfessionalMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    Professional toEntity(ProfessionalRequest request);
    
    // A propriedade correta é isActive
    ProfessionalResponse toResponse(Professional professional);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    void updateEntityFromRequest(ProfessionalRequest request, @MappingTarget Professional professional);
}
