package com.clinicsalon.client.service;

import com.clinicsalon.client.dto.ClientRequest;
import com.clinicsalon.client.dto.ClientResponse;
import com.clinicsalon.client.exception.ResourceAlreadyExistsException;
import com.clinicsalon.client.exception.ResourceNotFoundException;
import com.clinicsalon.client.mapper.ClientMapper;
import com.clinicsalon.client.model.Client;
import com.clinicsalon.client.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {
    
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    
    @Transactional(readOnly = true)
    public Page<ClientResponse> findAll(Pageable pageable) {
        log.info("Fetching all clients with pagination: {}", pageable);
        return clientRepository.findAll(pageable)
                .map(clientMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public ClientResponse findById(Long id) {
        log.info("Fetching client with id: {}", id);
        return clientRepository.findById(id)
                .map(clientMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }
    
    @Transactional
    public ClientResponse create(ClientRequest request) {
        log.info("Creating new client with email: {}", request.getEmail());
        
        if (clientRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already in use: " + request.getEmail());
        }
        
        if (request.getCpf() != null && clientRepository.existsByCpf(request.getCpf())) {
            throw new ResourceAlreadyExistsException("CPF already registered: " + request.getCpf());
        }
        
        if (clientRepository.existsByPhone(request.getPhone())) {
            throw new ResourceAlreadyExistsException("Phone number already in use: " + request.getPhone());
        }
        
        Client client = clientMapper.toEntity(request);
        client = clientRepository.save(client);
        
        log.info("Created client with id: {}", client.getId());
        return clientMapper.toResponse(client);
    }
    
    @Transactional
    public ClientResponse update(Long id, ClientRequest request) {
        log.info("Updating client with id: {}", id);
        
        Client existingClient = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
        
        // Check if email is being changed and if it's already in use
        if (!existingClient.getEmail().equals(request.getEmail()) && 
            clientRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already in use: " + request.getEmail());
        }
        
        // Check if CPF is being changed and if it's already in use
        if (request.getCpf() != null && !request.getCpf().equals(existingClient.getCpf()) && 
            clientRepository.existsByCpf(request.getCpf())) {
            throw new ResourceAlreadyExistsException("CPF already registered: " + request.getCpf());
        }
        
        // Check if phone is being changed and if it's already in use
        if (!existingClient.getPhone().equals(request.getPhone()) && 
            clientRepository.existsByPhone(request.getPhone())) {
            throw new ResourceAlreadyExistsException("Phone number already in use: " + request.getPhone());
        }
        
        clientMapper.updateEntity(request, existingClient);
        Client updatedClient = clientRepository.save(existingClient);
        
        log.info("Updated client with id: {}", id);
        return clientMapper.toResponse(updatedClient);
    }
    
    @Transactional
    public void delete(Long id) {
        log.info("Deleting client with id: {}", id);
        
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Client not found with id: " + id);
        }
        
        clientRepository.deleteById(id);
        log.info("Deleted client with id: {}", id);
    }
    
    @Transactional
    public ClientResponse deactivate(Long id) {
        log.info("Deactivating client with id: {}", id);
        
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
        
        if (!client.isActive()) {
            throw new IllegalStateException("Client is already inactive");
        }
        
        client.setActive(false);
        client = clientRepository.save(client);
        
        log.info("Deactivated client with id: {}", id);
        return clientMapper.toResponse(client);
    }
    
    @Transactional
    public ClientResponse activate(Long id) {
        log.info("Activating client with id: {}", id);
        
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
        
        if (client.isActive()) {
            throw new IllegalStateException("Client is already active");
        }
        
        client.setActive(true);
        client = clientRepository.save(client);
        
        log.info("Activated client with id: {}", id);
        return clientMapper.toResponse(client);
    }
}
