package com.clinicsalon.appointment.repository;

import com.clinicsalon.appointment.model.Appointment;
import com.clinicsalon.appointment.model.AppointmentService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentServiceRepository extends JpaRepository<AppointmentService, Long> {

    List<AppointmentService> findByAppointment(Appointment appointment);
    
    List<AppointmentService> findByAppointmentId(Long appointmentId);
    
    void deleteByAppointmentId(Long appointmentId);
}
