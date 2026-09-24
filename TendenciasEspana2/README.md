# Tendencias España — Aplicación Android Nativa y Widget Jetpack Glance

Aplicación nativa para Android (Kotlin + Jetpack Compose + Material 3) conectada a la fuente oficial de **Google Trends España (geo=ES)** con Widget de Pantalla de Inicio configurable y redimensionable desarrollado con **Jetpack Glance**.

## Requisitos
- **Android Studio**: Ladybug (2024.2.1+), Koala o Jellyfish.
- **JDK**: Java 17 o Java 21 (incluido en Android Studio en *Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK*).
- **Android SDK**: API 35 (compileSdk 35, targetSdk 35, minSdk 26 - Android 8.0 Oreo o superior).

## Cómo compilar el proyecto en Android Studio
1. Descomprime el archivo ZIP descargado.
2. Abre **Android Studio**.
3. Selecciona **Open** (Abrir) y elige la carpeta raíz del proyecto (`TendenciasEspana`).
4. Espera a que Gradle descargue las dependencias y sincronice el proyecto (*Sync Project with Gradle Files*).
5. Para generar el APK de prueba:
   - En el menú superior: **Build > Build Bundle(s) / APK(s) > Build APK(s)**
   - O desde la pestaña *Terminal* integrada en Android Studio:
     ```bash
     ./gradlew assembleDebug
     ```
     *(En Windows CMD / PowerShell: `gradlew.bat assembleDebug`)*
6. El archivo APK generado estará disponible en:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

## Cómo ejecutar en un dispositivo o emulador
1. Conecta tu teléfono Android mediante depuración USB o inicia un emulador con API 26+.
2. Pulsa el botón verde **Run** (`Shift + F10`) en Android Studio.
3. Para probar el Widget de inicio:
   - Ve a la pantalla de inicio de tu launcher Android.
   - Mantén pulsado un espacio vacío y selecciona **Widgets**.
   - Busca **Tendencias España**.
   - Arrastra el widget al escritorio y redimensiónalo a 2x2, 4x2, 2x4 o 4x4.
