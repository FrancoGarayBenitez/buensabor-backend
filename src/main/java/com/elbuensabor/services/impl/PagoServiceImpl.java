package com.elbuensabor.services.impl;

import com.elbuensabor.dto.request.PagoRequestDTO;
import com.elbuensabor.dto.response.PagoResponseDTO;
import com.elbuensabor.entities.*;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.repository.IPagoRepository;
import com.elbuensabor.repository.IFacturaRepository;
import com.elbuensabor.services.IPagoService;
import com.elbuensabor.services.mapper.PagoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PagoServiceImpl extends GenericServiceImpl<Pago, Long, PagoResponseDTO, IPagoRepository, PagoMapper>
        implements IPagoService {

    @Autowired
    private IFacturaRepository facturaRepository;

    public PagoServiceImpl(IPagoRepository repository, PagoMapper mapper) {
        super(repository, mapper, Pago.class, PagoResponseDTO.class);
    }

    @Override
    @Transactional
    public PagoResponseDTO crearPago(PagoRequestDTO pagoRequestDTO) {
        // Obtener la factura
        Factura factura = facturaRepository.findById(pagoRequestDTO.getFacturaId())
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada con ID: " + pagoRequestDTO.getFacturaId()));

        // Validar que el monto no exceda el saldo pendiente
        Double saldoPendiente = getSaldoPendienteFactura(pagoRequestDTO.getFacturaId());
        if (pagoRequestDTO.getMonto() > saldoPendiente) {
            throw new IllegalArgumentException("El monto del pago (" + pagoRequestDTO.getMonto() +
                    ") excede el saldo pendiente (" + saldoPendiente + ")");
        }

        // Mapear RequestDTO a Entity
        Pago pago = mapper.toEntityFromRequest(pagoRequestDTO);

        // Asignar valores que no vienen del DTO
        pago.setFactura(factura);
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setFechaCreacion(LocalDateTime.now());
        pago.setFechaActualizacion(LocalDateTime.now());

        Pago pagoGuardado = repository.save(pago);
        return mapper.toDTO(pagoGuardado);
    }

    @Override
    @Transactional
    public PagoResponseDTO actualizarEstadoPago(Long pagoId, EstadoPago nuevoEstado) {
        Pago pago = repository.findById(pagoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + pagoId));

        pago.setEstado(nuevoEstado);
        pago.setFechaActualizacion(LocalDateTime.now());

        Pago pagoActualizado = repository.save(pago);
        return mapper.toDTO(pagoActualizado);
    }

    @Override
    @Transactional
    public PagoResponseDTO procesarPagoMercadoPago(Long pagoId, String preferenceId) {
        Pago pago = repository.findById(pagoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + pagoId));

        pago.setMercadoPagoPreferenceId(preferenceId);
        pago.setEstado(EstadoPago.PROCESANDO);
        pago.setFechaActualizacion(LocalDateTime.now());

        Pago pagoActualizado = repository.save(pago);
        return mapper.toDTO(pagoActualizado);
    }

    @Override
    @Transactional
    public PagoResponseDTO confirmarPagoMercadoPago(Long paymentId, String status, String statusDetail) {
        Optional<Pago> pagoOpt = repository.findByMercadoPagoPaymentId(paymentId);

        if (pagoOpt.isEmpty()) {
            throw new ResourceNotFoundException("Pago no encontrado con Payment ID: " + paymentId);
        }

        Pago pago = pagoOpt.get();

        // Actualizar o crear datos de Mercado Pago
        if (pago.getDatosMercadoPago() == null) {
            DatosMercadoPago datosMp = new DatosMercadoPago();
            datosMp.setPago(pago);
            datosMp.setPaymentId(paymentId);
            pago.setDatosMercadoPago(datosMp);
        }

        pago.getDatosMercadoPago().setStatus(status);
        pago.getDatosMercadoPago().setStatusDetail(statusDetail);

        // Mapear estado de MP a estado interno
        EstadoPago estadoPago = mapMercadoPagoStatusToEstadoPago(status);
        pago.setEstado(estadoPago);
        pago.setFechaActualizacion(LocalDateTime.now());

        if ("approved".equals(status)) {
            pago.getDatosMercadoPago().setDateApproved(LocalDateTime.now());
        }

        Pago pagoActualizado = repository.save(pago);
        return mapper.toDTO(pagoActualizado);
    }

    @Override
    public List<PagoResponseDTO> getPagosByFactura(Long facturaId) {
        List<Pago> pagos = repository.findByFacturaIdFactura(facturaId);
        return pagos.stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<PagoResponseDTO> getPagosByEstado(EstadoPago estado) {
        List<Pago> pagos = repository.findByEstado(estado);
        return pagos.stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<PagoResponseDTO> getPagosByFormaPago(FormaPago formaPago) {
        List<Pago> pagos = repository.findByFormaPago(formaPago);
        return pagos.stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public boolean isFacturaCompletamentePagada(Long facturaId) {
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada con ID: " + facturaId));

        Double totalPagado = repository.getTotalPagadoByFactura(facturaId);
        return totalPagado >= factura.getTotalVenta();
    }

    @Override
    public Double getTotalPagadoFactura(Long facturaId) {
        return repository.getTotalPagadoByFactura(facturaId);
    }

    @Override
    public Double getSaldoPendienteFactura(Long facturaId) {
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada con ID: " + facturaId));

        Double totalPagado = getTotalPagadoFactura(facturaId);
        return factura.getTotalVenta() - totalPagado;
    }

    @Override
    @Transactional
    public PagoResponseDTO cancelarPago(Long pagoId) {
        return actualizarEstadoPago(pagoId, EstadoPago.CANCELADO);
    }

    @Override
    @Transactional
    public PagoResponseDTO procesarReembolso(Long pagoId) {
        return actualizarEstadoPago(pagoId, EstadoPago.REEMBOLSADO);
    }

    private EstadoPago mapMercadoPagoStatusToEstadoPago(String mpStatus) {
        return switch (mpStatus.toLowerCase()) {
            case "approved" -> EstadoPago.APROBADO;
            case "pending" -> EstadoPago.PENDIENTE;
            case "in_process" -> EstadoPago.PROCESANDO;
            case "rejected", "cancelled" -> EstadoPago.RECHAZADO;
            case "refunded" -> EstadoPago.REEMBOLSADO;
            default -> EstadoPago.PENDIENTE;
        };
    }
}