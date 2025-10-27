package com.elbuensabor.services.impl;

import com.elbuensabor.entities.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // 🔒 CLAVE SECRETA: Debe ser de al menos 256 bits (32 caracteres en Base64).
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration; // e.g., 86400000 (24 horas en ms)

    // Tiempo de expiración del token de reseteo (más corto)
    @Value("${application.security.jwt.reset-token.expiration}")
    private long resetTokenExpiration; // e.g., 600000 (10 minutos en ms)

    // Generación del Token de Reseteo
    /**
     * Genera un token JWT de corta duración para el proceso de reseteo de contraseña.
     * Incluye solo el email del usuario.
     * @param usuario El usuario que solicita el reseteo.
     * @return Token JWT de reseteo.
     */
    public String generateResetToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", usuario.getEmail());

        // Usamos la expiración específica para el token de reseteo
        return buildToken(claims, usuario, resetTokenExpiration);
    }

    // 1. Generación del Token principal para el login
    public String generateToken(Usuario usuario) {
        // Carga el rol del usuario en los claims para usarlo en la autorización
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", usuario.getRol().name());
        claims.put("email", usuario.getEmail());
        claims.put("id", usuario.getIdUsuario());

        return buildToken(claims, usuario, jwtExpiration);
    }

    // 2. Construcción del Token
    private String buildToken(
            Map<String, Object> extraClaims,
            Usuario usuario,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(usuario.getEmail()) // El email es el "subject" del token
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 3. Obtiene la clave de firma (Base64 decode)
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // --- Métodos de Validación y Extracción ---

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
