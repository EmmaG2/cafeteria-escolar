package org.fernandogranados.demo.controlador;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.fernandogranados.demo.consultas.Consultas;
import org.fernandogranados.demo.reporteador.DatoReporte;
import org.fernandogranados.demo.reporteador.GeneradorPdfReporte;
import org.fernandogranados.demo.reporteador.GraficaReporte;
import org.fernandogranados.demo.reporteador.ResumenReporte;
import org.fernandogranados.demo.utilidad.FormatoPesos;
import org.jfree.chart.JFreeChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Controlador de la vista {@code Reportes.fxml}, que muestra indicadores, gráficas y exporta el reporte a PDF.
 *
 * <p>Coordina tres partes del sistema: obtiene los datos con {@link Consultas}, genera la gráfica
 * con {@link GraficaReporte} (JFreeChart) y produce el PDF con {@link GeneradorPdfReporte} (iText).
 * Todas las cifras consideran únicamente ventas con estado {@code PAGADA}.</p>
 */
public class ReportesControlador {
    private static Logger logger = LoggerFactory.getLogger(ReportesControlador.class);

    @FXML
    private ComboBox<String> comboTipoReporte;

    @FXML
    private DatePicker selectorFechaInicio;

    @FXML
    private DatePicker selectorFechaFin;

    @FXML
    private ListView<DatoReporte> listaDatos;

    @FXML
    private ImageView imagenGrafica;

    @FXML
    private VBox panelGrafica;

    @FXML
    private Label etiquetaNumeroVentas;

    @FXML
    private Label etiquetaIngresos;

    @FXML
    private Label etiquetaTicketPromedio;

    @FXML
    private Label etiquetaPeriodo;

    private final Consultas consultas = new Consultas();
    private GraficaReporte graficaReporte = new GraficaReporte();
    private GeneradorPdfReporte generadorPdfReporte = new GeneradorPdfReporte();
    private List<DatoReporte> datosActuales;
    private List<DatoReporte> ingresosActuales;
    private List<DatoReporte> productosMasVendidos;
    private List<DatoReporte> productosMenosVendidos;
    private JFreeChart graficaActual;
    private JFreeChart graficaIngresos;
    private ResumenReporte resumenActual;

    /**
     * <h1>Inicializar vista</h1>
     * Prepara la vista y genera un primer reporte de ingresos del mes en curso.
     *
     * <p>El ancho de la imagen de la gráfica se enlaza ({@code bind}) al ancho de su panel, de
     * modo que la gráfica se escala sola cuando cambia el tamaño de la ventana.</p>
     */
    @FXML
    public void initialize() {
        listaDatos.setCellFactory(lista -> new Tarjetas.CeldaReporte());
        imagenGrafica.fitWidthProperty().bind(panelGrafica.widthProperty().subtract(26));

        comboTipoReporte.getItems().addAll("Ingresos por día", "Productos más vendidos", "Ventas por categoría",
                "Ventas por empleado", "Métodos de pago", "Resumen");
        comboTipoReporte.setValue("Ingresos por día");
        selectorFechaInicio.setValue(LocalDate.now().withDayOfMonth(1));
        selectorFechaFin.setValue(LocalDate.now());
        cargarReporte();
    }

    /**
     * <h1>Cargar reporte</h1>
     * Consulta los datos del período elegido y actualiza indicadores, lista y gráfica.
     *
     * <p><b>Integración de JFreeChart con JavaFX:</b> JFreeChart es una biblioteca de Swing, la
     * tecnología gráfica anterior de Java, por lo que no puede colocarse directamente en una
     * ventana JavaFX. La gráfica se dibuja como imagen de Swing ({@code BufferedImage}),
     * {@link SwingFXUtils#toFXImage} la convierte en una imagen de JavaFX y un
     * {@link ImageView} la muestra.</p>
     *
     * <p>Además de los datos del tipo de reporte elegido, se guardan los ingresos por día y los
     * productos más y menos vendidos, porque el PDF siempre los incluye.</p>
     */
    @FXML
    private void cargarReporte() {
        LocalDate fechaInicio = selectorFechaInicio.getValue();
        LocalDate fechaFin = selectorFechaFin.getValue();

        if (fechaInicio == null || fechaFin == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Período obligatorio", "Seleccione la fecha inicial y final.");
            return;
        }

        if (fechaInicio.isAfter(fechaFin)) {
            mostrarMensaje(Alert.AlertType.WARNING, "Fechas inválidas", "La fecha inicial no puede ser posterior a la fecha final.");
            return;
        }

        try {
            String tipoReporte = comboTipoReporte.getValue();
            datosActuales = obtenerDatos(tipoReporte, fechaInicio, fechaFin);
            resumenActual = consultas.obtenerResumen(fechaInicio, fechaFin);
            ingresosActuales = formatearFechas(consultas.ingresosPorDia(fechaInicio, fechaFin));
            productosMasVendidos = consultas.productosMasVendidos(fechaInicio, fechaFin);
            productosMenosVendidos = consultas.productosMenosVendidos(fechaInicio, fechaFin);
            String ingresos = formatearMoneda(resumenActual.getIngresos());
            String ticketPromedio = formatearMoneda(resumenActual.getTicketPromedio());
            etiquetaPeriodo.setText("Del " + fechaInicio + " al " + fechaFin + " · Solo ventas pagadas");
            etiquetaNumeroVentas.setText(String.valueOf(resumenActual.getNumeroVentas()));
            etiquetaIngresos.setText(ingresos);
            etiquetaTicketPromedio.setText(ticketPromedio);
            listaDatos.getItems().setAll(datosActuales);
            graficaIngresos = graficaReporte.crearGraficaBarras("Ingresos por día", "MXN", ingresosActuales);
            graficaActual = "Ingresos por día".equals(tipoReporte) ? graficaIngresos
                    : graficaReporte.crearGraficaBarras(tipoReporte, obtenerEtiquetaValor(tipoReporte), datosActuales);
            imagenGrafica.setImage(SwingFXUtils.toFXImage(graficaActual.createBufferedImage(600, 320), null));
        } catch (SQLException excepcion) {
            logger.error("No fue posible cargar el reporte.", excepcion);
            mostrarMensaje(Alert.AlertType.ERROR, "Error de conexión", "No fue posible cargar el reporte solicitado.");
        }
    }

    /**
     * <h1>Exportar PDF</h1>
     * Pide al usuario dónde guardar el archivo y genera el reporte PDF con los datos ya cargados.
     *
     * <p>{@link FileChooser} abre el diálogo nativo del sistema operativo para guardar archivos.
     * Necesita la ventana propietaria, que se obtiene a partir de cualquier control de la vista,
     * para mostrarse encima de ella. Si el usuario cancela el diálogo, se recibe {@code null} y no
     * se genera nada.</p>
     */
    @FXML
    private void exportarPdf() {
        if (graficaIngresos == null || ingresosActuales == null || resumenActual == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Reporte no disponible", "Genere un reporte antes de exportarlo.");
            return;
        }

        FileChooser selectorArchivo = new FileChooser();
        selectorArchivo.setTitle("Guardar reporte PDF");
        selectorArchivo.setInitialFileName("reporte-cafeteria.pdf");
        selectorArchivo.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo PDF", "*.pdf"));
        Window ventana = listaDatos.getScene().getWindow();
        File archivo = selectorArchivo.showSaveDialog(ventana);

        if (archivo == null) {
            return;
        }

        try {
            String periodo = selectorFechaInicio.getValue() + " a " + selectorFechaFin.getValue();
            generadorPdfReporte.generar(archivo.toPath(), periodo, resumenActual, graficaIngresos,
                    ingresosActuales, productosMasVendidos, productosMenosVendidos);
            mostrarMensaje(Alert.AlertType.INFORMATION, "PDF generado", "El reporte se guardó correctamente.");
        } catch (IOException excepcion) {
            logger.error("No fue posible generar el PDF.", excepcion);
            mostrarMensaje(Alert.AlertType.ERROR, "No se pudo generar", "No fue posible guardar el archivo PDF.");
        }
    }

    /**
     * <h1>Obtener datos del reporte</h1>
     * Ejecuta la consulta que corresponde al tipo de reporte elegido en la lista desplegable.
     *
     * @param tipoReporte Texto de la opción seleccionada, por ejemplo {@code "Ventas por categoría"}.
     * @param fechaInicio Primer día del período, inclusive.
     * @param fechaFin    Último día del período, inclusive.
     * @return Renglones del reporte; si el tipo no se reconoce, los ingresos por día.
     * @throws SQLException si la consulta a la base de datos falla.
     */
    private List<DatoReporte> obtenerDatos(String tipoReporte, LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        switch (tipoReporte) {
            case "Productos más vendidos":
                return consultas.productosMasVendidos(fechaInicio, fechaFin);
            case "Ventas por categoría":
                return consultas.ventasPorCategoria(fechaInicio, fechaFin);
            case "Ventas por empleado":
                return consultas.ventasPorEmpleado(fechaInicio, fechaFin);
            case "Métodos de pago":
                return consultas.ventasPorMetodoPago(fechaInicio, fechaFin);
            case "Resumen":
                return obtenerDatosResumen(fechaInicio, fechaFin);
            default:
                return formatearFechas(consultas.ingresosPorDia(fechaInicio, fechaFin));
        }
    }

    private List<DatoReporte> obtenerDatosResumen(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        ResumenReporte resumen = consultas.obtenerResumen(fechaInicio, fechaFin);
        List<DatoReporte> datos = new ArrayList<>();
        datos.add(new DatoReporte("Número de ventas", BigDecimal.valueOf(resumen.getNumeroVentas()), "ventas"));
        datos.add(new DatoReporte("Ingresos", resumen.getIngresos(), "pesos"));
        datos.add(new DatoReporte("Ticket promedio", resumen.getTicketPromedio(), "pesos"));
        return datos;
    }

    /**
     * <h1>Formatear fechas de la gráfica</h1>
     * Cambia las etiquetas con formato {@code 2026-11-22} por un formato breve en español, como
     * {@code 22 nov}, para que se lean mejor en la gráfica.
     *
     * @param datos Renglones cuya etiqueta es una fecha ISO.
     * @return Nueva lista con las fechas formateadas; una etiqueta que no es fecha se conserva igual.
     */
    private List<DatoReporte> formatearFechas(List<DatoReporte> datos) {
        List<DatoReporte> fechasFormateadas = new ArrayList<>();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd MMM", Locale.forLanguageTag("es-MX"));

        for (DatoReporte dato : datos) {
            try {
                LocalDate fecha = LocalDate.parse(dato.getEtiqueta());
                fechasFormateadas.add(new DatoReporte(fecha.format(formato), dato.getValor(), dato.getDetalle()));
            } catch (RuntimeException excepcion) {
                fechasFormateadas.add(dato);
            }
        }

        return fechasFormateadas;
    }

    private String obtenerEtiquetaValor(String tipoReporte) {
        if ("Productos más vendidos".equals(tipoReporte)) {
            return "Unidades";
        }

        if ("Métodos de pago".equals(tipoReporte)) {
            return "Ventas";
        }

        return "MXN";
    }

    private String formatearMoneda(BigDecimal importe) {
        return FormatoPesos.mostrar(importe);
    }

    private void mostrarMensaje(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}
