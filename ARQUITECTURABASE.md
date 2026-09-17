# Documentación de Arquitectura y Decisiones Técnicas - TerapIA

Este documento explica la estructura base del proyecto, las decisiones de arquitectura y los estándares que seguiremos como equipo para desarrollar **TerapIA**. El objetivo es mantener el código escalable, limpio y libre de conflictos al fusionar nuestras ramas.

## 1. Arquitectura del Proyecto (Clean Architecture + MVVM)

Hemos dividido el proyecto en capas estrictas. La regla de oro es: **Las capas internas no conocen a las externas**.

*   **`domain/` (El Corazón de la App):** Aquí viven las reglas de negocio puras. No hay nada de Android, Compose ni Firebase aquí.
    *   *¿Por qué?* Si en el futuro curso de Sistemas Distribuidos migramos la base de datos a microservicios (ej. Spring Boot), esta capa **no se toca**.
    *   *Contiene:* Modelos (`Usuario.kt`), Interfaces (`AuthRepository.kt`) y Casos de Uso (`LoginUseCase.kt`).
*   **`data/` (El Mundo Exterior):** Se encarga de conseguir los datos. Implementa las interfaces de `domain`.
    *   *¿Por qué?* Centraliza las llamadas a Firebase. La vista nunca debe llamar a Firestore directamente.
    *   *Contiene:* `AuthDataSource.kt` (llamadas directas a Firebase) y `AuthRepositoryImpl.kt` (traduce lo de Firebase a nuestros modelos de `domain`).
*   **`presentation/` (La Interfaz de Usuario):** Todo lo visual.
    *   *Contiene:* Las pantallas en Jetpack Compose (`LoginScreen.kt`) y los ViewModels. El ViewModel solo se comunica con los UseCases de `domain`.
*   **`core/` y `di/`:** Configuraciones transversales. Navegación e Inyección de Dependencias.

## 2. Gestión de Dependencias (Version Catalog)

En lugar de tener las versiones regadas en los archivos `build.gradle.kts`, usamos el archivo `gradle/libs.versions.toml`.

*   **Justificación Técnica:** Evita el clásico error de "en mi máquina sí compila". Centraliza las versiones. Además, usamos el **Firebase BoM (Bill of Materials) v34.19.0**.
*   *Nota para el equipo:* Google ya integró las funciones de Kotlin por defecto en Firebase, por lo que **hemos eliminado los sufijos `-ktx`** de las dependencias (`firebase-auth`, `firebase-firestore`).

## 3. Seguridad y Control de Acceso (RBAC)

El requerimiento de separar accesos entre "Paciente" y "Psicólogo" es crítico.

*   **Uso de Enums:** En `Usuario.kt`, el rol es un Enum (`PACIENTE`, `PSICOLOGO`), no un String libre ni un Boolean. Esto previene errores de tipeo silenciosos que romperían la navegación.
*   **Blindaje del Registro B2C:** En `RegistroUseCase.kt` y `AuthRepositoryImpl.kt`, la vista (UI) **no tiene permitido enviar el rol**. Todo usuario nuevo que se registre desde el app nace obligatoriamente con el rol `PACIENTE`.
*   **Justificación Técnica:** Nunca se debe confiar en los privilegios que envía el cliente (app), ya que un APK descompilado puede ser manipulado para enviar `rol = "psicologo"`. Las cuentas de psicólogos serán creadas manualmente o por un panel administrativo interno.
*   **Seguridad en la Nube:** Hemos respaldado esto en las Reglas de Firestore, impidiendo la creación de roles administrativos desde el cliente.

## 4. Navegación Type-Safe (Jetpack Compose)

En `core/navigation/Routes.kt` y `NavGraph.kt`, estamos usando la API de navegación más moderna de Google basada en objetos (Type-Safe Navigation) en lugar de Strings.

*   **Justificación Técnica:** Usar objetos `@Serializable` en lugar de textos como `"login"` hace que el compilador valide las rutas en tiempo real. Si cambiamos el nombre de una ruta o le agregamos un parámetro, Android Studio nos avisará de los errores antes de compilar, evitando que la app crashee en el celular.

## 5. Inyección de Dependencias (Koin)

Usamos **Koin** en lugar de Hilt. La configuración inicial ocurre en `TerapiaApp.kt` y los módulos viven en `di/AuthModule.kt`.

*   **Justificación Técnica:** Koin es ideal para este tamaño de equipo porque no requiere generación de código (KAPT/KSP), lo que hace que los tiempos de compilación sean mucho más rápidos y reduce drásticamente los errores extraños al hacer `git merge`. Para obtener una dependencia, el ViewModel simplemente usará `get()`.

---
**Flujo de Trabajo para el Equipo:**
1. Haz `git pull` de la rama principal (`main`/`develop`) para obtener esta base.
2. Crea tu propia rama: `git checkout -b feature/tu-funcionalidad`.
3. ¡Programa dentro de la capa que te corresponde según las tareas asignadas!