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
            request -> {
                // Extrair o caminho da requisição
                final String originalPath = request.getURI().getPath();
                
                // Processar o caminho para usar formato consistente
                final String processedPath = processPath(originalPath);
                
                // Verificar se o caminho processado corresponde a algum endpoint aberto
                return openApiEndpoints
                        .stream()
                        .noneMatch(uri -> matchesPattern(processedPath, uri));
            };
    
    // Processa o caminho da URL para formato consistente
    private String processPath(String originalPath) {
        if (originalPath.startsWith("/")) {
            // Já é um caminho relativo
            return originalPath;
        } else {
            // Extrai o caminho da URL completa
            int pathStartIndex = originalPath.indexOf('/', 8); // Pula 'http://' ou 'https:/'
            if (pathStartIndex >= 0) {
                return originalPath.substring(pathStartIndex);
            }
            return originalPath; // Retorna o original se não conseguir processar
        }
    }
    
    // Verifica se um caminho corresponde a um padrão, incluindo wildcards
    private boolean matchesPattern(String path, String pattern) {
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.startsWith(prefix);
        }
        return path.equals(pattern) || path.contains(pattern);
    }
}
