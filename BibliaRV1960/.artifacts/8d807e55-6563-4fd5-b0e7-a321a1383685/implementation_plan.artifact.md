# Plan de Optimización para Verbo Libre (Android)

Este plan detalla las mejoras estructurales, de rendimiento y de mantenimiento para el proyecto. El objetivo es reducir el tamaño de la aplicación, mejorar la estabilidad del ciclo de vida y modernizar las prácticas de desarrollo.

## User Review Required

> [!IMPORTANT]
> **Migración a Hilt**: Se propone introducir Hilt para la Inyección de Dependencias. Esto cambiará la forma en que se instancian los ViewModels y cómo se accede al repositorio desde `MainActivity`.
>
> **Eliminación de `material-icons-extended`**: Se eliminará esta librería para ahorrar ~5-10MB en el APK. Se importarán manualmente los iconos específicos necesarios.

## Proposed Changes

---

### 1. Build & Dependencies

#### [MODIFY] [libs.versions.toml](file:///C:/Users/jeuri/AndroidStudioProjects/BibliaRV1960/gradle/libs.versions.toml)
*   Añadir dependencias de Hilt.
*   Preparar la eliminación de `compose-icons-extended`.
*   Asegurar versiones estables de las librerías Core.

#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/jeuri/AndroidStudioProjects/BibliaRV1960/app/build.gradle.kts)
*   Aplicar plugins de Hilt.
*   Habilitar `optimization { enable = true }` en el bloque de `release`.
*   Subir `sourceCompatibility` y `targetCompatibility` a Java 17.

---

### 2. Arquitectura y DI (Inyección de Dependencias)

#### [NEW] [Hilt Modules](file:///C:/Users/jeuri/AndroidStudioProjects/BibliaRV1960/app/src/main/java/com/mi/bibliarv1960/di/AppModule.kt)
*   Definir módulos para proveer `Database`, `Dao`, `Repository` y `DataStoreManager`.

#### [MODIFY] [BibleApplication.kt](file:///C:/Users/jeuri/AndroidStudioProjects/BibliaRV1960/BibleApplication.kt)
*   Anotar con `@HiltAndroidApp` y eliminar las propiedades `lazy` manuales.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/jeuri/AndroidStudioProjects/BibliaRV1960/MainActivity.kt)
*   Anotar con `@AndroidEntryPoint`.
*   Simplificar la creación de ViewModels usando `by viewModels()`.

---

### 3. Refactorización de ViewModels

#### [MODIFY] [BibleViewModel.kt](file:///C:/Users/jeuri/AndroidStudioProjects/BibliaRV1960/app/src/main/java/com/mi/bibliarv1960/ui/viewmodel/BibleViewModel.kt)
*   Extraer la lógica de **TTS (Audio)** a un `AudioViewModel` o un `AudioHelper` inyectado.
*   Extraer la lógica de **Notas** (si es posible) completamente a `NotesViewModel`.
*   Corregir la fuga de memoria eliminando el paso de `Context` al ViewModel; usar un `SpeechManager` inyectado que gestione su propio ciclo de vida.

---

### 4. Modernización de Código y UI

#### [MODIFY] [ReaderScreen.kt](file:///C:/Users/jeuri/AndroidStudioProjects/BibliaRV1960/app/src/main/java/com/mi/bibliarv1960/ui/screens/ReaderScreen.kt)
*   Ajustar el uso de `WindowInsets` para un soporte real de Edge-to-Edge.
*   Optimizar el `Canvas` de marcadores para evitar cálculos redundantes.

#### [MODIFY] Global Date Handling
*   Reemplazar `SimpleDateFormat` por `java.time.LocalDate` y `DateTimeFormatter` en todo el proyecto.

---

### 5. Limpieza de Recursos

#### [DELETE] [ic_launcher-playstore.png](file:///C:/Users/jeuri/AndroidStudioProjects/BibliaRV1960/app/src/main/ic_launcher-playstore.png)
*   Mover fuera del directorio `src/main` para evitar que se incluya en el APK final.

#### [DELETE] Unused JSON files
*   Eliminar `gpt.json` y otros archivos temporales mencionados en el estado del proyecto.

## Verification Plan

### Automated Tests
*   Ejecutar `./gradlew assembleRelease` para verificar que R8 no rompa la persistencia de Room (se añadirán `keep` rules si es necesario).
*   Verificar la inyección de dependencias en el arranque de la app.

### Manual Verification
*   Probar el Lector Bíblico: asegurar que los marcadores y notas se renderizan correctamente.
*   Probar Audio Biblia: verificar que no haya crashes al rotar la pantalla o salir de la actividad.
*   Comparar el tamaño del APK antes y después de quitar `material-icons-extended`.
