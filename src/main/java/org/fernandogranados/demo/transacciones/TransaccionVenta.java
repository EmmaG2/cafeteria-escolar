package org.fernandogranados.demo.transacciones;

import org.fernandogranados.demo.consultas.Consultas;
import org.fernandogranados.demo.inserciones.Inserciones;
import org.fernandogranados.demo.modelo.DetalleVenta;
import org.fernandogranados.demo.modelo.Producto;
import org.fernandogranados.demo.modelo.Venta;
import org.fernandogranados.demo.utilidad.ConexionBD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Registra una venta y sus detalles como una sola transacción de base de datos.
 *
 * <p>Una venta ocupa un renglón en {@code Ventas} y uno en {@code DetallesVenta} por cada
 * producto. Si se guardaran por separado y fallara el tercer detalle, quedaría una venta
 * incompleta con un total incorrecto. La transacción garantiza atomicidad: se guardan todos los
 * renglones o ninguno.</p>
 *
 * <p>Para que esto funcione, todas las operaciones usan la <b>misma</b> {@link Connection}, que
 * se entrega como parámetro a {@link Consultas} e {@link Inserciones}. Una conexión distinta
 * quedaría fuera de la transacción.</p>
 */
public class TransaccionVenta {
    private static final Logger logger = LoggerFactory.getLogger(TransaccionVenta.class);

    private final Consultas consultas = new Consultas();
    private final Inserciones inserciones = new Inserciones();

    /**
     * <h1>Registrar venta</h1>
     * Valida y guarda una venta con todos sus productos dentro de una transacción.
     *
     * <p>Pasos:</p>
     * <ol>
     *   <li>Valida los datos y une los productos repetidos del carrito, porque la tabla
     *       {@code DetallesVenta} no admite el mismo producto dos veces en una venta.</li>
     *   <li>Desactiva el autocommit con {@code setAutoCommit(false)}. Desde ese momento, ningún
     *       cambio es definitivo hasta llamar a {@code commit()}.</li>
     *   <li>Comprueba que el empleado esté activo y toma el precio actual de cada producto
     *       disponible. Ese precio queda fijo en el detalle como precio histórico.</li>
     *   <li>Inserta la venta, obtiene su identificador generado e inserta cada detalle.</li>
     *   <li>Confirma todo con {@code commit()}. Ante cualquier error ejecuta {@code rollback()},
     *       que deshace lo insertado, y vuelve a lanzar la excepción.</li>
     *   <li>Restaura el autocommit original en el bloque {@code finally}.</li>
     * </ol>
     *
     * @param idEmpleado Identificador del empleado que atiende la venta.
     * @param idCliente  Identificador del cliente, o {@code null} si la venta no tiene cliente.
     * @param metodoPago {@code EFECTIVO}, {@code TARJETA} o {@code TRANSFERENCIA}.
     * @param detalles   Productos y cantidades del carrito; el precio se ignora y se toma de la base de datos.
     * @return El identificador (folio) de la venta registrada.
     * @throws IllegalArgumentException si los datos son inválidos, el empleado está inactivo o
     *                                  algún producto ya no está disponible.
     * @throws SQLException             si ocurre un error de base de datos; en ese caso la venta no se guarda.
     */
    public long registrarVenta(long idEmpleado, Long idCliente, String metodoPago, List<DetalleVenta> detalles) throws SQLException {
        validarDatos(idEmpleado, metodoPago, detalles);
        List<DetalleVenta> detallesUnidos = unirDetalles(detalles);

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            boolean autoCommitAnterior = conexion.getAutoCommit();

            try {
                conexion.setAutoCommit(false);

                if (!consultas.esEmpleadoActivo(conexion, idEmpleado)) {
                    throw new IllegalArgumentException("El empleado seleccionado no existe o está inactivo.");
                }

                actualizarPreciosDeDetalles(conexion, detallesUnidos);

                Venta venta = new Venta();
                venta.setIdCliente(idCliente);
                venta.setIdEmpleado(idEmpleado);
                venta.setMetodoPago(metodoPago);
                venta.setEstado("PAGADA");
                long idVenta = inserciones.insertarVenta(conexion, venta);

                for (DetalleVenta detalle : detallesUnidos) {
                    detalle.setIdVenta(idVenta);
                    inserciones.insertarDetalleVenta(conexion, detalle);
                }

                conexion.commit();
                logger.info("Venta {} registrada correctamente.", idVenta);
                return idVenta;
            } catch (SQLException | RuntimeException excepcion) {
                conexion.rollback();
                logger.error("No fue posible registrar la venta.", excepcion);
                throw excepcion;
            } finally {
                conexion.setAutoCommit(autoCommitAnterior);
            }
        }
    }

    /**
     * <h1>Validar datos de la venta</h1>
     * Revisa los datos de la venta antes de abrir la conexión, para no ocupar la base de datos
     * con una venta que de todos modos sería rechazada.
     *
     * @param idEmpleado Identificador del empleado que atiende.
     * @param metodoPago Método de pago elegido.
     * @param detalles   Productos del carrito.
     * @throws IllegalArgumentException si falta el empleado, el método de pago no es válido, el
     *                                  carrito está vacío o hay cantidades no positivas.
     */
    private void validarDatos(long idEmpleado, String metodoPago, List<DetalleVenta> detalles) {
        if (!"EFECTIVO".equals(metodoPago) && !"TARJETA".equals(metodoPago) && !"TRANSFERENCIA".equals(metodoPago)) {
            throw new IllegalArgumentException("Seleccione un método de pago válido.");
        }

        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("Agregue al menos un producto a la venta.");
        }

        for (DetalleVenta detalle : detalles) {
            if (detalle.getIdProducto() <= 0 || detalle.getCantidad() <= 0) {
                throw new IllegalArgumentException("Los productos y cantidades de la venta no son válidos.");
            }
        }
    }

    /**
     * <h1>Unir productos repetidos</h1>
     * Combina en un solo detalle los renglones que tienen el mismo producto, sumando sus cantidades.
     *
     * @param detalles Productos tal como vienen del carrito.
     * @return Nueva lista sin productos repetidos; la lista original no se modifica.
     */
    private List<DetalleVenta> unirDetalles(List<DetalleVenta> detalles) {
        List<DetalleVenta> detallesUnidos = new ArrayList<>();

        for (DetalleVenta detalle : detalles) {
            DetalleVenta detalleExistente = buscarDetalle(detallesUnidos, detalle.getIdProducto());

            if (detalleExistente == null) {
                DetalleVenta nuevoDetalle = new DetalleVenta();
                nuevoDetalle.setIdProducto(detalle.getIdProducto());
                nuevoDetalle.setCantidad(detalle.getCantidad());
                detallesUnidos.add(nuevoDetalle);
            } else {
                detalleExistente.setCantidad(detalleExistente.getCantidad() + detalle.getCantidad());
            }
        }

        return detallesUnidos;
    }

    /**
     * <h1>Asignar precios vigentes</h1>
     * Asigna a cada detalle el precio vigente del producto, consultado dentro de la transacción.
     *
     * <p>El precio se toma de la base de datos y no del carrito, para que no pueda registrarse un
     * precio desactualizado o alterado en la interfaz.</p>
     *
     * @param conexion Conexión de la transacción en curso.
     * @param detalles Detalles a los que se asignará el precio unitario.
     * @throws IllegalArgumentException si algún producto no existe o ya no está disponible.
     * @throws SQLException             si la consulta falla.
     */
    private void actualizarPreciosDeDetalles(Connection conexion, List<DetalleVenta> detalles) throws SQLException {
        for (DetalleVenta detalle : detalles) {
            Producto producto = consultas.buscarProductoDisponible(conexion, detalle.getIdProducto());
            if (producto == null) {
                throw new IllegalArgumentException("Uno de los productos no existe o no está disponible.");
            }

            detalle.setPrecioUnitario(producto.getPrecio());
        }
    }

    private DetalleVenta buscarDetalle(List<DetalleVenta> detalles, long idProducto) {
        for (DetalleVenta detalle : detalles) {
            if (detalle.getIdProducto() == idProducto) {
                return detalle;
            }
        }

        return null;
    }
}
