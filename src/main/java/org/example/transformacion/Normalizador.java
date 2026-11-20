package org.example.transformacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Normalizador {

    private static final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final BigDecimal mil = new BigDecimal("1000");
    private static final String sinRotulo = "SIN RÓTULO";
    private static final String prefijoPrecio = "Precio ";

    private Normalizador() {}

    public static String texto(String valor) {
        if (valor == null || valor.isBlank()) return null;

        return valor.strip().replaceAll("\\s+", " ");
    }

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

    public static String empresa(String valor) {
        String t = texto(valor);

        return t == null ? sinRotulo : t.toUpperCase();
    }

    public static String nombreCarburante(String cabecera) {
        String t = texto(cabecera);
        if (t == null) return null;

        if (t.startsWith(prefijoPrecio)) t = t.substring(prefijoPrecio.length());

        return Character.toUpperCase(t.charAt(0)) + t.substring(1);
    }

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