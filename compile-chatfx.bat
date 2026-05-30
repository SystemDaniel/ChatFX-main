@echo off
chcp 65001 >nul
title ChatFX - Compilación

REM ============================================================
REM  BÚSQUEDA AUTOMÁTICA DE JAVAFX
REM ============================================================

set "JAVAFX_PATH="

REM Búsqueda en ubicaciones comunes
for %%D in (
    "C:\JavaFX\javafx-sdk-21"
    "C:\Program Files\javafx-sdk-21"
    "C:\opt\javafx-sdk-21"
    "%USERPROFILE%\javafx-sdk-21"
) do (
    if exist "%%D\lib" (
        set "JAVAFX_PATH=%%D"
        goto found_javafx
    )
)

:javafx_not_found
echo.
echo ============================================
echo  ERROR: JavaFX no encontrado
echo ============================================
echo.
echo Por favor descarga JavaFX desde: https://gluonhq.com/products/javafx/
echo.
echo 1. Descarga: OpenJFX 21 (Windows x64 SDK)
echo 2. Extrae el archivo ZIP
echo 3. Coloca la carpeta en una de estas ubicaciones:
echo    - C:\JavaFX\javafx-sdk-21
echo    - C:\Program Files\javafx-sdk-21
echo    - C:\opt\javafx-sdk-21
echo.
echo Después intenta compilar nuevamente.
echo.
pause
exit /b 1

:found_javafx
echo.
echo ============================================
echo  ChatFX - Compilación
echo ============================================
echo.
echo JavaFX encontrado en: %JAVAFX_PATH%
echo.

cd /d "%~dp0"

REM Crear carpeta bin si no existe
if not exist "bin" mkdir bin

REM Compilar
echo Compilando...
javac --module-path "%JAVAFX_PATH%\lib" ^
      --add-modules javafx.controls,javafx.fxml ^
      -cp "lib\sqlite-jdbc-3.44.0.0.jar;lib\*" ^
      -d bin ^
      src\main\java\app\*.java ^
      src\main\java\client\*.java ^
      src\main\java\protocol\*.java ^
      src\main\java\server\*.java

if errorlevel 1 (
    echo.
    echo ERROR: Falló la compilación
    echo.
    pause
    exit /b 1
)

echo.
echo ✓ Compilación exitosa
echo.
echo Para ejecutar el cliente:
echo   java --module-path "%JAVAFX_PATH%\lib" --add-modules javafx.controls,javafx.fxml -cp "bin;lib\*" client.ClienteGUI
echo.
echo Para ejecutar el servidor:
echo   java --module-path "%JAVAFX_PATH%\lib" --add-modules javafx.controls,javafx.fxml -cp "bin;lib\*" server.ServidorGUI
echo.
pause
