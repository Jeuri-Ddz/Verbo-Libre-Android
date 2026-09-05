# Sistema de Aleatoriedad "Mazo Barajado" y Limpieza de Debug

Este plan detalla la transición del sistema de selección aleatoria basado en hash a un sistema de "Mazo Barajado" (Shuffled Deck) determinista y con ciclos, asegurando variedad total y cero repeticiones a corto plazo. También incluye la limpieza de herramientas de depuración manual.

## User Review Required

> [!IMPORTANT]
> Se introducirá una nueva clave en DataStore: `first_launch_date`. La primera vez que la app se abra con esta versión, se marcará ese día como el "Día 0" para el ciclo de versículos y devocionales.

> [!WARNING]
> La eliminación de `DebugConfig.kt` desactivará la posibilidad de forzar devocionales específicos mediante código. Si se desea mantener esta capacidad para QA, se recomienda implementar una pantalla oculta de "Ajustes de Desarrollador" en el futuro.

## Proposed Changes

### [Data Preferences]

#### [MODIFY] [DataStoreManager.kt](file:///C:/Users/jeuri/AndroidStudioProjects/BibliaRV1960/app/src/main/java/com/mi/bibliarv1960/data/preferences/DataStoreManager.kt)
- Añadir `FIRST_LAUNCH_DATE` (String) para persistir el día de inicio.
- Implementar el Flow y la función de guardado correspondientes.

### [Logic & ViewModels]

#### [MODIFY] [BibleViewModel.kt](file:///C:/Users/jeuri/AndroidStudioProjects/BibliaRV1960/app/src/main/java/com/mi/bibliarv1960/ui/viewmodel/BibleViewModel.kt)
- **`initializeDailyData()`**:
    - Obtener/Guardar `firstLaunchDate` usando `LocalDate`.
    - Calcular `diasTranscurridos` y `numeroDeCiclo`.
    - Implementar el shuffle determinista usando `Random(seed + numeroDeCiclo)`.
    - Seleccionar el versículo y devocional del día basado en `diaDentroDelCiclo`.
    - Sincronizar la selección de fondos con la misma lógica determinista.
- **Limpieza**:
    - Eliminar toda referencia a `DebugConfig.DEBUG_FORCE_DEVOTIONAL_ID`.
    - Limpiar la lógica de `nextDevotionalPreview` para que use el orden normal del ciclo.

### [Cleanup]

#### [DELETE] [DebugConfig.kt](file:///C:/Users/jeuri/AndroidStudioProjects/BibliaRV1960/app/src/main/java/com/mi/bibliarv1960/DebugConfig.kt)
- Eliminar el archivo de configuración de depuración manual.

## Verification Plan

### Automated Tests
- Verificar que `diasTranscurridos` se calcule correctamente al cruzar medianoche (simulando fechas).
- Comprobar que el shuffle es idéntico en múltiples arranques para el mismo día y semilla.

### Manual Verification
- Verificar que el versículo y devocional no cambian al reiniciar la app durante el mismo día.
- Comprobar en `SettingsScreen` (si se añade temporalmente un log) que el `numeroDeCiclo` es coherente.
- Confirmar que la app compila y arranca sin el archivo `DebugConfig.kt`.
