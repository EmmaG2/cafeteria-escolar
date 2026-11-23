package org.fernandogranados.demo.eliminaciones;

import org.fernandogranados.demo.utilidad.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Módulo de eliminaciones: reúne todas las sentencias {@code DELETE} del sistema.
 *
 * <p>Las ventas no se eliminan; se cancelan con {@code Actualizaciones.cancelarVenta}. Las
 * llaves foráneas de MySQL impiden borrar una categoría con productos, o un producto o empleado
 * que aparezca en ventas; en esos casos el método lanza {@link SQLException} y el controlador
 * muestra un mensaje al usuario.</p>
 */
public class Eliminaciones {
    public void eliminarCategoria(long idCategoria) throws SQLException {
        String sql = "DELETE FROM Categorias WHERE id_categoria = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {

            consulta.setLong(1, idCategoria);
            consulta.executeUpdate();
        }
    }

    public void eliminarProducto(long idProducto) throws SQLException {
        String sql = "DELETE FROM Productos WHERE id_producto = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {

            consulta.setLong(1, idProducto);
            consulta.executeUpdate();
        }
    }

    public void eliminarCliente(long idCliente) throws SQLException {
        String sql = "DELETE FROM Clientes WHERE id_cliente = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {

            consulta.setLong(1, idCliente);
            consulta.executeUpdate();
        }
    }

    public void eliminarEmpleado(long idEmpleado) throws SQLException {
        String sql = "DELETE FROM Empleados WHERE id_empleado = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {

            consulta.setLong(1, idEmpleado);
            consulta.executeUpdate();
        }
    }
}
