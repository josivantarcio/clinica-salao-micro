package com.clinicsalon.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtUtil {

    @Value("${clinicsalon.security.jwt.token.secret-key}")
    private String secretKey;

    /**
     * Valida o token JWT verificando se é bem formado, não expirou e tem assinatura válida
     * @param token O token JWT a ser validado
     * @return true se o token for válido, false caso contrário
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extrai o nome de usuário do token JWT
     * @param token O token JWT
     * @return O nome de usuário armazenado no token
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extrai as roles do token JWT como uma string separada por vírgulas
     * @param token O token JWT
     * @return Uma string com as roles separadas por vírgula
     */
    @SuppressWarnings("unchecked")
    public String extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        List<String> roles = (List<String>) claims.get("roles");
        if (roles != null) {
            return roles.stream().collect(Collectors.joining(","));
        }
        return "";
    }

    /**
     * Verifica se o token expirou
     * @param token O token JWT
     * @return true se o token estiver expirado, false caso contrário
     */
    public boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
