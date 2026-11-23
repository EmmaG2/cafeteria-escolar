package org.fernandogranados.demo.controlador;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.fernandogranados.demo.modelo.Categoria;
import org.fernandogranados.demo.modelo.Cliente;
import org.fernandogranados.demo.modelo.DetalleVenta;
import org.fernandogranados.demo.modelo.Empleado;
import org.fernandogranados.demo.modelo.Producto;
import org.fernandogranados.demo.modelo.Venta;
import org.fernandogranados.demo.reporteador.DatoReporte;
import org.fernandogranados.demo.utilidad.FormatoPesos;
import org.kordamp.ikonli.javafx.FontIcon;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Reúne las celdas personalizadas que dibujan cada registro como una tarjeta dentro de un {@code ListView}.
 *
 * <p><b>Cómo dibuja JavaFX una lista:</b> un {@code ListView} no crea un control por cada
 * elemento de la lista. Crea solo las celdas ({@link ListCell}) que caben en pantalla y las
 * <i>recicla</i> al desplazarse: la misma celda que mostraba el producto 1 pasa a mostrar el
 * producto 30. Por eso cada celda redefine {@code updateItem}, que JavaFX invoca cada vez que
 * le asigna un dato distinto o la deja vacía.</p>
 *
 * <p>Esta clase solo se encarga de la presentación; los datos y la selección los manejan los
 * controladores. Es {@code final} y tiene constructor privado porque únicamente agrupa clases
 * internas y métodos estáticos, y no debe instanciarse.</p>
 */
public final class Tarjetas {
    private Tarjetas() {
    }

    private static String moneda(BigDecimal importe) {
        return FormatoPesos.mostrar(importe);
    }

    private static String textoOpcional(String texto) {
        return texto == null || texto.isBlank() ? "No registrado" : texto;
    }

    /**
     * <h1>Colocar tarjeta en la celda</h1>
     * Coloca la tarjeta dentro de la celda y ajusta su ancho al de la lista.
     *
     * <p>{@code bind} crea un <i>enlace de propiedades</i>: el ancho de la tarjeta queda atado al
     * ancho de la celda menos 20 píxeles y se recalcula solo cuando el usuario redimensiona la
     * ventana. Si la tarjeta es {@code null}, la celda queda en blanco, lo cual es necesario
     * para que una celda reciclada no siga mostrando el registro anterior.</p>
     *
     * @param celda   Celda de la lista que mostrará la tarjeta.
     * @param tarjeta Tarjeta construida con {@link #crear}, o {@code null} para vaciar la celda.
     */
    private static void mostrar(ListCell<?> celda, HBox tarjeta) {
        if (tarjeta != null) {
            tarjeta.prefWidthProperty().bind(celda.widthProperty().subtract(20));
        }
        celda.setGraphic(tarjeta);
    }

    /**
     * <h1>Crear tarjeta</h1>
     * Construye la tarjeta visual con una estructura común a todos los registros.
     *
     * <p>La tarjeta es un {@link HBox} (contenedor horizontal) con tres partes: un emblema con
     * icono de Ikonli, un {@link VBox} (contenedor vertical) con los textos y un resumen a la
     * derecha con importe e insignia de estado. Los estilos se asignan con
     * {@code getStyleClass().add(...)}, que funciona igual que el atributo {@code class} de HTML
     * y conecta cada elemento con las reglas de {@code cafeteria.css}.</p>
     *
     * <p>Cuando un texto opcional viene vacío, su etiqueta se oculta con {@code setVisible(false)}
     * y además {@code setManaged(false)}, para que no reserve espacio en el diseño.</p>
     *
     * @param icono        Código del icono de Font Awesome, por ejemplo {@code "fas-coffee"}.
     * @param titulo       Texto principal de la tarjeta.
     * @param contexto     Texto pequeño superior, por ejemplo la categoría o el identificador.
     * @param detalle      Descripción secundaria; se oculta si está vacía.
     * @param valor        Importe que se muestra a la derecha; se oculta si está vacío.
     * @param estado       Texto de la insignia de estado; se oculta si está vacío.
     * @param estiloEstado Clase CSS que da color a la insignia, por ejemplo {@code "estado-positivo"}.
     * @return Contenedor listo para colocarse en una celda.
     */
    private static HBox crear(String icono, String titulo, String contexto, String detalle,
                              String valor, String estado, String estiloEstado) {
        StackPane emblema = new StackPane(new FontIcon(icono));
        emblema.getStyleClass().add("emblema-tarjeta");
        Label nombre = new Label(titulo);
        nombre.getStyleClass().add("nombre-tarjeta");
        nombre.setWrapText(true);
        Label subtitulo = new Label(contexto);
        subtitulo.getStyleClass().add("contexto-tarjeta");
        subtitulo.setWrapText(true);
        Label descripcion = new Label(detalle);
        descripcion.getStyleClass().add("descripcion-tarjeta");
        descripcion.setWrapText(true);
        descripcion.setVisible(detalle != null && !detalle.isBlank());
        descripcion.setManaged(descripcion.isVisible());
        VBox informacion = new VBox(6, subtitulo, nombre, descripcion);
        informacion.setMinWidth(0);
        HBox.setHgrow(informacion, Priority.ALWAYS);

        Label importe = new Label(valor);
        importe.getStyleClass().add("importe-tarjeta");
        importe.setVisible(valor != null && !valor.isBlank());
        importe.setManaged(importe.isVisible());
        Label insignia = new Label(estado);
        insignia.getStyleClass().addAll("insignia-tarjeta", estiloEstado);
        insignia.setVisible(estado != null && !estado.isBlank());
        insignia.setManaged(insignia.isVisible());
        VBox resumen = new VBox(8, importe, insignia);
        resumen.getStyleClass().add("resumen-tarjeta");
        resumen.setMinWidth(VBox.USE_PREF_SIZE);
        HBox tarjeta = new HBox(16, emblema, informacion, resumen);
        tarjeta.getStyleClass().add("registro-tarjeta");
        tarjeta.setMaxWidth(Double.MAX_VALUE);
        return tarjeta;
    }

    /**
     * Celda que dibuja una categoría. Las demás celdas de esta clase siguen el mismo esquema y
     * solo cambian los datos que muestran.
     */
    public static class CeldaCategoria extends ListCell<Categoria> {
        /**
         * <h1>Dibujar celda</h1>
         * Actualiza el contenido de la celda cuando JavaFX le asigna otro dato.
         *
         * <p>Siempre debe llamarse primero a {@code super.updateItem}, porque la clase base
         * actualiza el estado interno de la celda. {@code setText(null)} evita que JavaFX
         * muestre además el texto de {@code toString()} del modelo.</p>
         *
         * @param dato  Categoría que debe mostrar la celda.
         * @param vacia {@code true} si la celda no corresponde a ningún elemento de la lista.
         */
        @Override
        protected void updateItem(Categoria dato, boolean vacia) {
            super.updateItem(dato, vacia);
            setText(null);
            mostrar(this, vacia || dato == null ? null : crear("fas-tags", dato.getNombre(),
                    "CATEGORÍA · " + dato.getIdCategoria(), textoOpcional(dato.getDescripcion()),
                    "", "", "estado-neutro"));
        }
    }

    public static class CeldaProducto extends ListCell<Producto> {
        @Override
        protected void updateItem(Producto dato, boolean vacia) {
            super.updateItem(dato, vacia);
            setText(null);
            mostrar(this, vacia || dato == null ? null : crear("fas-coffee", dato.getNombre(),
                    dato.getNombreCategoria(), textoOpcional(dato.getDescripcion()), moneda(dato.getPrecio()),
                    dato.isDisponible() ? "Disponible" : "No disponible",
                    dato.isDisponible() ? "estado-positivo" : "estado-neutro"));
        }
    }

    public static class CeldaCliente extends ListCell<Cliente> {
        @Override
        protected void updateItem(Cliente dato, boolean vacia) {
            super.updateItem(dato, vacia);
            setText(null);
            mostrar(this, vacia || dato == null ? null : crear("fas-user", dato.getNombre(),
                    "CLIENTE · " + dato.getIdCliente(),
                    "Tel. " + textoOpcional(dato.getTelefono()) + "\nCorreo: " + textoOpcional(dato.getCorreo()),
                    "", "", "estado-neutro"));
        }
    }

    public static class CeldaEmpleado extends ListCell<Empleado> {
        @Override
        protected void updateItem(Empleado dato, boolean vacia) {
            super.updateItem(dato, vacia);
            setText(null);
            mostrar(this, vacia || dato == null ? null : crear("fas-user-tie", dato.getNombre(),
                    "EQUIPO · " + dato.getIdEmpleado(), dato.getPuesto(), "",
                    dato.isActivo() ? "Activo" : "Inactivo",
                    dato.isActivo() ? "estado-positivo" : "estado-neutro"));
        }
    }

    public static class CeldaVenta extends ListCell<Venta> {
        @Override
        protected void updateItem(Venta dato, boolean vacia) {
            super.updateItem(dato, vacia);
            setText(null);
            if (vacia || dato == null) {
                setGraphic(null);
                return;
            }
            String cliente = dato.getNombreCliente() == null ? "Sin cliente" : dato.getNombreCliente();
            mostrar(this, crear("fas-receipt", "Venta #" + dato.getIdVenta(),
                    dato.getFechaVenta().format(DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm", Locale.forLanguageTag("es-MX"))),
                    cliente + " · " + dato.getMetodoPago() + "\nAtendió: " + dato.getNombreEmpleado(),
                    moneda(dato.getTotal()), dato.getEstado(),
                    "PAGADA".equals(dato.getEstado()) ? "estado-positivo" : "estado-negativo"));
        }
    }

    /**
     * Celda que dibuja un renglón de reporte. El valor se muestra como moneda solo cuando el
     * detalle indica que representa pesos o ingresos; en otro caso, como cantidad sin decimales sobrantes.
     */
    public static class CeldaReporte extends ListCell<DatoReporte> {
        @Override
        protected void updateItem(DatoReporte dato, boolean vacia) {
            super.updateItem(dato, vacia);
            setText(null);
            if (vacia || dato == null) {
                setGraphic(null);
                return;
            }
            boolean esImporte = "pesos".equalsIgnoreCase(dato.getDetalle())
                    || "ingresos".equalsIgnoreCase(dato.getDetalle());
            String valor = esImporte ? moneda(dato.getValor()) : dato.getValor().stripTrailingZeros().toPlainString();
            mostrar(this, crear("fas-chart-bar", dato.getEtiqueta(), dato.getDetalle(), "",
                    valor, "", "estado-neutro"));
        }
    }

    /**
     * Celda que dibuja un producto dentro de una venta. Se reutiliza en el carrito de la pantalla
     * de ventas y en el detalle del historial; en ambos casos muestra el precio unitario guardado
     * en el detalle, no el precio actual del producto.
     */
    public static class CeldaDetalle extends ListCell<DetalleVenta> {
        @Override
        protected void updateItem(DetalleVenta dato, boolean vacia) {
            super.updateItem(dato, vacia);
            setText(null);
            mostrar(this, vacia || dato == null ? null : crear("fas-shopping-bag", dato.getNombreProducto(),
                    "DETALLE DE CONSUMO", dato.getCantidad() + " × " + moneda(dato.getPrecioUnitario()) + " por unidad",
                    moneda(dato.getImporte()), dato.getCantidad() + " uds.", "estado-neutro"));
        }
    }
}
