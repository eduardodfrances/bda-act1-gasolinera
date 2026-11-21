














-- =====================================================================
-- 04_consultas.sql
-- Consultas del enunciado sobre la base de datos de gasolineras.
-- Requiere haber ejecutado 01_DDL.sql, 02_datos_referencia.sql y la
-- carga en Java.
-- =====================================================================


-- ---------------------------------------------------------------------
-- Consulta 1: empresa con más estaciones terrestres.
-- Cuenta las estaciones terrestres de cada empresa y se queda con la
-- primera. WITH TIES muestra todas si hay empate.
-- ---------------------------------------------------------------------
SELECT em.nombre AS empresa,
       COUNT(*)  AS estaciones
FROM estacion e
JOIN empresa em ON em.id_empresa = e.id_empresa
WHERE e.tipo_estacion = 'TERRESTRE'
GROUP BY em.nombre
ORDER BY estaciones DESC
FETCH FIRST 1 ROWS WITH TIES;


-- ---------------------------------------------------------------------
-- Consulta 2: empresa con más estaciones marítimas.
-- Igual que la consulta 1, filtrando por postes marítimos.
-- ---------------------------------------------------------------------
SELECT em.nombre AS empresa,
       COUNT(*)  AS estaciones
FROM estacion e
JOIN empresa em ON em.id_empresa = e.id_empresa
WHERE e.tipo_estacion = 'MARITIMA'
GROUP BY em.nombre
ORDER BY estaciones DESC
FETCH FIRST 1 ROWS WITH TIES;


-- ---------------------------------------------------------------------
-- Consulta 3: estación con la gasolina 95 E5 más barata de la
-- Comunidad de Madrid, con su localización, empresa y margen.
-- Recorre la jerarquía estación → localidad → municipio → provincia →
-- comunidad para filtrar por comunidad. WITH TIES muestra los empates.
-- ---------------------------------------------------------------------
SELECT e.direccion,
       l.nombre  AS localidad,
       m.nombre  AS municipio,
       em.nombre AS empresa,
       e.margen,
       pr.precio
FROM precio pr
JOIN carburante c          ON c.id_carburante = pr.id_carburante
JOIN estacion e            ON e.id_estacion   = pr.id_estacion
JOIN empresa em            ON em.id_empresa   = e.id_empresa
JOIN localidad l           ON l.id_localidad  = e.id_localidad
JOIN municipio m           ON m.id_municipio  = l.id_municipio
JOIN provincia p           ON p.id_provincia  = m.id_provincia
JOIN comunidad_autonoma ca ON ca.id_ccaa      = p.id_ccaa
WHERE ca.nombre = 'Comunidad de Madrid'
  AND c.nombre = 'Gasolina 95 E5'
ORDER BY pr.precio
FETCH FIRST 1 ROWS WITH TIES;


-- ---------------------------------------------------------------------
-- Consulta 4: estación con el gasóleo A más barato a 10 km o menos del
-- centro de Albacete (plaza del Altozano: 38.995306, -1.854184).
-- El WITH calcula la distancia de cada estación con la fórmula de
-- Haversine; fuera se filtra por los 10 km y se ordena por precio.
-- ---------------------------------------------------------------------
WITH cercanas AS (
    SELECT e.direccion,
           l.nombre  AS localidad,
           m.nombre  AS municipio,
           em.nombre AS empresa,
           e.margen,
           pr.precio,
           ROUND(CAST(
               6371 * 2 * ASIN(SQRT(
                   POWER(SIN(RADIANS(e.latitud - 38.995306) / 2), 2)
                 + COS(RADIANS(38.995306)) * COS(RADIANS(e.latitud))
                 * POWER(SIN(RADIANS(e.longitud + 1.854184) / 2), 2)
               )) AS NUMERIC), 2) AS distancia_km
    FROM precio pr
    JOIN carburante c ON c.id_carburante = pr.id_carburante
    JOIN estacion e   ON e.id_estacion   = pr.id_estacion
    JOIN empresa em   ON em.id_empresa   = e.id_empresa
    JOIN localidad l  ON l.id_localidad  = e.id_localidad
    JOIN municipio m  ON m.id_municipio  = l.id_municipio
    WHERE c.nombre = 'Gasóleo A'
)
SELECT *
FROM cercanas
WHERE distancia_km <= 10
ORDER BY precio
FETCH FIRST 1 ROWS WITH TIES;


-- ---------------------------------------------------------------------
-- Consulta 5: provincia del poste marítimo con la gasolina 95 E5 más
-- cara. Filtra los postes marítimos con precio de 95 E5, ordena de
-- mayor a menor y sube hasta la provincia por la jerarquía de ubicación.
-- ---------------------------------------------------------------------
SELECT p.nombre  AS provincia,
       m.nombre  AS municipio,
       e.direccion,
       pr.precio
FROM precio pr
JOIN carburante c ON c.id_carburante = pr.id_carburante
JOIN estacion e   ON e.id_estacion   = pr.id_estacion
JOIN localidad l  ON l.id_localidad  = e.id_localidad
JOIN municipio m  ON m.id_municipio  = l.id_municipio
JOIN provincia p  ON p.id_provincia  = m.id_provincia
WHERE e.tipo_estacion = 'MARITIMA'
  AND c.nombre = 'Gasolina 95 E5'
ORDER BY pr.precio DESC
FETCH FIRST 1 ROWS WITH TIES;