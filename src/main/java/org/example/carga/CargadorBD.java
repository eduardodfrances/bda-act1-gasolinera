package org.example.carga;

import org.example.bd.ConexionBD;
import org.example.modelo.EstacionLeida;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CargadorBD {

    private static final String sqlVaciar =
            "TRUNCATE TABLE precio, estacion, localidad, municipio, empresa, carburante RESTART IDENTITY CASCADE";
    private static final String sqlProvincias =
            "SELECT id_provincia, nombre FROM provincia";
    private static final String sqlCarburante =
            "INSERT INTO carburante (nombre) VALUES (?)";
    private static final String sqlMunicipio =
            "INSERT INTO municipio (nombre, id_provincia) VALUES (?, ?)";
    private static final String sqlLocalidad =
            "INSERT INTO localidad (nombre, id_municipio) VALUES (?, ?)";
    private static final String sqlEmpresa =
            "INSERT INTO empresa (nombre) VALUES (?)";
    private static final String sqlEstacion =
            "INSERT INTO estacion (tipo_estacion, id_empresa, id_localidad, codigo_postal, direccion, margen, "
                    + "latitud, longitud, fecha_actualizacion, horario, tipo_venta, remision, tipo_servicio) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String sqlPrecio =
            "INSERT INTO precio (id_estacion, id_carburante, precio) VALUES (?, ?, ?)";

    private CargadorBD() {}

    public static void cargar(Set<String> carburantes, List<EstacionLeida> estaciones) throws SQLException {
        try (Connection con = ConexionBD.obtener()) {
            con.setAutoCommit(false);

            try {
                vaciar(con);

                Map<String, Integer> idMunicipios = new HashMap<>();
                Map<String, Integer> idLocalidades = new HashMap<>();
                Map<String, Integer> idEmpresas = new HashMap<>();
                Map<String, Integer> idCarburantes = new HashMap<>();
                Map<String, Integer> idProvincias = provincias(con);

                for (String nombre : carburantes) {
                    id(idCarburantes, nombre, con, sqlCarburante, nombre);
                }

                try (PreparedStatement psPrecio = con.prepareStatement(sqlPrecio)) {
                    for (EstacionLeida e : estaciones) {
                        Integer idProvincia = idProvincias.get(e.getProvincia());
                        if (idProvincia == null) {
                            throw new IllegalStateException("Provincia no encontrada: " + e.getProvincia());
                        }

                        int idMunicipio = id(idMunicipios, idProvincia + "|" + e.getMunicipio(),
                                con, sqlMunicipio, e.getMunicipio(), idProvincia);

                        int idLocalidad = id(idLocalidades, idMunicipio + "|" + e.getLocalidad(),
                                con, sqlLocalidad, e.getLocalidad(), idMunicipio);

                        int idEmpresa = id(idEmpresas, e.getEmpresa(),
                                con, sqlEmpresa, e.getEmpresa());

                        int idEstacion = insertar(con, sqlEstacion,
                                e.getTipoEstacion(), idEmpresa, idLocalidad,
                                e.getCodigoPostal(), e.getDireccion(), e.getMargen(),
                                e.getLatitud(), e.getLongitud(), e.getFechaActualizacion(),
                                e.getHorario(), e.getTipoVenta(), e.getRemision(), e.getTipoServicio());

                        for (Map.Entry<String, BigDecimal> entrada : e.getPrecios().entrySet()) {
                            psPrecio.setInt(1, idEstacion);
                            psPrecio.setInt(2, idCarburantes.get(entrada.getKey()));
                            psPrecio.setBigDecimal(3, entrada.getValue());
                            psPrecio.addBatch();
                        }
                    }

                    psPrecio.executeBatch();
                }

                con.commit();

            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    private static void vaciar(Connection con) throws SQLException {
        try (Statement st = con.createStatement()) {
            st.executeUpdate(sqlVaciar);
        }
    }

    private static Map<String, Integer> provincias(Connection con) throws SQLException {
        Map<String, Integer> mapa = new HashMap<>();

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sqlProvincias)) {
            while (rs.next()) {
                mapa.put(rs.getString("nombre"), rs.getInt("id_provincia"));
            }
        }

        return mapa;
    }

    private static int insertar(Connection con, String sql, Object... valores) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int k = 0; k < valores.length; k++) {
                ps.setObject(k + 1, valores[k]);
            }
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                return claves.getInt(1);
            }
        }
    }

    private static int id(Map<String, Integer> cache, String clave,
                          Connection con, String sql, Object... valores) throws SQLException {
        if (cache.containsKey(clave)) {
            return cache.get(clave);
        }

        int nuevo = insertar(con, sql, valores);
        cache.put(clave, nuevo);
        return nuevo;
    }
}