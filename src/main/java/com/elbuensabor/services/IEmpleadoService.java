package com.elbuensabor.services;

import com.elbuensabor.dto.request.EmpleadoRequestDTO;
import com.elbuensabor.entities.Usuario;

public interface IEmpleadoService {
    Usuario registrarEmpleado(EmpleadoRequestDTO request, Usuario adminCreador);
}