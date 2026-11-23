CREATE DATABASE cafeteria_escolar;

USE cafeteria_escolar;

CREATE TABLE Categorias
(
    id_categoria INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(60) NOT NULL UNIQUE,
    descripcion  VARCHAR(255)
) ENGINE = InnoDB;

CREATE TABLE Productos
(
    id_producto  INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_categoria INT UNSIGNED   NOT NULL,
    nombre       VARCHAR(100)   NOT NULL,
    descripcion  VARCHAR(255),
    precio       DECIMAL(10, 2) NOT NULL,
    disponible   BOOLEAN        NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_productos_categorias
        FOREIGN KEY (id_categoria) REFERENCES Categorias (id_categoria),
    CONSTRAINT uq_productos_categoria_nombre
        UNIQUE (id_categoria, nombre),
    CONSTRAINT chk_productos_precio
        CHECK (precio > 0)
) ENGINE = InnoDB;

CREATE TABLE Clientes
(
    id_cliente INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre     VARCHAR(100) NOT NULL,
    telefono   VARCHAR(20),
    correo     VARCHAR(120) UNIQUE
) ENGINE = InnoDB;

CREATE TABLE Empleados
(
    id_empleado INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    puesto      VARCHAR(50)  NOT NULL,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE
) ENGINE = InnoDB;

CREATE TABLE Ventas
(
    id_venta    INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_cliente  INT UNSIGNED NULL,
    id_empleado INT UNSIGNED NOT NULL,
    fecha_venta DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metodo_pago VARCHAR(20)  NOT NULL,
    estado      VARCHAR(15)  NOT NULL DEFAULT 'PAGADA',

    CONSTRAINT fk_ventas_clientes
        FOREIGN KEY (id_cliente) REFERENCES Clientes (id_cliente)
            ON DELETE SET NULL,
    CONSTRAINT fk_ventas_empleados
        FOREIGN KEY (id_empleado) REFERENCES Empleados (id_empleado),
    CONSTRAINT chk_ventas_metodo_pago
        CHECK (metodo_pago IN ('EFECTIVO', 'TARJETA', 'TRANSFERENCIA')),
    CONSTRAINT chk_ventas_estado
        CHECK (estado IN ('PAGADA', 'CANCELADA')),
    INDEX idx_ventas_fecha (fecha_venta)
) ENGINE = InnoDB;

CREATE TABLE DetallesVenta
(
    id_detalle      INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_venta        INT UNSIGNED   NOT NULL,
    id_producto     INT UNSIGNED   NOT NULL,
    cantidad        INT UNSIGNED   NOT NULL,
    precio_unitario DECIMAL(10, 2) NOT NULL,

    CONSTRAINT fk_detalles_ventas
        FOREIGN KEY (id_venta) REFERENCES Ventas (id_venta),
    CONSTRAINT fk_detalles_productos
        FOREIGN KEY (id_producto) REFERENCES Productos (id_producto),
    CONSTRAINT uq_detalles_venta_producto
        UNIQUE (id_venta, id_producto),
    CONSTRAINT chk_detalles_cantidad
        CHECK (cantidad > 0),
    CONSTRAINT chk_detalles_precio
        CHECK (precio_unitario > 0)
) ENGINE = InnoDB;
