package com.clinicsalon.appointment.repository;

import com.clinicsalon.appointment.model.Appointment;
import com.clinicsalon.appointment.model.AppointmentServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentServiceRepository extends JpaRepository<AppointmentServiceItem, Long> {

    List<AppointmentServiceItem> findByAppointment(Appointment appointment);
    
    List<AppointmentServiceItem> findByAppointmentId(Long appointmentId);
    
    void deleteByAppointmentId(Long appointmentId);
}
