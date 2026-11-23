package org.fernandogranados.demo.utilidad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Abre conexiones JDBC con la base de datos {@code cafeteria_escolar}.
 *
 * <p>Las credenciales no se escriben en el código fuente. Se leen del archivo {@code .env}, que
 * está excluido del control de versiones, o de variables de entorno.</p>
 */
public class ConexionBD {
    private static final Logger logger = LoggerFactory.getLogger(ConexionBD.class);
    private static final Properties config = cargarConfiguracion();

    private ConexionBD() {
    }

    public static Connection obtenerConexion() throws SQLException {
        String usuario = obtenerValor("DB_USER", null);
        String contrasena = obtenerValor("DB_PASSWORD", null);

        if (estaVacio(usuario) || estaVacio(contrasena)) {
            throw new SQLException("Falta configurar DB_USER o DB_PASSWORD para conectar a la base de datos.");
        }

        String url = obtenerUrl();
        logger.debug("Abriendo conexión a la base de datos en {}", url);
        return DriverManager.getConnection(url, usuario, contrasena);
    }

    public static boolean probarConexion() {
        try (Connection conexion = obtenerConexion()) {
            logger.info("Conexión a la base de datos establecida correctamente.");
            return true;
        } catch (SQLException excepcion) {
            logger.error("No fue posible conectar con la base de datos: {}", excepcion.getMessage(), excepcion);
            return false;
        }
    }

    private static Properties cargarConfiguracion() {
        Properties configuracion = new Properties();
        Path archivoConfiguracion = Path.of(".env");

        if (!Files.exists(archivoConfiguracion)) {
            logger.warn("No se encontró el archivo .env");
            return configuracion;
        }

        try (InputStream entrada = Files.newInputStream(archivoConfiguracion)) {
            configuracion.load(entrada);
        } catch (IOException excepcion) {
            logger.warn("No se pudo leer el archivo .env: {}", excepcion.getMessage());
        }

        return configuracion;
    }

    private static String obtenerUrl() {
        String url = obtenerValor("DB_URL", null);
        if (!estaVacio(url)) {
            return url;
        }

        String servidor = obtenerValor("DB_HOST", "localhost");
        String puerto = obtenerValor("DB_PORT", "3036");
        String baseDatos = obtenerValor("DB_NAME", "cafeteria_escolar");

        return "jdbc:mysql://" + servidor + ":" + puerto + "/" + baseDatos;
    }

    /**
     * <h1>Obtener valor de configuración</h1>
     * Busca un parámetro de configuración en tres fuentes, en este orden de prioridad: variable de
     * entorno, propiedad del sistema ({@code -Dnombre=valor}) y archivo {@code .env}.
     *
     * @param nombre              Nombre del parámetro, por ejemplo {@code DB_USER}.
     * @param valorPredeterminado Valor que se usa si ninguna fuente lo define.
     * @return El primer valor no vacío encontrado, o el valor predeterminado.
     */
    private static String obtenerValor(String nombre, String valorPredeterminado) {
        String valor = System.getenv(nombre);
        if (!estaVacio(valor)) {
            return valor;
        }

        valor = System.getProperty(nombre);
        if (!estaVacio(valor)) {
            return valor;
        }

        valor = config.getProperty(nombre);
        if (!estaVacio(valor)) {
            return valor;
        }

        return valorPredeterminado;
    }

    private static boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
