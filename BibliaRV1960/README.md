# Verbo Libre — Biblia Android Offline 📖

![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Dagger Hilt](https://img.shields.io/badge/Dagger%20Hilt-2.60-orange?style=for-the-badge&logo=dagger&logoColor=white)
![Room](https://img.shields.io/badge/Room%20DB-2.6-green?style=for-the-badge&logo=android&logoColor=white)

**Verbo Libre** es una aplicación bíblica minimalista y de alto rendimiento para Android, diseñada para ofrecer una experiencia de lectura fluida y sin distracciones. El proyecto destaca por su arquitectura robusta y soluciones personalizadas para la gestión de contenido diario.

---

## 🚀 Características Principales

- **📖 Lector Avanzado:** Interfaz fluida con selector de versiones (RVA/VBL) y progreso de lectura visual.
- **🎙️ Texto a Voz (TTS):** Escucha los capítulos integrando el motor de síntesis de voz de Android de forma eficiente.
- **📅 Contenido Diario Inteligente:** Sistema de "Shuffled Deck" (Mazo barajado) que garantiza versículos y devocionales frescos cada día sin repeticiones.
- **✅ Seguimiento de Progreso:** Visualización clara del progreso por capítulos y libros con indicadores de color dinámicos.
- **💾 100% Offline:** Acceso total a las escrituras sin necesidad de conexión a internet, optimizado mediante importación de datos eficiente.

---

## 🛠️ Stack Tecnológico

- **Lenguaje:** Kotlin + Coroutines & Flow.
- **UI:** Jetpack Compose (Material 3).
- **Inyección de Dependencias:** Dagger Hilt para una gestión desacoplada de componentes.
- **Persistencia:** Room Database con indexación avanzada para búsquedas rápidas.
- **Arquitectura:** MVVM (Model-View-ViewModel) siguiendo los principios de Clean Architecture.
- **Rendimiento:** Implementación de geometrías pre-calculadas en el Canvas para evitar lag durante el scroll en pantallas con gran cantidad de marcadores.

---

## 🧠 Desafíos Técnicos Resueltos

### Lógica de Contenido Determinista
Uno de los mayores retos fue evitar la repetición de versículos diarios. Implementé un algoritmo basado en ciclos deterministas:
- Se utiliza una **semilla fija por ciclo** derivada de la fecha de inicio del usuario.
- El sistema baraja la lista de contenidos y entrega uno por día. Al finalizar el ciclo, se genera una nueva semilla automáticamente, garantizando variedad infinita sin duplicados en periodos cortos.

### Optimización de Renderizado
Para mantener 60 FPS durante la lectura, se refactorizó la pantalla del lector para pre-calcular la posición de notas y marcadores antes de la fase de dibujo de Compose, eliminando el sobrecosto de I/O en el hilo de UI.

---

## 📦 Instalación y Uso

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/Jeuri-Ddz/Verbo-Libre-Android.git
   ```
2. Abrir el proyecto en **Android Studio Jellyfish** o superior.
3. Sincronizar Gradle y ejecutar en un emulador o dispositivo físico (API 24+).

---

## ✒️ Autor
**Jeuri-Ddz** - Desarrollador Android enfocado en experiencias de usuario fluidas y arquitecturas modernas.

---
*Este proyecto fue desarrollado como parte de un portafolio profesional para demostrar el dominio de las herramientas modernas de desarrollo Android.*
