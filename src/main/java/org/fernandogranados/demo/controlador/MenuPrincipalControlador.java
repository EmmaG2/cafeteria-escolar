package org.fernandogranados.demo.controlador;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * Controlador de la vista {@code MenuPrincipal.fxml}, que funciona como marco de navegación del sistema.
 *
 * <p>La vista principal es un {@link BorderPane}, un contenedor con cinco regiones: superior,
 * inferior, izquierda, derecha y centro. El menú lateral permanece fijo y cada opción reemplaza
 * únicamente la región central con la vista del módulo elegido. Así se navega sin abrir ventanas
 * nuevas.</p>
 *
 * <p><b>Vínculo con FXML:</b> los atributos marcados con {@link FXML} reciben automáticamente el
 * control del archivo FXML que tiene el mismo {@code fx:id}. Los métodos marcados con {@link FXML}
 * se ejecutan cuando el usuario pulsa el botón cuyo {@code onAction} los nombra, por ejemplo
 * {@code onAction="#abrirCategorias"}. Por eso estos métodos pueden ser privados y no tienen
 * llamadas visibles en el código Java.</p>
 */
public class MenuPrincipalControlador {
    private static Logger logger = LoggerFactory.getLogger(MenuPrincipalControlador.class);

    /** Contenedor raíz de la vista; en su región central se coloca el módulo activo. */
    @FXML
    private BorderPane contenedorPrincipal;

    @FXML
    private Label tituloModulo;

    @FXML
    private Label descripcionModulo;

    /**
     * <h1>Abrir categorías</h1>
     * Carga la vista de categorías y la coloca en la región central del menú.
     *
     * <p>{@link FXMLLoader#load} crea los controles de {@code Categorias.fxml} y su controlador,
     * que en ese momento ejecuta su método {@code initialize()} y consulta los datos. Los demás
     * métodos {@code abrir...} de esta clase siguen el mismo procedimiento con su vista
     * correspondiente.</p>
     */
    @FXML
    private void abrirCategorias() {
        try {
            BorderPane vistaCategorias = FXMLLoader.load(Objects.requireNonNull(MenuPrincipalControlador.class.getResource("/vistas/Categorias.fxml")));
            contenedorPrincipal.setCenter(vistaCategorias);
        } catch (IOException excepcion) {
            logger.error("No fue posible cargar la vista de categorías.", excepcion);
            mostrarModulo("Categorías", "No fue posible cargar este módulo.");
        }
    }

    @FXML
    private void abrirProductos() {
        try {
            BorderPane vistaProductos = FXMLLoader.load(Objects.requireNonNull(MenuPrincipalControlador.class.getResource("/vistas/Productos.fxml")));
            contenedorPrincipal.setCenter(vistaProductos);
        } catch (IOException excepcion) {
            logger.error("No fue posible cargar la vista de productos.", excepcion);
            mostrarModulo("Productos", "No fue posible cargar este módulo.");
        }
    }

    @FXML
    private void abrirClientes() {
        try {
            BorderPane vistaClientes = FXMLLoader.load(Objects.requireNonNull(MenuPrincipalControlador.class.getResource("/vistas/Clientes.fxml")));
            contenedorPrincipal.setCenter(vistaClientes);
        } catch (IOException excepcion) {
            logger.error("No fue posible cargar la vista de clientes.", excepcion);
            mostrarModulo("Clientes", "No fue posible cargar este módulo.");
        }
    }

    @FXML
    private void abrirEmpleados() {
        try {
            BorderPane vistaEmpleados = FXMLLoader.load(Objects.requireNonNull(MenuPrincipalControlador.class.getResource("/vistas/Empleados.fxml")));
            contenedorPrincipal.setCenter(vistaEmpleados);
        } catch (IOException excepcion) {
            logger.error("No fue posible cargar la vista de empleados.", excepcion);
            mostrarModulo("Empleados", "No fue posible cargar este módulo.");
        }
    }

    @FXML
    private void abrirVentas() {
        try {
            BorderPane vistaVentas = FXMLLoader.load(Objects.requireNonNull(MenuPrincipalControlador.class.getResource("/vistas/Ventas.fxml")));
            contenedorPrincipal.setCenter(vistaVentas);
        } catch (IOException excepcion) {
            logger.error("No fue posible cargar la vista de ventas.", excepcion);
            mostrarModulo("Ventas", "No fue posible cargar este módulo.");
        }
    }

    @FXML
    private void abrirHistorialVentas() {
        try {
            BorderPane vistaHistorial = FXMLLoader.load(Objects.requireNonNull(MenuPrincipalControlador.class.getResource("/vistas/HistorialVentas.fxml")));
            contenedorPrincipal.setCenter(vistaHistorial);
        } catch (IOException excepcion) {
            logger.error("No fue posible cargar el historial de ventas.", excepcion);
            mostrarModulo("Historial de ventas", "No fue posible cargar este módulo.");
        }
    }

    @FXML
    private void abrirReportes() {
        try {
            BorderPane vistaReportes = FXMLLoader.load(Objects.requireNonNull(MenuPrincipalControlador.class.getResource("/vistas/Reportes.fxml")));
            contenedorPrincipal.setCenter(vistaReportes);
        } catch (IOException excepcion) {
            logger.error("No fue posible cargar la vista de reportes.", excepcion);
            mostrarModulo("Reportes", "No fue posible cargar este módulo.");
        }
    }

    /**
     * <h1>Mostrar módulo</h1>
     * Muestra en la cabecera del menú un aviso cuando un módulo no pudo cargarse.
     *
     * @param titulo      Nombre del módulo que se intentó abrir.
     * @param descripcion Mensaje que explica el problema al usuario.
     */
    private void mostrarModulo(String titulo, String descripcion) {
        tituloModulo.setText(titulo);
        descripcionModulo.setText(descripcion + " Este módulo estará disponible en una siguiente etapa.");
    }
}
