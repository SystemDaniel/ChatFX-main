package server;

import protocol.Frame;
import protocol.FrameParser;
import protocol.TransactionProcessor;
import java.io.*;
import java.net.*;
import java.util.*;

public class Servidor {
    private static final int PUERTO = 12345;
    private static Set<ClientHandler> clientes = Collections.synchronizedSet(new HashSet<>());
    private static ServerSocket serverSocket;
    private static TransactionProcessor processor = new TransactionProcessor();

    public static void main(String[] args) {
        iniciarServidor();
    }

    public static void iniciarServidor() {
        try {
            serverSocket = new ServerSocket(PUERTO, 50, InetAddress.getByName("0.0.0.0"));
            System.out.println("=== SERVIDOR DE TRANSACCIONES INICIADO ===");
            System.out.println("Escuchando en puerto: " + PUERTO);
            System.out.println("IP: 0.0.0.0 (Todas las interfaces de red)");
            System.out.println("Los clientes remotos pueden conectarse usando la IP local de este equipo");
            System.out.println("Procesador de transacciones activo");
            System.out.println("Esperando conexiones...\n");

            while (true) {
                Socket clienteSocket = serverSocket.accept();
                System.out.println("[CONECTADO] Nuevo cliente: " + clienteSocket.getInetAddress().getHostAddress());
                
                ClientHandler handler = new ClientHandler(clienteSocket, processor);
                clientes.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Error en servidor: " + e.getMessage());
        }
    }

    public static synchronized void difundirMensaje(String mensaje, ClientHandler remitente) {
        for (ClientHandler cliente : clientes) {
            if (cliente != remitente) {
                cliente.enviarMensaje(mensaje);
            }
        }
    }

    public static synchronized void difundirTrama(Frame trama, ClientHandler remitente) {
        for (ClientHandler cliente : clientes) {
            if (cliente != remitente) {
                cliente.enviarTrama(trama);
            }
        }
    }

    public static synchronized void removerCliente(ClientHandler cliente) {
        clientes.remove(cliente);
        System.out.println("[DESCONECTADO] Cliente removido. Clientes activos: " + clientes.size());
    }

    public static synchronized int obtenerNumeroClientes() {
        return clientes.size();
    }

    public static synchronized List<String> obtenerListaClientes() {
        List<String> lista = new ArrayList<>();
        for (ClientHandler cliente : clientes) {
            lista.add(cliente.getNombre());
        }
        return lista;
    }

    public static TransactionProcessor obtenerProcesador() {
        return processor;
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter salida;
    private BufferedReader entrada;
    private String nombre;
    private TransactionProcessor processor;

    public ClientHandler(Socket socket, TransactionProcessor processor) {
        this.socket = socket;
        this.processor = processor;
        try {
            salida = new PrintWriter(socket.getOutputStream(), true);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.err.println("[ERROR] Error en ClientHandler: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            // Recibir trama de registro o nombre
            String primeraLinea = entrada.readLine();
            
            if (primeraLinea != null && primeraLinea.startsWith("<INICIO>")) {
                // Es una trama de registro
                Frame registroFrame = FrameParser.parsear(primeraLinea);
                if (registroFrame != null && registroFrame.getTipo().equals("REGISTRO")) {
                    this.nombre = registroFrame.obtener("USUARIO");
                } else {
                    this.nombre = "Cliente_" + System.currentTimeMillis() % 1000;
                }
            } else {
                // Compatibilidad: nombre en texto plano
                this.nombre = primeraLinea != null ? primeraLinea : "Cliente_" + System.currentTimeMillis() % 1000;
            }
            
            System.out.println("[NOMBRE] Cliente registrado como: " + nombre);
            
            // Notificar a otros clientes
            Frame notificacion = new Frame("NOTIFICACION")
                .campo("TIPO", "CONEXION")
                .campo("USUARIO", nombre)
                .campo("MENSAJE", nombre + " se ha conectado");
            Servidor.difundirTrama(notificacion, this);

            String linea;
            while ((linea = entrada.readLine()) != null) {
                procesarLinea(linea);
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Conexión perdida con " + nombre);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Servidor.removerCliente(this);
            
            // Notificar desconexión
            Frame notificacion = new Frame("NOTIFICACION")
                .campo("TIPO", "DESCONEXION")
                .campo("USUARIO", nombre);
            Servidor.difundirTrama(notificacion, this);
        }
    }

    /**
     * Procesa una línea recibida (puede ser trama o texto)
     */
    private void procesarLinea(String linea) {
        if (linea == null || linea.isEmpty()) {
            return;
        }

        if (linea.startsWith("<INICIO>")) {
            // Es una trama de protocolo
            Frame frame = FrameParser.parsear(linea);
            if (frame == null) {
                // Trama mal formada
                Frame error = new Frame("RESPUESTA")
                    .campo("ESTADO", "ERROR")
                    .campo("MENSAJE", "Trama mal formada")
                    .campo("CODIGO", "TRAMA_INVALIDA");
                enviarTrama(error);
                System.out.println("[ERROR] Trama mal formada de " + nombre + ": " + linea);
                return;
            }

            procesarTrama(frame);
        } else {
            // Mensaje de texto plano (compatibilidad)
            if (linea.equals("DESCONECTAR")) {
                return;
            }
            System.out.println("[" + nombre + "]: " + linea);
            Servidor.difundirMensaje("[" + nombre + "]: " + linea, this);
        }
    }

    /**
     * Procesa una trama según su tipo
     */
    private void procesarTrama(Frame trama) {
        System.out.println("[TRAMA RECIBIDA] Tipo: " + trama.getTipo() + " De: " + nombre);
        
        switch (trama.getTipo().toUpperCase()) {
            case "DEPOSITO":
            case "RETIRO":
            case "CONSULTA":
            case "TRANSFERENCIA":
                procesarTransaccion(trama);
                break;
            
            case "DESCONEXION":
                System.out.println("[DESCONEXION] Cliente " + nombre + " solicitó desconexión");
                break;
            
            default:
                Frame respuesta = new Frame("RESPUESTA")
                    .campo("ESTADO", "ERROR")
                    .campo("MENSAJE", "Tipo de operación no soportada: " + trama.getTipo())
                    .campo("CODIGO", "OPERACION_NO_SOPORTADA");
                enviarTrama(respuesta);
                break;
        }
    }

    /**
     * Procesa una transacción bancaria
     */
    private void procesarTransaccion(Frame trama) {
        // Procesar la transacción
        Frame respuesta = processor.procesarTrama(trama);
        
        // Enviar respuesta al cliente
        enviarTrama(respuesta);
        
        // Log de la transacción
        logTransaccion(trama, respuesta);
    }

    /**
     * Registra la transacción en el log
     */
    private void logTransaccion(Frame solicitud, Frame respuesta) {
        StringBuilder log = new StringBuilder();
        log.append("[TRANSACCION] ");
        log.append("Usuario: ").append(nombre).append(" | ");
        log.append("Tipo: ").append(solicitud.getTipo()).append(" | ");
        log.append("Estado: ").append(respuesta.obtener("ESTADO")).append(" | ");
        log.append("Mensaje: ").append(respuesta.obtener("MENSAJE"));
        
        if (respuesta.existe("CUENTA")) {
            log.append(" | Cuenta: ").append(respuesta.obtener("CUENTA"));
        }
        
        System.out.println(log.toString());
    }

    public void enviarMensaje(String mensaje) {
        salida.println(mensaje);
    }

    public void enviarTrama(Frame trama) {
        if (salida != null && trama != null) {
            String tramaCodificada = trama.toString();
            salida.println(tramaCodificada);
        }
    }

    public String getNombre() {
        return nombre;
    }
}
