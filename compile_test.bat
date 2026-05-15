@echo off
setlocal enabledelayedexpansion

echo [*] Compilando ChatFX con Login...
echo.

cd /d "C:\Users\dyang\OneDrive\Escritorio\Projectos-JFX\ChatFX-main"

REM Crear carpeta bin si no existe
if not exist bin mkdir bin

REM Limpiar compilaciones anteriores
del /Q bin\*.class 2>nul

REM Compilar todas las clases
echo [1/4] Compilando clases de aplicacion...
javac -d bin -encoding UTF-8 ^
  src\main\java\app\DatabaseInit.java ^
  src\main\java\app\UsuarioManager.java ^
  src\main\java\app\LoginGUI.java ^
  src\main\java\app\App.java

if %ERRORLEVEL% NEQ 0 (
    echo [✗] Error compilando app
    exit /b 1
)
echo [✓] OK

echo [2/4] Compilando clases de protocolo...
javac -d bin -encoding UTF-8 ^
  src\main\java\protocol\Frame.java ^
  src\main\java\protocol\FrameParser.java ^
  src\main\java\protocol\TransactionProcessor.java

if %ERRORLEVEL% NEQ 0 (
    echo [✗] Error compilando protocol
    exit /b 1
)
echo [✓] OK

echo [3/4] Compilando clases de servidor...
javac -d bin -encoding UTF-8 ^
  src\main\java\server\Servidor.java ^
  src\main\java\server\ServidorGUI.java

if %ERRORLEVEL% NEQ 0 (
    echo [✗] Error compilando servidor
    exit /b 1
)
echo [✓] OK

echo [4/4] Compilando clases de cliente...
javac -d bin -encoding UTF-8 ^
  src\main\java\client\Cliente.java ^
  src\main\java\client\ClienteGUI.java

if %ERRORLEVEL% NEQ 0 (
    echo [✗] Error compilando cliente
    exit /b 1
)
echo [✓] OK

echo.
echo [✓] Compilacion completada exitosamente!
echo.
echo Archivos compilados: bin\
echo.
dir /B bin\app\*.class 2>nul | find /c ".class" > nul && (
    echo [✓] Clases compiladas:
    dir /B bin\app\*.class
    dir /B bin\server\*.class
    dir /B bin\client\*.class
    dir /B bin\protocol\*.class
)

endlocal
