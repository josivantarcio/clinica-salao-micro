package com.clinicsalon.client.controller;

import com.clinicsalon.client.BaseIntegrationTest;
import com.clinicsalon.client.dto.ClientRequest;
import com.clinicsalon.client.model.Client;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ClientControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void givenClientObject_whenCreateClient_thenReturnSavedClient() throws Exception {
        // given - setup or precondition
        ClientRequest clientRequest = new ClientRequest();
        clientRequest.setName("John Doe");
        clientRequest.setEmail("john.doe@example.com");
        clientRequest.setPhone("(11) 98765-4321");
        clientRequest.setBirthDate(LocalDate.of(1990, 5, 15));
        clientRequest.setCpf("123.456.789-09");
        clientRequest.setAddress("123 Main St, Anytown, USA");

        // when - action or behavior that we are going to test
        ResultActions response = mockMvc.perform(post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientRequest)));

        // then - verify the output
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(clientRequest.getName())))
                .andExpect(jsonPath("$.email", is(clientRequest.getEmail()))
                );

        // Verify the database
        List<Client> clients = clientRepository.findAll();
        assertEquals(1, clients.size(), "Should have one client after save");
        assertEquals(clientRequest.getName(), clients.get(0).getName(), "Client name should match");
        assertEquals(clientRequest.getEmail(), clients.get(0).getEmail(), "Client email should match");
    }

    @Test
    void givenListOfClients_whenGetAllClients_thenReturnClientsList() throws Exception {
        // given - setup or precondition
        Client client1 = new Client();
        client1.setName("John Doe");
        client1.setEmail("john.doe@example.com");
        client1.setPhone("(11) 98765-4321");
        client1.setActive(true);

        Client client2 = new Client();
        client2.setName("Jane Smith");
        client2.setEmail("jane.smith@example.com");
        client2.setPhone("(11) 91234-5678");
        client2.setActive(true);

        clientRepository.save(client1);
        clientRepository.save(client2);

        // when - action or behavior that we are going to test
        ResultActions response = mockMvc.perform(get("/api/clients"));

        // then - verify the output
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(2)));
    }

    @Test
    void givenClientId_whenGetClientById_thenReturnClientObject() throws Exception {
        // given - setup or precondition
        Client client = new Client();
        client.setName("John Doe");
        client.setEmail("john.doe@example.com");
        client.setPhone("(11) 98765-4321");
        client.setActive(true);
        
        client = clientRepository.save(client);

        // when - action or behavior that we are going to test
        ResultActions response = mockMvc.perform(get("/api/clients/{id}", client.getId()));

        // then - verify the output
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(client.getName())))
                .andExpect(jsonPath("$.email", is(client.getEmail())));
    }

    @Test
    void givenInvalidClientId_whenGetClientById_thenReturnNotFound() throws Exception {
        // given - setup or precondition
        long clientId = 1L;

        // when - action or behavior that we are going to test
        ResultActions response = mockMvc.perform(get("/api/clients/{id}", clientId));

        // then - verify the output
        response.andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void givenUpdatedClient_whenUpdateClient_thenReturnUpdatedClient() throws Exception {
        // given - setup or precondition
        Client client = new Client();
        client.setName("John Doe");
        client.setEmail("john.doe@example.com");
        client.setPhone("(11) 98765-4321");
        client = clientRepository.save(client);

        ClientRequest updatedClient = new ClientRequest();
        updatedClient.setName("John Updated");
        updatedClient.setEmail("john.updated@example.com");
        updatedClient.setPhone("(11) 91234-5678");

        // when - action or behavior that we are going to test
        ResultActions response = mockMvc.perform(put("/api/clients/{id}", client.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedClient)));

        // then - verify the output
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(updatedClient.getName())))
                .andExpect(jsonPath("$.email", is(updatedClient.getEmail())));

        // Verify the database
        Client updatedClientFromDb = clientRepository.findById(client.getId()).orElseThrow();
        assertEquals(updatedClient.getName(), updatedClientFromDb.getName(), "Client name should be updated");
        assertEquals(updatedClient.getEmail(), updatedClientFromDb.getEmail(), "Client email should be updated");
    }

    @Test
    void givenClientId_whenDeleteClient_thenReturnNoContent() throws Exception {
        // given - setup or precondition
        Client client = new Client();
        client.setName("John Doe");
        client.setEmail("john.doe@example.com");
        client.setPhone("(11) 98765-4321");
        client = clientRepository.save(client);

        // when - action or behavior that we are going to test
        ResultActions response = mockMvc.perform(delete("/api/clients/{id}", client.getId()));

        // then - verify the output
        response.andDo(print())
                .andExpect(status().isNoContent());

        // Verify the database
        assertFalse(clientRepository.existsById(client.getId()), "Client should be deleted from database");
    }

    @Test
    void givenClientId_whenDeactivateClient_thenReturnDeactivatedClient() throws Exception {
        // given - setup or precondition
        Client client = new Client();
        client.setName("John Doe");
        client.setEmail("john.doe@example.com");
        client.setPhone("(11) 98765-4321");
        client.setActive(true);
        client = clientRepository.save(client);

        // when - action or behavior that we are going to test
        ResultActions response = mockMvc.perform(
                patch("/api/clients/{id}/deactivate", client.getId()));
        // then - verify the output
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(false)));

        // Verify the database
        Client updatedClient = clientRepository.findById(client.getId()).orElseThrow();
        assertFalse(updatedClient.isActive(), "Client should be deactivated");
    }

    @Test
    void givenInactiveClientId_whenActivateClient_thenReturnActivatedClient() throws Exception {
        // given - setup or precondition
        Client client = new Client();
        client.setName("John Doe");
        client.setEmail("john.doe@example.com");
        client.setPhone("(11) 98765-4321");
        client.setActive(false);
        client = clientRepository.save(client);

        // when - action or behavior that we are going to test
        ResultActions response = mockMvc.perform(
                patch("/api/clients/{id}/activate", client.getId()));
        // then - verify the output
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(true)));

        // Verify the database
        Client updatedClient = clientRepository.findById(client.getId()).orElseThrow();
        assertTrue(updatedClient.isActive(), "Client should be activated");
    }
}
