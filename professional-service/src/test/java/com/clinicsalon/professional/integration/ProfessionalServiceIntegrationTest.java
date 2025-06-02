package com.clinicsalon.professional.integration;

import com.clinicsalon.professional.dto.ProfessionalDto;
import com.clinicsalon.professional.dto.ScheduleDto;
import com.clinicsalon.professional.dto.ServiceDto;
import com.clinicsalon.professional.model.Professional;
import com.clinicsalon.professional.model.Service;
import com.clinicsalon.professional.repository.ProfessionalRepository;
import com.clinicsalon.professional.repository.ServiceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para o serviço de profissionais
 * Testa fluxos completos de gerenciamento de profissionais e serviços usando a API REST
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Tag("integration")
public class ProfessionalServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProfessionalRepository professionalRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    private String baseUrl;

    @BeforeEach
    public void setup() {
        baseUrl = "http://localhost:" + port;
        
        // Limpar dados de testes anteriores
        serviceRepository.deleteAll();
        professionalRepository.deleteAll();
    }

    @AfterEach
    public void cleanup() {
        // Limpar dados após os testes
        serviceRepository.deleteAll();
        professionalRepository.deleteAll();
    }

    /**
     * Testa o fluxo completo de criação de um profissional
     */
    @Test
    public void testCreateProfessionalFlow() {
        // Criar DTO para o novo profissional
        ProfessionalDto newProfessional = new ProfessionalDto();
        newProfessional.setName("Ana Silva");
        newProfessional.setEmail("ana.silva@example.com");
        newProfessional.setPhone("(11) 98765-4321");
        newProfessional.setSpecialties(Arrays.asList("Cabelo", "Maquiagem"));
        newProfessional.setWorkStartTime(LocalTime.of(9, 0));
        newProfessional.setWorkEndTime(LocalTime.of(18, 0));
        newProfessional.setWorkDays(Arrays.asList(
                DayOfWeek.MONDAY, 
                DayOfWeek.TUESDAY, 
                DayOfWeek.WEDNESDAY, 
                DayOfWeek.THURSDAY, 
                DayOfWeek.FRIDAY));

        // Fazer requisição REST para criar profissional
        ResponseEntity<ProfessionalDto> response = restTemplate.postForEntity(
                baseUrl + "/api/professionals",
                newProfessional,
                ProfessionalDto.class);

        // Verificar resposta HTTP
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Ana Silva", response.getBody().getName());
        assertEquals("ana.silva@example.com", response.getBody().getEmail());
        assertEquals(2, response.getBody().getSpecialties().size());
        assertTrue(response.getBody().getSpecialties().contains("Cabelo"));

        // Verificar que o profissional foi salvo no banco de dados
        List<Professional> professionals = professionalRepository.findAll();
        assertEquals(1, professionals.size());
        
        Professional savedProfessional = professionals.get(0);
        assertEquals("Ana Silva", savedProfessional.getName());
        assertEquals("ana.silva@example.com", savedProfessional.getEmail());
        assertEquals(2, savedProfessional.getSpecialties().size());
        assertEquals(5, savedProfessional.getWorkDays().size());
    }

    /**
     * Testa o fluxo de busca de profissionais por especialidade
     */
    @Test
    public void testFindProfessionalsBySpecialtyFlow() {
        // Preparar múltiplos profissionais com diferentes especialidades
        Professional p1 = new Professional();
        p1.setName("Carlos Oliveira");
        p1.setEmail("carlos@example.com");
        p1.setPhone("(11) 91234-5678");
        p1.setSpecialties(Arrays.asList("Cabelo", "Barba"));
        p1.setWorkStartTime(LocalTime.of(8, 0));
        p1.setWorkEndTime(LocalTime.of(17, 0));
        p1.setWorkDays(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));
        professionalRepository.save(p1);

        Professional p2 = new Professional();
        p2.setName("Mariana Souza");
        p2.setEmail("mariana@example.com");
        p2.setPhone("(11) 92345-6789");
        p2.setSpecialties(Arrays.asList("Unhas", "Maquiagem"));
        p2.setWorkStartTime(LocalTime.of(9, 0));
        p2.setWorkEndTime(LocalTime.of(18, 0));
        p2.setWorkDays(Arrays.asList(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY));
        professionalRepository.save(p2);

        Professional p3 = new Professional();
        p3.setName("Ricardo Santos");
        p3.setEmail("ricardo@example.com");
        p3.setPhone("(11) 93456-7890");
        p3.setSpecialties(Arrays.asList("Cabelo", "Estética"));
        p3.setWorkStartTime(LocalTime.of(10, 0));
        p3.setWorkEndTime(LocalTime.of(19, 0));
        p3.setWorkDays(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
        professionalRepository.save(p3);

        // Fazer requisição REST para buscar profissionais por especialidade
        ResponseEntity<List<ProfessionalDto>> response = restTemplate.exchange(
                baseUrl + "/api/professionals/specialty/Cabelo",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ProfessionalDto>>() {});

        // Verificar resposta HTTP
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        
        // Verificar que os profissionais retornados são os corretos
        List<String> names = new ArrayList<>();
        for (ProfessionalDto dto : response.getBody()) {
            names.add(dto.getName());
        }
        assertTrue(names.contains("Carlos Oliveira"));
        assertTrue(names.contains("Ricardo Santos"));
        assertFalse(names.contains("Mariana Souza"));
    }

    /**
     * Testa o fluxo completo de criação e busca de serviços
     */
    @Test
    public void testServiceCreationAndQueryFlow() {
        // Preparar um profissional
        Professional professional = new Professional();
        professional.setName("Juliana Lima");
        professional.setEmail("juliana@example.com");
        professional.setPhone("(11) 94567-8901");
        professional.setSpecialties(Arrays.asList("Cabelo", "Maquiagem"));
        professional.setWorkStartTime(LocalTime.of(9, 0));
        professional.setWorkEndTime(LocalTime.of(18, 0));
        professional.setWorkDays(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));
        professional = professionalRepository.save(professional);
        
        Long professionalId = professional.getId();

        // Criar serviços para o profissional
        ServiceDto service1 = new ServiceDto();
        service1.setName("Corte Feminino");
        service1.setDescription("Corte moderno com finalização");
        service1.setDuration(60);
        service1.setPrice(BigDecimal.valueOf(120.00));
        service1.setCategory("Cabelo");
        service1.setProfessionalId(professionalId);

        ServiceDto service2 = new ServiceDto();
        service2.setName("Maquiagem para Festa");
        service2.setDescription("Maquiagem completa para eventos");
        service2.setDuration(90);
        service2.setPrice(BigDecimal.valueOf(180.00));
        service2.setCategory("Maquiagem");
        service2.setProfessionalId(professionalId);

        // Fazer requisição REST para criar serviços
        ResponseEntity<ServiceDto> response1 = restTemplate.postForEntity(
                baseUrl + "/api/services",
                service1,
                ServiceDto.class);
                
        ResponseEntity<ServiceDto> response2 = restTemplate.postForEntity(
                baseUrl + "/api/services",
                service2,
                ServiceDto.class);

        // Verificar respostas HTTP
        assertEquals(HttpStatus.CREATED, response1.getStatusCode());
        assertEquals(HttpStatus.CREATED, response2.getStatusCode());
        
        // Fazer requisição REST para buscar serviços por profissional
        ResponseEntity<List<ServiceDto>> response = restTemplate.exchange(
                baseUrl + "/api/services/professional/" + professionalId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ServiceDto>>() {});

        // Verificar resposta HTTP
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        
        // Verificar que os serviços foram salvos no banco de dados
        List<Service> services = serviceRepository.findAll();
        assertEquals(2, services.size());
    }

    /**
     * Testa o fluxo de busca de disponibilidade de um profissional
     */
    @Test
    public void testAvailabilityQueryFlow() {
        // Preparar um profissional com horários
        Professional professional = new Professional();
        professional.setName("Bruno Costa");
        professional.setEmail("bruno@example.com");
        professional.setPhone("(11) 95678-9012");
        professional.setSpecialties(Arrays.asList("Barba", "Cabelo"));
        professional.setWorkStartTime(LocalTime.of(9, 0));
        professional.setWorkEndTime(LocalTime.of(17, 0));
        professional.setWorkDays(Arrays.asList(
                DayOfWeek.MONDAY, 
                DayOfWeek.TUESDAY, 
                DayOfWeek.WEDNESDAY, 
                DayOfWeek.THURSDAY, 
                DayOfWeek.FRIDAY));
        professional = professionalRepository.save(professional);
        
        Long professionalId = professional.getId();
        
        // Definir data para busca (próxima segunda-feira)
        LocalDate date = LocalDate.now();
        while (date.getDayOfWeek() != DayOfWeek.MONDAY) {
            date = date.plusDays(1);
        }

        // Fazer requisição REST para buscar disponibilidade
        ResponseEntity<List<ScheduleDto>> response = restTemplate.exchange(
                baseUrl + "/api/professionals/" + professionalId + "/availability?date=" + date,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ScheduleDto>>() {});

        // Verificar resposta HTTP
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Deve ter horários disponíveis (das 9h às 17h, tipicamente em intervalos de 30 min)
        assertTrue(response.getBody().size() > 0);
        
        // Verificar que os horários estão dentro do período de trabalho
        for (ScheduleDto slot : response.getBody()) {
            assertTrue(slot.getTime().getHour() >= 9);
            assertTrue(slot.getTime().getHour() < 17 || 
                   (slot.getTime().getHour() == 17 && slot.getTime().getMinute() == 0));
        }
    }

    /**
     * Testa o fluxo de atualização de um profissional
     */
    @Test
    public void testUpdateProfessionalFlow() {
        // Preparar um profissional
        Professional professional = new Professional();
        professional.setName("Fernanda Gomes");
        professional.setEmail("fernanda@example.com");
        professional.setPhone("(11) 96789-0123");
        professional.setSpecialties(Arrays.asList("Estética", "Massagem"));
        professional.setWorkStartTime(LocalTime.of(10, 0));
        professional.setWorkEndTime(LocalTime.of(19, 0));
        professional.setWorkDays(Arrays.asList(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY));
        professional = professionalRepository.save(professional);
        
        Long professionalId = professional.getId();
        
        // Criar DTO com dados atualizados
        ProfessionalDto updatedData = new ProfessionalDto();
        updatedData.setId(professionalId);
        updatedData.setName("Fernanda Gomes Silva");
        updatedData.setEmail("fernanda.silva@example.com");
        updatedData.setPhone("(11) 96789-0123");
        updatedData.setSpecialties(Arrays.asList("Estética", "Massagem", "Spa"));
        updatedData.setWorkStartTime(LocalTime.of(9, 0));
        updatedData.setWorkEndTime(LocalTime.of(18, 0));
        updatedData.setWorkDays(Arrays.asList(
                DayOfWeek.MONDAY, 
                DayOfWeek.TUESDAY, 
                DayOfWeek.WEDNESDAY, 
                DayOfWeek.THURSDAY, 
                DayOfWeek.FRIDAY));

        // Fazer requisição REST para atualizar profissional
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        ResponseEntity<ProfessionalDto> response = restTemplate.exchange(
                baseUrl + "/api/professionals/" + professionalId,
                HttpMethod.PUT,
                new HttpEntity<>(updatedData, headers),
                ProfessionalDto.class);

        // Verificar resposta HTTP
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Fernanda Gomes Silva", response.getBody().getName());
        assertEquals("fernanda.silva@example.com", response.getBody().getEmail());
        assertEquals(3, response.getBody().getSpecialties().size());
        assertEquals(5, response.getBody().getWorkDays().size());
        
        // Verificar que o profissional foi atualizado no banco de dados
        Professional updatedProfessional = professionalRepository.findById(professionalId).orElse(null);
        assertNotNull(updatedProfessional);
        assertEquals("Fernanda Gomes Silva", updatedProfessional.getName());
        assertEquals("fernanda.silva@example.com", updatedProfessional.getEmail());
        assertEquals(3, updatedProfessional.getSpecialties().size());
        assertTrue(updatedProfessional.getSpecialties().contains("Spa"));
    }

    /**
     * Testa a resposta do sistema ao tentar acessar um profissional inexistente
     */
    @Test
    public void testNonExistentProfessionalHandling() {
        // Tentar buscar um profissional com ID inexistente
        ResponseEntity<ProfessionalDto> response = restTemplate.getForEntity(
                baseUrl + "/api/professionals/9999",
                ProfessionalDto.class);
        
        // Verificar resposta HTTP
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Testa a busca de serviços por nome
     */
    @Test
    public void testFindServicesByName() {
        // Preparar um profissional
        Professional professional = new Professional();
        professional.setName("Gabriel Mendes");
        professional.setEmail("gabriel@example.com");
        professional.setPhone("(11) 97890-1234");
        professional.setSpecialties(Arrays.asList("Cabelo"));
        professional.setWorkStartTime(LocalTime.of(8, 0));
        professional.setWorkEndTime(LocalTime.of(16, 0));
        professional.setWorkDays(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));
        professional = professionalRepository.save(professional);
        
        // Criar serviços com nomes similares
        Service service1 = new Service();
        service1.setName("Corte Masculino");
        service1.setDescription("Corte tradicional masculino");
        service1.setDuration(30);
        service1.setPrice(BigDecimal.valueOf(60.00));
        service1.setCategory("Cabelo");
        service1.setProfessionalId(professional.getId());
        serviceRepository.save(service1);

        Service service2 = new Service();
        service2.setName("Corte Masculino com Barba");
        service2.setDescription("Corte e barba completo");
        service2.setDuration(60);
        service2.setPrice(BigDecimal.valueOf(100.00));
        service2.setCategory("Cabelo");
        service2.setProfessionalId(professional.getId());
        serviceRepository.save(service2);

        Service service3 = new Service();
        service3.setName("Hidratação Capilar");
        service3.setDescription("Tratamento profundo");
        service3.setDuration(45);
        service3.setPrice(BigDecimal.valueOf(80.00));
        service3.setCategory("Cabelo");
        service3.setProfessionalId(professional.getId());
        serviceRepository.save(service3);

        // Fazer requisição REST para buscar serviços por nome
        ResponseEntity<List<ServiceDto>> response = restTemplate.exchange(
                baseUrl + "/api/services/search?name=corte",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ServiceDto>>() {});

        // Verificar resposta HTTP
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        
        // Verificar que os serviços retornados são os corretos
        List<String> names = new ArrayList<>();
        for (ServiceDto dto : response.getBody()) {
            names.add(dto.getName());
        }
        assertTrue(names.contains("Corte Masculino"));
        assertTrue(names.contains("Corte Masculino com Barba"));
        assertFalse(names.contains("Hidratação Capilar"));
    }
}
