package com.clinicsalon.appointment.repository;

import com.clinicsalon.appointment.model.ServiceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {

    Page<ServiceEntity> findByActiveTrue(Pageable pageable);
    
    List<ServiceEntity> findByActiveTrue();
    
    Page<ServiceEntity> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);
}
