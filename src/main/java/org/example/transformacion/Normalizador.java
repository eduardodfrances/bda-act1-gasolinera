package org.example.transformacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Convierte el texto crudo del Excel al formato y tipo que espera la base de datos. */
public final class Normalizador {

    private static final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final BigDecimal mil = new BigDecimal("1000");
    private static final String sinRotulo = "SIN RÓTULO";
    private static final String prefijoPrecio = "Precio ";

    private Normalizador() {}

    // Quita espacios, tabuladores y espacios dobles; una celda vacía pasa a null
    public static String texto(String valor) {
        if (valor == null || valor.isBlank()) return null;

        return valor.strip().replaceAll("\\s+", " ");
    }

    // El Excel usa coma decimal
    public static BigDecimal decimal(String valor) {
        String t = texto(valor);
        if (t == null) return null;

        return new BigDecimal(t.replace(",", "."));
    }

    public static LocalDateTime fecha(String valor) {
        String t = texto(valor);
        if (t == null) return null;

        return LocalDateTime.parse(t, formatoFecha);
    }

    // Terrestres trae la letra; marítimos, el texto completo
    public static String tipoVenta(String valor) {
        String t = texto(valor);
        if (t == null) return null;

        return switch (t) {
            case "P", "Suministro a pesqueros" -> "P";
            case "R", "Suministro a barcos deportivos o de recreo" -> "R";
            case "M", "Mixto" -> "M";
            default -> throw new IllegalArgumentException("Tipo de venta desconocido: " + t);
        };
    }

    // La empresa es el rótulo; sin rótulo se agrupa en "SIN RÓTULO" para cumplir el NOT NULL
    public static String empresa(String valor) {
        String t = texto(valor);

        return t == null ? sinRotulo : t.toUpperCase();
    }

    // "Precio gasóleo A" → "Gasóleo A"
    public static String nombreCarburante(String cabecera) {
        String t = texto(cabecera);
        if (t == null) return null;

        if (t.startsWith(prefijoPrecio)) t = t.substring(prefijoPrecio.length());

        return Character.toUpperCase(t.charAt(0)) + t.substring(1);
    }

    // En marítimos, estos dos gasóleos vienen en €/1000 L; se pasan a €/L como el resto
    public static BigDecimal precio(String valor, String carburante, String tipoEstacion) {
        BigDecimal d = decimal(valor);
        if (d == null) return null;

        boolean enMilLitros = tipoEstacion.equals("MARITIMA")
                && (carburante.equals("Gasóleo B") || carburante.equals("Gasóleo de uso marítimo"));

        if (enMilLitros) {
            d = d.divide(mil, 3, RoundingMode.HALF_UP);
        }

        return d;
    }
}