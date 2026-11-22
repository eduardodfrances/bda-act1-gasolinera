-- 1. Empresa con más estaciones terrestres
SELECT em.nombre AS empresa,
       COUNT(*)  AS estaciones
FROM estacion e
JOIN empresa em ON em.id_empresa = e.id_empresa
WHERE e.tipo_estacion = 'TERRESTRE'
GROUP BY em.nombre
ORDER BY estaciones DESC
FETCH FIRST 1 ROWS WITH TIES;


-- 2. Empresa con más estaciones marítimas
SELECT em.nombre AS empresa,
       COUNT(*)  AS estaciones
FROM estacion e
JOIN empresa em ON em.id_empresa = e.id_empresa
WHERE e.tipo_estacion = 'MARITIMA'
GROUP BY em.nombre
ORDER BY estaciones DESC
FETCH FIRST 1 ROWS WITH TIES;


-- 3. Gasolina 95 E5 más barata en la Comunidad de Madrid
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


-- 4. Gasóleo A más barato a 10 km o menos de la plaza del Altozano (Albacete).
--    La distancia se calcula con Haversine dentro del WITH y se filtra fuera.
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


-- 5. Provincia del poste marítimo con la gasolina 95 E5 más cara
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