package com.elbuensabor.services.impl;

import com.elbuensabor.dto.response.FacturaResponseDTO;
import com.elbuensabor.dto.response.PagoSummaryDTO;
import com.elbuensabor.dto.response.DetallePedidoResponseDTO;
import com.elbuensabor.dto.response.DomicilioResponseDTO;
import com.elbuensabor.exceptions.ResourceNotFoundException;
import com.elbuensabor.services.IFacturaPdfService;
import com.elbuensabor.services.IFacturaService;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class FacturaPdfServiceImpl implements IFacturaPdfService {

    private static final Logger logger = LoggerFactory.getLogger(FacturaPdfServiceImpl.class);

    @Autowired
    private IFacturaService facturaService;

    // 🎨 COLORES CORPORATIVOS MEJORADOS DE EL BUEN SABOR
    private static final Color PRIMARY_COLOR = new DeviceRgb(205, 108, 80);      // #CD6C50 - Terracota principal
    private static final Color SECONDARY_COLOR = new DeviceRgb(184, 90, 66);     // #b85a42 - Terracota oscuro
    private static final Color ACCENT_COLOR = new DeviceRgb(184, 90, 66);        // #b85a42 - Para totales destacados
    private static final Color LIGHT_GRAY = new DeviceRgb(248, 249, 250);        // Gris más suave
    private static final Color WHITE_COLOR = new DeviceRgb(255, 255, 255);       // Blanco
    private static final Color BORDER_COLOR = new DeviceRgb(220, 220, 220);      // Bordes sutiles
    private static final Color TEXT_DARK = new DeviceRgb(33, 37, 41);            // Texto principal más suave
    private static final Color TEXT_MUTED = new DeviceRgb(108, 117, 125);        // Texto secundario

    // Formatter para moneda argentina
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

    @Override
    public byte[] generarFacturaPdf(Long facturaId) {
        logger.info("Generando PDF para factura ID: {}", facturaId);
        FacturaResponseDTO factura = facturaService.findById(facturaId);
        return generarFacturaPdf(factura);
    }

    @Override
    public byte[] generarFacturaPdf(FacturaResponseDTO facturaDTO) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);

            // Configurar márgenes
            document.setMargins(50, 50, 50, 50);

            // Cargar fuentes
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // 🐛 ===== AQUÍ VAN LOS LOGS DE DEBUG =====
            logger.info("🔍 =================================");
            logger.info("🔍 DEBUG COMPLETO FACTURA {}", facturaDTO.getIdFactura());
            logger.info("🔍 =================================");
            logger.info("🔍 SubTotal: {}", facturaDTO.getSubTotal());
            logger.info("🔍 Descuento: {}", facturaDTO.getDescuento());
            logger.info("🔍 GastosEnvio: {}", facturaDTO.getGastosEnvio());
            logger.info("🔍 TotalVenta: {}", facturaDTO.getTotalVenta());
            logger.info("🔍 TipoEnvio: {}", facturaDTO.getTipoEnvio());
            logger.info("🔍 ObservacionesPedido: '{}'", facturaDTO.getObservacionesPedido());
            logger.info("🔍 DetallesPedido count: {}", facturaDTO.getDetallesPedido() != null ? facturaDTO.getDetallesPedido().size() : "NULL");

            if (facturaDTO.getDetallesPedido() != null) {
                for (DetallePedidoResponseDTO detalle : facturaDTO.getDetallesPedido()) {
                    logger.info("🔍   Detalle: {} x{} = {} (obs: '{}')",
                            detalle.getDenominacionArticulo(),
                            detalle.getCantidad(),
                            detalle.getSubtotal(),
                            detalle.getObservaciones());
                }
            }
            logger.info("🔍 =================================");
            // ===== FIN LOGS DE DEBUG =====

            // Construir el documento
            agregarEncabezado(document, boldFont, regularFont);
            agregarSeparador(document);
            agregarInformacionFactura(document, facturaDTO, boldFont, regularFont);
            agregarSeparador(document);
            agregarInformacionCliente(document, facturaDTO, boldFont, regularFont);
            agregarSeparador(document);
            agregarDetallesPedido(document, facturaDTO, boldFont, regularFont);
            agregarTotales(document, facturaDTO, boldFont, regularFont);
            agregarInformacionPagos(document, facturaDTO, boldFont, regularFont);
            agregarPiePagina(document, regularFont);

            document.close();

            logger.info("PDF generado exitosamente para factura {}", facturaDTO.getIdFactura());
            return baos.toByteArray();

        } catch (Exception e) {
            logger.error("Error generando PDF para factura {}: {}", facturaDTO.getIdFactura(), e.getMessage(), e);
            throw new RuntimeException("Error al generar PDF de factura: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] generarFacturaPdfByPedidoId(Long pedidoId) {
        logger.info("Generando PDF para pedido ID: {}", pedidoId);
        FacturaResponseDTO factura = facturaService.findByPedidoId(pedidoId);
        return generarFacturaPdf(factura);
    }

    // ==================== MÉTODOS PRIVADOS PARA CONSTRUCCIÓN DEL PDF ====================

    private void agregarEncabezado(Document document, PdfFont boldFont, PdfFont regularFont) throws IOException {
        // 🎨 FONDO DECORATIVO SUTIL para el encabezado
        Table backgroundTable = new Table(1)
                .setWidth(UnitValue.createPercentValue(100))
                .setBackgroundColor(new DeviceRgb(253, 251, 250)); // Fondo muy sutil terracota

        Cell backgroundCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(20);

        // Tabla para el encabezado con logo y datos de empresa
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{2, 3}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(Border.NO_BORDER);

        // Columna izquierda - Logo con marco sutil
        Cell logoCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(15)
                .setBackgroundColor(WHITE_COLOR)
                .setBorder(new SolidBorder(BORDER_COLOR, 1));

        try {
            // 🖼️ LOGO OFICIAL: Método correcto para cargar desde classpath
            logger.info("🔍 Intentando cargar logo desde recursos...");

            // Método 1: Usar ClassLoader con InputStream
            InputStream logoStream = getClass().getClassLoader().getResourceAsStream("static/images/Logo-Completo.png");

            if (logoStream == null) {
                // Método 2: Intentar sin la carpeta static
                logoStream = getClass().getClassLoader().getResourceAsStream("images/Logo-Completo.png");
            }

            if (logoStream == null) {
                // Método 3: Directamente desde resources
                logoStream = getClass().getClassLoader().getResourceAsStream("Logo-Completo.png");
            }

            if (logoStream != null) {
                ImageData logoData = ImageDataFactory.create(logoStream.readAllBytes());
                Image logo = new Image(logoData)
                        .setWidth(160)     // Ligeramente más grande
                        .setHeight(65)
                        .setHorizontalAlignment(HorizontalAlignment.CENTER);
                logoCell.add(logo);

                logoStream.close(); // Cerrar el stream
                logger.info("✅ Logo oficial cargado exitosamente desde classpath!");

            } else {
                logger.warn("⚠️ Logo no encontrado en ninguna ubicación del classpath");
                throw new RuntimeException("Logo no encontrado en classpath");
            }

        } catch (Exception e) {
            logger.error("❌ Error cargando logo: {}", e.getMessage());
            logger.warn("📁 Verificar que el logo esté en una de estas ubicaciones:");
            logger.warn("   - src/main/resources/static/images/Logo-Completo.png");
            logger.warn("   - src/main/resources/images/Logo-Completo.png");
            logger.warn("   - src/main/resources/Logo-Completo.png");

            // Fallback: Texto con estilo corporativo si no se encuentra el logo
            Paragraph logoFallback = new Paragraph("EL BUEN SABOR")
                    .setFont(boldFont)
                    .setFontSize(26)
                    .setFontColor(PRIMARY_COLOR)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold();

            logoCell.add(logoFallback);
        }

        // Columna derecha - Datos de empresa con mejor estilo
        Cell companyInfoCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(15)
                .setTextAlignment(TextAlignment.RIGHT);

        // Nombre empresa más destacado
        companyInfoCell.add(new Paragraph("EL BUEN SABOR S.A.")
                .setFont(boldFont)
                .setFontSize(18)
                .setFontColor(SECONDARY_COLOR)
                .setMarginBottom(8));

        // Línea decorativa
        Table lineTable = new Table(1)
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(8);
        lineTable.addCell(new Cell()
                .setBorder(Border.NO_BORDER)
                .setHeight(2)
                .setBackgroundColor(PRIMARY_COLOR));
        companyInfoCell.add(lineTable);

        // Datos empresa con mejor espaciado
        companyInfoCell.add(new Paragraph("CUIT: 30-12345678-9")
                .setFont(regularFont)
                .setFontSize(10)
                .setFontColor(TEXT_DARK)
                .setMarginBottom(3));

        companyInfoCell.add(new Paragraph("Av. San Martín 1234")
                .setFont(regularFont)
                .setFontSize(10)
                .setFontColor(TEXT_DARK)
                .setMarginBottom(3));

        companyInfoCell.add(new Paragraph("Mendoza, Argentina")
                .setFont(regularFont)
                .setFontSize(10)
                .setFontColor(TEXT_DARK)
                .setMarginBottom(3));

        companyInfoCell.add(new Paragraph("Tel: (261) 123-4567")
                .setFont(regularFont)
                .setFontSize(10)
                .setFontColor(TEXT_DARK));

        headerTable.addCell(logoCell);
        headerTable.addCell(companyInfoCell);

        backgroundCell.add(headerTable);
        backgroundTable.addCell(backgroundCell);
        document.add(backgroundTable);

        // Título principal mejorado con sombra sutil
        Table titleTable = new Table(1)
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(25)
                .setMarginBottom(20);

        Cell titleCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(PRIMARY_COLOR)
                .setPadding(15)
                .setTextAlignment(TextAlignment.CENTER);

        titleCell.add(new Paragraph("FACTURA")
                .setFont(boldFont)
                .setFontSize(28)
                .setFontColor(WHITE_COLOR));

        titleTable.addCell(titleCell);
        document.add(titleTable);
    }

    private void agregarInformacionFactura(Document document, FacturaResponseDTO factura, PdfFont boldFont, PdfFont regularFont) {
        // 📋 Sección con fondo sutil
        Table sectionTable = new Table(1)
                .setWidth(UnitValue.createPercentValue(100))
                .setBackgroundColor(LIGHT_GRAY)
                .setBorder(new SolidBorder(PRIMARY_COLOR, 2))
                .setMarginBottom(20);

        Cell sectionCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(20);

        // Encabezado de sección
        Paragraph header = new Paragraph("INFORMACIÓN DE LA FACTURA")
                .setFont(boldFont)
                .setFontSize(14)
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(15);
        sectionCell.add(header);

        // Tabla para información con mejor diseño
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(Border.NO_BORDER);

        // Columna izquierda con fondo blanco
        Cell leftCell = new Cell()
                .setBorder(new SolidBorder(BORDER_COLOR, 1))
                .setBackgroundColor(WHITE_COLOR)
                .setPadding(15);

        leftCell.add(crearCampoInfoMejorado("📄 Número de Factura:", factura.getNroComprobante(), boldFont, regularFont));
        leftCell.add(crearCampoInfoMejorado("📅 Fecha:", factura.getFechaFactura().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), boldFont, regularFont));

        // Columna derecha con fondo blanco
        Cell rightCell = new Cell()
                .setBorder(new SolidBorder(BORDER_COLOR, 1))
                .setBackgroundColor(WHITE_COLOR)
                .setPadding(15);

        rightCell.add(crearCampoInfoMejorado("🛍️ Pedido N°:", String.valueOf(factura.getPedidoId()), boldFont, regularFont));
        rightCell.add(crearCampoInfoMejorado("🚚 Tipo de Envío:", factura.getTipoEnvio(), boldFont, regularFont));

        infoTable.addCell(leftCell);
        infoTable.addCell(rightCell);

        sectionCell.add(infoTable);
        sectionTable.addCell(sectionCell);
        document.add(sectionTable);
    }

    private void agregarInformacionCliente(Document document, FacturaResponseDTO factura, PdfFont boldFont, PdfFont regularFont) {
        // Encabezado de sección mejorado
        Table headerTable = new Table(1)
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(15);

        Cell headerCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(SECONDARY_COLOR)
                .setPadding(12)
                .setTextAlignment(TextAlignment.LEFT);

        headerCell.add(new Paragraph("👤 DATOS DEL CLIENTE")
                .setFont(boldFont)
                .setFontSize(14)
                .setFontColor(WHITE_COLOR));

        headerTable.addCell(headerCell);
        document.add(headerTable);

        // Información del cliente en tabla con mejor diseño
        Table clienteTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .setWidth(UnitValue.createPercentValue(70))
                .setBorder(new SolidBorder(PRIMARY_COLOR, 1))
                .setBackgroundColor(WHITE_COLOR);

        // ✅ DATOS REALES DEL CLIENTE
        clienteTable.addCell(crearCeldaInfoMejorada("Cliente:", boldFont, true));
        clienteTable.addCell(crearCeldaInfoMejorada(factura.getNombreCliente() + " " + factura.getApellidoCliente(), regularFont, false));

        // Email del cliente
        if (factura.getEmailCliente() != null && !factura.getEmailCliente().isEmpty()) {
            clienteTable.addCell(crearCeldaInfoMejorada("Email:", boldFont, true));
            clienteTable.addCell(crearCeldaInfoMejorada(factura.getEmailCliente(), regularFont, false));
        }

        // Teléfono del cliente
        if (factura.getTelefonoCliente() != null && !factura.getTelefonoCliente().isEmpty()) {
            clienteTable.addCell(crearCeldaInfoMejorada("Teléfono:", boldFont, true));
            clienteTable.addCell(crearCeldaInfoMejorada(factura.getTelefonoCliente(), regularFont, false));
        }

        document.add(clienteTable);

        // ✅ DOMICILIO DE ENTREGA (solo si es DELIVERY y tiene domicilio)
        if ("DELIVERY".equals(factura.getTipoEnvio()) && factura.getDomicilioEntrega() != null) {
            // Separador
            document.add(new Paragraph(" ").setMarginTop(15));

            // Encabezado domicilio
            Table domHeaderTable = new Table(1)
                    .setWidth(UnitValue.createPercentValue(70))
                    .setMarginBottom(10);

            Cell domHeaderCell = new Cell()
                    .setBorder(Border.NO_BORDER)
                    .setBackgroundColor(PRIMARY_COLOR)
                    .setPadding(8)
                    .setTextAlignment(TextAlignment.LEFT);

            domHeaderCell.add(new Paragraph("🏠 DIRECCIÓN DE ENTREGA")
                    .setFont(boldFont)
                    .setFontSize(12)
                    .setFontColor(WHITE_COLOR));

            domHeaderTable.addCell(domHeaderCell);
            document.add(domHeaderTable);

            // Información del domicilio
            Table domicilioTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                    .setWidth(UnitValue.createPercentValue(70))
                    .setBorder(new SolidBorder(PRIMARY_COLOR, 1))
                    .setBackgroundColor(WHITE_COLOR);

            DomicilioResponseDTO domicilio = factura.getDomicilioEntrega();

            // Dirección completa
            String direccionCompleta = domicilio.getCalle() + " " + domicilio.getNumero();

            domicilioTable.addCell(crearCeldaInfoMejorada("Dirección:", boldFont, true));
            domicilioTable.addCell(crearCeldaInfoMejorada(direccionCompleta, regularFont, false));

            domicilioTable.addCell(crearCeldaInfoMejorada("Localidad:", boldFont, true));
            domicilioTable.addCell(crearCeldaInfoMejorada(domicilio.getLocalidad() + " - CP " + domicilio.getCp(), regularFont, false));

            document.add(domicilioTable);
        }
    }

    private void agregarDetallesPedido(Document document, FacturaResponseDTO factura, PdfFont boldFont, PdfFont regularFont) {
        // Encabezado de sección mejorado
        Table headerTable = new Table(1)
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(10)
                .setMarginBottom(15);

        Cell headerCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(SECONDARY_COLOR)
                .setPadding(12)
                .setTextAlignment(TextAlignment.CENTER);

        headerCell.add(new Paragraph("🍽️ DETALLES DEL PEDIDO")
                .setFont(boldFont)
                .setFontSize(16)
                .setFontColor(WHITE_COLOR));

        headerTable.addCell(headerCell);
        document.add(headerTable);

        // ✅ TABLA EXPANDIDA: Agregar columna para descuentos
        Table detallesTable = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1.2f, 1, 1.2f}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(new SolidBorder(PRIMARY_COLOR, 2));

        // ✅ ENCABEZADOS ACTUALIZADOS
        detallesTable.addHeaderCell(crearCeldaHeaderMejorada("PRODUCTO", boldFont));
        detallesTable.addHeaderCell(crearCeldaHeaderMejorada("CANT.", boldFont));
        detallesTable.addHeaderCell(crearCeldaHeaderMejorada("PRECIO ORIG.", boldFont));
        detallesTable.addHeaderCell(crearCeldaHeaderMejorada("DESCUENTO", boldFont));
        detallesTable.addHeaderCell(crearCeldaHeaderMejorada("SUBTOTAL", boldFont));

        // ✅ DATOS REALES CON PROMOCIONES DETALLADAS
        if (factura.getDetallesPedido() != null && !factura.getDetallesPedido().isEmpty()) {
            logger.info("📦 Agregando {} productos con promociones al PDF", factura.getDetallesPedido().size());

            boolean isEvenRow = false;

            for (DetallePedidoResponseDTO detalle : factura.getDetallesPedido()) {
                Color rowColor = isEvenRow ? LIGHT_GRAY : WHITE_COLOR;

                // 1. NOMBRE DEL PRODUCTO (con observaciones y promoción)
                String nombreProducto = detalle.getDenominacionArticulo();

                // Agregar indicador de promoción si tiene
                if (detalle.getTienePromocion() != null && detalle.getTienePromocion() && detalle.getPromocionAplicada() != null) {
                    nombreProducto += "\n🎁 " + detalle.getPromocionAplicada().getDenominacion();
                }

                // Agregar observaciones si las tiene
                if (detalle.getObservaciones() != null && !detalle.getObservaciones().trim().isEmpty()) {
                    nombreProducto += "\n💬 " + detalle.getObservaciones();
                }

                detallesTable.addCell(crearCeldaDetalleMejorada(nombreProducto, regularFont, TextAlignment.LEFT, rowColor));

                // 2. CANTIDAD
                detallesTable.addCell(crearCeldaDetalleMejorada(String.valueOf(detalle.getCantidad()), boldFont, TextAlignment.CENTER, rowColor));

                // 3. ✅ PRECIO ORIGINAL (antes de descuento)
                Double precioOriginal = detalle.getPrecioUnitarioOriginal();
                if (precioOriginal == null) {
                    precioOriginal = detalle.getPrecioUnitario(); // Fallback
                }

                String precioOriginalTexto = CURRENCY_FORMAT.format(precioOriginal);

                // Si tiene promoción, mostrar precio tachado
                if (detalle.getTienePromocion() != null && detalle.getTienePromocion() &&
                        detalle.getDescuentoPromocion() != null && detalle.getDescuentoPromocion() > 0) {

                    // Crear párrafo con texto tachado (simulado con "~~")
                    precioOriginalTexto = "~~" + precioOriginalTexto + "~~";
                }

                detallesTable.addCell(crearCeldaDetalleMejorada(precioOriginalTexto, regularFont, TextAlignment.RIGHT, rowColor));

                // 4. ✅ DESCUENTO APLICADO
                String descuentoTexto = "-";
                if (detalle.getDescuentoPromocion() != null && detalle.getDescuentoPromocion() > 0) {
                    descuentoTexto = "-" + CURRENCY_FORMAT.format(detalle.getDescuentoPromocion());
                }

                Cell descuentoCell = crearCeldaDetalleMejorada(descuentoTexto, regularFont, TextAlignment.RIGHT, rowColor);
                if (!descuentoTexto.equals("-")) {
                    descuentoCell.setFontColor(new DeviceRgb(220, 53, 69)); // Color rojo para descuentos
                }
                detallesTable.addCell(descuentoCell);

                // 5. ✅ SUBTOTAL FINAL (precio con descuento aplicado)
                Double subtotalFinal = detalle.getSubtotal();
                String subtotalTexto = CURRENCY_FORMAT.format(subtotalFinal);

                Cell subtotalCell = crearCeldaDetalleMejorada(subtotalTexto, boldFont, TextAlignment.RIGHT, rowColor);

                // Si tiene descuento, resaltar el precio final en verde
                if (detalle.getTienePromocion() != null && detalle.getTienePromocion()) {
                    subtotalCell.setFontColor(new DeviceRgb(40, 167, 69)); // Verde para precio con descuento
                }

                detallesTable.addCell(subtotalCell);

                isEvenRow = !isEvenRow;

                // ✅ LOG DETALLADO
                logger.debug("✅ Producto: {} x{} - Original: {} | Descuento: {} | Final: {}",
                        detalle.getDenominacionArticulo(),
                        detalle.getCantidad(),
                        CURRENCY_FORMAT.format(precioOriginal),
                        descuentoTexto,
                        CURRENCY_FORMAT.format(subtotalFinal));
            }
        } else {
            logger.warn("⚠️ No se encontraron detalles del pedido para mostrar en el PDF");

            // Fila de fallback si no hay detalles
            for (int i = 0; i < 5; i++) {
                detallesTable.addCell(crearCeldaDetalleMejorada("Sin datos", regularFont, TextAlignment.CENTER, WHITE_COLOR));
            }
        }

        document.add(detallesTable);

        // ✅ RESUMEN DE PROMOCIONES (si hay)
        long productosConPromocion = factura.getDetallesPedido().stream()
                .mapToLong(d -> (d.getTienePromocion() != null && d.getTienePromocion()) ? 1 : 0)
                .sum();

        if (productosConPromocion > 0) {
            double totalDescuentos = factura.getDetallesPedido().stream()
                    .mapToDouble(d -> d.getDescuentoPromocion() != null ? d.getDescuentoPromocion() : 0.0)
                    .sum();

            Table promoTable = new Table(1)
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(15);

            Cell promoCell = new Cell()
                    .setBorder(new SolidBorder(new DeviceRgb(220, 53, 69), 2))
                    .setBackgroundColor(new DeviceRgb(255, 243, 245))
                    .setPadding(15);

            promoCell.add(new Paragraph("🎁 PROMOCIONES APLICADAS")
                    .setFont(boldFont)
                    .setFontSize(12)
                    .setFontColor(new DeviceRgb(220, 53, 69))
                    .setMarginBottom(8));

            promoCell.add(new Paragraph(String.format("%d producto(s) con promoción - Ahorro total: %s",
                    productosConPromocion, CURRENCY_FORMAT.format(totalDescuentos)))
                    .setFont(regularFont)
                    .setFontSize(11)
                    .setFontColor(TEXT_DARK));

            promoTable.addCell(promoCell);
            document.add(promoTable);
        }

        // ✅ OBSERVACIONES DEL PEDIDO (código existente sin cambios)
        if (factura.getObservacionesPedido() != null && !factura.getObservacionesPedido().trim().isEmpty()) {
            Table obsTable = new Table(1)
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(15);

            Cell obsCell = new Cell()
                    .setBorder(new SolidBorder(PRIMARY_COLOR, 2))
                    .setBackgroundColor(new DeviceRgb(255, 249, 246))
                    .setPadding(15);

            obsCell.add(new Paragraph("💬 OBSERVACIONES DEL PEDIDO")
                    .setFont(boldFont)
                    .setFontSize(12)
                    .setFontColor(PRIMARY_COLOR)
                    .setMarginBottom(8));

            obsCell.add(new Paragraph(factura.getObservacionesPedido())
                    .setFont(regularFont)
                    .setFontSize(11)
                    .setFontColor(TEXT_DARK));

            obsTable.addCell(obsCell);
            document.add(obsTable);
        }

        logger.info("📋 Detalles del pedido con promociones agregados exitosamente al PDF");
    }

    private void agregarTotales(Document document, FacturaResponseDTO factura, PdfFont boldFont, PdfFont regularFont) {
        // ✅ USAR DIRECTAMENTE LOS DATOS DEL BACKEND (ya calculados correctamente)
        double subtotal = factura.getSubTotal();
        double descuento = factura.getDescuento() != null ? factura.getDescuento() : 0.0;
        double gastosEnvio = factura.getGastosEnvio() != null ? factura.getGastosEnvio() : 0.0;
        double totalFinal = factura.getTotalVenta();

        logger.info("💰 TOTALES EN PDF:");
        logger.info("   Subtotal: {}", subtotal);
        logger.info("   Descuento: {}", descuento);
        logger.info("   Gastos Envío: {}", gastosEnvio);
        logger.info("   Total: {}", totalFinal);

        // Contenedor para totales con sombra simulada
        Table containerTable = new Table(1)
                .setWidth(UnitValue.createPercentValue(60))
                .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                .setMarginTop(25)
                .setBorder(new SolidBorder(PRIMARY_COLOR, 2));

        Cell containerCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(WHITE_COLOR)
                .setPadding(20);

        // Encabezado de totales
        containerCell.add(new Paragraph("💰 RESUMEN DE TOTALES")
                .setFont(boldFont)
                .setFontSize(14)
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15));

        // Tabla de totales
        Table totalesTable = new Table(UnitValue.createPercentArray(new float[]{2, 1}))
                .setWidth(UnitValue.createPercentValue(100));

        // Subtotal
        totalesTable.addCell(crearCeldaTotalesMejorada("Subtotal:", regularFont, false, false));
        totalesTable.addCell(crearCeldaTotalesMejorada(CURRENCY_FORMAT.format(subtotal), regularFont, false, true));

        // Descuento (mostrar si existe)
        if (descuento > 0) {
            String labelDescuento = "TAKE_AWAY".equals(factura.getTipoEnvio()) ?
                    "Descuento TAKE_AWAY (10%):" : "Descuento:";
            totalesTable.addCell(crearCeldaTotalesMejorada(labelDescuento, regularFont, false, false));
            totalesTable.addCell(crearCeldaTotalesMejorada("-" + CURRENCY_FORMAT.format(descuento), regularFont, false, true));
        }

        // Gastos de envío (si existen)
        if (gastosEnvio > 0) {
            totalesTable.addCell(crearCeldaTotalesMejorada("Gastos de Envío:", regularFont, false, false));
            totalesTable.addCell(crearCeldaTotalesMejorada(CURRENCY_FORMAT.format(gastosEnvio), regularFont, false, true));
        }

        // Línea separadora
        Cell separatorCell = new Cell(1, 2)
                .setBorder(Border.NO_BORDER)
                .setHeight(2)
                .setBackgroundColor(PRIMARY_COLOR)
                .setMarginTop(10)
                .setMarginBottom(10);
        totalesTable.addCell(separatorCell);

        // Total final destacado
        totalesTable.addCell(crearCeldaTotalesMejorada("TOTAL:", boldFont, true, false));
        totalesTable.addCell(crearCeldaTotalesMejorada(CURRENCY_FORMAT.format(totalFinal), boldFont, true, true));

        containerCell.add(totalesTable);
        containerTable.addCell(containerCell);
        document.add(containerTable);
    }

    private void agregarInformacionPagos(Document document, FacturaResponseDTO factura, PdfFont boldFont, PdfFont regularFont) {
        if (factura.getPagos() == null || factura.getPagos().isEmpty()) {
            return;
        }

        // Encabezado de sección mejorado
        Table headerTable = new Table(1)
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(30)
                .setMarginBottom(15);

        Cell headerCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(SECONDARY_COLOR)
                .setPadding(12)
                .setTextAlignment(TextAlignment.CENTER);

        headerCell.add(new Paragraph("💳 INFORMACIÓN DE PAGOS")
                .setFont(boldFont)
                .setFontSize(14)
                .setFontColor(WHITE_COLOR));

        headerTable.addCell(headerCell);
        document.add(headerTable);

        // Tabla de pagos con mejor diseño
        Table pagosTable = new Table(UnitValue.createPercentArray(new float[]{2, 2, 1, 2}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(new SolidBorder(PRIMARY_COLOR, 1));

        pagosTable.addHeaderCell(crearCeldaHeaderMejorada("FORMA DE PAGO", boldFont));
        pagosTable.addHeaderCell(crearCeldaHeaderMejorada("ESTADO", boldFont));
        pagosTable.addHeaderCell(crearCeldaHeaderMejorada("MONTO", boldFont));
        pagosTable.addHeaderCell(crearCeldaHeaderMejorada("FECHA", boldFont));

        boolean isEvenRow = false;
        for (PagoSummaryDTO pago : factura.getPagos()) {
            Color rowColor = isEvenRow ? LIGHT_GRAY : WHITE_COLOR;

            pagosTable.addCell(crearCeldaDetalleMejorada(pago.getFormaPago(), regularFont, TextAlignment.LEFT, rowColor));
            pagosTable.addCell(crearCeldaDetalleMejorada(pago.getEstado(), regularFont, TextAlignment.LEFT, rowColor));
            pagosTable.addCell(crearCeldaDetalleMejorada(CURRENCY_FORMAT.format(pago.getMonto()), regularFont, TextAlignment.RIGHT, rowColor));
            pagosTable.addCell(crearCeldaDetalleMejorada(pago.getFechaCreacion(), regularFont, TextAlignment.LEFT, rowColor));

            isEvenRow = !isEvenRow;
        }

        document.add(pagosTable);

        // Estado del pago con mejor diseño
        Table estadoTable = new Table(1)
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(15);

        String estadoPago = factura.getCompletamentePagada() ? "✅ PAGO COMPLETO" : "⚠️ PAGO PENDIENTE";
        Color colorEstado = factura.getCompletamentePagada() ? new DeviceRgb(46, 125, 50) : ACCENT_COLOR;
        Color fondoEstado = factura.getCompletamentePagada() ? new DeviceRgb(232, 245, 233) : new DeviceRgb(255, 243, 224);

        Cell estadoCell = new Cell()
                .setBorder(new SolidBorder(colorEstado, 2))
                .setBackgroundColor(fondoEstado)
                .setPadding(12)
                .setTextAlignment(TextAlignment.CENTER);

        estadoCell.add(new Paragraph(estadoPago)
                .setFont(boldFont)
                .setFontSize(14)
                .setFontColor(colorEstado));

        estadoTable.addCell(estadoCell);
        document.add(estadoTable);
    }

    private void agregarPiePagina(Document document, PdfFont regularFont) {
        // Separador elegante
        Table separatorTable = new Table(1)
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(40)
                .setMarginBottom(20);

        separatorTable.addCell(new Cell()
                .setBorder(Border.NO_BORDER)
                .setHeight(2)
                .setBackgroundColor(PRIMARY_COLOR));

        document.add(separatorTable);

        // Mensaje de agradecimiento
        Paragraph piePagina = new Paragraph("Gracias por su compra - El Buen Sabor")
                .setFont(regularFont)
                .setFontSize(12)
                .setFontColor(TEXT_DARK)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(8);
        document.add(piePagina);

        // Información de contacto
        Paragraph contacto = new Paragraph("www.elbuensabor.com | info@elbuensabor.com | (261) 123-4567")
                .setFont(regularFont)
                .setFontSize(9)
                .setFontColor(TEXT_MUTED)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(contacto);
    }

    // ==================== MÉTODOS AUXILIARES MEJORADOS ====================

    private void agregarSeparador(Document document) {
        document.add(new Paragraph("\n").setMarginTop(5).setMarginBottom(5));
    }

    private Paragraph crearCampoInfoMejorado(String etiqueta, String valor, PdfFont boldFont, PdfFont regularFont) {
        return new Paragraph()
                .add(new Text(etiqueta).setFont(boldFont).setFontSize(11).setFontColor(PRIMARY_COLOR))
                .add(new Text(" " + valor).setFont(regularFont).setFontSize(11).setFontColor(TEXT_DARK))
                .setMarginBottom(8);
    }

    private Cell crearCeldaInfoMejorada(String texto, PdfFont font, boolean esEtiqueta) {
        Cell cell = new Cell()
                .add(new Paragraph(texto).setFont(font).setFontSize(10))
                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f))
                .setPadding(8);

        if (esEtiqueta) {
            cell.setBackgroundColor(LIGHT_GRAY)
                    .setFontColor(PRIMARY_COLOR);
        } else {
            cell.setBackgroundColor(WHITE_COLOR)
                    .setFontColor(TEXT_DARK);
        }

        return cell;
    }

    private Cell crearCeldaHeaderMejorada(String texto, PdfFont boldFont) {
        return new Cell()
                .add(new Paragraph(texto).setFont(boldFont).setFontSize(11).setFontColor(WHITE_COLOR))
                .setBackgroundColor(PRIMARY_COLOR)
                .setPadding(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(new SolidBorder(SECONDARY_COLOR, 1));
    }

    private Cell crearCeldaDetalleMejorada(String texto, PdfFont font, TextAlignment alignment, Color backgroundColor) {
        return new Cell()
                .add(new Paragraph(texto).setFont(font).setFontSize(10).setFontColor(TEXT_DARK))
                .setPadding(10)
                .setTextAlignment(alignment)
                .setBackgroundColor(backgroundColor)
                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f));
    }

    private Cell crearCeldaTotalesMejorada(String texto, PdfFont font, boolean esTotal, boolean esValor) {
        Cell cell = new Cell()
                .add(new Paragraph(texto).setFont(font).setFontSize(esTotal ? 14 : 11))
                .setBorder(Border.NO_BORDER)
                .setPadding(8)
                .setTextAlignment(esValor ? TextAlignment.RIGHT : TextAlignment.LEFT);

        if (esTotal) {
            cell.setBackgroundColor(PRIMARY_COLOR)
                    .setFontColor(WHITE_COLOR);
        } else {
            cell.setFontColor(TEXT_DARK);
        }

        return cell;
    }
}