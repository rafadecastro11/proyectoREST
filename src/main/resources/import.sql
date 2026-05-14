-- 1. Limpieza (Opcional, para evitar errores si ya existen)
DROP TABLE IF EXISTS reservas;
DROP TABLE IF EXISTS clases;

-- 2. Creación de la tabla de Clases
CREATE TABLE clases (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    monitor VARCHAR(100) NOT NULL,
    plazas INTEGER NOT NULL
);

-- 3. Creación de la tabla de Reservas
CREATE TABLE reservas (
    id SERIAL PRIMARY KEY,
    id_clase INTEGER NOT NULL,
    socio VARCHAR(100) NOT NULL,
    fecha DATE NOT NULL,
    CONSTRAINT fk_reservas_clases FOREIGN KEY (id_clase) REFERENCES clases(id)
);

-- 4. Inserción de datos maestros (IMPORTANTE para que el POST no falle)
-- Insertamos la clase con ID 1 que estás intentando usar en Swagger
INSERT INTO clases (id, nombre, monitor, plazas) VALUES (1, 'Spinning', 'Juan del Rio', 1);
INSERT INTO clases (id, nombre, monitor, plazas) VALUES (2, 'Yoga', 'Marta Gomez', 15);
INSERT INTO clases (id, nombre, monitor, plazas) VALUES (3, 'CrossFit', 'Carlos Ortiz', 10);

-- Ajustar el contador del serial para que las próximas inserciones no choquen con los IDs manuales
SELECT setval('clases_id_seq', (SELECT MAX(id) FROM clases));