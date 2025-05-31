package com.clinicsalon.appointment.mapper;

import com.clinicsalon.appointment.dto.AppointmentServiceRequest;
import com.clinicsalon.appointment.dto.AppointmentServiceResponse;
import com.clinicsalon.appointment.model.Appointment;
import com.clinicsalon.appointment.model.AppointmentServiceItem;
import com.clinicsalon.appointment.model.ServiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentServiceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appointment", source = "appointment")
    @Mapping(target = "service", source = "service")
    @Mapping(target = "price", source = "service.price")
    @Mapping(target = "notes", source = "request.notes")
    AppointmentServiceItem toEntity(AppointmentServiceRequest request, Appointment appointment, ServiceEntity service);

    @Mapping(target = "serviceId", source = "service.id")
    @Mapping(target = "serviceName", source = "service.name")
    AppointmentServiceResponse toResponse(AppointmentServiceItem appointmentServiceItem);
}
