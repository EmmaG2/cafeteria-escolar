package org.fernandogranados.demo.modelo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Venta {
    private long idVenta;
    private Long idCliente;
    private String nombreCliente;
    private long idEmpleado;
    private String nombreEmpleado;
    private LocalDateTime fechaVenta;
    private String metodoPago;
    private String estado;
    private BigDecimal total;
}
