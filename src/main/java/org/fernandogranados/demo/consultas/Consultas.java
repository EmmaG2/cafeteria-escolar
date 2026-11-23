package org.fernandogranados.demo.consultas;

import org.fernandogranados.demo.modelo.Categoria;
import org.fernandogranados.demo.modelo.Cliente;
import org.fernandogranados.demo.modelo.DetalleVenta;
import org.fernandogranados.demo.modelo.Empleado;
import org.fernandogranados.demo.modelo.Producto;
import org.fernandogranados.demo.modelo.Venta;
import org.fernandogranados.demo.reporteador.DatoReporte;
import org.fernandogranados.demo.reporteador.ResumenReporte;
import org.fernandogranados.demo.utilidad.ConexionBD;
import org.fernandogranados.demo.utilidad.FormatoPesos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Módulo de consultas: reúne todas las sentencias {@code SELECT} del sistema.
 *
 * <p>Incluye los listados de cada pantalla, dos búsquedas que se ejecutan dentro de la
 * transacción de ventas y las consultas de agregación de los reportes. Los reportes solo
 * consideran ventas con estado {@code PAGADA} y calculan los importes como
 * {@code SUM(cantidad * precio_unitario)}, porque la tabla {@code Ventas} no guarda un total.</p>
 */
public class Consultas {
    public List<Categoria> listarCategorias() throws SQLException {
        String sql = "SELECT id_categoria, nombre, descripcion FROM Categorias ORDER BY nombre";
        List<Categoria> categorias = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {

            while (resultado.next()) {
                Categoria categoria = new Categoria();
                categoria.setIdCategoria(resultado.getLong("id_categoria"));
                categoria.setNombre(resultado.getString("nombre"));
                categoria.setDescripcion(resultado.getString("descripcion"));
                categorias.add(categoria);
            }
        }

        return categorias;
    }

    public List<Producto> listarProductos() throws SQLException {
        String sql = "SELECT p.id_producto, p.id_categoria, c.nombre AS nombre_categoria, "
                + "p.nombre, p.descripcion, p.precio, p.disponible "
                + "FROM Productos p INNER JOIN Categorias c ON p.id_categoria = c.id_categoria "
                + "ORDER BY c.nombre, p.nombre";
        return consultarProductos(sql);
    }

    public List<Producto> listarProductosDisponibles() throws SQLException {
        String sql = "SELECT p.id_producto, p.id_categoria, c.nombre AS nombre_categoria, "
                + "p.nombre, p.descripcion, p.precio, p.disponible "
                + "FROM Productos p INNER JOIN Categorias c ON p.id_categoria = c.id_categoria "
                + "WHERE p.disponible = TRUE ORDER BY c.nombre, p.nombre";
        return consultarProductos(sql);
    }

    /**
     * <h1>Buscar producto disponible</h1>
     * Busca un producto disponible usando la conexión de la transacción de ventas.
     *
     * @param conexion   Conexión de la transacción en curso; este método no la cierra.
     * @param idProducto Identificador del producto.
     * @return El producto con su precio actual, o {@code null} si no existe o no está disponible.
     * @throws SQLException si la consulta falla.
     */
    public Producto buscarProductoDisponible(Connection conexion, long idProducto) throws SQLException {
        String sql = "SELECT p.id_producto, p.id_categoria, c.nombre AS nombre_categoria, "
                + "p.nombre, p.descripcion, p.precio, p.disponible "
                + "FROM Productos p INNER JOIN Categorias c ON p.id_categoria = c.id_categoria "
                + "WHERE p.id_producto = ? AND p.disponible = TRUE";

        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setLong(1, idProducto);

            try (ResultSet resultado = consulta.executeQuery()) {
                if (resultado.next()) {
                    return mapearProducto(resultado);
                }
            }
        }

        return null;
    }

    public List<Cliente> listarClientes() throws SQLException {
        String sql = "SELECT id_cliente, nombre, telefono, correo FROM Clientes ORDER BY nombre";
        List<Cliente> clientes = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {

            while (resultado.next()) {
                Cliente cliente = new Cliente();
                cliente.setIdCliente(resultado.getLong("id_cliente"));
                cliente.setNombre(resultado.getString("nombre"));
                cliente.setTelefono(resultado.getString("telefono"));
                cliente.setCorreo(resultado.getString("correo"));
                clientes.add(cliente);
            }
        }

        return clientes;
    }

    public List<Empleado> listarEmpleados() throws SQLException {
        String sql = "SELECT id_empleado, nombre, puesto, activo FROM Empleados ORDER BY nombre";
        return consultarEmpleados(sql);
    }

    public List<Empleado> listarEmpleadosActivos() throws SQLException {
        String sql = "SELECT id_empleado, nombre, puesto, activo FROM Empleados WHERE activo = TRUE ORDER BY nombre";
        return consultarEmpleados(sql);
    }

    /**
     * <h1>Verificar empleado activo</h1>
     * Indica si un empleado existe y está activo, usando la conexión de la transacción de ventas.
     *
     * @param conexion   Conexión de la transacción en curso; este método no la cierra.
     * @param idEmpleado Identificador del empleado.
     * @return {@code true} solo si el empleado existe y está marcado como activo.
     * @throws SQLException si la consulta falla.
     */
    public boolean esEmpleadoActivo(Connection conexion, long idEmpleado) throws SQLException {
        String sql = "SELECT activo FROM Empleados WHERE id_empleado = ?";

        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setLong(1, idEmpleado);

            try (ResultSet resultado = consulta.executeQuery()) {
                return resultado.next() && resultado.getBoolean("activo");
            }
        }
    }

    /**
     * <h1>Listar ventas</h1>
     * Lista las ventas con su total, aplicando solo los filtros que se reciban.
     *
     * <p>La sentencia se arma agregando una condición por cada filtro presente. Los valores
     * siempre se envían como parámetros {@code ?}, nunca concatenados, y se lleva la cuenta de su
     * posición porque varía según los filtros usados. El {@code LEFT JOIN} con {@code Clientes}
     * conserva las ventas sin cliente.</p>
     *
     * @param fechaInicio Primer día del período, inclusive; {@code null} para no limitar.
     * @param fechaFin    Último día del período, inclusive; {@code null} para no limitar.
     * @param estado      {@code PAGADA} o {@code CANCELADA}; {@code null} para incluir ambos.
     * @return Ventas ordenadas de la más reciente a la más antigua.
     * @throws SQLException si la consulta falla.
     */
    public List<Venta> listarVentas(LocalDate fechaInicio, LocalDate fechaFin, String estado) throws SQLException {
        String sql = "SELECT v.id_venta, v.id_cliente, c.nombre AS nombre_cliente, v.id_empleado, "
                + "e.nombre AS nombre_empleado, v.fecha_venta, v.metodo_pago, v.estado, "
                + "SUM(d.cantidad * d.precio_unitario) AS total "
                + "FROM Ventas v "
                + "LEFT JOIN Clientes c ON v.id_cliente = c.id_cliente "
                + "INNER JOIN Empleados e ON v.id_empleado = e.id_empleado "
                + "INNER JOIN DetallesVenta d ON v.id_venta = d.id_venta "
                + "WHERE 1 = 1 ";

        if (fechaInicio != null) {
            sql += "AND v.fecha_venta >= ? ";
        }

        if (fechaFin != null) {
            sql += "AND v.fecha_venta < ? ";
        }

        if (estado != null) {
            sql += "AND v.estado = ? ";
        }

        sql += "GROUP BY v.id_venta, v.id_cliente, c.nombre, v.id_empleado, e.nombre, v.fecha_venta, "
                + "v.metodo_pago, v.estado ORDER BY v.fecha_venta DESC";

        List<Venta> ventas = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {

            int posicion = 1;
            if (fechaInicio != null) {
                consulta.setTimestamp(posicion, Timestamp.valueOf(fechaInicio.atStartOfDay()));
                posicion++;
            }

            if (fechaFin != null) {
                consulta.setTimestamp(posicion, Timestamp.valueOf(fechaFin.plusDays(1).atStartOfDay()));
                posicion++;
            }

            if (estado != null) {
                consulta.setString(posicion, estado);
            }

            try (ResultSet resultado = consulta.executeQuery()) {
                while (resultado.next()) {
                    Venta venta = new Venta();
                    venta.setIdVenta(resultado.getLong("id_venta"));
                    venta.setIdCliente(resultado.getObject("id_cliente", Long.class));
                    venta.setNombreCliente(resultado.getString("nombre_cliente"));
                    venta.setIdEmpleado(resultado.getLong("id_empleado"));
                    venta.setNombreEmpleado(resultado.getString("nombre_empleado"));
                    venta.setFechaVenta(resultado.getTimestamp("fecha_venta").toLocalDateTime());
                    venta.setMetodoPago(resultado.getString("metodo_pago"));
                    venta.setEstado(resultado.getString("estado"));
                    venta.setTotal(resultado.getBigDecimal("total"));
                    ventas.add(venta);
                }
            }
        }

        return ventas;
    }

    public List<DetalleVenta> listarDetallesVenta(long idVenta) throws SQLException {
        String sql = "SELECT d.id_detalle, d.id_venta, d.id_producto, p.nombre AS nombre_producto, "
                + "d.cantidad, d.precio_unitario "
                + "FROM DetallesVenta d INNER JOIN Productos p ON d.id_producto = p.id_producto "
                + "WHERE d.id_venta = ? ORDER BY p.nombre";
        List<DetalleVenta> detalles = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {

            consulta.setLong(1, idVenta);

            try (ResultSet resultado = consulta.executeQuery()) {
                while (resultado.next()) {
                    DetalleVenta detalle = new DetalleVenta();
                    detalle.setIdDetalle(resultado.getLong("id_detalle"));
                    detalle.setIdVenta(resultado.getLong("id_venta"));
                    detalle.setIdProducto(resultado.getLong("id_producto"));
                    detalle.setNombreProducto(resultado.getString("nombre_producto"));
                    detalle.setCantidad(resultado.getInt("cantidad"));
                    detalle.setPrecioUnitario(resultado.getBigDecimal("precio_unitario"));
                    detalles.add(detalle);
                }
            }
        }

        return detalles;
    }

    public List<DatoReporte> ingresosPorDia(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        String sql = "SELECT DATE(v.fecha_venta) AS etiqueta, SUM(d.cantidad * d.precio_unitario) AS valor "
                + "FROM Ventas v INNER JOIN DetallesVenta d ON v.id_venta = d.id_venta "
                + "WHERE v.estado = 'PAGADA' AND v.fecha_venta >= ? AND v.fecha_venta < ? "
                + "GROUP BY DATE(v.fecha_venta) ORDER BY DATE(v.fecha_venta)";
        return consultarDatosReporte(sql, fechaInicio, fechaFin, "Ingresos");
    }

    public List<DatoReporte> productosMasVendidos(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        String sql = "SELECT p.nombre AS etiqueta, SUM(d.cantidad) AS valor, "
                + "SUM(d.cantidad * d.precio_unitario) AS ingresos "
                + "FROM Ventas v INNER JOIN DetallesVenta d ON v.id_venta = d.id_venta "
                + "INNER JOIN Productos p ON d.id_producto = p.id_producto "
                + "WHERE v.estado = 'PAGADA' AND v.fecha_venta >= ? AND v.fecha_venta < ? "
                + "GROUP BY p.id_producto, p.nombre ORDER BY valor DESC, p.nombre";
        return consultarProductosVendidos(sql, fechaInicio, fechaFin);
    }

    /**
     * <h1>Productos menos vendidos</h1>
     * Lista los productos de menor a mayor venta en el período, incluidos los que no se vendieron.
     *
     * <p>Parte de {@code Productos} y une con {@code LEFT JOIN} una subconsulta de detalles de
     * ventas pagadas, de modo que un producto sin ventas aparezca con cero gracias a {@code COALESCE}.
     * El filtro de fechas va dentro de la subconsulta; si estuviera en el {@code WHERE} principal,
     * descartaría justamente esos productos.</p>
     *
     * @param fechaInicio Primer día del período, inclusive.
     * @param fechaFin    Último día del período, inclusive.
     * @return Renglones con unidades vendidas y, en el detalle, el ingreso obtenido.
     * @throws SQLException si la consulta falla.
     */
    public List<DatoReporte> productosMenosVendidos(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        String sql = "SELECT p.nombre AS etiqueta, COALESCE(SUM(d.cantidad), 0) AS valor, "
                + "COALESCE(SUM(d.cantidad * d.precio_unitario), 0) AS ingresos "
                + "FROM Productos p LEFT JOIN ("
                + "SELECT d.id_producto, d.cantidad, d.precio_unitario FROM DetallesVenta d "
                + "INNER JOIN Ventas v ON d.id_venta = v.id_venta "
                + "WHERE v.estado = 'PAGADA' AND v.fecha_venta >= ? AND v.fecha_venta < ?"
                + ") d ON p.id_producto = d.id_producto "
                + "GROUP BY p.id_producto, p.nombre ORDER BY valor, p.nombre";
        return consultarProductosVendidos(sql, fechaInicio, fechaFin);
    }

    public List<DatoReporte> ventasPorCategoria(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        String sql = "SELECT c.nombre AS etiqueta, SUM(d.cantidad * d.precio_unitario) AS valor "
                + "FROM Ventas v INNER JOIN DetallesVenta d ON v.id_venta = d.id_venta "
                + "INNER JOIN Productos p ON d.id_producto = p.id_producto "
                + "INNER JOIN Categorias c ON p.id_categoria = c.id_categoria "
                + "WHERE v.estado = 'PAGADA' AND v.fecha_venta >= ? AND v.fecha_venta < ? "
                + "GROUP BY c.id_categoria, c.nombre ORDER BY valor DESC";
        return consultarDatosReporte(sql, fechaInicio, fechaFin, "Ingresos");
    }

    public List<DatoReporte> ventasPorEmpleado(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        String sql = "SELECT e.nombre AS etiqueta, SUM(d.cantidad * d.precio_unitario) AS valor "
                + "FROM Ventas v INNER JOIN DetallesVenta d ON v.id_venta = d.id_venta "
                + "INNER JOIN Empleados e ON v.id_empleado = e.id_empleado "
                + "WHERE v.estado = 'PAGADA' AND v.fecha_venta >= ? AND v.fecha_venta < ? "
                + "GROUP BY e.id_empleado, e.nombre ORDER BY valor DESC";
        return consultarDatosReporte(sql, fechaInicio, fechaFin, "Ingresos");
    }

    public List<DatoReporte> ventasPorMetodoPago(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        String sql = "SELECT v.metodo_pago AS etiqueta, COUNT(v.id_venta) AS valor "
                + "FROM Ventas v WHERE v.estado = 'PAGADA' AND v.fecha_venta >= ? AND v.fecha_venta < ? "
                + "GROUP BY v.metodo_pago ORDER BY valor DESC";
        return consultarDatosReporte(sql, fechaInicio, fechaFin, "ventas");
    }

    /**
     * <h1>Obtener resumen</h1>
     * Calcula el número de ventas, los ingresos y el ticket promedio del período.
     *
     * <p>Primero obtiene el total de cada venta en una subconsulta y después aplica
     * {@code COUNT}, {@code SUM} y {@code AVG} sobre esos totales. Si se promediaran los
     * detalles directamente, el resultado sería el promedio por producto y no por venta.</p>
     *
     * @param fechaInicio Primer día del período, inclusive.
     * @param fechaFin    Último día del período, inclusive.
     * @return Indicadores del período; en cero si no hubo ventas pagadas.
     * @throws SQLException si la consulta falla.
     */
    public ResumenReporte obtenerResumen(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        String sql = "SELECT COUNT(*) AS numero_ventas, COALESCE(SUM(total), 0) AS ingresos, "
                + "COALESCE(AVG(total), 0) AS ticket_promedio FROM ("
                + "SELECT v.id_venta, SUM(d.cantidad * d.precio_unitario) AS total "
                + "FROM Ventas v INNER JOIN DetallesVenta d ON v.id_venta = d.id_venta "
                + "WHERE v.estado = 'PAGADA' AND v.fecha_venta >= ? AND v.fecha_venta < ? "
                + "GROUP BY v.id_venta) AS ventas_periodo";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {

            asignarPeriodo(consulta, fechaInicio, fechaFin);

            try (ResultSet resultado = consulta.executeQuery()) {
                if (resultado.next()) {
                    return new ResumenReporte(resultado.getLong("numero_ventas"), resultado.getBigDecimal("ingresos"),
                            resultado.getBigDecimal("ticket_promedio"));
                }
            }
        }

        return new ResumenReporte(0, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private List<Producto> consultarProductos(String sql) throws SQLException {
        List<Producto> productos = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {

            while (resultado.next()) {
                productos.add(mapearProducto(resultado));
            }
        }

        return productos;
    }

    private Producto mapearProducto(ResultSet resultado) throws SQLException {
        Producto producto = new Producto();
        producto.setIdProducto(resultado.getLong("id_producto"));
        producto.setIdCategoria(resultado.getLong("id_categoria"));
        producto.setNombreCategoria(resultado.getString("nombre_categoria"));
        producto.setNombre(resultado.getString("nombre"));
        producto.setDescripcion(resultado.getString("descripcion"));
        producto.setPrecio(resultado.getBigDecimal("precio"));
        producto.setDisponible(resultado.getBoolean("disponible"));
        return producto;
    }

    private List<Empleado> consultarEmpleados(String sql) throws SQLException {
        List<Empleado> empleados = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {

            while (resultado.next()) {
                Empleado empleado = new Empleado();
                empleado.setIdEmpleado(resultado.getLong("id_empleado"));
                empleado.setNombre(resultado.getString("nombre"));
                empleado.setPuesto(resultado.getString("puesto"));
                empleado.setActivo(resultado.getBoolean("activo"));
                empleados.add(empleado);
            }
        }

        return empleados;
    }

    private List<DatoReporte> consultarProductosVendidos(String sql, LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        List<DatoReporte> datos = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {

            asignarPeriodo(consulta, fechaInicio, fechaFin);

            try (ResultSet resultado = consulta.executeQuery()) {
                while (resultado.next()) {
                    BigDecimal ingresos = resultado.getBigDecimal("ingresos");
                    datos.add(new DatoReporte(resultado.getString("etiqueta"), resultado.getBigDecimal("valor"),
                            "unidades · " + FormatoPesos.mostrar(ingresos)));
                }
            }
        }

        return datos;
    }

    private List<DatoReporte> consultarDatosReporte(String sql, LocalDate fechaInicio, LocalDate fechaFin, String detalle) throws SQLException {
        List<DatoReporte> datos = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {

            asignarPeriodo(consulta, fechaInicio, fechaFin);

            try (ResultSet resultado = consulta.executeQuery()) {
                while (resultado.next()) {
                    datos.add(new DatoReporte(resultado.getString("etiqueta"), resultado.getBigDecimal("valor"), detalle));
                }
            }
        }

        return datos;
    }

    /**
     * <h1>Asignar período a la consulta</h1>
     * Asigna el período a los dos primeros parámetros de una consulta de reporte.
     *
     * <p>Como {@code fecha_venta} incluye la hora, el filtro se expresa como
     * {@code fecha_venta >= inicio AND fecha_venta < día siguiente al fin}. Así se incluyen todas
     * las ventas del último día, hasta las 23:59:59.</p>
     *
     * @param consulta    Sentencia con los dos primeros parámetros reservados para el período.
     * @param fechaInicio Primer día del período, inclusive.
     * @param fechaFin    Último día del período, inclusive.
     * @throws SQLException si no pueden asignarse los parámetros.
     */
    private void asignarPeriodo(PreparedStatement consulta, LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        consulta.setTimestamp(1, Timestamp.valueOf(fechaInicio.atStartOfDay()));
        consulta.setTimestamp(2, Timestamp.valueOf(fechaFin.plusDays(1).atStartOfDay()));
    }
}
