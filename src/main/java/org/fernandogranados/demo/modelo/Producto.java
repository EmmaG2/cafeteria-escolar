package org.fernandogranados.demo.modelo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class Producto {
    private long idProducto;
    private long idCategoria;
    private String nombreCategoria;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private boolean disponible;

    @Override
    public String toString() {
        return nombre + " - " + nombreCategoria;
    }
}
