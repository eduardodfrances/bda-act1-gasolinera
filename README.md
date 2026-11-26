# Base de datos de precios de carburantes

Carga en PostgreSQL de los precios de carburantes publicados por el Ministerio para la Transición Ecológica (geoportalgasolineras.es), de estaciones terrestres y postes marítimos, mediante una aplicación Java con JDBC.

## Requisitos

- Docker Desktop
- JDK 25
- Maven (o IntelliJ IDEA, que lo incluye)
- Un cliente SQL para ejecutar los scripts (DBeaver o `psql`)

## Estructura

```
docker/          docker-compose.yml con PostgreSQL 18
sql/             scripts SQL (esquema, datos de referencia, vaciado, consultas y comprobación)
docs/            análisis de datos, justificación del diseño, cambios al modelo y diagrama E-R
data/            ficheros Excel del Ministerio
src/main/java/org/example/
    bd/              ConexionBD: configuración y conexión
    lectura/         LectorExcel: lectura de los .xls
    transformacion/  Normalizador: limpieza y conversión de los datos
    modelo/          EstacionLeida: fila ya normalizada
    carga/           CargadorBD: inserción en la base de datos
    Main.java        punto de entrada de la carga
```

## Puesta en marcha

1. **Arrancar PostgreSQL**

   ```
   cd docker
   docker compose up -d
   ```

   Crea la base de datos `gasolineras` en `localhost:5432` (usuario `app`, contraseña `app`).

2. **Crear el esquema y los datos de referencia.** Ejecutar en este orden, conectado a la base de datos `gasolineras`:

    1. `sql/01_DDL.sql`
    2. `sql/02_datos_referencia.sql`

3. **Conexión.** Ya está configurada en `src/main/resources/db.properties`, con los mismos valores que el `docker-compose.yml`.

4. **Comprobar los datos.** Los ficheros deben estar en `data/terrestres.xls` y `data/maritimos.xls`.

5. **Ejecutar la carga.** Ejecutar la clase `org.example.Main`. Tarda unos segundos y muestra:

   ```
   Carburantes: 25
   Estaciones leídas: 11673
   Carga completada en … s
   ```

   La carga vacía las tablas antes de empezar, así que puede ejecutarse varias veces sin duplicar datos.

6. **Comprobar la carga.** Ejecutar `sql/05_comprobacion.sql`. Debe dar:

   | Comprobación | Resultado |
      |---|---|
   | Estaciones terrestres / marítimas | 11535 / 138 |
   | Precios | 43325 |
   | Carburantes | 25 |
   | Estaciones sin rótulo | 5 |

7. **Consultas del enunciado.** Ejecutar `sql/04_consultas.sql`.

`sql/03_vaciar.sql` vacía las tablas de la carga a mano. No hace falta ejecutarlo, porque la carga Java lo hace al empezar.

## Documentación

- `docs/analisis_datos.md`: análisis de los ficheros de origen y decisiones de modelado.
- `docs/justificacion_ER.md`: justificación del esquema relacional.
- `docs/cambios_modelo.md`: cambios aplicados al modelo inicial y orden de ejecución de los scripts.
- `docs/diagrama_ER.png`: diagrama entidad-relación.

## Vídeo

`video_presentacion.mp4`: explicación del diseño, la carga y las consultas.