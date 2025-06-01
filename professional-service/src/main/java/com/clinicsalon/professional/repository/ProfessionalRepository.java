package com.clinicsalon.professional.repository;

import com.clinicsalon.professional.model.Professional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessionalRepository extends JpaRepository<Professional, Long> {
    
    Optional<Professional> findByEmail(String email);
    
    Optional<Professional> findByCpf(String cpf);
    
    List<Professional> findByIsActiveTrue();
    
    List<Professional> findBySpecializationIgnoreCase(String specialization);
    
    boolean existsByEmail(String email);
    
    boolean existsByCpf(String cpf);
}
