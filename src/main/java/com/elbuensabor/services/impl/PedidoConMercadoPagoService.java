package com.elbuensabor.services.impl;

import com.elbuensabor.dto.request.MercadoPagoPreferenceDTO;
import com.elbuensabor.dto.request.PedidoConMercadoPagoRequestDTO;
import com.elbuensabor.dto.request.PedidoRequestDTO;
import com.elbuensabor.dto.request.PagoRequestDTO; // ✅ NUEVO IMPORT
import com.elbuensabor.dto.response.FacturaResponseDTO;
import com.elbuensabor.dto.response.MercadoPagoPreferenceResponseDTO;
import com.elbuensabor.dto.response.PedidoConMercadoPagoResponseDTO;
import com.elbuensabor.dto.response.PedidoResponseDTO;
import com.elbuensabor.dto.response.PagoResponseDTO; // ✅ NUEVO IMPORT
import com.elbuensabor.entities.Articulo;
import com.elbuensabor.entities.DatosMercadoPago; // ✅ NUEVO IMPORT
import com.elbuensabor.entities.FormaPago; // ✅ NUEVO IMPORT
import com.elbuensabor.repository.IArticuloRepository;
import com.elbuensabor.services.IFacturaService;
import com.elbuensabor.services.IMercadoPagoService;
import com.elbuensabor.services.IPedidoService;
import com.elbuensabor.services.IPagoService; // ✅ NUEVO IMPORT
import com.elbuensabor.services.IDatosMercadoPagoService; // ✅ NUEVO IMPORT
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoConMercadoPagoService {

    private static final Logger logger = LoggerFactory.getLogger(PedidoConMercadoPagoService.class);

    @Autowired
    private IPedidoService pedidoService;

    @Autowired
    private IFacturaService facturaService;

    @Autowired
    private IMercadoPagoService mercadoPagoService;

    @Autowired
    private IArticuloRepository articuloRepository;

    // ✅ NUEVAS DEPENDENCIAS PARA PERSISTENCIA
    @Autowired
    private IPagoService pagoService;

    @Autowired
    private IDatosMercadoPagoService datosMpService;

    @Transactional
    public PedidoConMercadoPagoResponseDTO crearPedidoConMercadoPago(PedidoConMercadoPagoRequestDTO request) {
        long inicioTiempo = System.currentTimeMillis();

        try {
            logger.info("=== INICIANDO CREACIÓN DE PEDIDO CON MERCADOPAGO ===");
            logger.info("Cliente: {}, Tipo envío: {}, Email: {}",
                    request.getIdCliente(), request.getTipoEnvio(), request.getEmailComprador());

            // ==================== PASO 1: CALCULAR TOTALES CON DESCUENTOS ====================
            PedidoConMercadoPagoResponseDTO.CalculoTotalesDTO calculoTotales = calcularTotalesConDescuentos(request);
            logger.info("Totales calculados: Subtotal: ${}, Descuento: ${}, Total final: ${}",
                    calculoTotales.getSubtotalProductos(),
                    calculoTotales.getDescuentoTakeAway(),
                    calculoTotales.getTotalFinal());

            // ==================== PASO 2: CREAR PEDIDO USANDO TU LÓGICA EXISTENTE ====================
            PedidoRequestDTO pedidoRequest = convertirAPedidoRequest(request, calculoTotales);

            logger.info("Creando pedido con tu PedidoService existente...");
            PedidoResponseDTO pedidoCreado = pedidoService.crearPedido(pedidoRequest);
            logger.info("✅ Pedido creado exitosamente: ID {}", pedidoCreado.getIdPedido());

            // ==================== PASO 3: OBTENER FACTURA (YA CREADA AUTOMÁTICAMENTE) ====================
            logger.info("Obteniendo factura automática del pedido...");
            FacturaResponseDTO factura = pedidoService.getFacturaPedido(pedidoCreado.getIdPedido());
            logger.info("✅ Factura obtenida: ID {}, Total: ${}", factura.getIdFactura(), factura.getTotalVenta());

            // ==================== PASO 4: CREAR PREFERENCIA DE MERCADOPAGO ====================
            PedidoConMercadoPagoResponseDTO.MercadoPagoInfoDTO mercadoPagoInfo = null;

            if (request.getCrearPreferenciaMercadoPago()) {
                logger.info("Creando preferencia de MercadoPago...");
                mercadoPagoInfo = crearPreferenciaMercadoPago(request, pedidoCreado, calculoTotales);
                logger.info("✅ Estado MercadoPago: {}", mercadoPagoInfo.getPreferenciaCreada() ? "EXITOSO" : "FALLÓ");

                // ==================== 🆕 PASO 5: PERSISTIR PAGO EN BASE DE DATOS 🆕 ====================
                if (mercadoPagoInfo.getPreferenciaCreada()) {
                    logger.info("💾 Paso 5: Persistiendo pago en base de datos...");

                    try {
                        // Crear registro de Pago
                        PagoRequestDTO pagoRequest = new PagoRequestDTO();
                        pagoRequest.setFacturaId(factura.getIdFactura());
                        pagoRequest.setFormaPago(FormaPago.MERCADO_PAGO);
                        pagoRequest.setMonto(calculoTotales.getTotalFinal());
                        pagoRequest.setMoneda("ARS");
                        pagoRequest.setDescripcion("Pago con MercadoPago - Pedido #" + pedidoCreado.getIdPedido());
                        pagoRequest.setMercadoPagoPreferenceId(mercadoPagoInfo.getPreferenceId()); // 🔑 CLAVE

                        PagoResponseDTO pagoCreado = pagoService.crearPago(pagoRequest);
                        logger.info("✅ Pago persistido con ID: {} | Preference: {}",
                                pagoCreado.getIdPago(), mercadoPagoInfo.getPreferenceId());

                        // Crear datos iniciales de MercadoPago
                        DatosMercadoPago datosMp = datosMpService.crearDatosInicialesPorPagoId(pagoCreado.getIdPago());
                        logger.info("✅ DatosMercadoPago creados con ID: {}", datosMp.getIdMercadoPago());

                    } catch (Exception e) {
                        logger.error("❌ Error persistiendo pago (el pedido ya fue creado): {}", e.getMessage(), e);
                        // No fallar el proceso completo, el pedido ya existe
                    }
                }

            } else {
                logger.info("⏭️ Creación de preferencia MP omitida por configuración");
                mercadoPagoInfo = new PedidoConMercadoPagoResponseDTO.MercadoPagoInfoDTO();
                mercadoPagoInfo.setPreferenciaCreada(false);
                mercadoPagoInfo.setErrorMercadoPago("No solicitado por el usuario");
            }

            // ==================== PASO 6: RESPUESTA UNIFICADA ====================
            long tiempoProcesamiento = System.currentTimeMillis() - inicioTiempo;

            logger.info("🎉 ¡PROCESO COMPLETADO EXITOSAMENTE EN {}ms!", tiempoProcesamiento);

            return PedidoConMercadoPagoResponseDTO.exitoso(
                    pedidoCreado,
                    factura,
                    calculoTotales,
                    mercadoPagoInfo,
                    tiempoProcesamiento
            );

        } catch (Exception e) {
            long tiempoProcesamiento = System.currentTimeMillis() - inicioTiempo;
            logger.error("❌ Error en el proceso: {}", e.getMessage(), e);

            return PedidoConMercadoPagoResponseDTO.conError(
                    "Error creando pedido: " + e.getMessage(),
                    tiempoProcesamiento
            );
        }
    }

    // ==================== RESTO DE MÉTODOS (SIN CAMBIOS) ====================

    private PedidoConMercadoPagoResponseDTO.CalculoTotalesDTO calcularTotalesConDescuentos(PedidoConMercadoPagoRequestDTO request) {
        logger.info("Calculando totales con descuentos...");

        // Calcular subtotal de productos
        double subtotalProductos = 0;
        for (var detalle : request.getDetalles()) {
            Articulo articulo = articuloRepository.findById(detalle.getIdArticulo())
                    .orElseThrow(() -> new RuntimeException("Artículo no encontrado: " + detalle.getIdArticulo()));

            subtotalProductos += articulo.getPrecioVenta() * detalle.getCantidad();
        }

        PedidoConMercadoPagoResponseDTO.CalculoTotalesDTO calculo = new PedidoConMercadoPagoResponseDTO.CalculoTotalesDTO();
        calculo.setSubtotalProductos(subtotalProductos);
        calculo.setTipoEnvio(request.getTipoEnvio());

        if ("TAKE_AWAY".equals(request.getTipoEnvio()) && request.getAplicarDescuentoTakeAway()) {
            // ✅ TAKE_AWAY: Aplicar descuento
            double porcentajeDescuento = request.getPorcentajeDescuentoTakeAway();
            double montoDescuento = subtotalProductos * (porcentajeDescuento / 100);

            calculo.setDescuentoTakeAway(montoDescuento);
            calculo.setPorcentajeDescuento(porcentajeDescuento);
            calculo.setGastosEnvio(0.0);
            calculo.setTotalFinal(subtotalProductos - montoDescuento);
            calculo.setSeAplicoDescuento(true);
            calculo.setResumenCalculo(String.format("Subtotal: $%.2f - Descuento TAKE_AWAY (%.1f%%): $%.2f = Total: $%.2f",
                    subtotalProductos, porcentajeDescuento, montoDescuento, calculo.getTotalFinal()));

            logger.info("✅ TAKE_AWAY: Descuento de {}% aplicado: ${}", porcentajeDescuento, montoDescuento);

        } else if ("DELIVERY".equals(request.getTipoEnvio())) {
            // ✅ DELIVERY: Sin descuento + gastos de envío
            double gastosEnvio = request.getGastosEnvioDelivery();

            calculo.setDescuentoTakeAway(0.0);
            calculo.setPorcentajeDescuento(0.0);
            calculo.setGastosEnvio(gastosEnvio);
            calculo.setTotalFinal(subtotalProductos + gastosEnvio);
            calculo.setSeAplicoDescuento(false);
            calculo.setResumenCalculo(String.format("Subtotal: $%.2f + Gastos envío: $%.2f = Total: $%.2f",
                    subtotalProductos, gastosEnvio, calculo.getTotalFinal()));

            logger.info("✅ DELIVERY: Gastos de envío agregados: ${}", gastosEnvio);

        } else {
            // ✅ TAKE_AWAY sin descuento
            calculo.setDescuentoTakeAway(0.0);
            calculo.setPorcentajeDescuento(0.0);
            calculo.setGastosEnvio(0.0);
            calculo.setTotalFinal(subtotalProductos);
            calculo.setSeAplicoDescuento(false);
            calculo.setResumenCalculo(String.format("Subtotal: $%.2f (sin descuentos) = Total: $%.2f",
                    subtotalProductos, calculo.getTotalFinal()));
        }

        return calculo;
    }

    private PedidoRequestDTO convertirAPedidoRequest(PedidoConMercadoPagoRequestDTO request,
                                                     PedidoConMercadoPagoResponseDTO.CalculoTotalesDTO totales) {

        PedidoRequestDTO pedidoRequest = new PedidoRequestDTO();

        // Mapear campos directamente
        pedidoRequest.setIdCliente(request.getIdCliente());
        pedidoRequest.setTipoEnvio(request.getTipoEnvio());
        pedidoRequest.setIdDomicilio(request.getIdDomicilio());
        pedidoRequest.setIdSucursal(request.getIdSucursal());
        pedidoRequest.setDetalles(request.getDetalles());

        // Observaciones enriquecidas con info de descuentos
        StringBuilder observaciones = new StringBuilder();
        if (request.getObservaciones() != null && !request.getObservaciones().trim().isEmpty()) {
            observaciones.append(request.getObservaciones()).append(". ");
        }
        observaciones.append(totales.getResumenCalculo());

        pedidoRequest.setObservaciones(observaciones.toString());

        return pedidoRequest;
    }

    private PedidoConMercadoPagoResponseDTO.MercadoPagoInfoDTO crearPreferenciaMercadoPago(
            PedidoConMercadoPagoRequestDTO request,
            PedidoResponseDTO pedido,
            PedidoConMercadoPagoResponseDTO.CalculoTotalesDTO totales) {

        PedidoConMercadoPagoResponseDTO.MercadoPagoInfoDTO mpInfo = new PedidoConMercadoPagoResponseDTO.MercadoPagoInfoDTO();

        try {
            // Crear items para MercadoPago
            List<MercadoPagoPreferenceDTO.ItemDTO> items = new ArrayList<>();

            MercadoPagoPreferenceDTO.ItemDTO item = new MercadoPagoPreferenceDTO.ItemDTO();
            item.setTitle("Pedido El Buen Sabor #" + pedido.getIdPedido());
            item.setQuantity(1);
            item.setUnitPrice(totales.getTotalFinal());
            item.setCurrencyId("ARS");
            item.setDescription("Pedido de El Buen Sabor - " + request.getTipoEnvio());
            items.add(item);

            // Crear payer
            MercadoPagoPreferenceDTO.PayerDTO payer = new MercadoPagoPreferenceDTO.PayerDTO();
            payer.setName(request.getNombreComprador());
            payer.setSurname(request.getApellidoComprador());
            payer.setEmail(request.getEmailComprador());

            // ✅ PREFERENCIA SIMPLIFICADA PARA SANDBOX
            MercadoPagoPreferenceDTO preferenceDTO = new MercadoPagoPreferenceDTO();
            preferenceDTO.setItems(items);
            preferenceDTO.setPayer(payer);

            // ✅ EXTERNAL REFERENCE SIMPLE
            String externalRef = "PEDIDO_" + pedido.getIdPedido() + "_" + System.currentTimeMillis();
            preferenceDTO.setExternalReference(externalRef);

            logger.info("Creando preferencia simplificada para sandbox...");
            logger.info("Items: {}, Payer: {}, External Ref: {}",
                    items.size(), payer.getEmail(), externalRef);

            // Llamar al service de MercadoPago
            MercadoPagoPreferenceResponseDTO preference = mercadoPagoService.crearPreferencia(preferenceDTO);

            // Mapear respuesta exitosa
            mpInfo.setPreferenciaCreada(true);
            mpInfo.setPreferenceId(preference.getId());
            mpInfo.setLinkPago(preference.getInitPoint());
            mpInfo.setLinkPagoSandbox(preference.getSandboxInitPoint());
            mpInfo.setExternalReference(externalRef);

            logger.info("✅ Preferencia MP simplificada creada exitosamente: {}", preference.getId());

        } catch (Exception e) {
            logger.error("❌ Error creando preferencia MP: {}", e.getMessage(), e);

            mpInfo.setPreferenciaCreada(false);
            mpInfo.setErrorMercadoPago("Error sandbox: " + e.getMessage());
        }

        return mpInfo;
    }

    @Transactional(readOnly = true)
    public PedidoConMercadoPagoResponseDTO.CalculoTotalesDTO calcularTotalesPreview(PedidoConMercadoPagoRequestDTO request) {
        logger.info("Calculando totales preview para tipo envío: {}", request.getTipoEnvio());
        return calcularTotalesConDescuentos(request);
    }
}