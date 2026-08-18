# Plan de Limpieza: Solo Trivia Bíblica

Este plan detalla la eliminación de la funcionalidad "Completa el versículo" para dejar únicamente la "Trivia Bíblica".

## Cambios Propuestos

### [Manual] Registro de Cambios
#### [NEW] [cambios_realizados.txt](file:///C:/Users/jeuri/AndroidStudioProjects/BibliaRV1960/cambios_realizados.txt)
Archivo para documentar cada paso de la ejecución.

### [Data] Datos de Desafíos
#### [MODIFY] [challenges.json](file:///C:/Users/jeuri/AndroidStudioProjects/BibliaRV1960/app/src/main/assets/data/challenges.json)
Se eliminarán todos los objetos cuyo campo `"type"` sea `"FILL_VERSE"`.

### [UI Logic] ViewModel
#### [MODIFY] [ChallengeViewModel.kt](file:///C:/Users/jeuri/AndroidStudioProjects/BibliaRV1960/app/src/main/java/com/mi/bibliarv1960/ui/challenges/ChallengeViewModel.kt)
- Se eliminará la lógica de alternancia basada en `dayOfYear % 2`.
- El método de carga se simplificará para filtrar siempre por `TRIVIA`.

### [UI Layout] Pantallas
#### [MODIFY] [ChallengeScreen.kt](file:///C:/Users/jeuri/AndroidStudioProjects/BibliaRV1960/app/src/main/java/com/mi/bibliarv1960/ui/challenges/ChallengeScreen.kt)
- Se eliminará el componente visual de completar espacios.
- Se simplificará el encabezado para que siempre muestre "Trivia bíblica".
- Se eliminarán estados innecesarios como `allBlanksFilled`.

## Plan de Verificación

### Manual
- Abrir la sección de desafíos en la app.
- Verificar que solo aparezcan preguntas de trivia con opciones múltiples.
- Comprobar que el título sea siempre "Trivia bíblica".
