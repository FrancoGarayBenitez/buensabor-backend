package com.elbuensabor.services.impl;

import com.elbuensabor.entities.DatosMercadoPago;
import com.elbuensabor.entities.Pago;
import com.elbuensabor.repository.IDatosMercadoPagoRepository;
import com.elbuensabor.repository.IPagoRepository;
import com.elbuensabor.services.IDatosMercadoPagoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class DatosMercadoPagoServiceImpl implements IDatosMercadoPagoService {

    private static final Logger logger = LoggerFactory.getLogger(DatosMercadoPagoServiceImpl.class);

    @Autowired
    private IDatosMercadoPagoRepository repository;

    @Autowired
    private IPagoRepository pagoRepository;

    @Override
    @Transactional
    public DatosMercadoPago crearDatosInicialesPorPagoId(Long pagoId) {
        logger.info("Creando datos iniciales de MercadoPago para pago ID: {}", pagoId);

        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + pagoId));

        return crearDatosIniciales(pago);
    }

    @Override
    @Transactional
    public DatosMercadoPago crearDatosIniciales(Pago pago) {
        logger.info("Creando datos iniciales de MercadoPago para pago ID: {}", pago.getIdPago());

        // Verificar si ya existe un registro para este pago
        Optional<DatosMercadoPago> existente = repository.findByPagoIdPago(pago.getIdPago());
        if (existente.isPresent()) {
            logger.info("Ya existe DatosMercadoPago para pago ID: {}, retornando existente", pago.getIdPago());
            return existente.get();
        }

        // ✅ CREAR REGISTRO INICIAL SIN PAYMENT_ID
        DatosMercadoPago datos = new DatosMercadoPago();
        datos.setPago(pago);

        // ✅ CAMPOS INICIALES - SIN payment_id
        datos.setPaymentId(null); // Se llenará cuando llegue el webhook
        datos.setStatus("preference_created"); // Estado inicial personalizado
        datos.setStatusDetail("Waiting for payment"); // Detalle inicial
        datos.setDateCreated(LocalDateTime.now());

        // Campos que se llenarán después del webhook
        datos.setPaymentMethodId(null);
        datos.setPaymentTypeId(null);
        datos.setDateApproved(null);

        DatosMercadoPago datosGuardados = repository.save(datos);
        logger.info("✅ Datos MercadoPago iniciales creados con ID: {} (sin payment_id)",
                datosGuardados.getIdMercadoPago());

        return datosGuardados;
    }

    @Override
    @Transactional
    public DatosMercadoPago actualizarDatosDelPago(Long paymentId, String status, String statusDetail,
                                                   String paymentMethodId, String paymentTypeId) {
        logger.info("Actualizando datos de MercadoPago para payment ID: {}", paymentId);

        // Buscar por payment ID o crear nuevo registro si no existe
        Optional<DatosMercadoPago> datosOpt = repository.findByPaymentId(paymentId);

        DatosMercadoPago datos;
        if (datosOpt.isPresent()) {
            datos = datosOpt.get();
            logger.info("Datos existentes encontrados para payment ID: {}", paymentId);
        } else {
            // Esto no debería pasar en flujo normal, pero manejamos el caso
            logger.warn("No se encontraron datos para payment ID: {}. Creando nuevo registro.", paymentId);
            datos = new DatosMercadoPago();
            datos.setDateCreated(LocalDateTime.now());
        }

        // ✅ ACTUALIZAR CON DATOS REALES DEL WEBHOOK
        datos.setPaymentId(paymentId); // ¡Ahora sí tenemos payment_id!
        datos.setStatus(status);
        datos.setStatusDetail(statusDetail);
        datos.setPaymentMethodId(paymentMethodId);
        datos.setPaymentTypeId(paymentTypeId);

        if ("approved".equals(status)) {
            datos.setDateApproved(LocalDateTime.now());
            logger.info("Pago aprobado - fecha de aprobación establecida");
        }

        DatosMercadoPago datosActualizados = repository.save(datos);
        logger.info("✅ Datos MercadoPago actualizados exitosamente con payment_id: {}", paymentId);

        return datosActualizados;
    }

    @Override
    public Optional<DatosMercadoPago> buscarPorPaymentId(Long paymentId) {
        return repository.findByPaymentId(paymentId);
    }

    @Override
    public Optional<DatosMercadoPago> buscarPorPagoId(Long pagoId) {
        return repository.findByPagoIdPago(pagoId);
    }

    @Override
    @Transactional
    public void eliminarPorPagoId(Long pagoId) {
        logger.info("Eliminando datos de MercadoPago para pago ID: {}", pagoId);

        Optional<DatosMercadoPago> datosOpt = repository.findByPagoIdPago(pagoId);
        if (datosOpt.isPresent()) {
            repository.delete(datosOpt.get());
            logger.info("✅ Datos de MercadoPago eliminados");
        } else {
            logger.warn("No se encontraron datos de MercadoPago para pago ID: {}", pagoId);
        }
    }
}