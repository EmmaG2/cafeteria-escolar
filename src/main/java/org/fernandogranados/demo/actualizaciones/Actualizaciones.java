package org.fernandogranados.demo.actualizaciones;

import org.fernandogranados.demo.modelo.Categoria;
import org.fernandogranados.demo.modelo.Cliente;
import org.fernandogranados.demo.modelo.Empleado;
import org.fernandogranados.demo.modelo.Producto;
import org.fernandogranados.demo.utilidad.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Módulo de actualizaciones: reúne todas las sentencias {@code UPDATE} del sistema,
 * incluida la cancelación de ventas.
 */
public class Actualizaciones {
    public void actualizarCategoria(Categoria categoria) throws SQLException {
        String sql = "UPDATE Categorias SET nombre = ?, descripcion = ? WHERE id_categoria = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {

            consulta.setString(1, categoria.getNombre());
            consulta.setString(2, categoria.getDescripcion());
            consulta.setLong(3, categoria.getIdCategoria());
            consulta.executeUpdate();
        }
    }

    public void actualizarProducto(Producto producto) throws SQLException {
        String sql = "UPDATE Productos SET id_categoria = ?, nombre = ?, descripcion = ?, precio = ?, disponible = ? "
                + "WHERE id_producto = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {

            consulta.setLong(1, producto.getIdCategoria());
            consulta.setString(2, producto.getNombre());
            consulta.setString(3, producto.getDescripcion());
            consulta.setBigDecimal(4, producto.getPrecio());
            consulta.setBoolean(5, producto.isDisponible());
            consulta.setLong(6, producto.getIdProducto());
            consulta.executeUpdate();
        }
    }

    public void actualizarCliente(Cliente cliente) throws SQLException {
        String sql = "UPDATE Clientes SET nombre = ?, telefono = ?, correo = ? WHERE id_cliente = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {

            consulta.setString(1, cliente.getNombre());
            consulta.setString(2, cliente.getTelefono());
            consulta.setString(3, cliente.getCorreo());
            consulta.setLong(4, cliente.getIdCliente());
            consulta.executeUpdate();
        }
    }

    public void actualizarEmpleado(Empleado empleado) throws SQLException {
        String sql = "UPDATE Empleados SET nombre = ?, puesto = ?, activo = ? WHERE id_empleado = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {

            consulta.setString(1, empleado.getNombre());
            consulta.setString(2, empleado.getPuesto());
            consulta.setBoolean(3, empleado.isActivo());
            consulta.setLong(4, empleado.getIdEmpleado());
            consulta.executeUpdate();
        }
    }

    /**
     * <h1>Cancelar venta</h1>
     * Cancela una venta cambiando su estado, sin borrar el registro ni sus detalles.
     *
     * <p>La condición {@code estado = 'PAGADA'} hace que una venta ya cancelada no se modifique
     * de nuevo. Como es una sola sentencia {@code UPDATE}, MySQL la ejecuta de forma atómica y no
     * necesita una transacción explícita.</p>
     *
     * @param idVenta Identificador de la venta a cancelar.
     * @return {@code true} si la venta estaba pagada y se canceló; {@code false} si no existe o ya estaba cancelada.
     * @throws SQLException si la actualización falla.
     */
    public boolean cancelarVenta(long idVenta) throws SQLException {
        String sql = "UPDATE Ventas SET estado = 'CANCELADA' WHERE id_venta = ? AND estado = 'PAGADA'";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {

            consulta.setLong(1, idVenta);
            return consulta.executeUpdate() == 1;
        }
    }
}
