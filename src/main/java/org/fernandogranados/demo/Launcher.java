package org.fernandogranados.demo;

import javafx.application.Application;
import org.fernandogranados.demo.utilidad.ConexionBD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Punto de entrada del sistema de cafetería.
 *
 * <p>Esta clase existe separada de {@link Aplicacion} por dos motivos. Primero, verifica la
 * conexión con MySQL antes de abrir cualquier ventana, de modo que el usuario no vea una
 * interfaz que fallaría en la primera consulta. Segundo, una clase {@code main} que no hereda de
 * {@link Application} evita el error "JavaFX runtime components are missing" cuando el programa
 * se ejecuta sin configurar los módulos de JavaFX.</p>
 */
public class Launcher {
    private static Logger logger = LoggerFactory.getLogger(Launcher.class);

    /**
     * <h1>Iniciar el sistema</h1>
     * Comprueba la conexión a la base de datos y, si es exitosa, inicia la aplicación JavaFX.
     *
     * <p>{@link Application#launch(Class, String...)} crea el hilo de interfaz de JavaFX
     * (<i>JavaFX Application Thread</i>), instancia {@link Aplicacion} e invoca su método
     * {@code start}. La llamada bloquea hasta que se cierra la última ventana.</p>
     *
     * @param args Argumentos de la línea de comandos, que se reenvían a JavaFX sin modificar.
     */
    public static void main(String[] args) {
        logger.info("Iniciando el sistema de cafetería");

        if (!ConexionBD.probarConexion()) {
            System.err.println("No se pudo conectar con la base de datos. Revise la configuración de .env y el contenedor MySQL.");
            return;
        }

        logger.info("El sistema está listo para usar la base de datos.");
        Application.launch(Aplicacion.class, args);
    }
}
