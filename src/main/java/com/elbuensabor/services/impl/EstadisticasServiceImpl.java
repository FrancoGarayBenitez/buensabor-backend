package com.elbuensabor.services.impl;

import com.elbuensabor.dto.response.MovimientosMonetariosDTO;
import com.elbuensabor.dto.response.RankingProductoDTO;
import com.elbuensabor.repository.IDetallePedidoRepository;
import com.elbuensabor.repository.IFacturaRepository;
import com.elbuensabor.repository.IPedidoRepository;
import com.elbuensabor.services.IEstadisticasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class EstadisticasServiceImpl implements IEstadisticasService {

    @Autowired
    private IFacturaRepository facturaRepository;

    @Autowired
    private IPedidoRepository pedidoRepository;

    @Autowired
    private IDetallePedidoRepository detallePedidoRepository; // <-- Inyectamos el nuevo repositorio

    @Override
    public MovimientosMonetariosDTO findMovimientosMonetarios(LocalDate fechaDesde, LocalDate fechaHasta) {
        LocalDateTime inicio = fechaDesde.atStartOfDay();
        LocalDateTime fin = fechaHasta.atTime(LocalTime.MAX);

        // Los ingresos se calculan desde las facturas
        Double ingresos = facturaRepository.getTotalVentasByPeriodo(fechaDesde, fechaHasta);
        if (ingresos == null) {
            ingresos = 0.0;
        }

        // Los costos vienen del campo 'totalCosto' de los pedidos no cancelados
        List<com.elbuensabor.entities.Pedido> pedidosEnRango = pedidoRepository.findByFechaBetween(inicio, fin);
        Double costos = pedidosEnRango.stream()
                .filter(pedido -> pedido.getEstado() != com.elbuensabor.entities.Estado.CANCELADO)
                .mapToDouble(com.elbuensabor.entities.Pedido::getTotalCosto)
                .sum();

        Double ganancias = ingresos - costos;

        return new MovimientosMonetariosDTO(ingresos, costos, ganancias);
    }

    @Override
    public List<RankingProductoDTO> findRankingProductos(LocalDate fechaDesde, LocalDate fechaHasta, Integer limit) {
        LocalDateTime inicio = fechaDesde.atStartOfDay();
        LocalDateTime fin = fechaHasta.atTime(LocalTime.MAX);

        // Creamos un objeto Pageable para limitar los resultados de la consulta
        Pageable pageable = PageRequest.of(0, limit);

        // Llamamos al método del repositorio que creaste
        return detallePedidoRepository.findRankingProductos(inicio, fin, pageable);
    }
}
