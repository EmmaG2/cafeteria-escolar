# Sistema de cafetería escolar

Aplicación de escritorio JavaFX + MySQL para administrar una cafetería, registrar ventas y generar reportes en PDF.

## Entregas del proyecto final

| Entrega | Archivo | Contenido |
|---|---|---|
| Módulo de inserciones | `src/main/java/org/fernandogranados/demo/inserciones/Inserciones.java` | `INSERT` de categorías, productos, clientes, empleados, ventas y detalles de venta |
| Módulo de consultas | `src/main/java/org/fernandogranados/demo/consultas/Consultas.java` | `SELECT` de todas las tablas y consultas de agregación de los reportes |
| Módulo de eliminaciones | `src/main/java/org/fernandogranados/demo/eliminaciones/Eliminaciones.java` | `DELETE` de categorías, productos, clientes y empleados |
| Módulo de actualizaciones | `src/main/java/org/fernandogranados/demo/actualizaciones/Actualizaciones.java` | `UPDATE` de categorías, productos, clientes y empleados; cancelación de ventas |
| Implementación de transacciones | `src/main/java/org/fernandogranados/demo/transacciones/TransaccionVenta.java` | Registro de una venta y sus detalles con `setAutoCommit(false)`, `commit()` y `rollback()` |
| Aplicación del reporteador | `src/main/java/org/fernandogranados/demo/reporteador/` | `GraficaReporte` (JFreeChart) y `GeneradorPdfReporte` (iText) |

Las ventas nunca se borran: se cancelan con `estado = 'CANCELADA'` y no cuentan como ingreso.

## Flujo

- CRUD: `controlador` → `Inserciones` / `Consultas` / `Actualizaciones` / `Eliminaciones` → MySQL
- Venta: `VentasControlador` → `TransaccionVenta` → `Consultas` + `Inserciones` (misma conexión) → MySQL
- Reportes: `ReportesControlador` → `Consultas` → `reporteador` (gráficas y PDF)

## Ejecutar

1. Crear la base con `src/main/resources/db/cafeteria_escolar.sql` (datos de prueba en `src/main/resources/bd/datos_prueba.sql`).
2. Configurar la conexión en un archivo `.env` (no se sube a Git).
3. `./mvnw javafx:run`
