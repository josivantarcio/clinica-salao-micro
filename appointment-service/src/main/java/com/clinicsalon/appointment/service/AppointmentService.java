package com.clinicsalon.appointment.service;

import com.clinicsalon.appointment.client.ClientServiceClient;
import com.clinicsalon.appointment.client.ProfessionalServiceClient;
import com.clinicsalon.appointment.dto.AppointmentRequest;
import com.clinicsalon.appointment.dto.AppointmentResponse;
import com.clinicsalon.appointment.dto.AppointmentServiceRequest;
import com.clinicsalon.appointment.exception.BusinessException;
import com.clinicsalon.appointment.exception.ResourceNotFoundException;
import com.clinicsalon.appointment.mapper.AppointmentMapper;
import com.clinicsalon.appointment.mapper.AppointmentServiceMapper;
import com.clinicsalon.appointment.model.Appointment;
import com.clinicsalon.appointment.model.AppointmentService;
import com.clinicsalon.appointment.model.AppointmentStatus;
import com.clinicsalon.appointment.model.ServiceEntity;
import com.clinicsalon.appointment.repository.AppointmentRepository;
import com.clinicsalon.appointment.repository.AppointmentServiceRepository;
import com.clinicsalon.appointment.repository.ServiceRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AppointmentService {
    
    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final AppointmentServiceRepository appointmentServiceRepository;
    private final ServiceRepository serviceRepository;
    private final AppointmentMapper appointmentMapper;
    private final AppointmentServiceMapper appointmentServiceMapper;
    private final ClientServiceClient clientServiceClient;
    private final ProfessionalServiceClient professionalServiceClient;

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> findAll(Pageable pageable) {
        return appointmentRepository.findAll(pageable)
                .map(this::enrichAppointmentResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> findByClientId(Long clientId, Pageable pageable) {
        return appointmentRepository.findByClientId(clientId, pageable)
                .map(this::enrichAppointmentResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> findByProfessionalId(Long professionalId, Pageable pageable) {
        return appointmentRepository.findByProfessionalId(professionalId, pageable)
                .map(this::enrichAppointmentResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> findByStatus(AppointmentStatus status, Pageable pageable) {
        return appointmentRepository.findByStatus(status, pageable)
                .map(this::enrichAppointmentResponse);
    }

    @Transactional(readOnly = true)
    public AppointmentResponse findById(Long id) {
        Appointment appointment = getAppointmentById(id);
        return enrichAppointmentResponse(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findProfessionalAppointmentsForDay(Long professionalId, LocalDateTime day) {
        LocalDateTime startOfDay = day.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

        return appointmentRepository.findProfessionalAppointmentsForDateRange(professionalId, startOfDay, endOfDay)
                .stream()
                .map(this::enrichAppointmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponse create(AppointmentRequest request) {
        validateAppointmentRequest(request);
        
        Appointment appointment = appointmentMapper.toEntity(request);
        appointment.setStatus(AppointmentStatus.PENDING);
        
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<AppointmentService> appointmentServices = new ArrayList<>();
        
        // Calculando o preço total
        for (AppointmentServiceRequest serviceRequest : request.getServices()) {
            ServiceEntity service = serviceRepository.findById(serviceRequest.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Serviço", "id", serviceRequest.getServiceId()));
            
            totalPrice = totalPrice.add(service.getPrice());
        }
        
        appointment.setPrice(totalPrice);
        appointment = appointmentRepository.save(appointment);
        
        // Salvando os serviços associados ao agendamento
        for (AppointmentServiceRequest serviceRequest : request.getServices()) {
            ServiceEntity service = serviceRepository.findById(serviceRequest.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Serviço", "id", serviceRequest.getServiceId()));
            
            AppointmentService appointmentService = appointmentServiceMapper.toEntity(
                    serviceRequest, appointment, service);
            appointmentServices.add(appointmentService);
        }
        
        appointmentServiceRepository.saveAll(appointmentServices);
        log.info("Agendamento criado com ID: {}", appointment.getId());
        
        return enrichAppointmentResponse(appointment);
    }

    @Transactional
    public AppointmentResponse update(Long id, AppointmentRequest request) {
        Appointment appointment = getAppointmentById(id);
        
        // Não permite alteração de agendamentos concluídos ou cancelados
        if (appointment.getStatus() == AppointmentStatus.COMPLETED 
                || appointment.getStatus() == AppointmentStatus.CANCELLED
                || appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new BusinessException("Não é possível alterar agendamentos concluídos, cancelados ou com ausência registrada");
        }
        
        // Atualiza os dados básicos
        appointment.setClientId(request.getClientId());
        appointment.setProfessionalId(request.getProfessionalId());
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(request.getEndTime());
        appointment.setNotes(request.getNotes());
        
        // Remove os serviços anteriores para adicionar os novos
        appointmentServiceRepository.deleteByAppointmentId(id);
        
        // Calculando o novo preço total
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<AppointmentService> appointmentServices = new ArrayList<>();
        
        for (AppointmentServiceRequest serviceRequest : request.getServices()) {
            ServiceEntity service = serviceRepository.findById(serviceRequest.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Serviço", "id", serviceRequest.getServiceId()));
            
            totalPrice = totalPrice.add(service.getPrice());
            
            AppointmentService appointmentService = appointmentServiceMapper.toEntity(
                    serviceRequest, appointment, service);
            appointmentServices.add(appointmentService);
        }
        
        appointment.setPrice(totalPrice);
        appointment = appointmentRepository.save(appointment);
        appointmentServiceRepository.saveAll(appointmentServices);
        
        log.info("Agendamento atualizado com ID: {}", id);
        
        return enrichAppointmentResponse(appointment);
    }

    @Transactional
    public AppointmentResponse updateStatus(Long id, AppointmentStatus status) {
        Appointment appointment = getAppointmentById(id);
        
        // Validações de mudança de status
        if ((status == AppointmentStatus.IN_PROGRESS || status == AppointmentStatus.COMPLETED)
                && appointment.getStatus() != AppointmentStatus.CONFIRMED 
                && appointment.getStatus() != AppointmentStatus.IN_PROGRESS) {
            throw new BusinessException("Apenas agendamentos confirmados podem ser iniciados ou concluídos");
        }
        
        if (status == AppointmentStatus.CANCELLED && 
                (appointment.getStatus() == AppointmentStatus.COMPLETED || 
                appointment.getStatus() == AppointmentStatus.NO_SHOW)) {
            throw new BusinessException("Não é possível cancelar um agendamento já concluído ou com ausência registrada");
        }
        
        appointment.setStatus(status);
        appointment = appointmentRepository.save(appointment);
        
        log.info("Status do agendamento ID: {} atualizado para: {}", id, status);
        
        return enrichAppointmentResponse(appointment);
    }

    private Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", "id", id));
    }

    private void validateAppointmentRequest(AppointmentRequest request) {
        // Validar se a data de início é menor que a de fim
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new BusinessException("A data/hora de início deve ser anterior à data/hora de fim");
        }
        
        // Validar se a data já passou
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Não é possível criar ou atualizar agendamentos com data passada");
        }
        
        // Validar conflito de horários para o profissional
        List<Appointment> existingAppointments = appointmentRepository.findProfessionalAppointmentsForDateRange(
                request.getProfessionalId(), 
                request.getStartTime(), 
                request.getEndTime());
        
        if (!existingAppointments.isEmpty()) {
            throw new BusinessException("O profissional já possui agendamento neste horário");
        }
    }

    private AppointmentResponse enrichAppointmentResponse(Appointment appointment) {
        AppointmentResponse response = appointmentMapper.toBasicResponse(appointment);
        
        // Buscar os serviços associados ao agendamento
        List<AppointmentService> services = appointmentServiceRepository.findByAppointmentId(appointment.getId());
        response.setServices(services.stream()
                .map(appointmentServiceMapper::toResponse)
                .collect(Collectors.toList()));
        
        // Obter o nome do cliente via Feign Client
        try {
            String clientName = clientServiceClient.getClientName(appointment.getClientId());
            response.setClientName(clientName);
        } catch (FeignException e) {
            log.warn("Não foi possível obter o nome do cliente com ID {}: {}", 
                    appointment.getClientId(), e.getMessage());
            response.setClientName("Cliente #" + appointment.getClientId());
        }
        
        // Obter o nome do profissional via Feign Client
        try {
            String professionalName = professionalServiceClient.getProfessionalName(appointment.getProfessionalId());
            response.setProfessionalName(professionalName);
        } catch (FeignException e) {
            log.warn("Não foi possível obter o nome do profissional com ID {}: {}", 
                    appointment.getProfessionalId(), e.getMessage());
            response.setProfessionalName("Profissional #" + appointment.getProfessionalId());
        }
        
        return response;
    }
}
