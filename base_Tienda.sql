CREATE DATABASE IF NOT EXISTS seguridad_tienda_iot;
USE seguridad_tienda_iot;

-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de historial de alertas
CREATE TABLE IF NOT EXISTS historial_alertas (
    id_alerta INT AUTO_INCREMENT PRIMARY KEY,
    fecha_hora VARCHAR(50) NOT NULL,
    tipo_sensor VARCHAR(50) NOT NULL,
    detalle_evento VARCHAR(150) NOT NULL,
    nivel_alerta VARCHAR(20) NOT NULL
);

-- Ejemplo del historial de alertas 
INSERT INTO historial_alertas (fecha_hora, tipo_sensor, detalle_evento, nivel_alerta) VALUES 
('18/10/2026 - 23:15 hrs', 'Sensor Sonido', 'Ruido fuerte (Persiana Bajada)', 'ROJO'),
('18/10/2026 - 21:40 hrs', 'Sensor PIR', 'Movimiento detectado', 'ROJO'),
('18/10/2026 - 20:00 hrs', 'Sistema Activado', '(Usuario: admin)', 'VERDE');

SELECT * FROM historial_alertas;

-- Para ver los usuarios registrados
USE seguridad_tienda_iot;
SELECT * FROM usuarios;