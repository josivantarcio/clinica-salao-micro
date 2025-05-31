package com.clinicsalon.appointment.mapper;

import com.clinicsalon.appointment.dto.AppointmentServiceRequest;
import com.clinicsalon.appointment.dto.AppointmentServiceResponse;
import com.clinicsalon.appointment.model.Appointment;
import com.clinicsalon.appointment.model.AppointmentService;
import com.clinicsalon.appointment.model.Service;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentServiceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appointment", source = "appointment")
    @Mapping(target = "service", source = "service")
    @Mapping(target = "price", source = "service.price")
    AppointmentService toEntity(AppointmentServiceRequest request, Appointment appointment, Service service);

    @Mapping(target = "serviceId", source = "service.id")
    @Mapping(target = "serviceName", source = "service.name")
    AppointmentServiceResponse toResponse(AppointmentService appointmentService);
}
