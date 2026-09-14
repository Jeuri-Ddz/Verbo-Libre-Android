# Corrección de Crash en el Botón de Compartir Devocionales

Se ha detectado que la aplicación se cierra al intentar compartir un devocional debido a que el objeto `graphicsLayer` se utiliza para generar un bitmap sin haber grabado contenido previamente. La solución consiste en implementar un bloque de renderizado invisible (off-screen) que registre el contenido del devocional en la capa de gráficos.

## Cambios Propuestos

### Componente de UI

#### [MODIFY] [DevotionalScreen.kt](file:///C:/Users/jeuri/AndroidStudioProjects/BibliaRV1960/app/src/main/java/com/mi/bibliarv1960/ui/screens/DevotionalScreen.kt)

- Importar `ShareableVerseCard`.
- Añadir un bloque `Box` invisible en el nivel raíz de la pantalla que utilice `graphicsLayer.record` para renderizar una `ShareableVerseCard` con los datos del devocional actual.
- Asegurar que el fondo utilizado en la captura coincida con el fondo diario de la pantalla.

## Plan de Verificación

### Verificación Manual
1. Abrir la sección de Devocionales.
2. Pulsar el botón "Compartir".
3. Verificar que la aplicación NO se cierre.
4. Confirmar que se abre el selector de compartir de Android con la imagen generada correctamente.
