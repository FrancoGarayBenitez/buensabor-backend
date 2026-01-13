package com.elbuensabor.entities;

/**
 * Enum para representar el estado calculado de una promoción.
 * VIGENTE: La promoción está activa y en su período de validez.
 * PROGRAMADA: La promoción está activa pero su fecha de inicio es futura.
 * EXPIRADA: La promoción ha pasado su fecha de finalización.
 * INACTIVA: La promoción ha sido desactivada manualmente por un administrador.
 */
public enum EstadoPromocion {
    VIGENTE,
    PROGRAMADA,
    EXPIRADA,
    INACTIVA
}