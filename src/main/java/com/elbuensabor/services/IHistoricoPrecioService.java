package com.elbuensabor.services;

import java.util.List;
import com.elbuensabor.dto.response.HistoricoPrecioDTO;
import com.elbuensabor.dto.response.HistoricoPrecioStats;
import com.elbuensabor.dto.response.PrecioVentaSugeridoDTO;

public interface IHistoricoPrecioService {

    // ✅ Obtener historial de precios de un artículo
    List<HistoricoPrecioDTO> getHistorialByArticulo(Long idArticulo);

    // ✅ Obtener últimos N precios
    List<HistoricoPrecioDTO> getLastNPrecios(Long idArticulo, int limit);

    // ✅ Registrar nuevo precio (se llama automáticamente cuando se compra)
    HistoricoPrecioDTO registrarPrecio(Long idArticulo, Double precioUnitario, Double cantidad);

    // ✅ Obtener estadísticas de precios
    HistoricoPrecioStats getEstadisticas(Long idArticulo);

    // ✅ Eliminar una compra individual
    void deleteById(Long id);

    // ✅ Calcula el precio sugerido de VENTA
    PrecioVentaSugeridoDTO calcularPrecioVentaSugerido(
            Long idArticulo,
            Double margenGanancia);
}
