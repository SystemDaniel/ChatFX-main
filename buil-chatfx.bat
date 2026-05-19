@echo off
chcp 65001 >nul
title ChatFX - Build Portable

:: ============================================================
::  CONFIGURACION - Ajusta estas rutas si cambian
:: ============================================================
set JDK=C:\Program Files\Java\jdk-21.0.10\bin
set JAVAFX=C:\JavaFX\javafx-sdk-21
set PROJECT=C:\Users\odchalic\Music\ChatFX-main

:: ============================================================
::  NO MODIFICAR DESDE AQUI
:: ============================================================
cd /d "%PROJECT%"

echo.
echo ============================================
echo          ChatFX - Build Portable           
echo ============================================
echo.

:: PASO 1: Verificar Java
echo [1/9] Verificando versiones...
"%JDK%\java.exe" --version
"%JDK%\javac.exe" --version
echo.

:: PASO 2: Limpiar build anterior
echo [2/9] Limpiando build anterior...
if exist out        rmdir /S /Q out
if exist app.jar    del /Q app.jar
if exist input-pkg  rmdir /S /Q input-pkg
if exist output-pkg rmdir /S /Q output-pkg
if exist runtime-pkg rmdir /S /Q runtime-pkg
echo      Limpieza completada.
echo.

:: PASO 3: Crear carpeta de compilacion
echo [3/9] Creando carpetas...
mkdir out
mkdir input-pkg
mkdir input-pkg\lib
mkdir output-pkg
echo      Carpetas creadas.
echo.

:: PASO 4: Compilar fuentes
echo [4/9] Compilando fuentes Java...
"%JDK%\javac.exe" ^
--module-path "%JAVAFX%\lib" ^
--add-modules javafx.controls,javafx.fxml ^
-cp "lib/*" ^
-d out ^
src\main\java\app\*.java ^
src\main\java\client\*.java ^
src\main\java\protocol\*.java ^
src\main\java\server\*.java

if errorlevel 1 (
    echo.
    echo [ERROR] Fallo la compilacion. Revisa los errores arriba.
    pause
    exit /b 1
)
echo      Compilacion exitosa.
echo.

:: PASO 5: Crear JAR
echo [5/9] Creando app.jar...
"%JDK%\jar.exe" cfm app.jar MANIFEST.MF -C out .

if errorlevel 1 (
    echo.
    echo [ERROR] Fallo la creacion del JAR.
    pause
    exit /b 1
)
echo      app.jar creado.
echo.

:: PASO 6: Preparar input-pkg
echo [6/9] Preparando archivos de empaquetado...
copy app.jar input-pkg\ >nul
xcopy lib input-pkg\lib\ /E /I /Q >nul
echo      Archivos copiados a input-pkg.
echo.

:: PASO 7: Crear runtime con jlink
echo [7/9] Creando runtime personalizado con jlink...
"%JDK%\jlink.exe" ^
--module-path "%JAVAFX%\lib" ^
--add-modules javafx.controls,javafx.fxml,javafx.graphics,java.base,java.desktop,java.logging,java.naming,java.net.http,java.sql ^
--output runtime-pkg ^
--strip-debug ^
--no-header-files ^
--no-man-pages

if errorlevel 1 (
    echo.
    echo [ERROR] Fallo jlink.
    pause
    exit /b 1
)
echo      Runtime creado.
echo.

:: PASO 8: Empaquetar con jpackage
echo [8/9] Empaquetando ejecutable con jpackage...
"%JDK%\jpackage.exe" ^
--type app-image ^
--name ChatFX ^
--input input-pkg ^
--dest output-pkg ^
--main-jar app.jar ^
--runtime-image runtime-pkg ^
--java-options "-Dprism.order=sw" ^
--java-options "-Djava.awt.headless=false" ^
--java-options "-Dglass.platform=Win" ^
--java-options "-Djavafx.platform=win"

if errorlevel 1 (
    echo.
    echo [ERROR] Fallo jpackage.
    pause
    exit /b 1
)
echo      Ejecutable creado.
echo.

:: PASO 9: Copiar DLLs de JavaFX y base de datos
echo [9/9] Copiando DLLs de JavaFX y base de datos...
xcopy "%JAVAFX%\bin\*.dll" "output-pkg\ChatFX\runtime\bin\" /Y /Q >nul

if exist chatfx.db (
    copy chatfx.db "output-pkg\ChatFX\app\" >nul
    echo      chatfx.db copiada.
) else (
    echo      AVISO: chatfx.db no encontrada, se creara al primer uso.
)
echo      DLLs copiadas.
echo.

:: RESULTADO FINAL
echo ============================================
echo          Build completado con exito       
echo ============================================
echo.
echo  Ejecutable: output-pkg\ChatFX\ChatFX.exe
echo  Para distribuir comprime la carpeta: output-pkg\ChatFX\
echo.

:: Preguntar si desea comprimir
set /p COMPRIMIR="Deseas crear ChatFX-portable.zip ahora? (S/N): "

if /i "%COMPRIMIR%"=="S" (
    echo Comprimiendo...
    powershell Compress-Archive -Path "output-pkg\ChatFX" -DestinationPath "ChatFX-portable.zip" -Force
    echo ZIP creado: ChatFX-portable.zip
)

echo.
echo Presiona cualquier tecla para salir...
pause >nul