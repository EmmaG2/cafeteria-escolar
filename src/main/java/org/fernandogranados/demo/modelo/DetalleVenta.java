package org.fernandogranados.demo.modelo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class DetalleVenta {
    private long idDetalle;
    private long idVenta;
    private long idProducto;
    private String nombreProducto;
    private int cantidad;
    private BigDecimal precioUnitario;

    public BigDecimal getImporte() {
        if (precioUnitario == null) {
            return BigDecimal.ZERO;
        }

        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
