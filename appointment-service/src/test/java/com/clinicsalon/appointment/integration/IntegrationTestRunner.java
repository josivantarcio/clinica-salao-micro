package com.clinicsalon.appointment.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Esta classe executa testes de integração para validar a comunicação entre
 * appointment-service e finance-service.
 * 
 * IMPORTANTE: Este teste requer que ambos os serviços estejam em execução.
 * Use o script start-services.bat para iniciar todos os serviços antes de executar.
 */
@SpringBootTest
@ActiveProfiles("test")
public class IntegrationTestRunner {
    
    // Configuração das URLs dos serviços
    private static final String EUREKA_SERVICE_URL = "http://localhost:8761";
    private static final String APPOINTMENT_SERVICE_URL = "http://localhost:8084";
    private static final String FINANCE_SERVICE_URL = "http://localhost:8083";
    
    private final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    @Test
    public void testAppointmentServiceIsRunning() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(APPOINTMENT_SERVICE_URL + "/actuator/health"))
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertTrue(response.statusCode() >= 200 && response.statusCode() < 300, 
                "Appointment service não está respondendo corretamente. Status: " + response.statusCode());
        assertTrue(response.body().contains("UP"), 
                "Appointment service não está saudável: " + response.body());
    }

    @Test
    public void testFinanceServiceIsAvailable() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(FINANCE_SERVICE_URL + "/actuator/health"))
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertTrue(response.statusCode() >= 200 && response.statusCode() < 300,
                "Finance service não está respondendo corretamente. Status: " + response.statusCode());
        assertTrue(response.body().contains("UP"),
                "Finance service não está saudável: " + response.body());
    }
    
    @Test
    public void testEurekaServiceIsAvailable() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(EUREKA_SERVICE_URL + "/actuator/health"))
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertTrue(response.statusCode() >= 200 && response.statusCode() < 300,
                "Eureka service não está respondendo corretamente. Status: " + response.statusCode());
    }
    
    // Nota: Este teste requer que haja um agendamento criado com ID 1
    // e que o appointment-service e finance-service estejam em execução
    @Test
    public void testPaymentLinkCreation() throws IOException, InterruptedException {
        String requestBody = """
                {
                  "description": "Teste de integração - Pagamento",
                  "clientEmail": "teste@example.com",
                  "dueDate": "2025-06-15"
                }
                """;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(APPOINTMENT_SERVICE_URL + "/api/appointments/1/payment-link"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Este teste pode falhar se o appointment com ID 1 não existir ou se o finance-service
        // não estiver acessível. Use apenas para testes manuais de integração.
        System.out.println("Payment Link Response: " + response.body());
        
        assertTrue(response.statusCode() < 500, 
                "Erro grave na criação do link de pagamento: " + response.body());
    }
    
    @Test
    public void testCreatePaymentLink() throws IOException, InterruptedException {
        System.out.println("\n1. Testando criação de link de pagamento...");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(APPOINTMENT_SERVICE_URL + "/api/payments/generate-link/1"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Status code: " + response.statusCode());
        System.out.println("Response body: " + response.body());
        
        assertTrue(response.statusCode() >= 200 && response.statusCode() < 300, 
                "Erro na criação do link de pagamento: " + response.body());
    }
    
    @Test
    public void testGetPaymentStatus() throws IOException, InterruptedException {
        System.out.println("\n2. Testando verificação de status de pagamento...");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(APPOINTMENT_SERVICE_URL + "/api/payments/status/1"))
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Status code: " + response.statusCode());
        System.out.println("Response body: " + response.body());
        
        assertTrue(response.statusCode() >= 200 && response.statusCode() < 300, 
                "Erro na verificação de status de pagamento: " + response.body());
    }
    
    @Test
    public void testProcessRefund() throws IOException, InterruptedException {
        System.out.println("\n3. Testando processamento de reembolso...");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(APPOINTMENT_SERVICE_URL + "/api/payments/refund/1"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Status code: " + response.statusCode());
        System.out.println("Response body: " + response.body());
        
        assertTrue(response.statusCode() >= 200 && response.statusCode() < 300, 
                "Erro no processamento de reembolso: " + response.body());
    }
}
