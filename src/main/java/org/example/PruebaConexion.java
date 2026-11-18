package org.example;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class PruebaConexion {
    public static void main(String[] args) throws Exception {
        Properties config = new Properties();

        try (InputStream entrada = PruebaConexion.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (entrada == null) {
                throw new IllegalStateException("No encuentro db.properties");
            }
            config.load(entrada);
        }

        String url = config.getProperty("db.url");
        String user = config.getProperty("db.user");
        String password = config.getProperty("db.password");

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT version()")) {
            if (rs.next()) {
                System.out.println("Conectado: " + rs.getString(1));
            }
        }
    }
}