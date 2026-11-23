package org.fernandogranados.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Clase principal de JavaFX que construye y muestra la ventana del sistema.
 *
 * <p>En JavaFX una ventana se compone de tres niveles:</p>
 * <ul>
 *   <li>{@link Stage} (escenario): la ventana del sistema operativo, con título y tamaño.</li>
 *   <li>{@link Scene} (escena): el contenido de la ventana y las hojas de estilo CSS que aplican.</li>
 *   <li>Nodo raíz: el contenedor visual cargado desde un archivo FXML, que a su vez contiene
 *       botones, campos y listas.</li>
 * </ul>
 */
public class Aplicacion extends Application {
    private static Logger logger = LoggerFactory.getLogger(Aplicacion.class);

    /**
     * <h1>Mostrar ventana principal</h1>
     * Carga el menú principal desde FXML, aplica los estilos y muestra la ventana.
     *
     * <p>JavaFX invoca este método automáticamente después de {@link Application#launch}.
     * {@link FXMLLoader} lee el archivo {@code MenuPrincipal.fxml}, crea los controles que
     * describe y crea también una instancia de su controlador
     * ({@code MenuPrincipalControlador}), indicado en el atributo {@code fx:controller} del FXML.</p>
     *
     * <p>Se aplican dos hojas de estilo: BootstrapFX, que aporta clases visuales predefinidas
     * como {@code btn} o {@code btn-primary}, y {@code cafeteria.css}, con los estilos propios.
     * Si la vista no puede cargarse, se registra el error y la aplicación termina.</p>
     *
     * @param escenario Ventana principal que JavaFX crea y entrega a la aplicación.
     */
    @Override
    public void start(Stage escenario) {
        try {
            FXMLLoader cargador = new FXMLLoader(Aplicacion.class.getResource("/vistas/MenuPrincipal.fxml"));
            BorderPane raiz = cargador.load();
            Scene escena = new Scene(raiz, 1280, 820);

            escena.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            escena.getStylesheets().add(Aplicacion.class.getResource("/estilos/cafeteria.css").toExternalForm());
            escenario.setTitle("Sistema de cafetería");
            escenario.setMinWidth(1100);
            escenario.setMinHeight(720);
            escenario.setScene(escena);
            escenario.show();
        } catch (IOException excepcion) {
            logger.error("No fue posible cargar el menú principal.", excepcion);
            Platform.exit();
        }
    }
}
