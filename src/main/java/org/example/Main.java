package org.example;

import org.example.bd.ConexionBD;
import org.example.lectura.LectorExcel;
import org.example.modelo.EstacionLeida;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws IOException {
        List<EstacionLeida> terrestres = LectorExcel.leer(ConexionBD.rutaTerrestres(), "TERRESTRE");
        List<EstacionLeida> maritimas = LectorExcel.leer(ConexionBD.rutaMaritimos(), "MARITIMA");

        System.out.println("Primera terrestre: " + terrestres.get(0));
        System.out.println("Primera marítima: " + maritimas.get(0));
        System.out.println();

        System.out.println("Estaciones terrestres: " + terrestres.size() + " (esperado 11535)");
        System.out.println("Estaciones marítimas: " + maritimas.size() + " (esperado 138)");

        int preciosTerrestres = 0;
        int preciosMaritimos = 0;
        Set<String> carburantes = new HashSet<>();

        for (EstacionLeida e : terrestres) {
            preciosTerrestres += e.getPrecios().size();
            carburantes.addAll(e.getPrecios().keySet());
        }

        int sinRotulo = 0;
        BigDecimal maxGasoleoB = null;

        for (EstacionLeida e : maritimas) {
            preciosMaritimos += e.getPrecios().size();
            carburantes.addAll(e.getPrecios().keySet());

            if (e.getEmpresa().equals("SIN RÓTULO")) {
                sinRotulo++;
            }

            BigDecimal gasoleoB = e.getPrecios().get("Gasóleo B");
            if (gasoleoB != null && (maxGasoleoB == null || gasoleoB.compareTo(maxGasoleoB) > 0)) {
                maxGasoleoB = gasoleoB;
            }
        }

        System.out.println("Precios terrestres: " + preciosTerrestres + " (esperado 43071)");
        System.out.println("Precios marítimos: " + preciosMaritimos + " (esperado 254)");
        System.out.println("Carburantes distintos: " + carburantes.size() + " (esperado 23)");
        System.out.println("Marítimas SIN RÓTULO: " + sinRotulo + " (esperado 5)");
        System.out.println("Máximo gasóleo B marítimo: " + maxGasoleoB + " (esperado 1.575)");

    }
}