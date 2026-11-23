package org.fernandogranados.demo.modelo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {
    private long idCategoria;
    private String nombre;
    private String descripcion;

    @Override
    public String toString() {
        return nombre;
    }
}
