package com.elbuensabor.services.impl;

import com.elbuensabor.dto.request.CompraInsumoRequestDTO;
import com.elbuensabor.dto.response.CompraInsumoResponseDTO;
import com.elbuensabor.entities.ArticuloInsumo;
import com.elbuensabor.entities.CompraInsumo;
import com.elbuensabor.entities.ArticuloManufacturadoDetalle;
import com.elbuensabor.entities.ArticuloManufacturado;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.repository.IArticuloInsumoRepository;
import com.elbuensabor.repository.ICompraInsumoRepository;
import com.elbuensabor.repository.IManufacturadoDetalleRepository;
import com.elbuensabor.repository.IArticuloManufacturadoRepository;
import com.elbuensabor.services.CompraInsumoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompraInsumoServiceImpl implements CompraInsumoService {

    private final ICompraInsumoRepository compraInsumoRepository;
    private final IArticuloInsumoRepository articuloInsumoRepository;
    private final IManufacturadoDetalleRepository manufacturadoDetalleRepository;
    private final IArticuloManufacturadoRepository articuloManufacturadoRepository;

    @Override
    @Transactional   // <<--- AGREGÁ ESTA ANOTACIÓN AQUÍ
    public void registrarCompra(CompraInsumoRequestDTO dto) {
        ArticuloInsumo insumo = articuloInsumoRepository.findById(dto.getInsumoId())
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado con ID: " + dto.getInsumoId()));

        CompraInsumo compra = new CompraInsumo();
        compra.setInsumo(insumo);
        compra.setCantidad(dto.getCantidad());
        compra.setPrecioUnitario(dto.getPrecioUnitario());
        compra.setFechaCompra(dto.getFechaCompra());

        insumo.setPrecioCompra(dto.getPrecioUnitario());
        insumo.setStockActual(insumo.getStockActual() + dto.getCantidad().intValue());

        compraInsumoRepository.save(compra);
        articuloInsumoRepository.save(insumo);

        List<ArticuloManufacturadoDetalle> detalles = manufacturadoDetalleRepository.findByArticuloInsumo_IdArticulo(insumo.getIdArticulo());
        for (ArticuloManufacturadoDetalle detalle : detalles) {
            ArticuloManufacturado producto = detalle.getArticuloManufacturado();

            double costoTotal = producto.getDetalles().stream()
                    .mapToDouble(d -> d.getCantidad() * d.getArticuloInsumo().getPrecioCompra())
                    .sum();

            double margen = producto.getMargenGanancia() != null ? producto.getMargenGanancia() : 2.0;
            double precioVenta = costoTotal * margen;

            producto.setPrecioVenta(precioVenta);
            articuloManufacturadoRepository.save(producto);
        }
    }

    @Override
    public List<CompraInsumo> getAllCompras() {
        return compraInsumoRepository.findAll();
    }

    @Override
    public CompraInsumo getCompraById(Long id) {
        return compraInsumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con ID: " + id));
    }

    @Override
    public List<CompraInsumo> getComprasByInsumoId(Long idInsumo) {
        return compraInsumoRepository.findByInsumo_IdArticulo(idInsumo);
    }

    // --- AGREGADO: método de mapeo a DTO
    public CompraInsumoResponseDTO toDto(CompraInsumo compra) {
        CompraInsumoResponseDTO dto = new CompraInsumoResponseDTO();
        dto.setId(compra.getId());
        dto.setIdArticuloInsumo(compra.getInsumo().getIdArticulo());
        dto.setDenominacionInsumo(compra.getInsumo().getDenominacion());
        dto.setCantidad(compra.getCantidad());
        dto.setPrecioUnitario(compra.getPrecioUnitario());
        dto.setFechaCompra(compra.getFechaCompra());
        if (compra.getInsumo().getImagenes() != null && !compra.getInsumo().getImagenes().isEmpty()) {
            dto.setImagenUrl(compra.getInsumo().getImagenes().get(0).getUrl());
        }
        return dto;
    }
}

