package org.fernandogranados.demo.utilidad;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class FormatoPesos {
    private FormatoPesos() {
    }

    public static String mostrar(BigDecimal importe) {
        DecimalFormatSymbols simbolos = DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-MX"));
        DecimalFormat formato = new DecimalFormat("'MXN $'#,##0.00", simbolos);
        return formato.format(importe == null ? BigDecimal.ZERO : importe);
    }
}
