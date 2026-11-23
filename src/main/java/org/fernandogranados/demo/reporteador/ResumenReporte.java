package org.fernandogranados.demo.reporteador;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Indicadores generales de un período, calculados solo con ventas pagadas.
 * El ticket promedio es el importe medio por venta.
 */
@Getter
@AllArgsConstructor
public class ResumenReporte {
    private long numeroVentas;
    private BigDecimal ingresos;
    private BigDecimal ticketPromedio;
}
