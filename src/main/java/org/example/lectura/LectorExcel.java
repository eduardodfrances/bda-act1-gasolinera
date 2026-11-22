package org.example.lectura;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.example.modelo.EstacionLeida;
import org.example.transformacion.Normalizador;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Lee los .xls del Ministerio y devuelve una EstacionLeida por fila. */
public final class LectorExcel {

    // Filas y columnas cuentan desde 0
    private static final int filaFecha = 0;
    private static final int columnaFecha = 1;
    private static final int filaCabeceras = 3;
    private static final int primeraFilaDatos = 4;

    private static final String colProvincia = "Provincia";
    private static final String colMunicipio = "Municipio";
    private static final String colLocalidad = "Localidad";
    private static final String colCodigoPostal = "Código postal";
    private static final String colDireccion = "Dirección";
    private static final String colMargen = "Margen";
    private static final String colLongitud = "Longitud";
    private static final String colLatitud = "Latitud";
    private static final String colTomaDatos = "Toma de datos";
    private static final String colRotulo = "Rótulo";
    private static final String colTipoVenta = "Tipo venta";
    private static final String colRemision = "Rem.";
    private static final String colHorario = "Horario";
    private static final String colTipoServicio = "Tipo servicio";
    private static final String prefijoPrecio = "Precio";

    private static final DataFormatter formateador = new DataFormatter();

    private LectorExcel() {}

    public static List<EstacionLeida> leer(String ruta, String tipoEstacion) throws IOException {
        try (FileInputStream entrada = new FileInputStream(ruta);
             HSSFWorkbook libro = new HSSFWorkbook(entrada)) {

            Sheet hoja = libro.getSheetAt(0);
            LocalDateTime fechaFichero = Normalizador.fecha(
                    formateador.formatCellValue(hoja.getRow(filaFecha).getCell(columnaFecha)));
            Map<String, Integer> cabeceras = mapaCabeceras(hoja.getRow(filaCabeceras));
            List<EstacionLeida> estaciones = new ArrayList<>();

            for (int i = primeraFilaDatos; i <= hoja.getLastRowNum(); i++) {
                Row fila = hoja.getRow(i);
                if (fila == null) continue;

                // Marítimos no tiene "Toma de datos": se usa la fecha del fichero
                LocalDateTime fechaEstacion = tipoEstacion.equals("TERRESTRE")
                        ? Normalizador.fecha(valor(fila, cabeceras, colTomaDatos))
                        : fechaFichero;

                estaciones.add(EstacionLeida.builder()
                        .tipoEstacion(tipoEstacion)
                        .provincia(valor(fila, cabeceras, colProvincia))
                        .municipio(valor(fila, cabeceras, colMunicipio))
                        .localidad(valor(fila, cabeceras, colLocalidad))
                        .empresa(Normalizador.empresa(valor(fila, cabeceras, colRotulo)))
                        .codigoPostal(valor(fila, cabeceras, colCodigoPostal))
                        .direccion(valor(fila, cabeceras, colDireccion))
                        .margen(valor(fila, cabeceras, colMargen))
                        .latitud(Normalizador.decimal(valor(fila, cabeceras, colLatitud)))
                        .longitud(Normalizador.decimal(valor(fila, cabeceras, colLongitud)))
                        .fechaActualizacion(fechaEstacion)
                        .horario(valor(fila, cabeceras, colHorario))
                        .tipoVenta(Normalizador.tipoVenta(valor(fila, cabeceras, colTipoVenta)))
                        .remision(valor(fila, cabeceras, colRemision))
                        .tipoServicio(valor(fila, cabeceras, colTipoServicio))
                        .precios(precios(fila, cabeceras, tipoEstacion))
                        .build());
            }

            return estaciones;
        }
    }

    // Nombres de carburante sacados de las cabeceras, para incluir también los que no tienen precios
    public static Set<String> carburantes(String ruta) throws IOException {
        try (FileInputStream entrada = new FileInputStream(ruta);
             HSSFWorkbook libro = new HSSFWorkbook(entrada)) {

            Map<String, Integer> cabeceras = mapaCabeceras(libro.getSheetAt(0).getRow(filaCabeceras));
            Set<String> nombres = new HashSet<>();

            for (String cabecera : cabeceras.keySet()) {
                if (cabecera.startsWith(prefijoPrecio)) {
                    nombres.add(Normalizador.nombreCarburante(cabecera));
                }
            }

            return nombres;
        }
    }

    // Nombre de columna → posición. Las columnas no están en el mismo sitio en los dos ficheros
    private static Map<String, Integer> mapaCabeceras(Row filaCabecera) {
        Map<String, Integer> mapa = new HashMap<>();

        for (Cell c : filaCabecera) {
            String nombre = Normalizador.texto(formateador.formatCellValue(c));
            if (nombre != null) {
                mapa.put(nombre, c.getColumnIndex());
            }
        }

        return mapa;
    }

    // Devuelve null si la columna no existe en ese fichero (por ejemplo, Margen en marítimos)
    private static String valor(Row fila, Map<String, Integer> cabeceras, String columna) {
        Integer pos = cabeceras.get(columna);
        if (pos == null) return null;

        Cell celda = fila.getCell(pos);
        if (celda == null) return null;

        return Normalizador.texto(formateador.formatCellValue(celda));
    }

    // Solo se guardan los carburantes con precio en esa fila
    private static Map<String, BigDecimal> precios(Row fila, Map<String, Integer> cabeceras, String tipoEstacion) {
        Map<String, BigDecimal> resultado = new HashMap<>();

        for (String cabecera : cabeceras.keySet()) {
            if (!cabecera.startsWith(prefijoPrecio)) continue;

            String carburante = Normalizador.nombreCarburante(cabecera);
            BigDecimal precio = Normalizador.precio(valor(fila, cabeceras, cabecera), carburante, tipoEstacion);

            if (precio != null) {
                resultado.put(carburante, precio);
            }
        }

        return resultado;
    }
}