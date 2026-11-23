package org.fernandogranados.demo.controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.fernandogranados.demo.actualizaciones.Actualizaciones;
import org.fernandogranados.demo.consultas.Consultas;
import org.fernandogranados.demo.eliminaciones.Eliminaciones;
import org.fernandogranados.demo.inserciones.Inserciones;
import org.fernandogranados.demo.modelo.Categoria;
import org.fernandogranados.demo.modelo.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Controlador de la vista {@code Productos.fxml}, que administra el menú de la cafetería.
 *
 * <p>Sigue el mismo patrón de lista y formulario descrito en {@link CategoriasControlador}.
 * Además usa un {@link ComboBox} (lista desplegable) para elegir la categoría y un
 * {@link CheckBox} para marcar la disponibilidad del producto.</p>
 */
public class ProductosControlador {
    private static Logger logger = LoggerFactory.getLogger(ProductosControlador.class);

    @FXML
    private ListView<Producto> listaProductos;

    @FXML
    private ComboBox<Categoria> comboCategoria;

    @FXML
    private TextField campoNombre;

    @FXML
    private TextArea campoDescripcion;

    @FXML
    private TextField campoPrecio;

    @FXML
    private CheckBox casillaDisponible;

    private final Actualizaciones actualizaciones = new Actualizaciones();
    private final Inserciones inserciones = new Inserciones();
    private final Eliminaciones eliminaciones = new Eliminaciones();
    private final Consultas consultas = new Consultas();
    private ObservableList<Producto> productos = FXCollections.observableArrayList();
    private Producto productoSeleccionado;

    @FXML
    public void initialize() {
        listaProductos.setCellFactory(lista -> new Tarjetas.CeldaProducto());
        listaProductos.setItems(productos);
        listaProductos.getSelectionModel().selectedItemProperty().addListener((observador, anterior, producto) -> mostrarProducto(producto));

        cargarCategorias();
        cargarProductos();
    }

    @FXML
    private void guardarProducto() {
        Categoria categoria = comboCategoria.getValue();
        String nombre = campoNombre.getText().trim();
        String descripcion = campoDescripcion.getText().trim();
        BigDecimal precio = obtenerPrecio();

        if (categoria == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Categoría obligatoria", "Seleccione la categoría del producto.");
            return;
        }

        if (nombre.isEmpty()) {
            mostrarMensaje(Alert.AlertType.WARNING, "Campo obligatorio", "Escriba el nombre del producto.");
            return;
        }

        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            mostrarMensaje(Alert.AlertType.WARNING, "Precio inválido", "Escriba un precio mayor que cero.");
            return;
        }

        try {
            if (productoSeleccionado == null) {
                Producto producto = new Producto();
                producto.setIdCategoria(categoria.getIdCategoria());
                producto.setNombre(nombre);
                producto.setDescripcion(descripcion);
                producto.setPrecio(precio);
                producto.setDisponible(casillaDisponible.isSelected());
                inserciones.insertarProducto(producto);
                mostrarMensaje(Alert.AlertType.INFORMATION, "Producto registrado", "El producto se registró correctamente.");
            } else {
                productoSeleccionado.setIdCategoria(categoria.getIdCategoria());
                productoSeleccionado.setNombre(nombre);
                productoSeleccionado.setDescripcion(descripcion);
                productoSeleccionado.setPrecio(precio);
                productoSeleccionado.setDisponible(casillaDisponible.isSelected());
                actualizaciones.actualizarProducto(productoSeleccionado);
                mostrarMensaje(Alert.AlertType.INFORMATION, "Producto actualizado", "El producto se actualizó correctamente.");
            }

            cargarProductos();
            limpiarFormulario();
        } catch (SQLException excepcion) {
            logger.error("No fue posible guardar el producto.", excepcion);
            mostrarMensaje(Alert.AlertType.ERROR, "No se pudo guardar", "Verifique que no exista otro producto con el mismo nombre en la categoría.");
        }
    }

    /**
     * <h1>Eliminar producto</h1>
     * Elimina el producto seleccionado después de pedir confirmación.
     *
     * <p>Si el producto ya aparece en ventas, MySQL rechaza el borrado para conservar el
     * historial. En ese caso se sugiere marcarlo como no disponible, lo que lo oculta de la
     * pantalla de ventas sin perder sus registros.</p>
     */
    @FXML
    private void eliminarProducto() {
        if (productoSeleccionado == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Seleccione un producto", "Seleccione el producto que desea eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar producto");
        confirmacion.setHeaderText("¿Desea eliminar el producto seleccionado?");
        confirmacion.setContentText(productoSeleccionado.getNombre());

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            eliminaciones.eliminarProducto(productoSeleccionado.getIdProducto());
            cargarProductos();
            limpiarFormulario();
            mostrarMensaje(Alert.AlertType.INFORMATION, "Producto eliminado", "El producto se eliminó correctamente.");
        } catch (SQLException excepcion) {
            logger.error("No fue posible eliminar el producto.", excepcion);
            mostrarMensaje(Alert.AlertType.ERROR, "No se pudo eliminar", "El producto puede estar relacionado con ventas. Puede marcarlo como no disponible.");
        }
    }

    @FXML
    private void limpiarFormulario() {
        productoSeleccionado = null;
        listaProductos.getSelectionModel().clearSelection();
        comboCategoria.getSelectionModel().clearSelection();
        campoNombre.clear();
        campoDescripcion.clear();
        campoPrecio.clear();
        casillaDisponible.setSelected(true);
        comboCategoria.requestFocus();
    }

    private void cargarCategorias() {
        try {
            comboCategoria.setItems(FXCollections.observableArrayList(consultas.listarCategorias()));
        } catch (SQLException excepcion) {
            logger.error("No fue posible cargar las categorías.", excepcion);
            mostrarMensaje(Alert.AlertType.ERROR, "Error de conexión", "No fue posible cargar las categorías.");
        }
    }

    private void cargarProductos() {
        try {
            productos.setAll(consultas.listarProductos());
        } catch (SQLException excepcion) {
            logger.error("No fue posible cargar los productos.", excepcion);
            mostrarMensaje(Alert.AlertType.ERROR, "Error de conexión", "No fue posible cargar los productos.");
        }
    }

    /**
     * <h1>Leer precio del formulario</h1>
     * Convierte el texto del campo de precio en un número decimal exacto.
     *
     * <p>Acepta coma o punto como separador decimal. Se usa {@link BigDecimal} en lugar de
     * {@code double} porque los importes de dinero no deben tener errores de redondeo.</p>
     *
     * @return El precio escrito, o {@code null} si el texto no es un número válido.
     */
    private BigDecimal obtenerPrecio() {
        String textoPrecio = campoPrecio.getText().trim().replace(',', '.');

        try {
            return new BigDecimal(textoPrecio);
        } catch (NumberFormatException excepcion) {
            return null;
        }
    }

    private void mostrarProducto(Producto producto) {
        productoSeleccionado = producto;

        if (producto == null) {
            return;
        }

        seleccionarCategoria(producto.getIdCategoria());
        campoNombre.setText(producto.getNombre());
        campoDescripcion.setText(producto.getDescripcion());
        campoPrecio.setText(producto.getPrecio().toPlainString());
        casillaDisponible.setSelected(producto.isDisponible());
    }

    /**
     * <h1>Seleccionar categoría del producto</h1>
     * Selecciona en el {@link ComboBox} la categoría con el identificador indicado.
     *
     * <p>La búsqueda se hace por identificador porque el producto y la lista desplegable tienen
     * objetos {@code Categoria} distintos, cargados en consultas separadas, aunque representen el
     * mismo registro.</p>
     *
     * @param idCategoria Identificador de la categoría del producto que se está editando.
     */
    private void seleccionarCategoria(long idCategoria) {
        for (Categoria categoria : comboCategoria.getItems()) {
            if (categoria.getIdCategoria() == idCategoria) {
                comboCategoria.setValue(categoria);
                return;
            }
        }
    }

    private void mostrarMensaje(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}
