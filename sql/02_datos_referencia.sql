-- =====================================================================
-- 02_datos_referencia.sql
-- Datos de referencia que no vienen en los ficheros del Ministerio:
-- 19 comunidades/ciudades autónomas y 52 provincias.
-- Se ejecuta después de 01_DDL.sql y antes de la carga Java.
-- Los nombres de provincia se escriben exactamente igual que en los Excel,
-- porque la carga busca cada provincia por su nombre.
-- =====================================================================

INSERT INTO comunidad_autonoma (nombre) VALUES
    ('Andalucía'),
    ('Aragón'),
    ('Principado de Asturias'),
    ('Illes Balears'),
    ('Canarias'),
    ('Cantabria'),
    ('Castilla y León'),
    ('Castilla-La Mancha'),
    ('Cataluña'),
    ('Comunitat Valenciana'),
    ('Extremadura'),
    ('Galicia'),
    ('Comunidad de Madrid'),
    ('Región de Murcia'),
    ('Comunidad Foral de Navarra'),
    ('País Vasco'),
    ('La Rioja'),
    ('Ceuta'),
    ('Melilla');

INSERT INTO provincia (nombre, id_ccaa)
SELECT v.provincia, c.id_ccaa
FROM (VALUES
    ('ALMERÍA',                 'Andalucía'),
    ('CÁDIZ',                   'Andalucía'),
    ('CÓRDOBA',                 'Andalucía'),
    ('GRANADA',                 'Andalucía'),
    ('HUELVA',                  'Andalucía'),
    ('JAÉN',                    'Andalucía'),
    ('MÁLAGA',                  'Andalucía'),
    ('SEVILLA',                 'Andalucía'),
    ('HUESCA',                  'Aragón'),
    ('TERUEL',                  'Aragón'),
    ('ZARAGOZA',                'Aragón'),
    ('ASTURIAS',                'Principado de Asturias'),
    ('BALEARS (ILLES)',         'Illes Balears'),
    ('PALMAS (LAS)',            'Canarias'),
    ('SANTA CRUZ DE TENERIFE',  'Canarias'),
    ('CANTABRIA',               'Cantabria'),
    ('ÁVILA',                   'Castilla y León'),
    ('BURGOS',                  'Castilla y León'),
    ('LEÓN',                    'Castilla y León'),
    ('PALENCIA',                'Castilla y León'),
    ('SALAMANCA',               'Castilla y León'),
    ('SEGOVIA',                 'Castilla y León'),
    ('SORIA',                   'Castilla y León'),
    ('VALLADOLID',              'Castilla y León'),
    ('ZAMORA',                  'Castilla y León'),
    ('ALBACETE',                'Castilla-La Mancha'),
    ('CIUDAD REAL',             'Castilla-La Mancha'),
    ('CUENCA',                  'Castilla-La Mancha'),
    ('GUADALAJARA',             'Castilla-La Mancha'),
    ('TOLEDO',                  'Castilla-La Mancha'),
    ('BARCELONA',               'Cataluña'),
    ('GIRONA',                  'Cataluña'),
    ('LLEIDA',                  'Cataluña'),
    ('TARRAGONA',               'Cataluña'),
    ('ALICANTE',                'Comunitat Valenciana'),
    ('CASTELLÓN / CASTELLÓ',    'Comunitat Valenciana'),
    ('VALENCIA / VALÈNCIA',     'Comunitat Valenciana'),
    ('BADAJOZ',                 'Extremadura'),
    ('CÁCERES',                 'Extremadura'),
    ('CORUÑA (A)',              'Galicia'),
    ('LUGO',                    'Galicia'),
    ('OURENSE',                 'Galicia'),
    ('PONTEVEDRA',              'Galicia'),
    ('MADRID',                  'Comunidad de Madrid'),
    ('MURCIA',                  'Región de Murcia'),
    ('NAVARRA',                 'Comunidad Foral de Navarra'),
    ('ARABA/ÁLAVA',             'País Vasco'),
    ('BIZKAIA',                 'País Vasco'),
    ('GIPUZKOA',                'País Vasco'),
    ('RIOJA (LA)',              'La Rioja'),
    ('CEUTA',                   'Ceuta'),
    ('MELILLA',                 'Melilla')
) AS v(provincia, comunidad)
JOIN comunidad_autonoma c ON c.nombre = v.comunidad;