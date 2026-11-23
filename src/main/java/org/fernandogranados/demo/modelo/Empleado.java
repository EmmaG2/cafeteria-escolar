package org.fernandogranados.demo.modelo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
public class Empleado {
    private long idEmpleado;
    private String nombre;
    private String puesto;
    private boolean activo;

    @Override
    public String toString() {
        return nombre + " - " + puesto;
    }
}
