# Plan de Modernización de Textos Bíblicos (JSON)

Este plan detalla la corrección de arcaísmos ortográficos y gramaticales en los archivos de datos del proyecto (`challenges.json`, `devotionals.json`, `daily_verses.json`). El objetivo es alinear el contenido de las trivias, devocionales y versículos diarios con la versión modernizada de la Biblia RV1909/RV1960 utilizada en el resto de la aplicación.

## User Review Required

> [!IMPORTANT]
> Se realizarán cambios masivos en más de 250 líneas de texto. La mayoría son correcciones de acentuación que ya no se usan en el español moderno (ej. `á` -> `a`).

> [!NOTE]
> Las formas verbales con enclíticos (ej. `respondióle`) se cambiarán a formas modernas (`le respondió`) para mejorar la legibilidad del usuario final, siguiendo el ejemplo solicitado por el usuario.

## Proposed Changes

Se intervendrán los tres archivos JSON ubicados en `app/src/main/assets/data/`.

### [Componente: Datos de la Aplicación]

#### [MODIFY] [challenges.json](file:///C:/Users/jeuri/AndroidStudioProjects/BibliaRV1960/app/src/main/assets/data/challenges.json)
- Corrección de enclíticos: `respondióle` -> `le respondió`, `disgustóles` -> `les disgustó`, `vistiólos` -> `los vistió`, `cercóla` -> `la cercó`, `hízole` -> `le hizo`.
- Corrección de acentos arcaicos: `á` -> `a`, `ó` -> `o`, `fué` -> `fue`, `dió` -> `dio`, `vió` -> `vio`.
- Modernización de nombres/términos específicos: `Tharsis` -> `Tarsis`, `Séphora` -> `Séfora`, `empero` -> `pero`.

#### [MODIFY] [devotionals.json](file:///C:/Users/jeuri/AndroidStudioProjects/BibliaRV1960/app/src/main/assets/data/devotionals.json)
- Corrección de acentos arcaicos en `verse_text` y `reflection`: `á` -> `a`, `ó` -> `o`, `fué` -> `fue`, `dió` -> `dio`.
- Corrección de formas verbales arcaicas si se detectan.

#### [MODIFY] [daily_verses.json](file:///C:/Users/jeuri/AndroidStudioProjects/BibliaRV1960/app/src/main/assets/data/daily_verses.json)
- Corrección de acentos arcaicos en `verseText`: `á` -> `a`, `ó` -> `o`, `fué` -> `fue`.

## Verification Plan

### Automated Tests
- No aplican tests unitarios para cambios en assets JSON, pero se verificará la integridad del JSON mediante validación sintáctica tras las ediciones.

### Manual Verification
- Inspección visual de los archivos editados para asegurar que no se hayan roto etiquetas JSON o escapado caracteres incorrectamente.
- Verificación de que las palabras `a` y `o` no tengan tilde en contextos de preposición/conjunción.
- Verificación del caso específico `Juan 6:68` en la trivia.
