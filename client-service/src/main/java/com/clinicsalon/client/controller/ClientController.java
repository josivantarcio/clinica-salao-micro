package com.clinicsalon.client.controller;

import com.clinicsalon.client.dto.ClientRequest;
import com.clinicsalon.client.dto.ClientResponse;
import com.clinicsalon.client.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Clients", description = "API for managing clients")
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @Operation(summary = "Get all clients with pagination")
    @GetMapping
    public ResponseEntity<Page<ClientResponse>> getAllClients(
            @Parameter(description = "Pagination and sorting parameters") 
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(clientService.findAll(pageable));
    }

    @Operation(summary = "Get a client by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getClientById(
            @Parameter(description = "ID of the client to be obtained") 
            @PathVariable Long id) {
        return ResponseEntity.ok(clientService.findById(id));
    }

    @Operation(summary = "Create a new client")
    @PostMapping
    public ResponseEntity<ClientResponse> createClient(
            @Parameter(description = "Client data to create") 
            @Valid @RequestBody ClientRequest request) {
        return new ResponseEntity<>(clientService.create(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing client")
    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> updateClient(
            @Parameter(description = "ID of the client to be updated") 
            @PathVariable Long id,
            @Parameter(description = "Updated client data") 
            @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(clientService.update(id, request));
    }

    @Operation(summary = "Delete a client")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteClient(
            @Parameter(description = "ID of the client to be deleted") 
            @PathVariable Long id) {
        clientService.delete(id);
    }

    @Operation(summary = "Deactivate a client")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ClientResponse> deactivateClient(
            @Parameter(description = "ID of the client to be deactivated") 
            @PathVariable Long id) {
        return ResponseEntity.ok(clientService.deactivate(id));
    }

    @Operation(summary = "Activate a client")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ClientResponse> activateClient(
            @Parameter(description = "ID of the client to be activated") 
            @PathVariable Long id) {
        return ResponseEntity.ok(clientService.activate(id));
    }
}
