package com.clinicsalon.appointment.service;

import com.clinicsalon.appointment.dto.ServiceRequest;
import com.clinicsalon.appointment.dto.ServiceResponse;
import com.clinicsalon.appointment.exception.ResourceNotFoundException;
import com.clinicsalon.appointment.mapper.ServiceMapper;
import com.clinicsalon.appointment.model.ServiceEntity;
import com.clinicsalon.appointment.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ServiceService {

    private static final Logger log = LoggerFactory.getLogger(ServiceService.class);

    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;

    @Transactional(readOnly = true)
    public Page<ServiceResponse> findAllActive(Pageable pageable) {
        return serviceRepository.findByActiveTrue(pageable)
                .map(serviceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ServiceResponse> findByNameContaining(String name, Pageable pageable) {
        return serviceRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, pageable)
                .map(serviceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> findAllActive() {
        return serviceRepository.findByActiveTrue().stream()
                .map(serviceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ServiceResponse findById(Long id) {
        return serviceRepository.findById(id)
                .map(serviceMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", "id", id));
    }

    @Transactional
    public ServiceResponse create(ServiceRequest request) {
        ServiceEntity service = serviceMapper.toEntity(request);
        service = serviceRepository.save(service);
        log.info("Serviço criado com ID: {}", service.getId());
        return serviceMapper.toResponse(service);
    }

    @Transactional
    public ServiceResponse update(Long id, ServiceRequest request) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", "id", id));

        serviceMapper.updateEntityFromRequest(request, service);
        service = serviceRepository.save(service);
        log.info("Serviço atualizado com ID: {}", id);
        return serviceMapper.toResponse(service);
    }

    @Transactional
    public void delete(Long id) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", "id", id));

        service.setActive(false);
        serviceRepository.save(service);
        log.info("Serviço desativado com ID: {}", id);
    }

    @Transactional
    public ServiceResponse activate(Long id) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", "id", id));

        service.setActive(true);
        service = serviceRepository.save(service);
        log.info("Serviço ativado com ID: {}", id);
        return serviceMapper.toResponse(service);
    }
}
