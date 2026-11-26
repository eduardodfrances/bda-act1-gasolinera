# Análisis de los datos de origen

## 1. Ficheros

| Dato | terrestres.xls | maritimos.xls |
|---|---|---|
| Nombre original | preciosEESS_es.xls | embarcacionesPrecios_es.xls |
| Origen | geoportalgasolineras.es → Información actualizada de precios | geoportalgasolineras.es → Información actualizada de precios |
| Fecha de los datos | 17/11/2026 19:00 | 09/11/2026 19:00 |
| Contenido | Todos los productos en todas las estaciones de servicio | Todos los productos en todos los postes marítimos |

**Siglas (terrestres):** Margen - D: Derecho, I: Izquierdo, N: No aplica · Tipo venta - P: Venta al público en general · Rem. - OM: Datos procedentes del operador mayorista, dm: Datos procedentes del distribuidor minorista · Tipo servicio - P: Servicio asistido por personal, A: Autoservicio por parte del cliente, con presencia de personal en la instalación, D: Autoservicio por parte del cliente, sin presencia de personal en la instalación

**Siglas (marítimos):** Tipo venta - P: Suministro a pesqueros, R: Suministro a barcos deportivos y de recreo, M: Mixta · Rem. - OM: Datos procedentes del operador mayorista, dm: Datos procedentes del distribuidor minorista
**Formato elegido:** se usan los ficheros .xls porque incluyen todos los productos con sus precios en un solo fichero por tipo de estación; el CSV disponible no traía los precios.
## 2. Estructura

| Dato | terrestres.xls | maritimos.xls |
|---|---|---|
| Filas de metadatos antes de cabeceras | 3 | 3 |
| Fila de cabeceras (Excel) | 4 | 4 |
| Primera fila de datos (Excel) | 5 | 5 |
| Filas de datos | 11535 | 138 |
| Columnas | 40 | 26 |
| Columnas sin cabecera | 0 | 1 (columna C, vacía) |

## 3. Mapeo columnas → DDL

Resultado: OK = encaja · MAL = encaja mal · SD = sin destino

### 3a. Columnas (no precio)

| Columna | Terr. | Marít. | Destino DDL | Vacíos T/M | Long. máx T/M | Formato | Resultado | Nota |
|---|---|---|---|---|---|---|---|---|
| Provincia | A | A | provincia.nombre VARCHAR(60) NN | 0 / 0 | 22 / 22 | — | OK | 52 provincias en terr., 22 en marít. (mismos nombres en ambos ficheros) |
| Municipio | B | B | municipio.nombre VARCHAR(120) NN | 0 / 0 | 59 / 42 | — | OK | |
| (sin cabecera) | — | C | — | — / 138 | — / 0 | Columna vacía | SD | Se ignora en la carga |
| Localidad | C | D | localidad.nombre VARCHAR(120) NN | 0 / 0 | 47 / 30 | — | OK | |
| Código postal | D | E | estacion.codigo_postal CHAR(5) | 0 / 0 | 5 / 5 | Texto con ceros a la izquierda (2450 / 37 empiezan por 0; 0 numéricos) | OK | Leer como texto, nunca como número |
| Dirección | E | F | estacion.direccion VARCHAR(255) NN | 0 / 0 | 104 / 80 | — | OK | |
| Margen | F | — | estacion.margen CHAR(1) CHECK | 0 / — | 1 / — | — | OK | NULL en marítimas |
| Longitud | G | G | estacion.longitud NUMERIC(9,6) | 0 / 5 | 10 / 10 | Texto con coma decimal (0 numéricos) | OK | Cambiar coma por punto antes de convertir. 5 postes sin coordenadas |
| Latitud | H | H | estacion.latitud NUMERIC(9,6) | 0 / 5 | 9 / 9 | Texto con coma decimal | OK | Igual que Longitud |
| Toma de datos | I | — | estacion.fecha_actualizacion TIMESTAMP NN | 0 / — | 16 / — | Texto `dd/MM/yyyy HH:mm` | MAL | Es NOT NULL y marítimos no trae esta columna |
| % bioalcohol | T | — | precio.porcentaje_bio | 11535 / — | 0 / — | — | MAL | Columna sin ningún dato |
| % éster metílico | V | — | precio.porcentaje_bio | 11535 / — | 0 / — | — | MAL | Columna sin ningún dato |
| Rótulo | AJ | W | empresa.nombre VARCHAR(150) NN UQ | 0 / 5 | 86 / 45 | Espacios al final (214 / 2). Valores no válidos ("NINGUNO", vacíos) | MAL | id_empresa es NOT NULL y hay 5 postes sin rótulo. Rótulo es la marca, no el operador |
| Tipo venta | AK | X | estacion.tipo_venta CHAR(1) + ck_tipo_venta | 0 / 0 | 1 / 42 | Terr.: letra. Marít.: texto largo | MAL | En marítimos no cabe en CHAR(1) ni cumple el CHECK |
| Rem. | AL | Y | estacion.remision VARCHAR(2) CHECK | 0 / 0 | 2 / 2 | — | OK | |
| Horario | AM | Z | estacion.horario TEXT | 0 / 0 | 78 / 60 | Texto libre | OK | |
| Tipo servicio | AN | — | estacion.tipo_servicio TEXT | 0 / — | 494 / — | Repite el horario y añade (A)/(P)/(D) por franja | MAL | Redundante con Horario (8216 de 11535 empiezan por el horario) |

### Valores de las columnas con CHECK

| Columna | Terrestres | Marítimos | CHECK del DDL | ¿Cumple? |
|---|---|---|---|---|
| Margen | D, I, N | — | D, I, N | Sí |
| Tipo venta | P | "Suministro a barcos deportivos o de recreo", "Mixto", "Suministro a pesqueros" | TERRESTRE: P, R · MARITIMA: P, R, M | Terr.: sí · Marít.: no (hay que convertir el texto a R / M / P según las siglas del fichero) |
| Rem. | dm, OM | dm, OM | OM, dm | Sí |

### 3b. Carburantes

| Carburante | Terr. | Marít. | ¿Se carga? |
|---|---|---|---|
| Gasolina 95 E5 | 10963 | 86 | Sí |
| Gasolina 95 E10 | 30 | 0 | Sí |
| Gasolina 95 E5 Premium | 1117 | — | Sí |
| Gasolina 98 E5 | 5505 | — | Sí |
| Gasolina 98 E10 | 17 | — | Sí |
| Gasóleo A | 11313 | 88 | Sí |
| Gasóleo Premium | 5920 | — | Sí |
| Gasóleo B | 2286 | 73 | Sí (marít. en €/1000 L: se divide entre 1000) |
| Gasóleo C | 11 | — | Sí |
| Gasóleo de uso marítimo | — | 6 | Sí (solo marítimos, en €/1000 L: se divide entre 1000) |
| Bioetanol | 1 | — | Sí |
| Biodiésel | 29 | — | Sí |
| Gases licuados del petróleo | 1003 | — | Sí |
| Gas natural comprimido | 95 | — | Sí |
| Gas natural licuado | 54 | — | Sí |
| Hidrógeno | 2 | — | Sí |
| Gasolina 95 E25 | 1 | 0 | Sí |
| Gasolina 95 E85 | 2 | 0 | Sí |
| AdBlue | 2952 | 0 | Sí |
| Diésel renovable | 1632 | 1 | Sí |
| Gasolina renovable | 31 | 0 | Sí |
| Metanol | 0 | 0 | Sí (en el catálogo, sin precios) |
| Amoníaco | 0 | 0 | Sí (en el catálogo, sin precios) |
| BGNC | 53 | 0 | Sí |
| BGNL | 54 | 0 | Sí |

Formato: precios como texto con coma decimal (0 numéricos). 4 cabeceras de precio terminan en tabulación y una tiene un espacio doble ("Precio  amoniaco"): hay que limpiar los nombres antes de guardarlos en `carburante.nombre`. **Unidades:** en marítimos, gasóleo B y gasóleo de uso marítimo vienen en €/1000 L (valores entre 1090 y 1999); el resto, en €/L. El hidrógeno se expresa en €/kg (9,90 y 25,00).

### 3c. Columnas del DDL sin origen directo

| Tabla.columna | Origen |
|---|---|
| todas las `id_*` | Generadas por PostgreSQL (IDENTITY) |
| comunidad_autonoma.nombre | No viene en los .xls. Hace falta otra fuente (CSV de marítimos o tabla fija provincia → comunidad) |
| provincia.id_ccaa | Depende de la fila anterior |
| estacion.tipo_estacion | No es una columna: se asigna en la carga según el fichero de origen |
| carburante.nombre | Cabeceras de las columnas de precio (3b) |
| precio.precio | Celdas de las columnas de precio |
| precio.porcentaje_bio | Columnas vacías: sobra |
| estacion.fecha_actualizacion (marítimas) | No hay columna. Solo la fecha de la fila 1 del fichero |

### 3d. Fallos detectados en el DDL

1. **tipo_venta:** en marítimos viene como texto largo; no cabe en CHAR(1) ni cumple `ck_tipo_venta`.
2. **fecha_actualizacion NOT NULL:** el fichero de marítimos no tiene *Toma de datos*.
3. **id_empresa NOT NULL:** 5 postes marítimos no tienen rótulo.
4. **empresa = Rótulo:** el rótulo es la marca comercial, no la empresa que opera (como pide el enunciado). Hay que justificarlo o cambiarlo.
5. **precio.porcentaje_bio:** las columnas de origen no tienen ningún dato.
6. **tipo_servicio:** repite el horario; la información nueva es solo la modalidad (A/P/D).
7. **comunidad_autonoma:** no tiene origen en los .xls.
8. **Identificación de estación:** no hay ID en los ficheros y la combinación provincia + municipio + dirección se repite 219 veces. Sin estrategia, una segunda carga duplicaría todo.
9. **Diagrama ≠ DDL:** el diagrama dice tipo_venta {P, R}, el DDL admite también M.
10. **Carburantes sin precios:** metanol y amoníaco tienen 0 precios en los dos ficheros.
11. **Unidades de precio:** en marítimos, gasóleo B (73 precios, de 1090 a 1575) y gasóleo de uso marítimo (6 precios, de 1199 a 1999) vienen en €/1000 L, mientras que el resto de precios vienen en €/L. `precio NUMERIC(6,3)` admite como máximo 999,999, así que la carga fallaría.

## 4. Problemas de formato

Integrados en la columna *Formato* de la tabla 3a y en la nota de la tabla 3b.

## 5. Decisiones de modelado

### 5.1 Comunidad autónoma
**Datos:** ningún .xls la trae; el CSV de marítimos sí (columna `CCAA`). Hay 52 provincias en terrestres.
**Propuesta:** mantener la tabla y rellenar la relación provincia → comunidad con una tabla fija de 52 filas, cargada antes que las estaciones.
**Alternativa:** eliminar la tabla y filtrar la consulta 3 por la provincia MADRID (es uniprovincial).
**Decisión:** se mantiene la tabla `comunidad_autonoma` y se carga una relación fija provincia → comunidad antes que las estaciones. Así la consulta 3 filtra por comunidad, como pide el enunciado, y el modelo conserva la jerarquía administrativa completa.

### 5.2 Empresa
**Datos:** 3605 rótulos distintos en terrestres y 61 en marítimos; 214 / 2 con espacios al final; valores como "NINGUNO"; 5 postes marítimos sin rótulo. El dato *Operador* solo existe en el CSV de marítimos.
**Propuesta:** usar *Rótulo* como empresa, normalizado, y agrupar los vacíos en "SIN RÓTULO".
**Decisión:** la empresa se toma del *Rótulo*, porque es el único dato de empresa común a los dos ficheros. En la carga se eliminan los espacios sobrantes y se unifican mayúsculas. Los rótulos vacíos se asignan a una empresa "SIN RÓTULO", de modo que `id_empresa` puede seguir siendo NOT NULL.

### 5.3 Identificar una estación
**Datos:** no hay ID en los ficheros. Provincia + municipio + dirección se repite 219 veces en terrestres (2 en marítimos). Las coordenadas se repiten 18 veces (en marítimos, las 4 repeticiones son los 5 postes sin coordenadas).
**Propuesta:** clave sustituta y carga total, vaciando las tablas antes de cada carga.
**Decisión:** cada estación se identifica con una clave sustituta generada por PostgreSQL. La carga es **total**: antes de cargar se vacían las tablas, de modo que ejecutarla varias veces no duplica datos. No se añade un UNIQUE natural porque los datos no ofrecen ninguna combinación única fiable.

### 5.4 Jerarquía de ubicación
**Datos:** 0 municipios repetidos en varias provincias; 50 nombres de localidad repetidos en municipios distintos; la localidad coincide con el municipio en 7485 de 11535 estaciones terrestres (78 de 138 marítimas).
**Propuesta:** mantener provincia → municipio → localidad con los UNIQUE compuestos actuales.
**Decisión:** se mantiene la jerarquía comunidad → provincia → municipio → localidad. El nombre de municipio es único dentro de su provincia, y el de localidad, dentro de su municipio. Las 50 localidades con nombre repetido en municipios distintos confirman que la unicidad de la localidad no puede ser global.

### 5.5 Fecha de actualización
**Datos:** terrestres la trae por estación (*Toma de datos*, sin vacíos, `dd/MM/yyyy HH:mm`). Marítimos no; solo tiene la fecha del fichero (09/11/2026 19:00).
**Propuesta:** la fecha pertenece a la estación; en marítimas se usa la fecha del fichero.
**Alternativa:** permitir NULL en marítimas.
**Decisión:** la fecha de actualización es un atributo de la estación y se mantiene NOT NULL. En terrestres se toma de *Toma de datos*; en marítimas, de la fecha de generación del fichero (fila 1).

### 5.6 Terrestre frente a marítima
**Datos:** solo terrestres tiene *Margen*, *Toma de datos*, *Tipo servicio* y los porcentajes de bio. El resto es común.
**Propuesta:** una sola tabla `estacion` con `tipo_estacion` y nulos donde no aplica.
**Alternativa:** especialización (tabla común + una tabla por tipo).
**Decisión:** una sola tabla `estacion` con el atributo `tipo_estacion` (TERRESTRE / MARITIMA). Las columnas que solo existen en terrestres quedan a NULL en las marítimas. Se elige esta opción porque son pocas columnas distintas y las consultas 1 y 2 solo necesitan filtrar por tipo. El *Tipo venta* de marítimos se convierte a su letra (P / R / M) en la carga, según las siglas del propio fichero.

### 5.7 Porcentajes de bio
**Datos:** las dos columnas están vacías en las 11535 filas.
**Propuesta:** eliminar `precio.porcentaje_bio`.
**Decisión:** se elimina `precio.porcentaje_bio` porque no hay ningún dato que guardar.

### 5.8 Tipo servicio
**Datos:** 8216 de 11535 valores son el horario seguido de la modalidad (A), (P) o (D) por franja: autoservicio con personal, servicio asistido y autoservicio sin personal, según las siglas.
**Propuesta:** mantenerlo como TEXT tal cual.
**Alternativa:** eliminarlo por redundante con *Horario*.
**Decisión:** se mantiene `tipo_servicio` como TEXT, sin transformar. Ninguna consulta lo usa, pero forma parte de la información publicada y guardarlo tal cual no añade complejidad a la carga.

### 5.9 Unidades de precio
**Datos:** en marítimos, gasóleo B y gasóleo de uso marítimo vienen en €/1000 L (1090–1999); el resto de precios de los dos ficheros, en €/L (0,49–3,41), salvo el hidrógeno, que se expresa en €/kg (9,90 y 25,00). El tipo `NUMERIC(6,3)` admite hasta 999,999.
**Propuesta:** normalizar en la carga dividiendo entre 1000 esos dos carburantes de marítimos.
**Alternativa:** ampliar el tipo a `NUMERIC(8,3)` y guardar el dato tal cual.
**Decisión:** se normalizan a €/L en la carga los dos carburantes de marítimos que vienen en €/1000 L. Así los precios de los combustibles líquidos están en la misma unidad y se pueden comparar entre estaciones y entre terrestres y marítimas. El hidrógeno se mantiene en €/kg, que es su unidad de venta. El esquema no cambia.

## 6. Cobertura de las consultas

| Consulta | Datos necesarios | ¿Fichero? | ¿DDL? |
|---|---|---|---|
| 1. Empresa con más terrestres | Rótulo, tipo de estación | Sí | Sí (empresa, estacion.tipo_estacion) |
| 2. Empresa con más marítimas | Rótulo, tipo de estación | Sí (5 sin rótulo → "SIN RÓTULO") | Sí |
| 3. 95 E5 más barata en C. de Madrid | Comunidad, precio 95 E5, localización, empresa, margen | Comunidad no (provincia MADRID sí: 896 estaciones) | Sí, con la relación provincia → comunidad |
| 4. Gasóleo A más barato ≤ 10 km de Albacete | Coordenadas, precio gasóleo A, localización, empresa, margen | Sí (coordenadas sin vacíos en terrestres; 44 de las 45 estaciones del municipio tienen gasóleo A) | Sí |
| 5. Provincia de la marítima con 95 E5 más cara | Provincia, precio 95 E5, tipo de estación | Sí (86 postes con precio) | Sí |

**Centro de Albacete:** plaza del Altozano, 38.995306, -1.854184 (Google Maps). Es el centro histórico y administrativo de la ciudad.
**Cálculo de distancia:** fórmula de Haversine escrita directamente en SQL, sin instalar extensiones en PostgreSQL. El filtro es por distancia, no por municipio: a 10 km pueden entrar estaciones de municipios vecinos.
**Nombre de la comunidad para la consulta 3:** Comunidad de Madrid (escrito así en `comunidad_autonoma`).