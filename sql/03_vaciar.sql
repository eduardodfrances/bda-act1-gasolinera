-- Vacía las tablas de la carga sin tocar comunidades ni provincias.
-- La carga Java hace lo mismo al empezar; esto es para vaciar a mano.
TRUNCATE TABLE precio, estacion, localidad, municipio, empresa, carburante
    RESTART IDENTITY CASCADE;