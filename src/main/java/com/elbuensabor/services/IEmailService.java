package com.elbuensabor.services;

public interface IEmailService {
    /**
     * Envía un email al usuario con el enlace para restablecer su contraseña.
     * @param toEmail Email del destinatario.
     * @param resetUrl El enlace completo que el usuario debe clickear.
     */
    void sendPasswordResetEmail(String toEmail, String resetUrl);
}
