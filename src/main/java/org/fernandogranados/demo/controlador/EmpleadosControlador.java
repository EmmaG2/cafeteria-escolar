package org.fernandogranados.demo.controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.fernandogranados.demo.actualizaciones.Actualizaciones;
import org.fernandogranados.demo.consultas.Consultas;
import org.fernandogranados.demo.eliminaciones.Eliminaciones;
import org.fernandogranados.demo.inserciones.Inserciones;
import org.fernandogranados.demo.modelo.Empleado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Controlador de la vista {@code Empleados.fxml}, que administra al personal de la cafetería.
 *
 * <p>Sigue el mismo patrón de lista y formulario descrito en {@link CategoriasControlador}.
 * El {@link CheckBox} de activo determina si el empleado aparece en la pantalla de ventas. Un
 * empleado con ventas registradas no puede eliminarse, porque MySQL protege el historial con
 * una llave foránea; en ese caso se recomienda marcarlo como inactivo.</p>
 */
public class EmpleadosControlador {
    private static Logger logger = LoggerFactory.getLogger(EmpleadosControlador.class);

    @FXML
    private ListView<Empleado> listaEmpleados;

    @FXML
    private TextField campoNombre;

    @FXML
    private TextField campoPuesto;

    @FXML
    private CheckBox casillaActivo;

    private final Actualizaciones actualizaciones = new Actualizaciones();
    private final Inserciones inserciones = new Inserciones();
    private final Eliminaciones eliminaciones = new Eliminaciones();
    private final Consultas consultas = new Consultas();
    private ObservableList<Empleado> empleados = FXCollections.observableArrayList();
    private Empleado empleadoSeleccionado;

    @FXML
    public void initialize() {
        listaEmpleados.setCellFactory(lista -> new Tarjetas.CeldaEmpleado());
        listaEmpleados.setItems(empleados);
        listaEmpleados.getSelectionModel().selectedItemProperty().addListener((observador, anterior, empleado) -> mostrarEmpleado(empleado));
        cargarEmpleados();
    }

    @FXML
    private void guardarEmpleado() {
        String nombre = campoNombre.getText().trim();
        String puesto = campoPuesto.getText().trim();

        if (nombre.isEmpty() || puesto.isEmpty()) {
            mostrarMensaje(Alert.AlertType.WARNING, "Campos obligatorios", "Escriba el nombre y puesto del empleado.");
            return;
        }

        try {
            if (empleadoSeleccionado == null) {
                Empleado empleado = new Empleado();
                empleado.setNombre(nombre);
                empleado.setPuesto(puesto);
                empleado.setActivo(casillaActivo.isSelected());
                inserciones.insertarEmpleado(empleado);
                mostrarMensaje(Alert.AlertType.INFORMATION, "Empleado registrado", "El empleado se registró correctamente.");
            } else {
                empleadoSeleccionado.setNombre(nombre);
                empleadoSeleccionado.setPuesto(puesto);
                empleadoSeleccionado.setActivo(casillaActivo.isSelected());
                actualizaciones.actualizarEmpleado(empleadoSeleccionado);
                mostrarMensaje(Alert.AlertType.INFORMATION, "Empleado actualizado", "El empleado se actualizó correctamente.");
            }

            cargarEmpleados();
            limpiarFormulario();
        } catch (SQLException excepcion) {
            logger.error("No fue posible guardar el empleado.", excepcion);
            mostrarMensaje(Alert.AlertType.ERROR, "No se pudo guardar", "No fue posible guardar los datos del empleado.");
        }
    }

    @FXML
    private void eliminarEmpleado() {
        if (empleadoSeleccionado == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Seleccione un empleado", "Seleccione el empleado que desea eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar empleado");
        confirmacion.setHeaderText("¿Desea eliminar el empleado seleccionado?");
        confirmacion.setContentText(empleadoSeleccionado.getNombre());

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            eliminaciones.eliminarEmpleado(empleadoSeleccionado.getIdEmpleado());
            cargarEmpleados();
            limpiarFormulario();
            mostrarMensaje(Alert.AlertType.INFORMATION, "Empleado eliminado", "El empleado se eliminó correctamente.");
        } catch (SQLException excepcion) {
            logger.error("No fue posible eliminar el empleado.", excepcion);
            mostrarMensaje(Alert.AlertType.ERROR, "No se pudo eliminar", "El empleado tiene ventas relacionadas. Puede marcarlo como inactivo.");
        }
    }

    @FXML
    private void limpiarFormulario() {
        empleadoSeleccionado = null;
        listaEmpleados.getSelectionModel().clearSelection();
        campoNombre.clear();
        campoPuesto.clear();
        casillaActivo.setSelected(true);
        campoNombre.requestFocus();
    }

    private void cargarEmpleados() {
        try {
            empleados.setAll(consultas.listarEmpleados());
        } catch (SQLException excepcion) {
            logger.error("No fue posible cargar los empleados.", excepcion);
            mostrarMensaje(Alert.AlertType.ERROR, "Error de conexión", "No fue posible cargar los empleados.");
        }
    }

    private void mostrarEmpleado(Empleado empleado) {
        empleadoSeleccionado = empleado;

        if (empleado == null) {
            return;
        }

        campoNombre.setText(empleado.getNombre());
        campoPuesto.setText(empleado.getPuesto());
        casillaActivo.setSelected(empleado.isActivo());
    }

    private void mostrarMensaje(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}
