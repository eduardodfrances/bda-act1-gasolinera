package org.example.bd;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Lee db.properties y abre conexiones con PostgreSQL. */
public final class ConexionBD {

    private static final Properties propiedades = cargar();

    private ConexionBD() {}

    private static Properties cargar() {
        Properties config = new Properties();

        try (InputStream entrada = ConexionBD.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (entrada == null) {
                throw new IllegalStateException("No se encuentra db.properties en src/main/resources");
            }
            config.load(entrada);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer db.properties", e);
        }

        return config;
    }

    // Falla en el momento si falta una clave, en vez de devolver null y romper más tarde
    private static String leer(String clave) {
        String valor = propiedades.getProperty(clave);

        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta la clave " + clave + " en db.properties");
        }

        return valor;
    }

    public static Connection obtener() throws SQLException {
        String url = leer("db.url");
        String user = leer("db.user");
        String password = leer("db.password");

        return DriverManager.getConnection(url, user, password);
    }

    public static String rutaTerrestres() {
        return leer("ruta.terrestres");
    }

    public static String rutaMaritimos() {
        return leer("ruta.maritimos");
    }
}