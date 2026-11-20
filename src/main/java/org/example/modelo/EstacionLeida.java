package org.example.modelo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class EstacionLeida {

    private String tipoEstacion;

    private String provincia;
    private String municipio;
    private String localidad;

    private String empresa;

    private String codigoPostal;
    private String direccion;
    private String margen;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private LocalDateTime fechaActualizacion;
    private String horario;
    private String tipoVenta;
    private String remision;
    private String tipoServicio;

    private Map<String, BigDecimal> precios;
}