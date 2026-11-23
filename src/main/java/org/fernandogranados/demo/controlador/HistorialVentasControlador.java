package org.fernandogranados.demo.controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.fernandogranados.demo.actualizaciones.Actualizaciones;
import org.fernandogranados.demo.consultas.Consultas;
import org.fernandogranados.demo.modelo.DetalleVenta;
import org.fernandogranados.demo.modelo.Venta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Controlador de la vista {@code HistorialVentas.fxml}, que consulta y cancela ventas registradas.
 *
 * <p>La vista tiene dos listas que funcionan como maestro-detalle: al seleccionar una venta en
 * la lista de ventas, la segunda lista muestra sus productos. Los filtros usan
 * {@link DatePicker} (selector de fecha con calendario, que entrega un {@link LocalDate}) y un
 * {@link ComboBox} con el estado de la venta.</p>
 */
public class HistorialVentasControlador {
    private static Logger logger = LoggerFactory.getLogger(HistorialVentasControlador.class);

    @FXML
    private DatePicker selectorFechaInicio;

    @FXML
    private DatePicker selectorFechaFin;

    @FXML
    private ComboBox<String> comboEstado;

    @FXML
    private ListView<Venta> listaVentas;

    @FXML
    private ListView<DetalleVenta> listaDetalles;

    @FXML
    private Label etiquetaTotal;

    private final Actualizaciones actualizaciones = new Actualizaciones();
    private final Consultas consultas = new Consultas();
    private ObservableList<Venta> ventas = FXCollections.observableArrayList();
    private ObservableList<DetalleVenta> detalles = FXCollections.observableArrayList();
    private Venta ventaSeleccionada;

    /**
     * <h1>Inicializar vista</h1>
     * Configura las dos listas con sus celdas y enlaza la selección de una venta con la carga
     * de sus detalles. Al abrir la vista se muestran todas las ventas, sin filtros.
     */
    @FXML
    public void initialize() {
        listaVentas.setCellFactory(lista -> new Tarjetas.CeldaVenta());
        listaVentas.setItems(ventas);
        listaVentas.getSelectionModel().selectedItemProperty().addListener((observador, anterior, venta) -> mostrarDetalles(venta));

        listaDetalles.setCellFactory(lista -> new Tarjetas.CeldaDetalle());
        listaDetalles.setItems(detalles);

        comboEstado.setItems(FXCollections.observableArrayList("TODAS", "PAGADA", "CANCELADA"));
        comboEstado.setValue("TODAS");
        cargarVentas();
    }

    @FXML
    private void filtrarVentas() {
        LocalDate fechaInicio = selectorFechaInicio.getValue();
        LocalDate fechaFin = selectorFechaFin.getValue();

        if (fechaInicio != null && fechaFin != null && fechaInicio.isAfter(fechaFin)) {
            mostrarMensaje(Alert.AlertType.WARNING, "Fechas inválidas", "La fecha inicial no puede ser posterior a la fecha final.");
            return;
        }

        cargarVentas();
    }

    @FXML
    private void limpiarFiltros() {
        selectorFechaInicio.setValue(null);
        selectorFechaFin.setValue(null);
        comboEstado.setValue("TODAS");
        cargarVentas();
    }

    /**
     * <h1>Cancelar venta</h1>
     * Cancela la venta seleccionada después de pedir confirmación.
     *
     * <p>Las ventas nunca se borran: solo cambian su estado a {@code CANCELADA}, conservan sus
     * detalles y dejan de contar como ingreso en los reportes. Si otro usuario ya la había
     * cancelado, la actualización no modifica ningún registro y se informa con una advertencia.</p>
     */
    @FXML
    private void cancelarVenta() {
        if (ventaSeleccionada == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Seleccione una venta", "Seleccione la venta que desea cancelar.");
            return;
        }

        if ("CANCELADA".equals(ventaSeleccionada.getEstado())) {
            mostrarMensaje(Alert.AlertType.WARNING, "Venta cancelada", "La venta seleccionada ya está cancelada.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cancelar venta");
        confirmacion.setHeaderText("¿Desea cancelar la venta seleccionada?");
        confirmacion.setContentText("La venta conservará sus detalles y no se considerará como ingreso.");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            if (actualizaciones.cancelarVenta(ventaSeleccionada.getIdVenta())) {
                mostrarMensaje(Alert.AlertType.INFORMATION, "Venta cancelada", "La venta se canceló correctamente.");
                cargarVentas();
                limpiarDetalles();
            } else {
                mostrarMensaje(Alert.AlertType.WARNING, "No se pudo cancelar", "La venta ya estaba cancelada o no existe.");
            }
        } catch (SQLException excepcion) {
            logger.error("No fue posible cancelar la venta.", excepcion);
            mostrarMensaje(Alert.AlertType.ERROR, "Error al cancelar", "No fue posible cancelar la venta seleccionada.");
        }
    }

    /**
     * <h1>Cargar ventas</h1>
     * Consulta las ventas con los filtros actuales. Una fecha vacía o el estado {@code TODAS}
     * se envían como {@code null}, lo que indica a la consulta que no filtre por ese criterio.
     */
    private void cargarVentas() {
        try {
            String estado = "TODAS".equals(comboEstado.getValue()) ? null : comboEstado.getValue();
            ventas.setAll(consultas.listarVentas(selectorFechaInicio.getValue(), selectorFechaFin.getValue(), estado));
            ventaSeleccionada = null;
            listaVentas.getSelectionModel().clearSelection();
            limpiarDetalles();
        } catch (SQLException excepcion) {
            logger.error("No fue posible cargar el historial de ventas.", excepcion);
            mostrarMensaje(Alert.AlertType.ERROR, "Error de conexión", "No fue posible cargar el historial de ventas.");
        }
    }

    /**
     * <h1>Mostrar detalles</h1>
     * Carga los productos de la venta seleccionada en la lista de detalles.
     *
     * <p>El total mostrado es histórico: se calcula con el precio guardado en cada detalle al
     * momento de la venta, no con el precio actual del producto.</p>
     *
     * @param venta Venta seleccionada, o {@code null} si se quitó la selección.
     */
    private void mostrarDetalles(Venta venta) {
        ventaSeleccionada = venta;

        if (venta == null) {
            limpiarDetalles();
            return;
        }

        try {
            detalles.setAll(consultas.listarDetallesVenta(venta.getIdVenta()));
            etiquetaTotal.setText("Total histórico: $" + venta.getTotal());
        } catch (SQLException excepcion) {
            logger.error("No fue posible cargar los detalles de la venta.", excepcion);
            mostrarMensaje(Alert.AlertType.ERROR, "Error de conexión", "No fue posible cargar los detalles de la venta.");
        }
    }

    private void limpiarDetalles() {
        detalles.clear();
        etiquetaTotal.setText("Total histórico: $0");
    }

    private void mostrarMensaje(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}
