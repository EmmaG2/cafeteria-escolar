package org.fernandogranados.demo.inserciones;

import org.fernandogranados.demo.modelo.Categoria;
import org.fernandogranados.demo.modelo.Cliente;
import org.fernandogranados.demo.modelo.DetalleVenta;
import org.fernandogranados.demo.modelo.Empleado;
import org.fernandogranados.demo.modelo.Producto;
import org.fernandogranados.demo.modelo.Venta;
import org.fernandogranados.demo.utilidad.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * Módulo de inserciones: reúne todas las sentencias {@code INSERT} del sistema.
 *
 * <p>Los métodos de catálogos abren y cierran su propia conexión. Los de ventas reciben la
 * conexión como parámetro porque forman parte de la transacción de {@code TransaccionVenta}.
 * Todas las sentencias usan {@code PreparedStatement} con parámetros {@code ?}, lo que evita la
 * inyección de SQL.</p>
 */
public class Inserciones {
    public void insertarCategoria(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO Categorias (nombre, descripcion) VALUES (?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {

            consulta.setString(1, categoria.getNombre());
            consulta.setString(2, categoria.getDescripcion());
            consulta.executeUpdate();
        }
    }

    public void insertarProducto(Producto producto) throws SQLException {
        String sql = "INSERT INTO Productos (id_categoria, nombre, descripcion, precio, disponible) VALUES (?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {

            consulta.setLong(1, producto.getIdCategoria());
            consulta.setString(2, producto.getNombre());
            consulta.setString(3, producto.getDescripcion());
            consulta.setBigDecimal(4, producto.getPrecio());
            consulta.setBoolean(5, producto.isDisponible());
            consulta.executeUpdate();
        }
    }

    public void insertarCliente(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO Clientes (nombre, telefono, correo) VALUES (?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {

            consulta.setString(1, cliente.getNombre());
            consulta.setString(2, cliente.getTelefono());
            consulta.setString(3, cliente.getCorreo());
            consulta.executeUpdate();
        }
    }

    public void insertarEmpleado(Empleado empleado) throws SQLException {
        String sql = "INSERT INTO Empleados (nombre, puesto, activo) VALUES (?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {

            consulta.setString(1, empleado.getNombre());
            consulta.setString(2, empleado.getPuesto());
            consulta.setBoolean(3, empleado.isActivo());
            consulta.executeUpdate();
        }
    }

    /**
     * <h1>Insertar venta</h1>
     * Inserta el encabezado de una venta y devuelve el identificador que MySQL le asignó.
     *
     * <p>{@code Statement.RETURN_GENERATED_KEYS} pide al controlador JDBC el valor
     * {@code AUTO_INCREMENT} generado, que hace falta para relacionar los detalles con la venta.
     * La fecha no se envía porque la base de datos asigna la fecha y hora actuales.</p>
     *
     * @param conexion Conexión de la transacción en curso; este método no la cierra.
     * @param venta    Venta a registrar; {@code idCliente} puede ser {@code null}.
     * @return El identificador generado de la nueva venta.
     * @throws SQLException si la inserción falla o MySQL no devuelve el identificador.
     */
    public long insertarVenta(Connection conexion, Venta venta) throws SQLException {
        String sql = "INSERT INTO Ventas (id_cliente, id_empleado, metodo_pago, estado) VALUES (?, ?, ?, ?)";

        try (PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (venta.getIdCliente() == null) {
                consulta.setNull(1, Types.BIGINT);
            } else {
                consulta.setLong(1, venta.getIdCliente());
            }

            consulta.setLong(2, venta.getIdEmpleado());
            consulta.setString(3, venta.getMetodoPago());
            consulta.setString(4, venta.getEstado());
            consulta.executeUpdate();

            try (ResultSet claves = consulta.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getLong(1);
                }
            }
        }

        throw new SQLException("No fue posible obtener el identificador de la venta.");
    }

    /**
     * <h1>Insertar detalle venta</h1>
     * Inserta un producto de una venta con el precio unitario vigente en el momento de la compra.
     *
     * @param conexion Conexión de la transacción en curso; este método no la cierra.
     * @param detalle  Detalle con el identificador de venta, producto, cantidad y precio ya asignados.
     * @throws SQLException si la inserción falla, por ejemplo si el producto se repite en la misma venta.
     */
    public void insertarDetalleVenta(Connection conexion, DetalleVenta detalle) throws SQLException {
        String sql = "INSERT INTO DetallesVenta (id_venta, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";

        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setLong(1, detalle.getIdVenta());
            consulta.setLong(2, detalle.getIdProducto());
            consulta.setInt(3, detalle.getCantidad());
            consulta.setBigDecimal(4, detalle.getPrecioUnitario());
            consulta.executeUpdate();
        }
    }
}
