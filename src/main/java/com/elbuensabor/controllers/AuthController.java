package com.elbuensabor.controllers;

import com.elbuensabor.dto.request.ClienteRegisterDTO;
import com.elbuensabor.dto.request.LoginRequestDTO;
import com.elbuensabor.dto.request.PasswordResetRequest;
import com.elbuensabor.dto.response.LoginResponseDTO;
import com.elbuensabor.dto.response.MessageResponse;
import com.elbuensabor.entities.Usuario;
import com.elbuensabor.services.IAuthService;
import jakarta.validation.Valid;

import java.util.logging.Logger;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = Logger.getLogger(AuthController.class.getName());
    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody ClienteRegisterDTO request){
        logger.info("=== NUEVO REGISTRO - DevTools funcionando ===");
        logger.info("Email del registro: " + request.getEmail());

        // Llama al servicio que hashea la contraseña, crea Usuario, Cliente y Domicilio
        authService.register(request);

        // Tras el registro, se recomienda que el cliente haga un POST /login
        return ResponseEntity.status(201).body(new MessageResponse("Registro exitoso. Procede al login."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        // Llama al servicio que valida las credenciales y genera el JWT
        LoginResponseDTO response = authService.login(request);

        // Devuelve el JWT en la respuesta
        return ResponseEntity.ok(response);
    }

    // Inicia el flujo de reseteo
    @PostMapping("/request-password-reset")
    public ResponseEntity<MessageResponse> requestPasswordReset(
            // @AuthenticationPrincipal inyecta el objeto Usuario del JWT
            @AuthenticationPrincipal Usuario usuarioAutenticado
    ) {
        authService.requestPasswordReset(usuarioAutenticado);
        return ResponseEntity.ok(new MessageResponse("Instrucciones de reseteo de contraseña enviadas a su email."));
    }

    // Recibe el token y la nueva contraseña del email
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new MessageResponse("Contraseña restablecida exitosamente."));
    }

    // Eliminación de cuenta
    @DeleteMapping("/delete-account")
    public ResponseEntity<MessageResponse> deleteAccount(
            @AuthenticationPrincipal Usuario usuarioAutenticado
    ) {
        authService.deleteAccount(usuarioAutenticado);
        return ResponseEntity.ok(new MessageResponse("Su cuenta ha sido eliminada."));
    }
}
