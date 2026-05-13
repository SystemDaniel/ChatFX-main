package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Servidor {
    private static final int PUERTO = 12345;
    private static Set<ClientHandler> clientes = Collections.synchronizedSet(new HashSet<>());
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        iniciarServidor();
    }

    public static void iniciarServidor() {
        try {
            serverSocket = new ServerSocket(PUERTO);
            System.out.println("=== SERVIDOR INICIADO ===");
            System.out.println("Escuchando en puerto: " + PUERTO);
            System.out.println("Esperando conexiones...\n");

            while (true) {
                Socket clienteSocket = serverSocket.accept();
                System.out.println("[CONECTADO] Nuevo cliente: " + clienteSocket.getInetAddress().getHostAddress());
                
                ClientHandler handler = new ClientHandler(clienteSocket);
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
}

class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter salida;
    private BufferedReader entrada;
    private String nombre;

    public ClientHandler(Socket socket) {
        this.socket = socket;
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
            // Recibir nombre del cliente
            this.nombre = entrada.readLine();
            System.out.println("[NOMBRE] Cliente registrado como: " + nombre);
            
            // Notificar a otros clientes
            Servidor.difundirMensaje("[SISTEMA] " + nombre + " se ha conectado", this);

            String mensaje;
            while ((mensaje = entrada.readLine()) != null) {
                if (mensaje.equals("DESCONECTAR")) {
                    break;
                }
                System.out.println("[" + nombre + "]: " + mensaje);
                Servidor.difundirMensaje("[" + nombre + "]: " + mensaje, this);
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
            Servidor.difundirMensaje("[SISTEMA] " + nombre + " se ha desconectado", this);
        }
    }

    public void enviarMensaje(String mensaje) {
        salida.println(mensaje);
    }

    public String getNombre() {
        return nombre;
    }
}
