package org.fernandogranados.demo.reporteador;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Renglón genérico de un reporte: una etiqueta y su valor numérico.
 *
 * <p>Las anotaciones de Lombok generan en tiempo de compilación los métodos {@code get} de cada
 * atributo ({@code @Getter}) y un constructor con todos ellos ({@code @AllArgsConstructor}).</p>
 */
@Getter
@AllArgsConstructor
public class DatoReporte {
    /** Texto que identifica el renglón, por ejemplo una fecha, un producto o un método de pago. */
    private String etiqueta;
    /** Cantidad del renglón: un importe en pesos, unidades vendidas o número de ventas. */
    private BigDecimal valor;
    /** Descripción de la unidad del valor, por ejemplo {@code "Ingresos"} o {@code "unidades · $120.00"}. */
    private String detalle;
}
