# Plan de Implementación: Ajustes de Lectura y Traducción VBL

Este plan detalla los cambios para establecer la **Versión Biblia Libre (VBL)** y el **Modo por Versículos** como predeterminados, además de renovar la sección de Ajustes con una vista previa interactiva.

## Cambios Propuestos

### Configuración y Valores por Defecto

#### [MODIFY] [DataStoreManager.kt](file:///C:/Users/jeuri/AndroidStudioProjects/BibliaRV1960/app/src/main/java/com/mi/bibliarv1960/data/preferences/DataStoreManager.kt)
- Cambiar el valor predeterminado de `SELECTED_TRANSLATION_ID` a `"vbl"`.
- Cambiar el valor predeterminado de `IS_DISCONTINUOUS_MODE` a `true`.

#### [MODIFY] [BibleViewModel.kt](file:///C:/Users/jeuri/AndroidStudioProjects/BibliaRV1960/app/src/main/java/com/mi/bibliarv1960/ui/viewmodel/BibleViewModel.kt)
- Actualizar `initialValue` de `selectedTranslationId` a `"vbl"`.

---

### Interfaz de Ajustes

#### [MODIFY] [SettingsScreen.kt](file:///C:/Users/jeuri/AndroidStudioProjects/BibliaRV1960/app/src/main/java/com/mi/bibliarv1960/ui/screens/SettingsScreen.kt)
- **Renombrar Ajuste:** Cambiar "Lectura descontinua" por **"Lectura continua"**.
- **Invertir Lógica del Switch:** El switch mostrará `!isDiscontinuous`.
- **Eliminar Tutorial:** Quitar la imagen `img_tutorial_discontinua`.
- **Nuevo Componente `ReadingModePreview`:**
    - Mostrará un ejemplo de texto de Génesis 1:1 y 1:2.
    - Si `Lectura continua` está **OFF** (por defecto): Mostrará versículos separados con una nota ficticia en rojo debajo del versículo 1.
    - Si `Lectura continua` está **ON**: Mostrará los versículos unidos en párrafo con el versículo 1 subrayado (indicando nota existente).

## Verificación Plan

### Manual Verification
1. **Instalación limpia:** Borrar datos de la app y verificar que inicie en **VBL** y con **Modo Versículo** (separados).
2. **Ajustes:**
    - Verificar que el switch "Lectura continua" aparezca **apagado** al inicio.
    - Al encenderlo, la vista previa debe cambiar a estilo párrafo y el subrayado debe ser visible en el ejemplo.
    - Al apagarlo, debe volver a mostrar la nota en rojo debajo del versículo.
3. **Lector:**
    - Confirmar que al activar "Lectura continua", los versículos se unan y las notas se conviertan en subrayados/iconos.
    - Confirmar que al desactivarlo, las notas vuelvan a aparecer en rojo entre versículos.
