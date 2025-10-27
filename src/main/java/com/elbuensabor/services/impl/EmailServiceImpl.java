package com.elbuensabor.services.impl;


import com.elbuensabor.services.IEmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements IEmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetUrl) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Restablecer Contraseña para El Buen Sabor");

        String content = "Hola,\n\n"
                + "Has solicitado restablecer tu contraseña. Haz clic en el siguiente enlace para continuar:\n\n"
                + resetUrl + "\n\n"
                + "Si no solicitaste este cambio, ignora este correo.\n\n"
                + "El equipo de El Buen Sabor";

        message.setText(content);

        try {
            mailSender.send(message);
            System.out.println("Email de reseteo enviado a: " + toEmail);
        } catch (Exception e) {
            System.err.println("Error al enviar el email de reseteo: " + e.getMessage());
        }
    }
}
