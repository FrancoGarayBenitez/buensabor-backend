package com.elbuensabor.controllers;

import com.elbuensabor.dto.request.EmpleadoRequestDTO;
import com.elbuensabor.dto.response.MessageResponse;
import com.elbuensabor.entities.Usuario;
import com.elbuensabor.services.IEmpleadoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/empleados")
@PreAuthorize("hasAuthority('ADMIN')") // Solo ADMIN puede acceder
public class EmpleadoController {

    private final IEmpleadoService empleadoService;

    public EmpleadoController(IEmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @PostMapping("/registrar")
    public ResponseEntity<MessageResponse> registrarEmpleado(
            @Valid @RequestBody EmpleadoRequestDTO request,
            @AuthenticationPrincipal Usuario adminAutenticado) {
        empleadoService.registrarEmpleado(request, adminAutenticado);

        return ResponseEntity.status(201).body(
                new MessageResponse(
                        String.format("Empleado %s creado exitosamente con rol %s",
                                request.getEmail(), request.getRol())));
    }
}