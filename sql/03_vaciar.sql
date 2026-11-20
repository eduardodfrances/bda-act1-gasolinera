-- =====================================================================
-- 03_vaciar.sql
-- Vacía las tablas que rellena la carga Java, antes de cada carga.
-- NO toca comunidad_autonoma ni provincia (datos de referencia de 02).
-- RESTART IDENTITY reinicia los identificadores generados.
-- =====================================================================

TRUNCATE TABLE precio, estacion, localidad, municipio, empresa, carburante
    RESTART IDENTITY CASCADE;