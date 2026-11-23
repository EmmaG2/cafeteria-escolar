package org.fernandogranados.demo.controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.fernandogranados.demo.actualizaciones.Actualizaciones;
import org.fernandogranados.demo.consultas.Consultas;
import org.fernandogranados.demo.eliminaciones.Eliminaciones;
import org.fernandogranados.demo.inserciones.Inserciones;
import org.fernandogranados.demo.modelo.Categoria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Controlador de la vista {@code Categorias.fxml}, que permite registrar, editar y eliminar categorías.
 *
 * <p>Este controlador establece el patrón que siguen también los de productos, clientes y empleados:</p>
 * <ul>
 *   <li>A la izquierda, un {@link ListView} muestra los registros como tarjetas.</li>
 *   <li>A la derecha, un formulario sirve tanto para crear como para editar.</li>
 *   <li>Si no hay un registro seleccionado, el botón Guardar inserta; si lo hay, actualiza.</li>
 * </ul>
 *
 * <p><b>Ciclo de vida:</b> JavaFX crea esta clase al cargar el FXML, inyecta los atributos
 * {@link FXML} y después invoca {@link #initialize()}. Los métodos {@link FXML} privados se
 * enlazan a los botones mediante el atributo {@code onAction} del FXML.</p>
 */
public class CategoriasControlador {
    private static Logger logger = LoggerFactory.getLogger(CategoriasControlador.class);

    @FXML
    private ListView<Categoria> listaCategorias;

    @FXML
    private TextField campoNombre;

    @FXML
    private TextArea campoDescripcion;

    private final Actualizaciones actualizaciones = new Actualizaciones();
    private final Inserciones inserciones = new Inserciones();
    private final Eliminaciones eliminaciones = new Eliminaciones();
    private final Consultas consultas = new Consultas();
    /**
     * Lista observable enlazada al {@link ListView}. A diferencia de un {@code ArrayList} común,
     * notifica sus cambios a la interfaz: al reemplazar su contenido con {@code setAll}, la lista
     * en pantalla se redibuja sola, sin llamadas adicionales.
     */
    private final ObservableList<Categoria> categorias = FXCollections.observableArrayList();

    /** Categoría que se está editando; {@code null} indica que el formulario creará una nueva. */
    private Categoria categoriaSeleccionada;

    /**
     * <h1>Inicializar vista</h1>
     * Prepara la vista. JavaFX lo invoca una sola vez, cuando ya están inyectados los controles del FXML.
     *
     * <p>Configura tres aspectos del {@link ListView}:</p>
     * <ol>
     *   <li>La <i>fábrica de celdas</i> ({@code setCellFactory}), que define cómo se dibuja cada
     *       elemento. Aquí se usa {@link Tarjetas.CeldaCategoria} para mostrar una tarjeta en lugar
     *       del texto plano de {@code toString()}.</li>
     *   <li>La fuente de datos: la lista observable {@link #categorias}.</li>
     *   <li>Un <i>listener</i> sobre la selección: cada vez que el usuario elige una tarjeta, se
     *       ejecuta {@link #mostrarCategoria(Categoria)} con la nueva categoría seleccionada.</li>
     * </ol>
     */
    @FXML
    public void initialize() {
        listaCategorias.setCellFactory(lista -> new Tarjetas.CeldaCategoria());
        listaCategorias.setItems(categorias);
        listaCategorias.getSelectionModel().selectedItemProperty().addListener((observador, anterior, categoria) -> mostrarCategoria(categoria));
        cargarCategorias();
    }

    /**
     * <h1>Guardar categoría</h1>
     * Inserta o actualiza la categoría con los datos del formulario.
     *
     * <p>Decide la operación según {@link #categoriaSeleccionada}: si es {@code null} llama al
     * módulo de inserciones; en otro caso, al de actualizaciones. Un error de base de datos se
     * informa al usuario con un {@link Alert}, porque la causa más común es un nombre duplicado,
     * que la restricción {@code UNIQUE} de la tabla rechaza.</p>
     */
    @FXML
    private void guardarCategoria() {
        String nombre = campoNombre.getText().trim();
        String descripcion = campoDescripcion.getText().trim();

        if (nombre.isEmpty()) {
            mostrarMensaje(Alert.AlertType.WARNING, "Campo obligatorio", "Escriba el nombre de la categoría.");
            return;
        }

        try {
            if (categoriaSeleccionada == null) {
                Categoria categoria = new Categoria(0, nombre, descripcion);
                inserciones.insertarCategoria(categoria);
                mostrarMensaje(Alert.AlertType.INFORMATION, "Categoría registrada", "La categoría se registró correctamente.");
            } else {
                categoriaSeleccionada.setNombre(nombre);
                categoriaSeleccionada.setDescripcion(descripcion);
                actualizaciones.actualizarCategoria(categoriaSeleccionada);
                mostrarMensaje(Alert.AlertType.INFORMATION, "Categoría actualizada", "La categoría se actualizó correctamente.");
            }

            cargarCategorias();
            limpiarFormulario();
        } catch (SQLException excepcion) {
            logger.error("No fue posible guardar la categoría.", excepcion);
            mostrarMensaje(Alert.AlertType.ERROR, "No se pudo guardar", "Verifique que el nombre no esté repetido.");
        }
    }

    /**
     * <h1>Eliminar categoría</h1>
     * Elimina la categoría seleccionada después de pedir confirmación.
     *
     * <p>{@link Alert#showAndWait()} detiene la ejecución hasta que el usuario cierra el diálogo y
     * devuelve el botón pulsado. Si la categoría tiene productos, la llave foránea de MySQL impide
     * borrarla y se muestra un mensaje explicativo en lugar de un error técnico.</p>
     */
    @FXML
    private void eliminarCategoria() {
        if (categoriaSeleccionada == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Seleccione una categoría", "Seleccione la categoría que desea eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar categoría");
        confirmacion.setHeaderText("¿Desea eliminar la categoría seleccionada?");
        confirmacion.setContentText(categoriaSeleccionada.getNombre());

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            eliminaciones.eliminarCategoria(categoriaSeleccionada.getIdCategoria());
            cargarCategorias();
            limpiarFormulario();
            mostrarMensaje(Alert.AlertType.INFORMATION, "Categoría eliminada", "La categoría se eliminó correctamente.");
        } catch (SQLException excepcion) {
            logger.error("No fue posible eliminar la categoría.", excepcion);
            mostrarMensaje(Alert.AlertType.ERROR, "No se pudo eliminar", "La categoría puede estar relacionada con productos.");
        }
    }

    /**
     * <h1>Limpiar formulario</h1>
     * Quita la selección y vacía el formulario, de modo que el siguiente guardado cree un registro nuevo.
     */
    @FXML
    private void limpiarFormulario() {
        categoriaSeleccionada = null;
        listaCategorias.getSelectionModel().clearSelection();
        campoNombre.clear();
        campoDescripcion.clear();
        campoNombre.requestFocus();
    }

    private void cargarCategorias() {
        try {
            categorias.setAll(consultas.listarCategorias());
        } catch (SQLException excepcion) {
            logger.error("No fue posible cargar las categorías.", excepcion);
            mostrarMensaje(Alert.AlertType.ERROR, "Error de conexión", "No fue posible cargar las categorías.");
        }
    }

    /**
     * <h1>Mostrar categoría</h1>
     * Copia los datos de la categoría elegida en el formulario para editarla.
     *
     * @param categoria Categoría seleccionada en la lista, o {@code null} si se quitó la selección.
     */
    private void mostrarCategoria(Categoria categoria) {
        categoriaSeleccionada = categoria;

        if (categoria == null) {
            return;
        }

        campoNombre.setText(categoria.getNombre());
        campoDescripcion.setText(categoria.getDescripcion());
    }

    /**
     * <h1>Mostrar mensaje</h1>
     * Muestra un cuadro de diálogo modal, es decir, bloquea la ventana hasta que el usuario lo cierra.
     *
     * @param tipo      Tipo de alerta, que define el icono: información, advertencia o error.
     * @param titulo    Texto de la barra de título del diálogo.
     * @param contenido Mensaje dirigido al usuario.
     */
    private void mostrarMensaje(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}
