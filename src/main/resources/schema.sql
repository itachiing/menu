-- =====================================================================
-- Esquema completo y datos de: ASADOS AL CARBÓN (Sabor que enciende)
-- Base de datos: menu_digital (MariaDB / MySQL / HeidiSQL)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS menu_digital CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE menu_digital;

-- 1. Usuarios del panel
CREATE TABLE IF NOT EXISTS usuario (
    id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(60) NOT NULL,
    password VARCHAR(100) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuario_username (username),
    CONSTRAINT chk_usuario_rol CHECK (rol IN ('ADMIN', 'COCINA', 'MESERO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Categorías del menú
CREATE TABLE IF NOT EXISTS categoria (
    id INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(60) NOT NULL,
    orden INT NOT NULL DEFAULT 0,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Productos de la carta
CREATE TABLE IF NOT EXISTS producto (
    id INT NOT NULL AUTO_INCREMENT,
    categoria_id INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(300),
    precio DECIMAL(10,2) NOT NULL,
    imagen VARCHAR(255),
    disponible BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    KEY idx_producto_categoria (categoria_id),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Variantes de producto
CREATE TABLE IF NOT EXISTS variante_producto (
    id INT NOT NULL AUTO_INCREMENT,
    producto_id INT NOT NULL,
    nombre VARCHAR(60) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    disponible BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    KEY idx_variante_producto (producto_id),
    CONSTRAINT fk_variante_producto FOREIGN KEY (producto_id) REFERENCES producto (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Adicionales
CREATE TABLE IF NOT EXISTS adicional (
    id INT NOT NULL AUTO_INCREMENT,
    categoria_id INT NULL,
    nombre VARCHAR(80) NOT NULL,
    precio DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    disponible BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    KEY idx_adicional_categoria (categoria_id),
    CONSTRAINT fk_adicional_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Mesas y códigos QR
CREATE TABLE IF NOT EXISTS mesa (
    id INT NOT NULL AUTO_INCREMENT,
    numero INT NOT NULL,
    codigo VARCHAR(64) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_mesa_numero (numero),
    UNIQUE KEY uk_mesa_codigo (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Pedidos
CREATE TABLE IF NOT EXISTS pedido (
    id INT NOT NULL AUTO_INCREMENT,
    mesa_id INT NULL,
    tipo_pedido VARCHAR(20) NOT NULL,
    nombre_cliente VARCHAR(100),
    telefono VARCHAR(20),
    direccion VARCHAR(200),
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) NOT NULL DEFAULT 'RECIBIDO',
    total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    observaciones VARCHAR(300),
    PRIMARY KEY (id),
    KEY idx_pedido_mesa (mesa_id),
    CONSTRAINT fk_pedido_mesa FOREIGN KEY (mesa_id) REFERENCES mesa (id) ON DELETE SET NULL,
    CONSTRAINT chk_pedido_tipo CHECK (tipo_pedido IN ('MESA', 'LLEVAR', 'DOMICILIO')),
    CONSTRAINT chk_pedido_estado CHECK (estado IN ('RECIBIDO', 'EN_PREPARACION', 'LISTO', 'ENTREGADO', 'CANCELADO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Líneas de detalle del pedido
CREATE TABLE IF NOT EXISTS detalle_pedido (
    id INT NOT NULL AUTO_INCREMENT,
    pedido_id INT NOT NULL,
    producto_id INT NOT NULL,
    variante_id INT NULL,
    cantidad INT NOT NULL DEFAULT 1,
    precio_unitario DECIMAL(10,2) NOT NULL,
    observacion VARCHAR(200),
    PRIMARY KEY (id),
    KEY idx_detalle_pedido (pedido_id),
    KEY idx_detalle_producto (producto_id),
    KEY idx_detalle_variante (variante_id),
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (pedido_id) REFERENCES pedido (id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (producto_id) REFERENCES producto (id),
    CONSTRAINT fk_detalle_variante FOREIGN KEY (variante_id) REFERENCES variante_producto (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Adicionales de cada detalle
CREATE TABLE IF NOT EXISTS detalle_pedido_adicional (
    id INT NOT NULL AUTO_INCREMENT,
    detalle_pedido_id INT NOT NULL,
    adicional_id INT NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_det_adic_detalle (detalle_pedido_id),
    KEY idx_det_adic_adicional (adicional_id),
    CONSTRAINT fk_det_adic_detalle FOREIGN KEY (detalle_pedido_id) REFERENCES detalle_pedido (id) ON DELETE CASCADE,
    CONSTRAINT fk_det_adic_adicional FOREIGN KEY (adicional_id) REFERENCES adicional (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- DATOS INICIALES: ASADOS AL CARBÓN
-- =====================================================================

-- Categorías
INSERT INTO categoria (id, nombre, orden, activa) VALUES
(1, 'Hamburguesas', 1, 1),
(2, 'Salchipapas', 2, 1),
(3, 'Suizos', 3, 1),
(4, 'Gratinados', 4, 1),
(5, 'Shawarma', 5, 1),
(6, 'Picadas', 6, 1)
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre), orden=VALUES(orden), activa=VALUES(activa);

-- Productos
INSERT INTO producto (id, categoria_id, nombre, descripcion, precio, imagen, disponible) VALUES
-- Hamburguesas
(1, 1, 'Burguer al Carbón', 'Pan burguer, salsa de la casa, carne artesanal, cebolla, BBQ, jamón, lechuga, tomate, queso mozarella.', 12000.00, '', 1),
(2, 1, 'Burguer Chorimeta', 'Pan burguer, salsa de la casa, carne artesanal, cebolla, BBQ, jamón, lechuga, tomate, queso mozarella, chorizo.', 15000.00, '', 1),
(3, 1, 'Doble Burguer al Carbón', 'Pan burguer, salsa de la casa, dos carnes artesanales, huevos, aguacate, cebolla, BBQ, jamón, lechuga, tomate, queso mozarella.', 20000.00, '', 1),

-- Salchipapas
(4, 2, 'Mini Salchipapas', 'Papas a la francesa, lechuga, salchicha, papa cheta, queso costeño.', 7000.00, '', 1),
(5, 2, 'Salchipapas de la Casa', 'Papas a la francesa, lechuga, salchicha, papa cheta, queso costeño.', 10000.00, '', 1),
(6, 2, 'Salchipollo', 'Papas a la francesa, lechuga, chorizo, pechuga de pollo, cebolla, queso costeño.', 13000.00, '', 1),

-- Suizos
(7, 3, 'Mini Suizo', 'Papas a la francesa, carne de cerdo y res, pechuga de pollo, salchicha suiza, lechuga, cebolla, queso costeño.', 17000.00, '', 1),
(8, 3, 'Suizo', 'Papas a la francesa, carne de cerdo y res, pechuga de pollo, salchicha suiza, lechuga, cebolla, queso costeño, tocineta.', 25000.00, '', 1),

-- Gratinados
(9, 4, 'Gratinado', 'Papas, salsa de la casa, lechuga, carne de cerdo, pechuga, chorizo, salchicha suiza, cebolla, queso mozarella derretido.', 20000.00, '', 1),
(10, 4, 'Gratinado con Maíz', 'Papas, salsa de la casa, lechuga, carne de cerdo, pechuga, chorizo, salchicha suiza, cebolla, queso mozarella derretido, maíz.', 25000.00, '', 1),
(11, 4, 'Gratinado con Tocineta', 'Papas, salsa de la casa, lechuga, carne de cerdo, pechuga, chorizo, salchicha suiza, cebolla, queso mozarella derretido, tocineta.', 35000.00, '', 1),

-- Shawarma
(12, 5, 'Shawarma', 'Tortilla de harina de trigo, salsa de la casa, pechuga de pollo, carne de cerdo y res, cebolla, jamón, lechuga, tomate, aguacate, queso mozarella.', 13000.00, '', 1),

-- Picadas
(13, 6, 'Picada de la Casa', 'Papas a la francesa, salsa de la casa, pechuga de pollo, carne de cerdo, cebolla, lechuga, chorizo, queso costeño.', 15000.00, '', 1),
(14, 6, 'Picada la Cordonera', 'Papas a la francesa, salsa de la casa, pechuga de pollo, carne de cerdo y res, cebolla, lechuga, chorizo, salchicha, tocineta, queso costeño.', 20000.00, '', 1),
(15, 6, 'Picada la Parrillera', 'Papas a la francesa, salsa de la casa, pechuga de pollo, carne de cerdo y res, cebolla, lechuga, chorizo, jamón, butifarra, salchicha, tocineta, queso costeño.', 30000.00, '', 1),
(16, 6, 'Picada Familiar', 'Papas a la francesa, salsa de la casa, pechuga de pollo, carne de cerdo y res, cebolla, lechuga, chorizo, salchicha, tocineta, queso costeño.', 50000.00, '', 1)
ON DUPLICATE KEY UPDATE categoria_id=VALUES(categoria_id), nombre=VALUES(nombre), descripcion=VALUES(descripcion), precio=VALUES(precio), disponible=VALUES(disponible);

-- Adicionales / Extras
INSERT INTO adicional (id, categoria_id, nombre, precio, disponible) VALUES
(1, NULL, 'Porción de papas a la francesa', 3000.00, 1),
(2, NULL, 'Adición de papa', 5000.00, 1),
(3, NULL, 'Mini Salchipapas (Extra)', 7000.00, 1),
(4, NULL, 'Queso Mozarella Extra', 4000.00, 1),
(5, NULL, 'Tocineta Extra', 5000.00, 1),
(6, NULL, 'Chorizo Extra', 4000.00, 1)
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre), precio=VALUES(precio), disponible=VALUES(disponible);

-- Mesas del establecimiento (1 al 8)
INSERT INTO mesa (id, numero, codigo, activa) VALUES
(1, 1, 'MESA-01', 1),
(2, 2, 'MESA-02', 1),
(3, 3, 'MESA-03', 1),
(4, 4, 'MESA-04', 1),
(5, 5, 'MESA-05', 1),
(6, 6, 'MESA-06', 1),
(7, 7, 'MESA-07', 1),
(8, 8, 'MESA-08', 1)
ON DUPLICATE KEY UPDATE numero=VALUES(numero), codigo=VALUES(codigo), activa=VALUES(activa);
