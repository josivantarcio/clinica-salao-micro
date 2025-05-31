package com.clinicsalon.appointment.repository;

import com.clinicsalon.appointment.model.Appointment;
import com.clinicsalon.appointment.model.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Page<Appointment> findByClientId(Long clientId, Pageable pageable);
    
    Page<Appointment> findByProfessionalId(Long professionalId, Pageable pageable);
    
    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);
    
    @Query("SELECT a FROM Appointment a WHERE " +
           "a.professionalId = :professionalId AND " +
           "a.startTime >= :startDate AND " +
           "a.endTime <= :endDate AND " +
           "a.status NOT IN ('CANCELLED', 'NO_SHOW')")
    List<Appointment> findProfessionalAppointmentsForDateRange(
            @Param("professionalId") Long professionalId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM Appointment a WHERE " +
           "a.startTime >= :startDate AND " +
           "a.endTime <= :endDate AND " +
           "a.status NOT IN ('CANCELLED', 'NO_SHOW')")
    List<Appointment> findAppointmentsForDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    List<Appointment> findByClientIdAndStartTimeAfterOrderByStartTime(
            Long clientId, LocalDateTime startTime);
}
