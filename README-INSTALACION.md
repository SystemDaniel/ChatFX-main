# 📱 ChatFX - Sistema de Transacciones Bancarias con Transferencias

**Versión 3.0** - Incluye soporte completo para transferencias y generación de vouchers JPEG

## 🎯 Nuevas Características de esta Versión

### ✨ Transferencias Bancarias Mejoradas
- ✅ Soporte para cuentas de 8 dígitos (locales y externas)
- ✅ Validación automática de cuentas externas según protocolo
- ✅ Transferencias remotas entre dispositivos con diferentes IPs
- ✅ Creación automática de cuentas externas al transferir

### 🧾 Generador de Vouchers JPEG Avanzado
- ✅ Información completa de transacción
- ✅ **Hora exacta y fecha** en formato legible
- ✅ **Cuenta destino** claramente visible para transferencias
- ✅ Detalles financieros (monto, saldo anterior/nuevo)
- ✅ Formato profesional con encabezado decorativo
- ✅ Almacenamiento automático en `Descargas/Vouchers/`

## 📋 Requisitos Previos

### Sistema Operativo
- **Windows 10 o superior**
- **macOS 10.13 o superior**
- **Linux (Ubuntu 18.04+)**

### Software Requerido
- **Java JDK 21 o superior** (Descargar de: https://adoptopenjdk.net/)
- **JavaFX SDK 21** (Descargar de: https://gluonhq.com/products/javafx/)
- **Git** (opcional, para clonar el repositorio)

## 🔧 Instalación de JavaFX (IMPORTANTE)

### Paso 1: Descargar JavaFX
1. Visita: https://gluonhq.com/products/javafx/
2. Selecciona: **"OpenJFX 21 Windows x64"** (o tu SO)
3. Descarga el archivo ZIP

### Paso 2: Instalar JavaFX
1. **Windows:**
   ```
   1. Extrae el ZIP descargado
   2. Crea una carpeta: C:\JavaFX
   3. Mueve la carpeta "javafx-sdk-21" a "C:\JavaFX\"
   4. Resultado: C:\JavaFX\javafx-sdk-21
   ```

2. **macOS:**
   ```bash
   unzip openjfx-21_osx-x64_bin-sdk.zip
   mv javafx-sdk-21 /Library/Frameworks/
   ```

3. **Linux:**
   ```bash
   unzip openjfx-21_linux-x64_bin-sdk.zip
   mv javafx-sdk-21 ~/opt/javafx-sdk-21
   ```

### Paso 3: Verificar Instalación
```
Si instalaste en C:\JavaFX\javafx-sdk-21, debería existir:
✓ C:\JavaFX\javafx-sdk-21\lib\javafx.controls.jar
✓ C:\JavaFX\javafx-sdk-21\lib\javafx.fxml.jar
```

## 💻 Compilación y Ejecución

### Opción 1: Usar Script (Windows)
```batch
double-click compile-chatfx.bat
```

El script buscará JavaFX automáticamente en las ubicaciones comunes.

### Opción 2: Compilar Manualmente

#### Windows (PowerShell):
```powershell
$JAVAFX = "C:\JavaFX\javafx-sdk-21"
javac --module-path "$JAVAFX\lib" `
      --add-modules javafx.controls,javafx.fxml `
      -cp "lib\*" -d bin `
      src\main\java\app\*.java `
      src\main\java\client\*.java `
      src\main\java\protocol\*.java `
      src\main\java\server\*.java
```

#### macOS/Linux (Bash):
```bash
export JAVAFX=/Library/Frameworks/javafx-sdk-21
javac --module-path $JAVAFX/lib \
      --add-modules javafx.controls,javafx.fxml \
      -cp "lib/*" -d bin \
      src/main/java/app/*.java \
      src/main/java/client/*.java \
      src/main/java/protocol/*.java \
      src/main/java/server/*.java
```

## 🚀 Ejecución

### Iniciar Servidor
```batch
REM Windows
set JAVAFX=C:\JavaFX\javafx-sdk-21
java --module-path "%JAVAFX%\lib" --add-modules javafx.controls,javafx.fxml -cp "bin;lib\*" server.ServidorGUI
```

```bash
# macOS/Linux
export JAVAFX=/Library/Frameworks/javafx-sdk-21
java --module-path $JAVAFX/lib --add-modules javafx.controls,javafx.fxml -cp "bin:lib/*" server.ServidorGUI
```

### Iniciar Cliente
```batch
REM Windows
set JAVAFX=C:\JavaFX\javafx-sdk-21
java --module-path "%JAVAFX%\lib" --add-modules javafx.controls,javafx.fxml -cp "bin;lib\*" client.ClienteGUI
```

```bash
# macOS/Linux
export JAVAFX=/Library/Frameworks/javafx-sdk-21
java --module-path $JAVAFX/lib --add-modules javafx.controls,javafx.fxml -cp "bin:lib/*" client.ClienteGUI
```

## 📊 Operaciones Disponibles

### 1. DEPÓSITO
- Enviar dinero a una cuenta
- **Requiere:** Cuenta destino (8 dígitos), Monto
- **Ejemplo:** Depositar $500 a cuenta 12345678

### 2. RETIRO
- Retirar dinero de tu cuenta
- **Requiere:** Tu cuenta (8 dígitos), Monto
- **Validación:** Saldo suficiente

### 3. CONSULTA
- Ver saldo actual de una cuenta
- **Requiere:** Número de cuenta (8 dígitos)

### 4. **TRANSFERENCIA** ⭐ (NUEVO)
- **Transferir dinero a otra cuenta**
- **Requiere:** Tu cuenta, Cuenta destino, Monto
- **Características:**
  - ✓ Cuentas locales (en el mismo dispositivo)
  - ✓ Cuentas externas (en otros dispositivos con IPs diferentes)
  - ✓ Validación automática de 8 dígitos
  - ✓ Creación automática de cuentas externas

### 5. **GENERAR VOUCHER** ⭐ (MEJORADO)
- Generar recibo JPEG de la transacción
- **Características:**
  - ✓ **Fecha exacta** (DD/MM/YYYY)
  - ✓ **Hora exacta** (HH:MM:SS)
  - ✓ **Cuenta origen**
  - ✓ **Cuenta destino** (si aplica)
  - ✓ **Montos y saldos**
  - ✓ Formato profesional
  - ✓ Guardado automático en: `C:\Users\{usuario}\Descargas\Vouchers\`

## 💳 Cuentas de Prueba Predefinidas

```
Cuenta: 10000001
Titular: Juan Pérez
Saldo: $5,000.00

Cuenta: 10000002
Titular: María García
Saldo: $3,500.00

Cuenta: 10000003
Titular: Carlos López
Saldo: $10,000.00
```

## 🔐 Protocolo de Comunicación

El sistema utiliza un protocolo de **tramas estructuradas**:

```
TRANSAC|TIPO_OPERACION|CUENTA_ORIGEN|CUENTA_DESTINO|MONTO|CONCEPTO
```

### Ejemplo de Transferencia:
```
TRANSAC|TRANSFERENCIA|10000001|12345678|500.00|Pago de servicios
```

### Respuesta del Servidor:
```
RESPUESTA|OK|Transferencia exitosa de $500.00 a cuenta 12345678
```

## 🎮 Guía Rápida de Uso

### Paso 1: Conectar
1. Inicia el servidor
2. Abre el cliente
3. Ingresa tu nombre de usuario
4. Ingresa IP y puerto del servidor (default: localhost:12345)
5. Haz clic en "Conectar"

### Paso 2: Realizar una Transferencia
1. Ingresa tu número de cuenta (o usa el automático)
2. Ingresa número de cuenta destino (8 dígitos)
3. Ingresa monto a transferir
4. Haz clic en "TRANSFER."
5. Espera confirmación del servidor

### Paso 3: Generar Voucher
1. Realiza una transacción (cualquiera)
2. Haz clic en "GENERAR VOUCHER"
3. El sistema crea un JPEG profesional
4. Se guarda automáticamente en `Descargas/Vouchers/`
5. ¡Conserva el recibo!

## 📝 Ejemplos de Uso

### Transferencia entre Cuentas del Sistema:
```
Mi cuenta: 10000001 (Juan Pérez - Saldo: $5,000)
Transferir a: 10000002 (María García)
Monto: $500

Resultado:
✓ Juan: $5,000 → $4,500
✓ María: $3,500 → $4,000
✓ Voucher generado con hora exacta y cuentas
```

### Transferencia a Cuenta Externa:
```
Mi cuenta: 10000001
Transferir a: 98765432 (cuenta externa/otro dispositivo)
Monto: $1,000

Resultado:
✓ Tu cuenta: $5,000 → $4,000
✓ Cuenta externa creada automáticamente
✓ Voucher con referencia de cuenta destino
```

## ❌ Solución de Problemas

| Problema | Causa | Solución |
|----------|-------|----------|
| "package javafx does not exist" | JavaFX no está en el classpath | Verifica que JavaFX está en C:\JavaFX\javafx-sdk-21 |
| "Connection refused" | Servidor no está ejecutándose | Inicia el servidor primero |
| "Cuenta no existe" | Número de cuenta no registrado | Usa cuentas de 8 dígitos (se crean automáticamente) |
| "Saldo insuficiente" | No hay fondos | Consulta tu saldo primero |
| Voucher no se genera | Transacción no exitosa | Realiza una transacción exitosa primero |
| Voucher vacío | Datos faltantes | Verifica que capturó correctamente los datos |

## 📂 Estructura de Carpetas

```
ChatFX-main/
├── src/main/java/
│   ├── app/              # Interfaz gráfica y utilidades
│   │   ├── App.java
│   │   ├── ClienteGUI.java (ACTUALIZADO)
│   │   ├── VoucherGenerator.java (ACTUALIZADO)
│   │   └── ...
│   ├── client/           # Cliente bancario
│   │   └── Cliente.java (ACTUALIZADO)
│   ├── server/           # Servidor bancario
│   │   └── Servidor.java
│   └── protocol/         # Protocolo de comunicación
│       ├── Frame.java
│       ├── FrameParser.java
│       ├── ProtocolConstants.java
│       └── TransactionProcessor.java (ACTUALIZADO)
├── lib/                  # Librerías externas
├── bin/                  # Archivos compilados
├── compile-chatfx.bat    # Script de compilación
└── README.md             # Este archivo
```

## 🔄 Flujo de una Transacción

```
1. Cliente envía transacción
   └─> TRANSAC|TRANSFERENCIA|10000001|12345678|500.00|Concepto

2. Servidor valida
   └─> ✓ Cuenta origen existe
   └─> ✓ Saldo suficiente
   └─> ✓ Cuenta destino válida (o se crea)

3. Servidor procesa
   └─> Debita cuenta origen
   └─> Acredita cuenta destino
   └─> Registra en historial

4. Servidor responde
   └─> RESPUESTA|OK|Transferencia exitosa...

5. Cliente genera voucher (opcional)
   └─> Captura datos
   └─> Dibuja JPEG
   └─> Guarda en Descargas/Vouchers/

6. ¡Transacción completada!
```

## 📄 Contenido del Voucher JPEG

```
┌────────────────────────────────────────┐
│            VOUCHER (encabezado azul)   │
├────────────────────────────────────────┤
│ INFORMACIÓN DE TRANSACCIÓN             │
│ Tipo: TRANSFERENCIA                    │
│ Transacción: TRANS_1716921234567       │
│ Usuario: Juan Pérez                    │
│ Cuenta Origen: 10000001                │
│ Cuenta Destino: 12345678               │
├────────────────────────────────────────┤
│ DETALLES FINANCIEROS                   │
│ Monto: $500.00                         │
│ Saldo Anterior: $5,000.00              │
│ Saldo Nuevo: $4,500.00                 │
├────────────────────────────────────────┤
│ FECHA Y HORA                           │
│ Fecha: 28/05/2026                      │
│ Hora: 14:35:45                         │
├────────────────────────────────────────┤
│ Gracias por usar nuestro servicio      │
│ Conserve este recibo                   │
└────────────────────────────────────────┘
```

## 🔗 Enlaces Útiles

- **JavaFX Official:** https://gluonhq.com/products/javafx/
- **OpenJDK:** https://adoptopenjdk.net/
- **Documentación:** Ver PROTOCOLO_Y_VOUCHERS.md
- **Protocolo:** Ver INSTRUCCIONES.md

## 📞 Soporte

Para problemas o preguntas:
1. Revisa la sección "Solución de Problemas" arriba
2. Verifica que Java y JavaFX estén correctamente instalados
3. Consulta los archivos de documentación incluidos

## 📝 Licencia y Créditos

**Proyecto:** ChatFX - Sistema de Transacciones Bancarias
**Versión:** 3.0
**Desarrollador:** SystemDaniel
**Fecha:** Mayo 2026
**Lenguaje:** Java 21 + JavaFX 21
**Base de Datos:** SQLite 3.44.0.0

## ✅ Checklist de Configuración

- [ ] Java JDK 21+ instalado
- [ ] JavaFX SDK 21 descargado
- [ ] JavaFX en C:\JavaFX\javafx-sdk-21 (Windows)
- [ ] Proyecto clonado o descargado
- [ ] Script compile-chatfx.bat ejecutado exitosamente
- [ ] Servidor compilado y ejecutable
- [ ] Cliente compilado y ejecutable
- [ ] Primera transferencia realizada
- [ ] Voucher generado correctamente

---

**¡Felicidades! Ahora tienes un sistema de transacciones bancarias completamente funcional con transferencias y vouchers.** 🎉
