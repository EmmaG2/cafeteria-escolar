package org.fernandogranados.demo.modelo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Cliente {
    private long idCliente;
    private String nombre;
    private String telefono;
    private String correo;

    @Override
    public String toString() {
        return nombre;
    }
}
