# Sistema de diseño — Cafetería escolar

Este documento es la referencia visual de la aplicación. El estilo toma como punto de partida una cafetería limpia y cálida: superficies claras, acento café y tarjetas suaves, sin adornos que compitan con los datos.

> **Nota técnica:** la paleta se define en HSL. JavaFX CSS no admite la función `hsl()` ni variables CSS; por ello `src/main/resources/estilos/cafeteria.css` usa el equivalente hexadecimal de cada token. No se deben elegir colores nuevos directamente en hexadecimal: primero se define o ajusta aquí en HSL y después se convierte.

## Tipografía

- **Familia:** `System`, fuente nativa de JavaFX en cada plataforma. Evita depender de fuentes no instaladas.
- **Título de pantalla:** 28 px, peso 700, color `texto-principal`.
- **Título de sección:** 18 px, peso 700.
- **Título de tarjeta / etiqueta destacada:** 13 px, peso 600.
- **Texto base y controles:** 14 px, peso 400.
- **Texto auxiliar:** 12 px, color `texto-secundario`.
- Usar mayúsculas solo en etiquetas cortas de contexto; no en párrafos ni botones.

## Paleta

| Token | HSL (fuente de verdad) | Equivalente JavaFX | Uso |
| --- | --- | --- | --- |
| `fondo-app` | `hsl(30, 33%, 97%)` | `#fcf8f4` | Fondo general |
| `superficie` | `hsl(0, 0%, 100%)` | `#ffffff` | Tarjetas, tabla y formularios |
| `superficie-suave` | `hsl(30, 25%, 94%)` | `#f4eee7` | Navegación y áreas secundarias |
| `borde` | `hsl(27, 18%, 82%)` | `#d8cec5` | Bordes discretos |
| `texto-principal` | `hsl(24, 22%, 15%)` | `#2e2723` | Títulos y contenido principal |
| `texto-secundario` | `hsl(22, 10%, 39%)` | `#6e625b` | Descripciones y etiquetas |
| `cafe` | `hsl(22, 56%, 31%)` | `#7b3c23` | Acción principal y barras de gráfica |
| `cafe-hover` | `hsl(22, 60%, 24%)` | `#622d18` | Estado hover/pressed |
| `miel` | `hsl(32, 82%, 47%)` | `#da8516` | Importes y énfasis cálido |
| `exito` | `hsl(145, 43%, 30%)` | `#2c6d48` | Confirmaciones y exportar |
| `peligro` | `hsl(2, 57%, 43%)` | `#ad342e` | Acciones destructivas |
| `informacion` | `hsl(205, 50%, 34%)` | `#2b6682` | Estados informativos |

El texto `texto-principal` sobre `superficie` y el texto blanco sobre `cafe`, `exito` o `peligro` son las combinaciones obligatorias para texto normal. No usar `miel` como fondo de un botón con texto blanco.

## Espaciado y tamaños

La unidad base es **4 px**. Usar únicamente estos pasos: `4`, `8`, `12`, `16`, `20`, `24`, `32` y `40` px.

- Pantallas: margen interior de 24 px.
- Separación dentro de un bloque: 8 o 12 px.
- Separación entre secciones: 20 o 24 px.
- Tarjetas: 16 px de relleno; 12 px entre tarjetas.
- Controles: 36 px de alto mínimo; 10 px horizontales y 8 px verticales en botones.
- Iconos: 16 px en controles; 18 px en la navegación; 22 px en elementos de marca.

## Forma, elevación e interacción

- Botones, campos y ComboBox: radio de **8 px**.
- Tarjetas y paneles: radio de **12 px**.
- Las acciones principales usan `cafe`; las secundarias son blancas con borde; las destructivas usan `peligro`.
- La interfaz no usa tablas: muestra tarjetas seleccionables sobre un fondo limpio, sin cuadrícula ni encabezados de columnas. La selección añade fondo suave y un contorno café uniforme de 1 px, sin franjas laterales ni cambios de relleno.
- En hover, oscurecer el fondo; no cambiar el tamaño del componente.
- Inputs sin borde de layout: el contorno se pinta con dos fondos superpuestos (1 px normal, 2 px enfocado). JavaFX no admite `outline`; estas capas cumplen la misma finalidad sin alterar dimensiones ni relleno.
- Todo botón de navegación debe llevar icono y texto. Los iconos aclaran la acción, no la sustituyen.

## Aplicación

1. Cargar BootstrapFX primero y `cafeteria.css` después; el segundo archivo adapta BootstrapFX sin modificar la dependencia.
2. Aplicar las clases `vista`, `tarjeta`, `tarjeta-resumen`, `titulo-pantalla`, `texto-secundario`, `boton-secundario` y `boton-icono` según corresponda.
3. Mantener gráficas y PDF con la misma paleta: café como serie principal, miel como destaque y fondo blanco.
4. Antes de introducir una clase o color nuevo, comprobar si un token y una clase existentes resuelven el caso.

## Componentes visuales de registros

`Tarjetas.java` contiene celdas concretas para cada entidad y comparte únicamente su composición visual. Las listas siguen siendo `ListView` nativas: conservan desplazamiento, selección con teclado y reciclado de celdas. No hay SQL ni modelos nuevos para presentación.

- **Producto:** emblema de café, categoría, nombre, descripción, precio MXN y disponibilidad escrita.
- **Categoría:** emblema de etiquetas, identificador, nombre y descripción.
- **Cliente:** emblema personal, nombre, teléfono y correo; datos opcionales muestran «No registrado».
- **Empleado:** nombre, puesto e insignia Activo/Inactivo.
- **Venta:** recibo con folio, fecha, cliente, empleado, pago, total histórico y estado. Seleccionarlo abre el detalle lateral.
- **Consumo:** producto, cantidad × precio unitario e importe. Se reutiliza en carrito e historial.
- **Reporte:** concepto, unidad y valor destacado; mismo lenguaje visual que los demás módulos.

### Anatomía y comportamiento

- Radio 12 px, relleno 16 px, separación vertical 12 px.
- Emblema 44 × 44 px con icono Ikonli de 20 px.
- Nombre 17 px en negrita; contexto 12 px; importe 20 px en café (no miel, para mejorar legibilidad).
- Insignia de estado 11 px con radio 20 px. El estado siempre aparece escrito, no depende solo del color.
- Nombres y descripciones admiten varias líneas; las tarjetas se ajustan al ancho de la lista.
- Los formularios se presentan en una tarjeta lateral, alineada arriba y sin extenderse artificialmente hasta el fondo.
- Ventana inicial 1280 × 820; mínimo 1100 × 720 para conservar espacio de lectura en escritorio.
- No inventar fotografías de productos: el esquema no incluye imágenes; se usan iconos reales de FontAwesome 5.
- Listas vacías muestran una indicación específica para cada módulo.

### Verificación reproducible

Ejecutar `bash scripts/verificar-vistas.sh` en un entorno con escritorio JavaFX. Carga los ocho FXML, verifica inyección y aplica CSS; comprueba iconos, selección y reciclado de tarjetas. Genera `target/productos-visual.png` con datos de prueba. Omite la inicialización que consulta MySQL: no sustituye pruebas de CRUD contra la base real.

La exportación PDF conserva su tabla de detalle documental; la sustitución por tarjetas corresponde a todas las pantallas interactivas.
