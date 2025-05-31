package com.clinicsalon.gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    // Lista de rotas abertas que não requerem autenticação
    public static final List<String> openApiEndpoints = List.of(
            "/auth/register",
            "/auth/login",
            "/actuator/**",
            "/eureka/**"
    );

    // Predicate para verificar se uma rota está protegida
    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}
