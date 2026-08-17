# Plan de Refinamiento: Pantalla de Retos Adaptativa

Este plan detalla las mejoras para la pantalla "Reto del Día", asegurando que el contenido sea 100% adaptable, no requiera scroll vertical y ajuste el tamaño de fuente de forma dinámica y robusta.

## User Review Required

> [!IMPORTANT]
> Se implementará un componente de diseño personalizado `SimpleFlowLayout` para sustituir el uso de `Column` en el versículo. Esto permitirá que el texto y los espacios en blanco fluyan como un párrafo natural sin depender del componente experimental `FlowRow`.

## Proposed Changes

### UI Component: ChallengeScreen

#### [MODIFY] [ChallengeScreen.kt](file:///C:/Users/jeuri/AndroidStudioProjects/BibliaRV1960/app/src/main/java/com/mi/bibliarv1960/ui/challenges/ChallengeScreen.kt)

- **Optimización de Escala**: Refinar el bucle de ajuste de `textSize` para evitar recomposiciones innecesarias y asegurar que el contenido se ajuste en un solo paso proporcional.
- **Implementación de `SimpleFlowLayout`**: Crear un componente de diseño interno que distribuya los fragmentos de texto y los "blanks" (espacios para completar) de forma horizontal, saltando de línea solo cuando sea necesario.
- **Mejora en `FillVerseLayout`**:
    - Integrar `SimpleFlowLayout` para el versículo.
    - Asegurar que los botones del banco de palabras también se ajusten proporcionalmente al `textSize`.
    - Unificar el diseño del contenedor de explicación con el de Trivia.
- **Mejora en `TriviaLayout`**:
    - Asegurar que las opciones de respuesta no excedan el espacio disponible reduciendo su fuente si es necesario.

## Verification Plan

### Automated Tests
- Se verificará la compilación del proyecto tras los cambios.
- Se puede usar `render_compose_preview` para visualizar el ajuste en diferentes tamaños (si hay previews disponibles).

### Manual Verification
- Probar el ciclo de retos pulsando en "Verbo Libre" para verificar que diferentes longitudes de versículos y preguntas de trivia se ajusten correctamente sin scroll.
- Verificar el feedback visual (colores verde/rojo) al completar un reto.
