package com.elbuensabor.tools;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Clase utilitaria para generar claves secretas seguras (Base64) para JWT.
 * Esta clase no forma parte de la lógica de la aplicación y debe ser ejecutada
 * una única vez para obtener el valor del secreto, y luego puede ser eliminada.
 */
public class KeyGenerator {

    public static void main(String[] args) {
        // Genera 32 bytes (256 bits) de datos aleatorios.
        // 256 bits es el tamaño mínimo recomendado para HMAC-SHA256 en JWT.
        int keyLengthBytes = 32;

        byte[] keyBytes = new byte[keyLengthBytes];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(keyBytes);

        // Codifica la clave en formato Base64 para que sea fácil de almacenar
        // en archivos de propiedades.
        String secretKeyBase64 = Base64.getEncoder().encodeToString(keyBytes);

        System.out.println("===============================================================");
        System.out.println("           🔑 CLAVE SECRETA JWT GENERADA (Base64) 🔑           ");
        System.out.println("===============================================================");
        System.out.println("Longitud de bytes: " + keyLengthBytes + " (256 bits)");
        System.out.println("CLAVE GENERADA:");
        System.out.println(secretKeyBase64);
        System.out.println("===============================================================");
        System.out.println("COPIA esta clave y pégala en tu archivo 'application.properties' o configúrala como variable de entorno.");
    }
}
