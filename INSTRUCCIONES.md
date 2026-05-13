# INSTRUCCIONES DE USO - ChatFX

## Estructura del Proyecto

El proyecto ha sido reorganizado y mejorado con la siguiente estructura:

```
ChatFX/
├── src/main/java/
│   ├── app/
│   │   └── App.java                 # Punto de entrada principal
│   ├── cliente/
│   │   ├── Cliente.java             # Lógica del cliente
│   │   └── Clienteapp.java          # GUI del cliente
│   └── servidor/
│       ├── Servidor.java            # Servidor sin GUI (consola)
│       ├── Servidorapp.java         # Servidor con GUI (JavaFX)
│       └── ClienteHandler.java      # Manejador de cliente
├── bin/                             # Archivos compilados
├── lib/                             # Librerías JavaFX
├── resources/
│   └── styles.css                   # Estilos CSS
├── run.bat                          # Ejecutar Panel Principal
├── runServidor.bat                  # Ejecutar Servidor (consola)
├── runServidorApp.bat               # Ejecutar Servidor (GUI)
├── runCliente.bat                   # Ejecutar Cliente
└── README.md                        # Documentación del proyecto
```

## Mejoras Implementadas

### 1. **Código Limpio y Bien Indentado**
   - 4 espacios de indentación consistente en todo el proyecto
   - Nomenclatura clara y descriptiva de variables
   - Métodos documentados con Javadoc

### 2. **Identificación de Clientes**
   - Cada cliente debe ingresar un nombre único
   - El servidor muestra quién envía cada mensaje
   - Mensajes del sistema para conexiones y desconexiones

### 3. **Arquitectura Mejorada**
   - `ClienteHandler`: Clase separada para manejar cada cliente
   - Separación clara entre lógica y GUI
   - Uso de threading seguro con `Collections.synchronizedList()`

### 4. **Interfaz Gráfica Mejorada**
   - CSS profesional con colores azul Material Design
   - Layouts responsivos y bien organizados
   - Área de chat con monospace font para mejor legibilidad

### 5. **Manejo de Conexiones Robusto**
   - Try-catch apropiados
   - Cierres de recursos correctos
   - Detección de desconexiones

## Cómo Ejecutar

### Opción 1: Usar los Scripts (Windows)

#### Panel Principal
```batch
run.bat
```
Esto abrirá la ventana principal desde donde puedes:
- Iniciar el Servidor (con GUI)
- Iniciar Clientes (múltiples instancias)

#### Servidor (Consola - Sin GUI)
```batch
runServidor.bat
```
Para pruebas rápidas sin interfaz gráfica

#### Servidor (Con GUI)
```batch
runServidorApp.bat
```
Servidor con interfaz gráfica para monitoreo

#### Cliente
```batch
runCliente.bat
```
Inicia un cliente. Puedes abrir múltiples ventanas.

### Opción 2: Compilar Manualmente

```batch
cd C:\Users\odchalic\Documents\Project\ChatFX
javac -d bin -cp "lib/*" src\main\java\app\*.java src\main\java\cliente\*.java src\main\java\servidor\*.java
```

Luego ejecutar:
```batch
java -cp bin;lib\* app.App
```

## Flujo de Operación

1. **Inicia el Servidor**
   - Haz clic en "Iniciar Servidor" en el panel principal
   - O ejecuta `runServidorApp.bat`

2. **Conecta Clientes**
   - Haz clic en "Iniciar Cliente" (múltiples veces para varios clientes)
   - O ejecuta `runCliente.bat` en diferentes terminales

3. **Ingresa tu Nombre**
   - Cada cliente debe ingresar un nombre único

4. **Chatea**
   - Escribe mensajes en el campo de texto
   - Presiona Enter o haz clic en "Enviar"
   - Todos los clientes connecados reciben el mensaje

5. **Cierra la Sesión**
   - Cierra la ventana para desconectarte

## Características

✅ **Chat en Tiempo Real**
- Sincronización instantánea de mensajes

✅ **Múltiples Clientes**
- Soporta conexiones simultáneas

✅ **Identificación**
- Cada usuario se identifica por nombre

✅ **Interfaz Intuitiva**
- Botones y campos de entrada claros
- Área de chat con historial

✅ **Servidor Robusto**
- Maneja múltiples conexiones
- Sistema de log detallado

✅ **Código de Calidad**
- Bien estructurado
- Fácil de mantener y extender

## Solución de Problemas

### "Puerto ya en uso"
- El puerto 12345 está en uso
- Solución: Cambiar el puerto en las clases (PUERTO = 12345)

### "Conexión rechazada"
- El servidor no está iniciado
- Solución: Inicia el servidor primero

### Problemas con CSS
- El archivo styles.css no se encuentra en resources/
- Solución: Verifica que exista en resources/styles.css

### Errores de compilación
- Verifica que JavaFX esté en la carpeta lib/
- Asegúrate de compilar con -cp "lib/*"

## Arquitectura del Sistema

```
┌─────────────────┐
│   Aplicación    │
│   Principal     │
│   (app.App)     │
└────────┬────────┘
         │
    ┌────┴────┐
    │          │
┌───▼──┐  ┌───▼──┐
│Panel │  │Panel │
│Serv. │  │Cli.  │
└───┬──┘  └───┬──┘
    │         │
┌───▼──┐  ┌───▼───────┐
│Serv. │  │Clienteapp │
│App   │  └───┬───────┘
└───┬──┘      │
    │    ┌────▼──────┐
    │    │  Cliente  │
    │    └────┬──────┘
    │         │
┌───▼─────────▼───┐
│ Puerto 12345    │
│ (Conexión TCP)  │
└─────────────────┘
```

## Datos de Protocolo

**Primer Mensaje**: Nombre del cliente (String)
**Mensajes Posteriores**: Contenido del chat (String)

Ejemplo:
```
Cliente A conecta -> "ChatA"
Cliente B conecta -> "ChatB"
ClienteA envía   -> "Hola a todos!"
```

## Prueba de Funcionalidad

1. Abre Panel Principal (`run.bat`)
2. Haz clic en "Iniciar Servidor"
3. Espera a que diga "Servidor iniciado en puerto 12345"
4. Abre otra terminal y ejecuta `runCliente.bat`
5. Ingresa nombre: "ChatA"
6. Abre otra terminal más y ejecuta `runCliente.bat`
7. Ingresa nombre: "ChatB"
8. En ChatA escribe: "Hola ChatB!"
9. Verás el mensaje en ambas ventanas del cliente y en el servidor

¡Listo! Tu sistema de chat está funcionando correctamente.
