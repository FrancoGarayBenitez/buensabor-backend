package com.elbuensabor.controllers;

import com.elbuensabor.entities.UnidadMedida;
import com.elbuensabor.repository.IUnidadMedidaRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/unidades-medida")

public class UnidadMedidaController {

    @Autowired
    private IUnidadMedidaRepository unidadMedidaRepository;

    @GetMapping
    public ResponseEntity<List<UnidadMedida>> getAllUnidadesMedida() {
        List<UnidadMedida> unidades = unidadMedidaRepository.findAll();
        return ResponseEntity.ok(unidades);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnidadMedida> getUnidadMedidaById(@PathVariable Long id) {
        UnidadMedida unidad = unidadMedidaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unidad de medida no encontrada"));
        return ResponseEntity.ok(unidad);
    }

    @PostMapping
    public ResponseEntity<UnidadMedida> createUnidadMedida(@Valid @RequestBody UnidadMedida unidadMedida) {
        // Limpiar ID para creación
        unidadMedida.setIdUnidadMedida(null);
        UnidadMedida savedUnidad = unidadMedidaRepository.save(unidadMedida);
        return new ResponseEntity<>(savedUnidad, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UnidadMedida> updateUnidadMedida(@PathVariable Long id, @Valid @RequestBody UnidadMedida unidadMedida) {
        if (!unidadMedidaRepository.existsById(id)) {
            throw new RuntimeException("Unidad de medida no encontrada");
        }
        unidadMedida.setIdUnidadMedida(id);
        UnidadMedida updatedUnidad = unidadMedidaRepository.save(unidadMedida);
        return ResponseEntity.ok(updatedUnidad);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUnidadMedida(@PathVariable Long id) {
        if (!unidadMedidaRepository.existsById(id)) {
            throw new RuntimeException("Unidad de medida no encontrada");
        }
        unidadMedidaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}