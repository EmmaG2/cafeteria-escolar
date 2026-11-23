/# Plan de desarrollo por etapas

## Situación actual

- El proyecto JavaFX cuenta con su estructura inicial.
- MySQL se ejecuta en un contenedor Docker y expone el puerto `3036`.
- La base `cafeteria_escolar` y sus tablas ya están creadas.
- `.env` ya contiene `DB_USER` y `DB_PASSWORD`. Este archivo no se debe versionar.
- MySQL Connector/J y SLF4J están configurados en Maven.

> **Reestructuración por entregas:** después de las etapas 1 a 8, el código se reorganizó en los módulos que evalúa el profesor. Los repositorios por entidad (`*Repositorio`) se unieron en `inserciones/Inserciones`, `eliminaciones/Eliminaciones`, `actualizaciones/Actualizaciones` y `consultas/Consultas`. `VentaServicio` pasó a `transacciones/TransaccionVenta` y `reporte/` a `reporteador/`. Las etapas de abajo conservan los nombres originales como historial. Ver `README.md`.

## Etapa 1: conexión a la base de datos — implementada

Objetivo: dejar una conexión JDBC reutilizable y comprobada contra el contenedor local.

1. Crear `utilidad/ConexionBD.java`.
2. Cargar la configuración desde `.env` o variables de entorno. Java no carga `.env` automáticamente, por lo que se debe definir una solución sencilla antes de usar las credenciales.
3. Configurar la URL de conexión para el entorno actual:
   `jdbc:mysql://localhost:3036/cafeteria_escolar`.
4. Usar `DB_USER` y `DB_PASSWORD` para abrir la conexión.
5. Registrar con SLF4J el inicio correcto de la conexión y los errores técnicos, sin escribir contraseñas en los logs.
6. Mostrar un mensaje claro en español si la conexión no puede abrirse.
7. Hacer una prueba de conexión antes de comenzar los módulos CRUD.

Criterio de término: la aplicación obtiene y cierra una `Connection` correctamente; un fallo de servidor, puerto o credenciales se informa con claridad y se registra en el log.

Estado actual: `ConexionBD` y la prueba de inicio están implementadas. La conexión al contenedor MySQL se comprobó correctamente.

## Etapa 2: estructura inicial de JavaFX y navegación — implementada

1. Preparar la aplicación JavaFX y la vista de menú principal.
2. Crear la navegación hacia categorías, productos, clientes, empleados, ventas y reportes.
3. Aplicar BootstrapFX de forma mínima y consistente.
4. Centralizar mensajes visuales sencillos para éxito, advertencia y error.

Criterio de término: la aplicación abre y permite navegar a las pantallas base sin SQL en los controladores.

Estado actual: se creó el menú principal, la navegación inicial a los módulos y se aplicó BootstrapFX. Los módulos muestran una vista provisional hasta que se implemente su etapa correspondiente.

## Etapa 3: CRUD de categorías — implementada

1. Crear `Categoria` en `modelo`.
2. Crear `CategoriaRepositorio` con consultas JDBC y `PreparedStatement`.
3. Crear `Categorias.fxml` y `CategoriasControlador`.
4. Implementar listar, registrar, editar y eliminar.
5. Validar nombre obligatorio, nombre duplicado y categorías relacionadas con productos.

Criterio de término: los cambios se conservan después de reiniciar la aplicación y los errores se muestran en español.

Estado actual: se implementaron el modelo, repositorio JDBC, pantalla y controlador de categorías. El menú principal abre el módulo de categorías.

## Etapa 4: CRUD de productos — implementada

1. Crear `Producto`, repositorio, vista y controlador.
2. Permitir seleccionar una categoría existente.
3. Administrar nombre, descripción, precio y disponibilidad.
4. Usar `BigDecimal` para el precio y validar que sea mayor que cero.
5. Evitar eliminar productos que tengan ventas; usar disponibilidad cuando corresponda.

Criterio de término: el menú puede administrarse sin afectar las referencias históricas.

Estado actual: se implementaron el modelo, repositorio JDBC, pantalla y controlador de productos. La pantalla permite seleccionar categoría, asignar precio y controlar disponibilidad.

## Etapa 5: CRUD de clientes y empleados — implementada

1. Crear los modelos, repositorios, vistas y controladores necesarios.
2. Validar campos obligatorios y correo único para clientes cuando se proporcione.
3. Administrar el estado activo de empleados.
4. Explicar claramente las restricciones al intentar eliminar registros relacionados.

Criterio de término: clientes y empleados quedan disponibles para registrar ventas.

Estado actual: se implementaron ambos CRUD, con validación de correo, correo único en la base de datos y control del estado activo de empleados.

## Etapa 6: registro de ventas — implementada

1. Crear `Venta`, `DetalleVenta`, sus repositorios y `VentaServicio`.
2. Crear el carrito de venta con productos disponibles, cantidades, empleado, cliente opcional y método de pago.
3. Validar carrito no vacío, cantidades positivas y empleado activo.
4. Obtener el precio actual de cada producto al confirmar la venta.
5. Unir productos repetidos en el carrito.
6. Insertar la venta y todos los detalles dentro de una única transacción JDBC.
7. Ejecutar `commit()` si todo funciona o `rollback()` ante un error.
8. Registrar el resultado y errores técnicos con SLF4J, sin datos sensibles.

Criterio de término: una venta se guarda completa o no se guarda; los precios históricos quedan en `DetallesVenta.precio_unitario`.

Estado actual: se implementaron el carrito, los modelos, repositorios y `VentaServicio`. El servicio registra la venta y sus detalles en una única transacción y conserva el precio vigente como precio histórico.

## Etapa 7: historial y cancelación de ventas — implementada

1. Crear consultas para listar ventas y sus detalles.
2. Agregar filtros por fechas y estado.
3. Calcular el total con `SUM(cantidad * precio_unitario)`.
4. Cancelar con `estado = 'CANCELADA'`, sin eliminar registros ni detalles.

Criterio de término: las ventas canceladas se conservan y no se consideran ingresos.

Estado actual: se implementaron filtros por fecha y estado, consulta de detalles con precios históricos y cancelación mediante cambio de estado. El historial está disponible desde el menú principal.

## Etapa 8: reportes y PDF — implementada

1. Crear consultas de agregación en repositorios para ingresos, productos más vendidos, categorías, empleados, métodos de pago y ticket promedio.
2. Considerar únicamente ventas con estado `PAGADA`.
3. Incorporar gráficas JFreeChart en la pantalla de reportes.
4. Generar PDF con iText: encabezado, período, resumen y tabla o gráfica.
5. Comparar los resultados contra ventas conocidas antes de dar por terminada la etapa.

Criterio de término: las cifras son correctas, excluyen cancelaciones y el PDF se genera correctamente.

Estado actual: se implementaron consultas de agregación para los reportes previstos, gráficas JFreeChart visibles en JavaFX y exportación a PDF con iText.

## Etapa 9: revisión y entrega

1. Probar cada CRUD, venta, cancelación y reporte, incluyendo un caso de error común.
2. Verificar que controladores no contienen SQL y que los repositorios usan `PreparedStatement`.
3. Confirmar que las credenciales no están en Git ni en los logs.
4. Revisar que el cambio de precio actual no modifica ventas anteriores.
5. Documentar los pasos para iniciar el contenedor, configurar `.env` y ejecutar el proyecto.

Criterio de término: el flujo completo funciona: administrar menú, registrar ventas, consultar historial, cancelar y generar reportes.

## Orden de ejecución

Se completará una etapa antes de comenzar la siguiente: implementar, compilar, probar un caso correcto y uno de error, y continuar. El siguiente trabajo concreto es finalizar y probar la conexión de la Etapa 1.
