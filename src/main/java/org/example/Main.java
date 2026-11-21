package org.example;

import org.example.bd.ConexionBD;
import org.example.carga.CargadorBD;
import org.example.lectura.LectorExcel;
import org.example.modelo.EstacionLeida;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Main {

    public static void main(String[] args) throws IOException, SQLException {
        long inicio = System.currentTimeMillis();

        Set<String> carburantes = new TreeSet<>();
        carburantes.addAll(LectorExcel.carburantes(ConexionBD.rutaTerrestres()));
        carburantes.addAll(LectorExcel.carburantes(ConexionBD.rutaMaritimos()));

        List<EstacionLeida> estaciones = new ArrayList<>();
        estaciones.addAll(LectorExcel.leer(ConexionBD.rutaTerrestres(), "TERRESTRE"));
        estaciones.addAll(LectorExcel.leer(ConexionBD.rutaMaritimos(), "MARITIMA"));

        System.out.println("Carburantes: " + carburantes.size() + " (esperado 25)");
        System.out.println("Estaciones leídas: " + estaciones.size() + " (esperado 11673)");

        CargadorBD.cargar(carburantes, estaciones);

        long segundos = (System.currentTimeMillis() - inicio) / 1000;
        System.out.println("Carga completada en " + segundos + " s");
    }
}