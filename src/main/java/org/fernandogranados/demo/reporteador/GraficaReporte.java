package org.fernandogranados.demo.reporteador;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

/**
 * Construye las gráficas de barras de los reportes con la biblioteca JFreeChart.
 *
 * <p>El objeto {@link JFreeChart} resultante no depende de JavaFX ni de PDF: la misma gráfica
 * se convierte en imagen para la pantalla de reportes y en PNG para el documento PDF.</p>
 */
public class GraficaReporte {
    private static final Color FONDO = Color.decode("#FFFFFF");
    private static final Color TEXTO = Color.decode("#2E2723");
    private static final Color LINEA = Color.decode("#D8CEC5");
    private static final Color CAFE = Color.decode("#7B3C23");

    /**
     * <h1>Crear gráfica de barras</h1>
     * Crea una gráfica de barras verticales con los colores del sistema.
     *
     * <p>JFreeChart separa los datos de su representación: los valores se cargan en un
     * {@link DefaultCategoryDataset}, {@link ChartFactory} crea la gráfica, y después se
     * personalizan el área de dibujo ({@link CategoryPlot}), las barras ({@link BarRenderer}) y
     * los ejes. Cuando la serie representa dinero, el eje vertical se formatea como pesos mexicanos.</p>
     *
     * @param titulo        Título que aparece encima de la gráfica.
     * @param etiquetaValor Nombre de la serie y del eje vertical: {@code "MXN"}, {@code "Unidades"} o {@code "Ventas"}.
     * @param datos         Renglones del reporte; cada uno se convierte en una barra.
     * @return La gráfica configurada, lista para convertirse en imagen.
     */
    public JFreeChart crearGraficaBarras(String titulo, String etiquetaValor, List<DatoReporte> datos) {
        DefaultCategoryDataset conjuntoDatos = new DefaultCategoryDataset();

        for (DatoReporte dato : datos) {
            conjuntoDatos.addValue(dato.getValor(), etiquetaValor, dato.getEtiqueta());
        }

        JFreeChart grafica = ChartFactory.createBarChart(titulo, "", etiquetaValor, conjuntoDatos,
                PlotOrientation.VERTICAL, false, true, false);
        grafica.setBackgroundPaint(FONDO);
        grafica.getTitle().setFont(new Font("SansSerif", Font.BOLD, 16));
        grafica.getTitle().setPaint(TEXTO);

        CategoryPlot area = grafica.getCategoryPlot();
        area.setBackgroundPaint(FONDO);
        area.setRangeGridlinePaint(LINEA);
        area.setOutlineVisible(false);

        BarRenderer barras = (BarRenderer) area.getRenderer();
        barras.setSeriesPaint(0, CAFE);
        barras.setDrawBarOutline(false);
        barras.setItemMargin(0.16);

        CategoryAxis ejeCategorias = area.getDomainAxis();
        ejeCategorias.setLabelPaint(TEXTO);
        ejeCategorias.setTickLabelPaint(TEXTO);
        ejeCategorias.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 11));
        ejeCategorias.setAxisLinePaint(LINEA);
        ejeCategorias.setTickMarkPaint(LINEA);

        ValueAxis ejeValores = area.getRangeAxis();
        if ("MXN".equals(etiquetaValor)) {
            DecimalFormat formato = new DecimalFormat("'MXN $'#,##0", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-MX")));
            ((NumberAxis) ejeValores).setNumberFormatOverride(formato);
        }
        ejeValores.setLabelPaint(TEXTO);
        ejeValores.setTickLabelPaint(TEXTO);
        ejeValores.setAxisLinePaint(LINEA);
        ejeValores.setTickMarkPaint(LINEA);

        return grafica;
    }
}
