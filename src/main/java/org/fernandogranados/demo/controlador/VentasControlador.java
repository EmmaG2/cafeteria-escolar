package org.fernandogranados.demo.controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.fernandogranados.demo.consultas.Consultas;
import org.fernandogranados.demo.modelo.Cliente;
import org.fernandogranados.demo.modelo.DetalleVenta;
import org.fernandogranados.demo.modelo.Empleado;
import org.fernandogranados.demo.modelo.Producto;
import org.fernandogranados.demo.transacciones.TransaccionVenta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Controlador de la vista {@code Ventas.fxml}, que funciona como punto de venta.
 *
 * <p>El usuario arma un carrito en memoria eligiendo productos y cantidades. Nada se guarda en
 * la base de datos hasta pulsar "Registrar venta"; en ese momento el carrito completo se envía a
 * {@link TransaccionVenta}, que guarda la venta y todos sus detalles en una sola transacción.</p>
 */
public class VentasControlador {
    private static Logger logger = LoggerFactory.getLogger(VentasControlador.class);

    @FXML
    private ComboBox<Producto> comboProducto;

    @FXML
    private TextField campoCantidad;

    @FXML
    private ListView<DetalleVenta> listaCarrito;

    @FXML
    private ComboBox<Empleado> comboEmpleado;

    @FXML
    private ComboBox<Cliente> comboCliente;

    @FXML
    private ComboBox<String> comboMetodoPago;

    @FXML
    private Label etiquetaTotal;

    private final Consultas consultas = new Consultas();
    private final TransaccionVenta transaccionVenta = new TransaccionVenta();
    /** Productos agregados a la venta en curso. Al ser observable, el carrito en pantalla se actualiza solo. */
    private ObservableList<DetalleVenta> carrito = FXCollections.observableArrayList();

    /**
     * <h1>Inicializar vista</h1>
     * Prepara la vista: enlaza el carrito a su lista, llena las listas desplegables y deja
     * {@code EFECTIVO} y cantidad 1 como valores iniciales.
     */
    @FXML
    public void initialize() {
        listaCarrito.setCellFactory(lista -> new Tarjetas.CeldaDetalle());
        listaCarrito.setItems(carrito);
        comboMetodoPago.setItems(FXCollections.observableArrayList("EFECTIVO", "TARJETA", "TRANSFERENCIA"));
        comboMetodoPago.setValue("EFECTIVO");
        campoCantidad.setText("1");
        actualizarTotal();
        cargarDatosVenta();
    }

    /**
     * <h1>Agregar producto</h1>
     * Agrega al carrito el producto elegido con la cantidad indicada.
     *
     * <p>Si el producto ya está en el carrito, se suma la cantidad en lugar de crear otro
     * renglón. Como en ese caso se modifica un objeto que ya está en la lista y no la lista
     * misma, JavaFX no detecta el cambio; por eso se llama a {@code listaCarrito.refresh()}
     * para que las celdas se vuelvan a dibujar.</p>
     */
    @FXML
    private void agregarProducto() {
        Producto producto = comboProducto.getValue();
        int cantidad = obtenerCantidad();

        if (producto == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Producto obligatorio", "Seleccione un producto disponible.");
            return;
        }

        if (cantidad <= 0) {
            mostrarMensaje(Alert.AlertType.WARNING, "Cantidad inválida", "Escriba una cantidad entera mayor que cero.");
            return;
        }

        DetalleVenta detalle = buscarDetalle(producto.getIdProducto());
        if (detalle == null) {
            detalle = new DetalleVenta();
            detalle.setIdProducto(producto.getIdProducto());
            detalle.setNombreProducto(producto.getNombre());
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(producto.getPrecio());
            carrito.add(detalle);
        } else {
            detalle.setCantidad(detalle.getCantidad() + cantidad);
            listaCarrito.refresh();
        }

        comboProducto.getSelectionModel().clearSelection();
        campoCantidad.setText("1");
        actualizarTotal();
    }

    @FXML
    private void quitarProducto() {
        DetalleVenta detalle = listaCarrito.getSelectionModel().getSelectedItem();

        if (detalle == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Seleccione un producto", "Seleccione el producto que desea quitar del carrito.");
            return;
        }

        carrito.remove(detalle);
        actualizarTotal();
    }

    /**
     * <h1>Registrar venta</h1>
     * Envía el carrito a {@link TransaccionVenta} para guardarlo como una venta.
     *
     * <p>El cliente es opcional, por eso su identificador se maneja como {@code Long}, que admite
     * {@code null}. Se distinguen dos tipos de error:</p>
     * <ul>
     *   <li>{@link IllegalArgumentException}: datos inválidos, como un carrito vacío o un
     *       producto que dejó de estar disponible. Su mensaje se muestra tal cual al usuario.</li>
     *   <li>{@link SQLException}: falla de base de datos. La transacción ya hizo
     *       {@code rollback}, así que no queda una venta a medias.</li>
     * </ul>
     */
    @FXML
    private void registrarVenta() {
        Empleado empleado = comboEmpleado.getValue();
        Cliente cliente = comboCliente.getValue();
        String metodoPago = comboMetodoPago.getValue();

        if (empleado == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Empleado obligatorio", "Seleccione el empleado que registra la venta.");
            return;
        }

        try {
            Long idCliente = cliente == null ? null : cliente.getIdCliente();
            long idVenta = transaccionVenta.registrarVenta(empleado.getIdEmpleado(), idCliente, metodoPago, carrito);
            mostrarMensaje(Alert.AlertType.INFORMATION, "Venta registrada", "La venta con folio " + idVenta + " se registró correctamente.");
            limpiarVenta();
            cargarProductosDisponibles();
        } catch (IllegalArgumentException excepcion) {
            mostrarMensaje(Alert.AlertType.WARNING, "No se pudo registrar", excepcion.getMessage());
        } catch (SQLException excepcion) {
            logger.error("No fue posible registrar la venta.", excepcion);
            mostrarMensaje(Alert.AlertType.ERROR, "Error al registrar", "No fue posible guardar la venta. Intente nuevamente.");
        }
    }

    @FXML
    private void limpiarVenta() {
        carrito.clear();
        comboProducto.getSelectionModel().clearSelection();
        comboEmpleado.getSelectionModel().clearSelection();
        comboCliente.getSelectionModel().clearSelection();
        comboMetodoPago.setValue("EFECTIVO");
        campoCantidad.setText("1");
        actualizarTotal();
    }

    private void cargarDatosVenta() {
        cargarProductosDisponibles();

        try {
            comboEmpleado.setItems(FXCollections.observableArrayList(consultas.listarEmpleadosActivos()));
            comboCliente.setItems(FXCollections.observableArrayList(consultas.listarClientes()));
        } catch (SQLException excepcion) {
            logger.error("No fue posible cargar los datos de la venta.", excepcion);
            mostrarMensaje(Alert.AlertType.ERROR, "Error de conexión", "No fue posible cargar empleados o clientes.");
        }
    }

    private void cargarProductosDisponibles() {
        try {
            comboProducto.setItems(FXCollections.observableArrayList(consultas.listarProductosDisponibles()));
        } catch (SQLException excepcion) {
            logger.error("No fue posible cargar los productos disponibles.", excepcion);
            mostrarMensaje(Alert.AlertType.ERROR, "Error de conexión", "No fue posible cargar los productos disponibles.");
        }
    }

    /**
     * <h1>Leer cantidad del formulario</h1>
     * Convierte el texto del campo de cantidad en un número entero.
     *
     * @return La cantidad escrita, o {@code 0} si el texto no es un entero válido, que después
     *         se rechaza como cantidad inválida.
     */
    private int obtenerCantidad() {
        try {
            return Integer.parseInt(campoCantidad.getText().trim());
        } catch (NumberFormatException excepcion) {
            return 0;
        }
    }

    private DetalleVenta buscarDetalle(long idProducto) {
        for (DetalleVenta detalle : carrito) {
            if (detalle.getIdProducto() == idProducto) {
                return detalle;
            }
        }

        return null;
    }

    /**
     * <h1>Actualizar total</h1>
     * Recalcula el total del carrito y lo muestra en pantalla. Es solo informativo: la base de
     * datos no guarda el total, sino que lo calcula a partir de los detalles de la venta.
     */
    private void actualizarTotal() {
        BigDecimal total = BigDecimal.ZERO;

        for (DetalleVenta detalle : carrito) {
            total = total.add(detalle.getImporte());
        }

        etiquetaTotal.setText("Total: $" + total);
    }

    private void mostrarMensaje(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}
