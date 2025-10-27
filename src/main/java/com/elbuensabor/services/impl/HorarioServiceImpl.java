package com.elbuensabor.services.impl;

import com.elbuensabor.dto.response.HorarioStatusResponseDTO;
import com.elbuensabor.services.IHorarioService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Service
public class HorarioServiceImpl implements IHorarioService {

    private final LocalTime NOCHE_APERTURA = LocalTime.of(19, 0);
    private final LocalTime NOCHE_CIERRE = LocalTime.of(23, 30); // Cambié de 23:59:59 a 23:30
    private final LocalTime MEDIODIA_APERTURA = LocalTime.of(11, 0);
    private final LocalTime MEDIODIA_CIERRE = LocalTime.of(15, 0);

    @Override
    public HorarioStatusResponseDTO getEstadoHorario() {
        LocalDateTime ahora = LocalDateTime.now();
        DayOfWeek dia = ahora.getDayOfWeek();
        LocalTime hora = ahora.toLocalTime();

        // 🚨 PARA TESTING - SIEMPRE RETORNAR ABIERTO
        // TODO: Quitar esto cuando quieras activar la validación real
        System.out.println("🕐 DEBUG Horario - Día: " + dia + ", Hora: " + hora);
        System.out.println("🏪 MODO TESTING: Siempre abierto");
        return new HorarioStatusResponseDTO(true, "Modo testing - siempre abierto");

        // LA LÓGICA REAL (comentada para testing):
        /*
        boolean estaAbierto = false;

        // Horario nocturno (todos los días)
        if (hora.isAfter(NOCHE_APERTURA.minusMinutes(1)) && hora.isBefore(NOCHE_CIERRE.plusMinutes(1))) {
            estaAbierto = true;
        }
        // Horario mediodía (solo fines de semana)
        else if ((dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) &&
                 hora.isAfter(MEDIODIA_APERTURA.minusMinutes(1)) && hora.isBefore(MEDIODIA_CIERRE.plusMinutes(1))) {
            estaAbierto = true;
        }

        if (estaAbierto) {
            return new HorarioStatusResponseDTO(true, "El local se encuentra abierto.");
        } else {
            return new HorarioStatusResponseDTO(false, "El local se encuentra cerrado y no es posible realizar pedidos en este momento.");
        }
        */
    }
}