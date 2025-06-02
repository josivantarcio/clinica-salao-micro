package com.clinicsalon.appointment.mapper;

import com.clinicsalon.appointment.dto.AppointmentRequest;
import com.clinicsalon.appointment.dto.AppointmentResponse;
import com.clinicsalon.appointment.model.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {AppointmentServiceMapper.class})
public interface AppointmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Appointment toEntity(AppointmentRequest request);

    // Propriedades clientName e professionalName devem ser preenchidas manualmente
    // pois não temos acesso direto aos nomes no modelo Appointment
    @Mapping(target = "clientName", ignore = true)
    @Mapping(target = "professionalName", ignore = true)
    @Mapping(target = "totalPrice", source = "price")
    @Mapping(target = "services", ignore = true) // Será preenchido pelo serviço
    AppointmentResponse toResponse(Appointment appointment);
    
    @Mapping(target = "clientName", ignore = true)
    @Mapping(target = "professionalName", ignore = true)
    @Mapping(target = "services", ignore = true)
    @Mapping(target = "totalPrice", source = "price")
    AppointmentResponse toBasicResponse(Appointment appointment);
}
