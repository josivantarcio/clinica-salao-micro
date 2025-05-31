package com.clinicsalon.auth.repository;

import com.clinicsalon.auth.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByName(String name);
    boolean existsByName(String name);
}
