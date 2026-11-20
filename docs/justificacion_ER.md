# Justificación del diseño del esquema relacional

## 1. Objetivo y datos de origen

La base de datos almacena los precios de los carburantes de las estaciones de servicio terrestres y de los postes marítimos de España, publicados por el Ministerio para la Transición Ecológica y el Reto Demográfico (geoportalgasolineras.es). Los datos proceden de dos ficheros Excel: `preciosEESS_es.xls` (11 535 estaciones terrestres, datos del 17/11/2026) y `embarcacionesPrecios_es.xls` (138 postes marítimos, datos del 09/11/2026). El esquema debe permitir las cinco consultas del enunciado: empresa con más estaciones de cada tipo, estación más barata por comunidad o por distancia, y provincia del poste marítimo más caro.

El análisis completo de los ficheros está en `analisis_datos.md`, y la lista de cambios aplicados al modelo inicial, en `cambios_modelo.md`.

## 2. Entidades

**comunidad_autonoma.** Representa cada una de las 19 comunidades y ciudades autónomas. Su clave primaria es un identificador generado. Es necesaria porque la consulta 3 filtra por comunidad y los ficheros del Ministerio no incluyen este dato. Se carga desde `02_datos_referencia.sql`.

**provincia.** Las 52 provincias, cada una con su comunidad. Se separa de la estación porque en 11 535 estaciones solo hay 52 provincias distintas. El nombre es único y está escrito igual que en los ficheros del Ministerio, porque la carga busca cada provincia por su nombre. Se carga desde `02_datos_referencia.sql`.

**municipio.** Los municipios donde hay al menos una estación (3 256 en terrestres). Su nombre solo es único dentro de su provincia (`UNIQUE (id_provincia, nombre)`). Se rellena durante la carga.

**localidad.** El núcleo de población concreto de cada estación (4 066 en terrestres). Se separa del municipio porque un municipio puede tener varias localidades, y su nombre solo es único dentro de su municipio: 50 nombres de localidad se repiten en municipios distintos. Se rellena durante la carga.

**empresa.** La empresa asociada a cada estación, tomada del rótulo (3 605 distintos para 11 535 estaciones). Separarla evita repetir el nombre en cada estación y permite contar estaciones por empresa (consultas 1 y 2). El nombre es único. Se rellena durante la carga.

**carburante.** Catálogo de los 25 carburantes que aparecen en los ficheros. Cada columna «Precio …» del Excel se convierte en una fila de esta tabla. Se rellena durante la carga.

**estacion.** Cada estación terrestre o poste marítimo. Su clave primaria es un identificador generado, porque los ficheros no traen ningún código de estación y la combinación provincia + municipio + dirección se repite 219 veces. Guarda la dirección, el código postal, las coordenadas, el margen, la fecha de actualización, el horario, el tipo de venta, la remisión y el tipo de servicio.

**precio.** El precio de un carburante en una estación, en €/L. Su clave primaria es compuesta, `(id_estacion, id_carburante)`.

## 3. Relaciones y cardinalidades

| Relación | Cardinalidad | Explicación |
|---|---|---|
| comunidad_autonoma — provincia | 1:N | Una comunidad tiene una o varias provincias; cada provincia pertenece a una sola comunidad |
| provincia — municipio | 1:N | Una provincia tiene cero o varios municipios; cada municipio está en una sola provincia |
| municipio — localidad | 1:N | Un municipio tiene una o varias localidades; cada localidad está en un solo municipio |
| localidad — estacion | 1:N | Una localidad tiene una o varias estaciones; cada estación está en una sola localidad |
| empresa — estacion | 1:N | Una empresa opera una o varias estaciones; cada estación tiene una sola empresa |
| estacion — precio | 1:N | Una estación tiene cero o varios precios; cada precio es de una sola estación |
| carburante — precio | 1:N | Un carburante tiene cero o varios precios; cada precio es de un solo carburante |

**Municipio y localidad.** El municipio es la unidad administrativa (con ayuntamiento propio) y la localidad es el núcleo de población concreto donde está la estación. Un municipio puede tener varias localidades: por ejemplo, la estación de San José pertenece al municipio de Níjar, y la de Aguadulce, al de Roquetas de Mar. En 7 485 de las 11 535 estaciones terrestres los dos nombres coinciden, pero en el resto no; por eso hay 3 256 municipios y 4 066 localidades.

**Relación entre estación y carburante.** Es una relación N:M: una estación vende varios carburantes y un carburante se vende en muchas estaciones. En el modelo relacional una N:M no se puede representar directamente, así que se resuelve con la tabla intermedia `precio`: cada fila representa una pareja estación–carburante y guarda un atributo propio de esa pareja, el importe. Por eso no aparece una línea directa entre `estacion` y `carburante` en el diagrama. Con este diseño solo se almacenan los precios que existen: el Excel de terrestres tiene 25 columnas de precio, pero la mayoría están casi vacías (por ejemplo, 30 precios de gasolina 95 E10 frente a 10 963 de gasolina 95 E5). Además, añadir un carburante nuevo no obliga a cambiar el esquema.

## 4. Restricciones de integridad

- **Claves primarias:** identificadores generados (`GENERATED BY DEFAULT AS IDENTITY`) en todas las tablas salvo `precio`, cuya clave compuesta impide que una estación tenga dos precios del mismo carburante.
- **Claves foráneas:** garantizan que no puede existir una provincia sin comunidad, un municipio sin provincia, una localidad sin municipio, una estación sin localidad ni empresa, ni un precio sin estación o sin carburante.
- **UNIQUE:** nombres únicos de comunidad, provincia, empresa y carburante. En municipio y localidad la unicidad es compuesta (dentro de su provincia o de su municipio), porque hay nombres repetidos en lugares distintos.
- **NOT NULL:** los datos imprescindibles para identificar y ubicar una estación (tipo, empresa, localidad, dirección y fecha de actualización) y el importe del precio.
- **CHECK:**
    - `tipo_estacion` solo admite TERRESTRE o MARITIMA.
    - `margen` solo admite D, I o N.
    - `remision` solo admite OM o dm.
    - `tipo_venta` depende del tipo de estación: P o R en terrestres; P, R o M en marítimas.
    - Latitud entre -90 y 90; longitud entre -180 y 180.
    - El precio debe ser mayor que 0.

## 5. Decisiones de diseño

1. **Comunidad autónoma:** se mantiene y se carga con una relación fija provincia → comunidad, porque la consulta 3 filtra por comunidad.
2. **Empresa:** se toma del rótulo, único dato de empresa común a los dos ficheros. Los rótulos vacíos se agrupan en «SIN RÓTULO», de modo que toda estación tiene empresa.
3. **Identificación de la estación:** clave sustituta y carga total (se vacían las tablas antes de cada carga), porque no hay un identificador fiable en los datos.
4. **Jerarquía de ubicación:** comunidad → provincia → municipio → localidad, con unicidad compuesta en municipio y localidad.
5. **Fecha de actualización:** atributo de la estación y obligatorio. En marítimas se usa la fecha de generación del fichero, que no trae una fecha por poste.
6. **Terrestre y marítima en la misma tabla:** solo 4 columnas son exclusivas de las terrestres; en las marítimas quedan a NULL.
7. **Porcentajes de bio:** se eliminan, porque las dos columnas de origen están vacías.
8. **Tipo de servicio:** se guarda tal cual como texto.
9. **Unidades de precio:** todos los precios se guardan en €/L.

## 6. Transformaciones resueltas en la carga

El modelo no cambia en estos casos, porque es el dato el que hay que adaptar:

| Dato | Transformación |
|---|---|
| Precios y coordenadas | Coma decimal → punto |
| Código postal | Se lee como texto para conservar los ceros a la izquierda |
| Fecha de toma de datos | Texto `dd/MM/yyyy HH:mm` → TIMESTAMP |
| Fecha de los postes marítimos | Se toma de la fila 1 del fichero |
| Tipo de venta (marítimos) | Texto → letra: «Suministro a pesqueros» → P, «Suministro a barcos deportivos o de recreo» → R, «Mixto» → M |
| Rótulo | Se eliminan espacios y se unifican mayúsculas; vacío → «SIN RÓTULO» |
| Nombres de carburante | Se eliminan el prefijo «Precio», las tabulaciones y los espacios dobles |
| Gasóleo B y gasóleo de uso marítimo (marítimos) | €/1000 L → €/L (se divide entre 1000) |

### Orden de ejecución

1. `01_DDL.sql`: crea el esquema.
2. `02_datos_referencia.sql`: comunidades y provincias.
3. `03_vaciar.sql`: deja vacías las tablas de datos.
4. Carga en Java.
5. `04_consultas.sql`: consultas del enunciado.

**Nota sobre el diagrama.** El diagrama, generado por DBeaver a partir del esquema, muestra todas las relaciones como 1 — 0..n: las claves foráneas garantizan que cada fila hija tiene su padre, pero no pueden exigir que un padre tenga al menos un hijo. Las cardinalidades mínimas de 1 de la tabla anterior se cumplen por la forma en que se realiza la carga (solo se crean municipios, localidades y empresas a partir de estaciones existentes).