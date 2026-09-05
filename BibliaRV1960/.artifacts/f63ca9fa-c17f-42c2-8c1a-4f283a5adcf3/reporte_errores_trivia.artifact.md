# Reporte de Errores y Arcaísmos en Trivia (challenges.json)

Se ha realizado una búsqueda de arcaísmos y discrepancias en las explicaciones de la trivia, tomando como referencia el error reportado en **Juan 6:68** (*respondióle* → *y le respondió*).

## Resumen de hallazgos
El archivo `challenges.json` contiene numerosos términos en español antiguo (propios de la versión Reina Valera 1909 original) que no coinciden con una versión modernizada.

### 1. Enclíticos en verbos (Pasado simple)
Se han detectado formas donde el pronombre se adjunta al final del verbo, lo cual resulta arcaico:

| ID | Referencia | Texto Actual | Propuesta de Cambio |
|:---|:---|:---|:---|
| 13 | Juan 6:68 | "Y **respondióle** Simón Pedro..." | "Y **le respondió** Simón Pedro..." |
| 123 | Jueces 4:21 | "...**metióle** la estaca..." | "...**le metió** la estaca..." |
| 50 | Nehemías 2:10 | "...**disgustóles** en extremo..." | "...**les disgustó** en extremo..." |
| 120 | Daniel 1:1 | "...y **cercóla**." | "...y **la cercó**." |
| 115 | Números 20:28 | "...y **vistiólos** á Eleazar..." | "...y **se los vistió** a Eleazar..." |

### 2. Acentuación Arcaica (Preposiciones y Conjunciones)
Se detectó un uso masivo de la tilde en la preposición "a" y la conjunción "o", lo cual ya no se usa en el español moderno:

- **á** (preposición): Aparece en casi todas las explicaciones (ej: "vino **á** Antioquía", "viendo **á** Pedro", "cántico **á** Jehová").
- **ó** (conjunción): Aparece en varios lugares (ej: "redención **ó** contrato").

### 3. Acentuación Arcaica en Monosílabos y Verbos
- **fué** -> **fue** (Líneas 1596, 1666, 1778, 2002)
- **dió** -> **dio** (Línea 1834)
- **vió** -> **vio** (Línea 1624)

## Comparación con Base de Datos (RV 1909)
Aunque no he podido extraer el texto directamente de los archivos binarios `.db`, el patrón de errores indica que las explicaciones en `challenges.json` conservan la ortografía original de 1909, mientras que el usuario requiere la versión modernizada que presumiblemente está en la base de datos del app.

### Nota Importante
Si se confirma que la base de datos `bible_multi.db` (traducción `rv1909`) ya tiene estos textos modernizados, se recomienda una actualización masiva de `challenges.json` para mantener la consistencia.

---
**Acción recomendada:** Proceder con la modernización de `challenges.json` una vez aprobada esta lista.
