-- Recuentos para comprobar la carga. Se ejecutan después de Main.

-- Estaciones por tipo: 11535 terrestres y 138 marítimas
SELECT tipo_estacion, COUNT(*) AS estaciones
FROM estacion
GROUP BY tipo_estacion;

-- Precios cargados: 43325
SELECT COUNT(*) AS precios
FROM precio;

-- Carburantes del catálogo, también los que no tienen precios: 25
SELECT COUNT(*) AS carburantes
FROM carburante;

-- Estaciones sin rótulo, agrupadas en 'SIN RÓTULO': 5
SELECT COUNT(*) AS sin_rotulo
FROM estacion e
JOIN empresa em ON em.id_empresa = e.id_empresa
WHERE em.nombre = 'SIN RÓTULO';