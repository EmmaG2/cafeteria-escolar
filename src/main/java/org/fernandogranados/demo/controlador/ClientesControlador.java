package org.fernandogranados.demo.controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.fernandogranados.demo.actualizaciones.Actualizaciones;
import org.fernandogranados.demo.consultas.Consultas;
import org.fernandogranados.demo.eliminaciones.Eliminaciones;
import org.fernandogranados.demo.inserciones.Inserciones;
import org.fernandogranados.demo.modelo.Cliente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Controlador de la vista {@code Clientes.fxml}, que administra los clientes de la cafetería.
 *
 * <p>Sigue el mismo patrón de lista y formulario descrito en {@link CategoriasControlador}.
 * El teléfono y el correo son opcionales.</p>
 */
public class ClientesControlador {
    private static Logger logger = LoggerFactory.getLogger(ClientesControlador.class);

    @FXML
    private ListView<Cliente> listaClientes;

    @FXML
    private TextField campoNombre;

    @FXML
    private TextField campoTelefono;

    @FXML
    private TextField campoCorreo;

    private final Actualizaciones actualizaciones = new Actualizaciones();
    private final Inserciones inserciones = new Inserciones();
    private final Eliminaciones eliminaciones = new Eliminaciones();
    private final Consultas consultas = new Consultas();
    private ObservableList<Cliente> clientes = FXCollections.observableArrayList();
    private Cliente clienteSeleccionado;

    @FXML
    public void initialize() {
        listaClientes.setCellFactory(lista -> new Tarjetas.CeldaCliente());
        listaClientes.setItems(clientes);
        listaClientes.getSelectionModel().selectedItemProperty().addListener((observador, anterior, cliente) -> mostrarCliente(cliente));
        cargarClientes();
    }

    /**
     * <h1>Guardar cliente</h1>
     * Inserta o actualiza el cliente con los datos del formulario.
     *
     * <p>Antes de guardar valida el formato del correo con una expresión regular sencilla:
     * texto sin espacios, una arroba y un dominio con punto. El correo es {@code UNIQUE} en la
     * base de datos, por lo que un error al guardar suele indicar que otro cliente ya lo usa.</p>
     */
    @FXML
    private void guardarCliente() {
        String nombre = campoNombre.getText().trim();
        String telefono = obtenerTextoOpcional(campoTelefono);
        String correo = obtenerTextoOpcional(campoCorreo);

        if (nombre.isEmpty()) {
            mostrarMensaje(Alert.AlertType.WARNING, "Campo obligatorio", "Escriba el nombre del cliente.");
            return;
        }

        if (correo != null && !correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            mostrarMensaje(Alert.AlertType.WARNING, "Correo inválido", "Escriba un correo electrónico válido.");
            return;
        }

        try {
            if (clienteSeleccionado == null) {
                Cliente cliente = new Cliente();
                cliente.setNombre(nombre);
                cliente.setTelefono(telefono);
                cliente.setCorreo(correo);
                inserciones.insertarCliente(cliente);
                mostrarMensaje(Alert.AlertType.INFORMATION, "Cliente registrado", "El cliente se registró correctamente.");
            } else {
                clienteSeleccionado.setNombre(nombre);
                clienteSeleccionado.setTelefono(telefono);
                clienteSeleccionado.setCorreo(correo);
                actualizaciones.actualizarCliente(clienteSeleccionado);
                mostrarMensaje(Alert.AlertType.INFORMATION, "Cliente actualizado", "El cliente se actualizó correctamente.");
            }

            cargarClientes();
            limpiarFormulario();
        } catch (SQLException excepcion) {
            logger.error("No fue posible guardar el cliente.", excepcion);
            mostrarMensaje(Alert.AlertType.ERROR, "No se pudo guardar", "Verifique que el correo no esté registrado por otro cliente.");
        }
    }

    /**
     * <h1>Eliminar cliente</h1>
     * Elimina el cliente seleccionado después de pedir confirmación.
     *
     * <p>A diferencia de productos y empleados, un cliente con ventas sí puede eliminarse: la
     * llave foránea de {@code Ventas} está definida con {@code ON DELETE SET NULL}, así que esas
     * ventas se conservan y solo quedan sin cliente asociado.</p>
     */
    @FXML
    private void eliminarCliente() {
        if (clienteSeleccionado == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Seleccione un cliente", "Seleccione el cliente que desea eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar cliente");
        confirmacion.setHeaderText("¿Desea eliminar el cliente seleccionado?");
        confirmacion.setContentText("Las ventas existentes conservarán su historial sin cliente asociado.");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            eliminaciones.eliminarCliente(clienteSeleccionado.getIdCliente());
            cargarClientes();
            limpiarFormulario();
            mostrarMensaje(Alert.AlertType.INFORMATION, "Cliente eliminado", "El cliente se eliminó correctamente.");
        } catch (SQLException excepcion) {
            logger.error("No fue posible eliminar el cliente.", excepcion);
            mostrarMensaje(Alert.AlertType.ERROR, "No se pudo eliminar", "No fue posible eliminar el cliente seleccionado.");
        }
    }

    @FXML
    private void limpiarFormulario() {
        clienteSeleccionado = null;
        listaClientes.getSelectionModel().clearSelection();
        campoNombre.clear();
        campoTelefono.clear();
        campoCorreo.clear();
        campoNombre.requestFocus();
    }

    private void cargarClientes() {
        try {
            clientes.setAll(consultas.listarClientes());
        } catch (SQLException excepcion) {
            logger.error("No fue posible cargar los clientes.", excepcion);
            mostrarMensaje(Alert.AlertType.ERROR, "Error de conexión", "No fue posible cargar los clientes.");
        }
    }

    private void mostrarCliente(Cliente cliente) {
        clienteSeleccionado = cliente;

        if (cliente == null) {
            return;
        }

        campoNombre.setText(cliente.getNombre());
        campoTelefono.setText(cliente.getTelefono());
        campoCorreo.setText(cliente.getCorreo());
    }

    /**
     * <h1>Leer campo opcional</h1>
     * Lee un campo opcional del formulario.
     *
     * <p>Un campo vacío se convierte en {@code null} para guardar {@code NULL} en la base de datos
     * en lugar de una cadena vacía. Así, varios clientes sin correo no chocan con la restricción
     * {@code UNIQUE} de esa columna.</p>
     *
     * @param campo Campo de texto que se va a leer.
     * @return El texto sin espacios al inicio ni al final, o {@code null} si el campo está vacío.
     */
    private String obtenerTextoOpcional(TextField campo) {
        String texto = campo.getText().trim();
        return texto.isEmpty() ? null : texto;
    }

    private void mostrarMensaje(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}
