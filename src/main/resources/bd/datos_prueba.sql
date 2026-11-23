-- Este archivo está guardado en UTF-8; indicar la codificación antes de importar.
SET NAMES utf8mb4;

USE cafeteria_escolar;

-- Datos de prueba para desarrollo. No elimina registros existentes.
-- El script es seguro ante una segunda ejecución: no duplica las ventas de prueba.

INSERT IGNORE INTO Categorias (nombre, descripcion) VALUES
    ('Bebidas calientes', 'Café, chocolate y té'),
    ('Bebidas frías', 'Bebidas con hielo y refrescos'),
    ('Panadería', 'Pan dulce y salado'),
    ('Postres', 'Pasteles y postres individuales'),
    ('Desayunos', 'Opciones para iniciar el día'),
    ('Comidas', 'Platillos fuertes'),
    ('Snacks', 'Botanas y aperitivos'),
    ('Especiales', 'Productos de temporada');

INSERT IGNORE INTO Productos (id_categoria, nombre, descripcion, precio, disponible) VALUES
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Bebidas calientes'), 'Café americano', 'Café filtrado', 42.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Bebidas calientes'), 'Capuchino', 'Café con leche espumada', 58.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Bebidas calientes'), 'Chocolate caliente', 'Chocolate con leche', 55.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Bebidas calientes'), 'Té chai', 'Té especiado con leche', 60.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Bebidas calientes'), 'Espresso', 'Café espresso doble', 48.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Bebidas frías'), 'Agua natural', 'Botella de agua', 25.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Bebidas frías'), 'Limonada', 'Limonada natural', 42.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Bebidas frías'), 'Té helado', 'Té negro con limón', 45.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Bebidas frías'), 'Frappé de café', 'Café frío licuado', 75.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Bebidas frías'), 'Refresco', 'Refresco de lata', 32.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Panadería'), 'Cuernito', 'Cuernito de mantequilla', 38.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Panadería'), 'Concha', 'Pan dulce tradicional', 28.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Panadería'), 'Muffin de chocolate', 'Muffin individual', 48.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Panadería'), 'Bagel con queso crema', 'Bagel tostado', 62.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Panadería'), 'Panqué de limón', 'Rebanada de panqué', 42.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Postres'), 'Rebanada de pastel', 'Pastel de chocolate', 72.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Postres'), 'Gelatina', 'Gelatina de mosaico', 35.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Postres'), 'Galleta de avena', 'Galleta individual', 28.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Postres'), 'Brownie', 'Brownie con nuez', 52.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Postres'), 'Yogur con fruta', 'Yogur natural con fruta', 58.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Desayunos'), 'Chilaquiles verdes', 'Con pollo y crema', 115.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Desayunos'), 'Molletes', 'Con frijoles y queso', 92.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Desayunos'), 'Sándwich de huevo', 'Con queso y jamón', 88.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Desayunos'), 'Avena con fruta', 'Avena preparada', 75.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Comidas'), 'Sándwich de pollo', 'Pollo, lechuga y jitomate', 118.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Comidas'), 'Ensalada César', 'Ensalada con pollo', 125.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Comidas'), 'Pasta al pesto', 'Pasta con salsa pesto', 128.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Comidas'), 'Burrito de frijoles', 'Frijoles, queso y arroz', 95.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Snacks'), 'Papas horneadas', 'Bolsa individual', 38.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Snacks'), 'Palomitas', 'Palomitas naturales', 32.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Snacks'), 'Barra de granola', 'Barra de avena', 35.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Snacks'), 'Fruta picada', 'Vaso de fruta de temporada', 48.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Especiales'), 'Pumpkin spice latte', 'Bebida de temporada', 82.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Especiales'), 'Chocolate con bombones', 'Chocolate de temporada', 78.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Especiales'), 'Galleta navideña', 'Galleta decorada', 35.00, TRUE),
    ((SELECT id_categoria FROM Categorias WHERE nombre = 'Especiales'), 'Pan de muerto', 'Pan tradicional de temporada', 48.00, TRUE);

INSERT IGNORE INTO Clientes (nombre, telefono, correo) VALUES
    ('Sofía Martínez', '5551000001', 'sofia.martinez@prueba.com'),
    ('Diego Hernández', '5551000002', 'diego.hernandez@prueba.com'),
    ('Valeria López', '5551000003', 'valeria.lopez@prueba.com'),
    ('Mateo García', '5551000004', 'mateo.garcia@prueba.com'),
    ('Camila Torres', '5551000005', 'camila.torres@prueba.com'),
    ('Santiago Ramírez', '5551000006', 'santiago.ramirez@prueba.com'),
    ('Regina Flores', '5551000007', 'regina.flores@prueba.com'),
    ('Emiliano Cruz', '5551000008', 'emiliano.cruz@prueba.com'),
    ('Natalia Reyes', '5551000009', 'natalia.reyes@prueba.com'),
    ('Sebastián Morales', '5551000010', 'sebastian.morales@prueba.com'),
    ('Ximena Ortiz', '5551000011', 'ximena.ortiz@prueba.com'),
    ('Daniel Vargas', '5551000012', 'daniel.vargas@prueba.com'),
    ('Fernanda Silva', '5551000013', 'fernanda.silva@prueba.com'),
    ('Alejandro Castro', '5551000014', 'alejandro.castro@prueba.com'),
    ('Mariana Rojas', '5551000015', 'mariana.rojas@prueba.com'),
    ('Pablo Mendoza', '5551000016', 'pablo.mendoza@prueba.com'),
    ('Andrea Navarro', '5551000017', 'andrea.navarro@prueba.com'),
    ('Jorge Salas', '5551000018', 'jorge.salas@prueba.com'),
    ('Paola Luna', '5551000019', 'paola.luna@prueba.com'),
    ('Ricardo Vega', '5551000020', 'ricardo.vega@prueba.com');

INSERT INTO Empleados (nombre, puesto, activo)
SELECT 'Ana Pérez', 'Cajera', TRUE WHERE NOT EXISTS (SELECT 1 FROM Empleados WHERE nombre = 'Ana Pérez' AND puesto = 'Cajera');
INSERT INTO Empleados (nombre, puesto, activo)
SELECT 'Luis Gómez', 'Barista', TRUE WHERE NOT EXISTS (SELECT 1 FROM Empleados WHERE nombre = 'Luis Gómez' AND puesto = 'Barista');
INSERT INTO Empleados (nombre, puesto, activo)
SELECT 'María Sánchez', 'Cajera', TRUE WHERE NOT EXISTS (SELECT 1 FROM Empleados WHERE nombre = 'María Sánchez' AND puesto = 'Cajera');
INSERT INTO Empleados (nombre, puesto, activo)
SELECT 'Carlos Ruiz', 'Cocinero', TRUE WHERE NOT EXISTS (SELECT 1 FROM Empleados WHERE nombre = 'Carlos Ruiz' AND puesto = 'Cocinero');
INSERT INTO Empleados (nombre, puesto, activo)
SELECT 'Elena Díaz', 'Barista', TRUE WHERE NOT EXISTS (SELECT 1 FROM Empleados WHERE nombre = 'Elena Díaz' AND puesto = 'Barista');
INSERT INTO Empleados (nombre, puesto, activo)
SELECT 'Miguel León', 'Cocinero', TRUE WHERE NOT EXISTS (SELECT 1 FROM Empleados WHERE nombre = 'Miguel León' AND puesto = 'Cocinero');
INSERT INTO Empleados (nombre, puesto, activo)
SELECT 'Laura Medina', 'Cajera', TRUE WHERE NOT EXISTS (SELECT 1 FROM Empleados WHERE nombre = 'Laura Medina' AND puesto = 'Cajera');
INSERT INTO Empleados (nombre, puesto, activo)
SELECT 'Roberto Campos', 'Auxiliar', FALSE WHERE NOT EXISTS (SELECT 1 FROM Empleados WHERE nombre = 'Roberto Campos' AND puesto = 'Auxiliar');

DELIMITER //

CREATE PROCEDURE LlenarVentasPrueba()
BEGIN
    DECLARE contador INT DEFAULT 1;
    DECLARE totalProductos INT;
    DECLARE totalClientes INT;
    DECLARE totalEmpleados INT;
    DECLARE desplazamiento INT;
    DECLARE idProductoUno BIGINT;
    DECLARE idProductoDos BIGINT;
    DECLARE idProductoTres BIGINT;
    DECLARE idCliente BIGINT;
    DECLARE idEmpleado BIGINT;
    DECLARE idVenta BIGINT;
    DECLARE ventasPrueba INT;

    SELECT COUNT(*) INTO ventasPrueba
    FROM Ventas v INNER JOIN Empleados e ON v.id_empleado = e.id_empleado
    WHERE e.nombre = 'Ana Pérez' AND v.fecha_venta >= DATE_SUB(CURDATE(), INTERVAL 130 DAY);

    IF ventasPrueba = 0 THEN
    SELECT COUNT(*) INTO totalProductos FROM Productos WHERE disponible = TRUE;
    SELECT COUNT(*) INTO totalClientes FROM Clientes;
    SELECT COUNT(*) INTO totalEmpleados FROM Empleados WHERE activo = TRUE;

    WHILE contador <= 120 DO
        SET desplazamiento = MOD(contador - 1, totalProductos);
        SELECT id_producto INTO idProductoUno FROM Productos WHERE disponible = TRUE ORDER BY id_producto LIMIT desplazamiento, 1;
        SET desplazamiento = MOD(contador + 6, totalProductos);
        SELECT id_producto INTO idProductoDos FROM Productos WHERE disponible = TRUE ORDER BY id_producto LIMIT desplazamiento, 1;
        SET desplazamiento = MOD(contador + 14, totalProductos);
        SELECT id_producto INTO idProductoTres FROM Productos WHERE disponible = TRUE ORDER BY id_producto LIMIT desplazamiento, 1;
        SET desplazamiento = MOD(contador - 1, totalEmpleados);
        SELECT id_empleado INTO idEmpleado FROM Empleados WHERE activo = TRUE ORDER BY id_empleado LIMIT desplazamiento, 1;

        IF MOD(contador, 4) = 0 THEN
            SET idCliente = NULL;
        ELSE
            SET desplazamiento = MOD(contador - 1, totalClientes);
            SELECT id_cliente INTO idCliente FROM Clientes ORDER BY id_cliente LIMIT desplazamiento, 1;
        END IF;

        INSERT INTO Ventas (id_cliente, id_empleado, fecha_venta, metodo_pago, estado)
        VALUES (
            idCliente,
            idEmpleado,
            DATE_SUB(NOW(), INTERVAL contador DAY),
            CASE MOD(contador, 3)
                WHEN 0 THEN 'EFECTIVO'
                WHEN 1 THEN 'TARJETA'
                ELSE 'TRANSFERENCIA'
            END,
            CASE WHEN MOD(contador, 12) = 0 THEN 'CANCELADA' ELSE 'PAGADA' END
        );
        SET idVenta = LAST_INSERT_ID();

        INSERT INTO DetallesVenta (id_venta, id_producto, cantidad, precio_unitario)
        SELECT idVenta, idProductoUno, 1 + MOD(contador, 3), precio FROM Productos WHERE id_producto = idProductoUno;
        INSERT INTO DetallesVenta (id_venta, id_producto, cantidad, precio_unitario)
        SELECT idVenta, idProductoDos, 1 + MOD(contador + 1, 2), precio FROM Productos WHERE id_producto = idProductoDos;
        INSERT INTO DetallesVenta (id_venta, id_producto, cantidad, precio_unitario)
        SELECT idVenta, idProductoTres, 1, precio FROM Productos WHERE id_producto = idProductoTres;

        SET contador = contador + 1;
    END WHILE;
    END IF;
END //

DELIMITER ;

CALL LlenarVentasPrueba();
DROP PROCEDURE LlenarVentasPrueba;
