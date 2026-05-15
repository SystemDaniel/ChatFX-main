@echo off
REM Script de compilación para ChatFX con Login

setlocal enabledelayedexpansion

REM Crear carpeta bin si no existe
if not exist bin mkdir bin

REM Compilar todas las clases
echo [*] Compilando ChatFX con sistema de Login...

javac -d bin -cp lib\* ^
  src\main\java\app\AccountNumberGenerator.java ^
  src\main\java\app\DatabaseInit.java ^
  src\main\java\app\UsuarioManager.java ^
  src\main\java\app\LoginGUI.java ^
  src\main\java\app\VoucherGenerator.java ^
  src\main\java\app\App.java ^
  src\main\java\protocol\ProtocolConstants.java ^
  src\main\java\protocol\Frame.java ^
  src\main\java\protocol\FrameParser.java ^
  src\main\java\protocol\TransactionProcessor.java ^
  src\main\java\server\Servidor.java ^
  src\main\java\server\ServidorGUI.java ^
  src\main\java\client\Cliente.java ^
  src\main\java\client\ClienteGUI.java

if %ERRORLEVEL% EQU 0 (
    echo [✓] Compilación exitosa!
    echo.
    echo [*] Para ejecutar:
    echo     java --module-path lib --add-modules javafx.controls,javafx.fxml -cp bin;lib\* app.App
) else (
    echo [✗] Error en la compilación
    exit /b 1
)

endlocal
