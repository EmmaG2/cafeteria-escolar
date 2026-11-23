package org.fernandogranados.demo.reporteador;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.fernandogranados.demo.utilidad.FormatoPesos;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Genera el reporte de ventas en formato PDF con la biblioteca iText.
 *
 * <p>iText organiza un PDF en tres capas: {@link PdfWriter} escribe los bytes en el archivo,
 * {@link PdfDocument} representa la estructura del PDF y {@link Document} permite agregar
 * contenido de alto nivel (párrafos, tablas e imágenes) que se acomoda solo en las páginas.</p>
 */
public class GeneradorPdfReporte {
    private static final DeviceRgb CAFE = new DeviceRgb(123, 60, 35);
    private static final DeviceRgb VERDE = new DeviceRgb(47, 117, 91);
    private static final DeviceRgb FONDO_SUAVE = new DeviceRgb(244, 238, 231);
    private static final DeviceRgb TEXTO = new DeviceRgb(46, 39, 35);
    private static final DeviceRgb TEXTO_SECUNDARIO = new DeviceRgb(110, 98, 91);

    /**
     * <h1>Generar archivo PDF</h1>
     * Crea el archivo PDF con encabezado, indicadores, gráfica de ingresos y tablas de productos.
     *
     * <p>La gráfica de JFreeChart se escribe primero como imagen PNG en memoria
     * ({@link ByteArrayOutputStream}) y después se inserta en el documento. El bloque
     * try-with-resources cierra el documento al terminar; ese cierre es el que termina de escribir
     * el archivo en disco.</p>
     *
     * @param archivo                Ruta donde se guardará el PDF; si ya existe, se reemplaza.
     * @param periodo                Texto del período reportado, por ejemplo {@code "2026-11-01 a 2026-11-22"}.
     * @param resumen                Número de ventas, ingresos y ticket promedio del período.
     * @param graficaIngresos        Gráfica de ingresos por día que se incluirá como imagen.
     * @param ingresos               Ingresos por día; se usan para decidir el mensaje del pie de página.
     * @param productosMasVendidos   Productos ordenados de mayor a menor venta; se muestran los primeros cinco.
     * @param productosMenosVendidos Productos ordenados de menor a mayor venta; se muestran los primeros cinco.
     * @throws IOException si el archivo no puede escribirse, por ejemplo porque está abierto en otro programa.
     */
    public void generar(Path archivo, String periodo, ResumenReporte resumen, JFreeChart graficaIngresos,
                        List<DatoReporte> ingresos, List<DatoReporte> productosMasVendidos,
                        List<DatoReporte> productosMenosVendidos) throws IOException {
        try (ByteArrayOutputStream imagenGrafica = new ByteArrayOutputStream();
             PdfWriter escritor = new PdfWriter(archivo.toString());
             PdfDocument pdf = new PdfDocument(escritor);
             Document documento = new Document(pdf)) {

            PdfFont fuente = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fuenteNegrita = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            documento.setMargins(34, 40, 38, 40);
            ChartUtils.writeChartAsPNG(imagenGrafica, graficaIngresos, 700, 400);

            agregarEncabezado(documento, periodo, fuente, fuenteNegrita);
            agregarResumen(documento, resumen, fuente, fuenteNegrita);
            agregarGraficaIngresos(documento, imagenGrafica.toByteArray(), fuente, fuenteNegrita);
            agregarTablaProductos(documento, "Productos más vendidos", "Mayor número de unidades vendidas", productosMasVendidos,
                    fuente, fuenteNegrita);
            agregarTablaProductos(documento, "Productos con menor rotación", "Productos sin ventas o con menos unidades vendidas", productosMenosVendidos,
                    fuente, fuenteNegrita);
            agregarPie(documento, ingresos, fuente);
        }
    }

    private void agregarEncabezado(Document documento, String periodo, PdfFont fuente, PdfFont fuenteNegrita) {
        Table encabezado = new Table(1).useAllAvailableWidth();
        Paragraph titulo = new Paragraph("Reporte de ventas e ingresos")
                .setFont(fuenteNegrita).setFontSize(24).setFontColor(TEXTO).setMarginTop(5).setMarginBottom(5);
        Paragraph corte = new Paragraph("Período contable: " + periodo)
                .setFont(fuente).setFontSize(10).setFontColor(TEXTO_SECUNDARIO).setMargin(0);
        encabezado.addCell(new Cell().add(titulo).add(corte).setPadding(0).setBorder(null));
        documento.add(encabezado);
    }

    private void agregarResumen(Document documento, ResumenReporte resumen, PdfFont fuente, PdfFont fuenteNegrita) {
        Table tabla = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1})).useAllAvailableWidth().setMarginTop(16);
        agregarIndicador(tabla, "INGRESOS NETOS", FormatoPesos.mostrar(resumen.getIngresos()), fuente, fuenteNegrita, CAFE);
        agregarIndicador(tabla, "VENTAS PAGADAS", String.valueOf(resumen.getNumeroVentas()), fuente, fuenteNegrita, VERDE);
        agregarIndicador(tabla, "TICKET PROMEDIO", FormatoPesos.mostrar(resumen.getTicketPromedio()), fuente, fuenteNegrita, CAFE);
        documento.add(tabla);
    }

    private void agregarIndicador(Table tabla, String etiqueta, String valor, PdfFont fuente, PdfFont fuenteNegrita, DeviceRgb color) {
        Paragraph titulo = new Paragraph(etiqueta).setFont(fuente).setFontSize(8).setFontColor(TEXTO_SECUNDARIO).setMargin(0);
        Paragraph importe = new Paragraph(valor).setFont(fuenteNegrita).setFontSize(15).setFontColor(color).setMarginTop(5).setMarginBottom(0);
        tabla.addCell(new Cell().add(titulo).add(importe).setBackgroundColor(FONDO_SUAVE).setPadding(12));
    }

    private void agregarGraficaIngresos(Document documento, byte[] imagenGrafica, PdfFont fuente, PdfFont fuenteNegrita) {
        documento.add(new Paragraph("Ingresos diarios")
                .setFont(fuenteNegrita).setFontSize(14).setFontColor(TEXTO).setMarginTop(20).setMarginBottom(3));
        documento.add(new Paragraph("Cada barra representa los ingresos de ventas pagadas; las fechas se muestran como día y mes.")
                .setFont(fuente).setFontSize(9).setFontColor(TEXTO_SECUNDARIO).setMarginBottom(8));
        Image imagen = new Image(ImageDataFactory.create(imagenGrafica));
        imagen.scaleToFit(515, 294);
        imagen.setHorizontalAlignment(HorizontalAlignment.CENTER);
        documento.add(imagen);
    }

    private void agregarTablaProductos(Document documento, String titulo, String descripcion, List<DatoReporte> productos,
                                       PdfFont fuente, PdfFont fuenteNegrita) {
        documento.add(new Paragraph(titulo).setFont(fuenteNegrita).setFontSize(14).setFontColor(VERDE)
                .setMarginTop(20).setMarginBottom(3));
        documento.add(new Paragraph(descripcion).setFont(fuente).setFontSize(9).setFontColor(TEXTO_SECUNDARIO)
                .setMarginBottom(8));

        Table tabla = new Table(UnitValue.createPercentArray(new float[]{4, 2, 2})).useAllAvailableWidth();
        agregarEncabezadoTabla(tabla, "Producto", TextAlignment.LEFT, fuenteNegrita);
        agregarEncabezadoTabla(tabla, "Unidades", TextAlignment.RIGHT, fuenteNegrita);
        agregarEncabezadoTabla(tabla, "Ingreso", TextAlignment.RIGHT, fuenteNegrita);

        int limite = Math.min(productos.size(), 5);
        for (int indice = 0; indice < limite; indice++) {
            DatoReporte producto = productos.get(indice);
            boolean fondoAlterno = indice % 2 != 0;
            agregarCelda(tabla, producto.getEtiqueta(), TextAlignment.LEFT, fondoAlterno, fuente);
            agregarCelda(tabla, producto.getValor().stripTrailingZeros().toPlainString(), TextAlignment.RIGHT, fondoAlterno, fuente);
            agregarCelda(tabla, obtenerIngreso(producto), TextAlignment.RIGHT, fondoAlterno, fuenteNegrita);
        }

        if (limite == 0) {
            tabla.addCell(new Cell(1, 3).add(new Paragraph("Sin movimientos en el período.").setFont(fuente).setFontSize(9))
                    .setTextAlignment(TextAlignment.CENTER).setPadding(10));
        }
        documento.add(tabla);
    }

    /**
     * <h1>Obtener importe del producto</h1>
     * Extrae el importe del texto de detalle de un producto.
     *
     * <p>La consulta de productos vendidos guarda el detalle como
     * {@code "unidades · $1,250.00"}; este método devuelve solo la parte posterior al punto medio.</p>
     *
     * @param producto Renglón del reporte de productos.
     * @return El importe formateado, o el detalle completo si no contiene el separador.
     */
    private String obtenerIngreso(DatoReporte producto) {
        String detalle = producto.getDetalle();
        int posicion = detalle.indexOf("·");
        return posicion >= 0 ? detalle.substring(posicion + 1).trim() : detalle;
    }

    private void agregarEncabezadoTabla(Table tabla, String texto, TextAlignment alineacion, PdfFont fuente) {
        Paragraph contenido = new Paragraph(texto).setFont(fuente).setFontSize(9).setFontColor(ColorConstants.WHITE).setMargin(0);
        tabla.addHeaderCell(new Cell().add(contenido).setTextAlignment(alineacion).setBackgroundColor(VERDE).setPadding(8));
    }

    private void agregarCelda(Table tabla, String texto, TextAlignment alineacion, boolean fondoAlterno, PdfFont fuente) {
        Cell celda = new Cell().add(new Paragraph(texto).setFont(fuente).setFontSize(9).setFontColor(TEXTO).setMargin(0))
                .setTextAlignment(alineacion).setPadding(7);
        if (fondoAlterno) {
            celda.setBackgroundColor(FONDO_SUAVE);
        }
        tabla.addCell(celda);
    }

    private void agregarPie(Document documento, List<DatoReporte> ingresos, PdfFont fuente) {
        String mensaje = ingresos.isEmpty() ? "No se registraron ingresos pagados en este período."
                : "Importes expresados en pesos mexicanos (MXN). Se excluyen ventas canceladas.";
        documento.add(new Paragraph(mensaje).setFont(fuente).setFontSize(8).setFontColor(TEXTO_SECUNDARIO).setMarginTop(16));
    }
}
