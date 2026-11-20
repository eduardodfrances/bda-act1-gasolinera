# Cambios al modelo

| # | Fallo (análisis 3d) | Decisión | Dónde se resuelve | Cambio concreto |
|---|---|---|---|---|
| 1 | tipo_venta en marítimos es texto largo | 5.6 | Carga | Convertir el texto a P / R / M según las siglas del fichero. El DDL no cambia |
| 2 | fecha_actualizacion NOT NULL sin dato en marítimos | 5.5 | Carga | Usar la fecha de la fila 1 del fichero. El DDL no cambia |
| 3 | id_empresa NOT NULL con 5 postes sin rótulo | 5.2 | Carga | Asignar la empresa "SIN RÓTULO". El DDL no cambia |
| 4 | empresa = Rótulo y no operador | 5.2 | 01_DDL.sql (comentario) | Corregir el comentario de la tabla empresa: es el rótulo (marca), justificado |
| 5 | precio.porcentaje_bio sin datos | 5.7 | 01_DDL.sql + diagrama | Eliminar la columna |
| 6 | tipo_servicio redundante | 5.8 | — | Sin cambio: se guarda tal cual |
| 7 | comunidad_autonoma sin origen | 5.1 | 02_datos_referencia.sql | Insertar las 19 comunidades y las 52 provincias antes de la carga |
| 8 | Sin identificador de estación | 5.3 | 03_vaciar.sql + carga | Clave sustituta (ya existe) + vaciar las tablas antes de cada carga |
| 9 | Diagrama ≠ DDL en tipo_venta | — | Diagrama | Se resuelve al generar el diagrama desde el DDL |
| 10 | Metanol y amoníaco sin precios | 3b | — | Sin cambio: entran en el catálogo de carburantes sin precios |
| 11 | Gasóleo B y de uso marítimo en €/1000 L en marítimos | 5.9 | Carga | Dividir entre 1000 al cargar. El DDL no cambia |


## Scripts SQL y orden de ejecución

| Orden | Script | Contenido |
|---|---|---|
| 1 | 01_DDL.sql | Creación de las 8 tablas |
| 2 | 02_datos_referencia.sql | 19 comunidades autónomas y 52 provincias |
| 3 | 03_vaciar.sql | Vaciado de las tablas de datos antes de cada carga |
| 4 | Carga Java | Estaciones, empresas, municipios, localidades, carburantes y precios |
| 5 | 04_consultas.sql | Las 5 consultas del enunciado |

