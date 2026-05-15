@echo off
setlocal enabledelayedexpansion

echo [*] Compilando ChatFX con burbujas de mensajes...
echo.

cd /d "C:\Users\dyang\OneDrive\Escritorio\Projectos-JFX\ChatFX-main"

REM Crear carpeta bin si no existe
if not exist bin mkdir bin

echo [*] Compilando...
javac -d bin -encoding UTF-8 ^
  src\main\java\app\*.java ^
  src\main\java\server\*.java ^
  src\main\java\client\*.java ^
  src\main\java\protocol\*.java 2>&1 | head -50

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [✓] Compilación exitosa!
    echo [✓] Los mensajes ahora aparecerán como burbujas celestes
) else (
    echo.
    echo [✗] Error en la compilación
    exit /b 1
)

endlocal
