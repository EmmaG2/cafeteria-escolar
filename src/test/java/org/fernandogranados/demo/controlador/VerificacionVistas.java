package org.fernandogranados.demo.controlador;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import org.fernandogranados.demo.modelo.*;
import org.fernandogranados.demo.reporteador.DatoReporte;
import org.kordamp.bootstrapfx.BootstrapFX;

import javax.imageio.ImageIO;
import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Prueba de escritorio sin MySQL. Ejecutar con scripts/verificar-vistas.sh. */
public class VerificacionVistas {
    private static Throwable fallo;

    public static void main(String[] argumentos) throws Exception {
        CountDownLatch terminada = new CountDownLatch(1);
        Platform.startup(() -> {
            try {
                comprobarVistas();
                comprobarCeldas();
                comprobarDimensionesInputs();
            } catch (Throwable excepcion) {
                fallo = excepcion;
            } finally {
                terminada.countDown();
            }
        });
        boolean completada = terminada.await(45, TimeUnit.SECONDS);
        Platform.exit();
        if (!completada) {
            throw new AssertionError("La prueba visual excedió el tiempo disponible.");
        }
        if (fallo != null) {
            throw new AssertionError("Falló la prueba visual", fallo);
        }
        System.out.println("OK: 8 FXML, inyección, CSS, iconos, selección y reciclado de tarjetas.");
    }

    private static void comprobarVistas() throws Exception {
        String[] vistas = {"MenuPrincipal", "Categorias", "Productos", "Clientes", "Empleados", "Ventas", "HistorialVentas", "Reportes"};
        for (String vista : vistas) {
            FXMLLoader cargador = new FXMLLoader(VerificacionVistas.class.getResource("/vistas/" + vista + ".fxml"));
            cargador.setControllerFactory(tipo -> controladorSinConsultas(tipo));
            Parent raiz = cargador.load();
            Object controlador = cargador.getController();
            Class<?> tipo = controlador.getClass();
            if (tipo.getSuperclass() != Object.class) {
                tipo = tipo.getSuperclass();
            }
            for (Field campo : tipo.getDeclaredFields()) {
                if (campo.isAnnotationPresent(FXML.class)) {
                    campo.setAccessible(true);
                    comprobar(campo.get(controlador) != null, vista + ": no se inyectó " + campo.getName());
                }
            }
            Scene escena = new Scene(raiz, 1056, 760);
            escena.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            escena.getStylesheets().add(VerificacionVistas.class.getResource("/estilos/cafeteria.css").toExternalForm());
            if ("Productos".equals(vista)) {
                @SuppressWarnings("unchecked")
                ListView<Producto> lista = (ListView<Producto>) cargador.getNamespace().get("listaProductos");
                lista.setCellFactory(control -> new Tarjetas.CeldaProducto());
                Producto producto = producto();
                lista.getItems().add(producto);
                lista.getSelectionModel().select(producto);
                comprobar(lista.getSelectionModel().getSelectedItem() == producto, "La selección debe conservar la entidad.");
                raiz.applyCss();
                raiz.layout();
                ImageIO.write(SwingFXUtils.fromFXImage(raiz.snapshot(null, null), null), "png", new File("target/productos-visual.png"));
            }
            raiz.applyCss();
            raiz.layout();
            comprobar(raiz.lookupAll(".table-view").isEmpty(), vista + ": todavía contiene tablas.");
            System.out.println("FXML y CSS: " + vista);
        }
    }

    // Solo se omite initialize para que la prueba de presentación no consulte ni modifique MySQL.
    private static Object controladorSinConsultas(Class<?> tipo) {
        if (tipo == CategoriasControlador.class) return new CategoriasControlador() { public void initialize() {} };
        if (tipo == ProductosControlador.class) return new ProductosControlador() { public void initialize() {} };
        if (tipo == ClientesControlador.class) return new ClientesControlador() { public void initialize() {} };
        if (tipo == EmpleadosControlador.class) return new EmpleadosControlador() { public void initialize() {} };
        if (tipo == VentasControlador.class) return new VentasControlador() { public void initialize() {} };
        if (tipo == HistorialVentasControlador.class) return new HistorialVentasControlador() { public void initialize() {} };
        if (tipo == ReportesControlador.class) return new ReportesControlador() { public void initialize() {} };
        return new MenuPrincipalControlador() { public void initialize() {} };
    }

    private static Producto producto() {
        Producto producto = new Producto();
        producto.setNombre("Café americano de la casa");
        producto.setNombreCategoria("Bebidas calientes");
        producto.setDescripcion("Café recién preparado, de sabor intenso. Disponible en barra.");
        producto.setPrecio(new BigDecimal("35.50"));
        producto.setDisponible(true);
        return producto;
    }

    private static void comprobarCeldas() {
        Tarjetas.CeldaProducto producto = new Tarjetas.CeldaProducto();
        producto.updateItem(producto(), false);
        comprobar(producto.getGraphic() != null, "No se creó la tarjeta de producto.");
        producto.updateItem(null, true);
        comprobar(producto.getGraphic() == null, "Una celda reciclada no debe mostrar el producto anterior.");

        Tarjetas.CeldaCategoria categoria = new Tarjetas.CeldaCategoria();
        categoria.updateItem(new Categoria(1, "Bebidas", null), false);
        Tarjetas.CeldaCliente cliente = new Tarjetas.CeldaCliente();
        Cliente persona = new Cliente();
        persona.setNombre("Ana López");
        cliente.updateItem(persona, false);
        Tarjetas.CeldaEmpleado empleado = new Tarjetas.CeldaEmpleado();
        Empleado integrante = new Empleado();
        integrante.setNombre("Luis Pérez");
        integrante.setPuesto("Cajero");
        empleado.updateItem(integrante, false);
        Tarjetas.CeldaVenta venta = new Tarjetas.CeldaVenta();
        Venta recibo = new Venta();
        recibo.setFechaVenta(LocalDateTime.now());
        recibo.setTotal(new BigDecimal("71.00"));
        recibo.setEstado("CANCELADA");
        venta.updateItem(recibo, false);
        Tarjetas.CeldaDetalle detalle = new Tarjetas.CeldaDetalle();
        DetalleVenta consumo = new DetalleVenta();
        consumo.setNombreProducto("Café");
        consumo.setCantidad(2);
        consumo.setPrecioUnitario(new BigDecimal("35.50"));
        detalle.updateItem(consumo, false);
        Tarjetas.CeldaReporte reporte = new Tarjetas.CeldaReporte();
        reporte.updateItem(new DatoReporte("Ingresos", new BigDecimal("71.00"), "pesos"), false);
        comprobar(categoria.getGraphic() != null && cliente.getGraphic() != null && empleado.getGraphic() != null
                && venta.getGraphic() != null && detalle.getGraphic() != null && reporte.getGraphic() != null,
                "Todas las entidades deben generar una tarjeta, incluso con datos opcionales nulos.");
    }

    private static void comprobarDimensionesInputs() {
        javafx.scene.control.Control[] campos = {
                new javafx.scene.control.TextField("Nombre"),
                new javafx.scene.control.TextArea("Descripción"),
                new javafx.scene.control.ComboBox<String>(),
                new javafx.scene.control.DatePicker()
        };
        javafx.scene.layout.VBox raiz = new javafx.scene.layout.VBox(campos);
        Scene escena = new Scene(raiz, 500, 500);
        escena.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        escena.getStylesheets().add(VerificacionVistas.class.getResource("/estilos/cafeteria.css").toExternalForm());
        raiz.applyCss();
        raiz.layout();
        for (javafx.scene.control.Control campo : campos) {
            double ancho = campo.prefWidth(-1);
            double alto = campo.prefHeight(-1);
            javafx.geometry.Insets relleno = campo.getInsets();
            campo.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("focused"), true);
            raiz.applyCss();
            raiz.layout();
            comprobar(ancho == campo.prefWidth(-1) && alto == campo.prefHeight(-1)
                    && relleno.equals(campo.getInsets()), "El foco cambia las dimensiones de " + campo.getClass().getSimpleName());
        }
        System.out.println("Inputs: dimensiones y relleno estables al enfocar.");
    }

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }
}
