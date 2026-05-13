package client;

import java.io.*;
import java.net.*;

public class Cliente {
    private String host = "localhost";
    private int puerto = 12345;
    private Socket socket;
    private PrintWriter salida;
    private BufferedReader entrada;
    private String nombre;
    private ClienteListener listener;

    public interface ClienteListener {
        void onMensajeRecibido(String mensaje);
        void onConexionCambiada(boolean conectado);
    }

    public Cliente(String nombre, ClienteListener listener) {
        this.nombre = nombre;
        this.listener = listener;
    }

    public Cliente(String nombre, String host, int puerto, ClienteListener listener) {
        this.nombre = nombre;
        this.host = host;
        this.puerto = puerto;
        this.listener = listener;
    }

    public boolean conectar() {
        try {
            socket = new Socket(host, puerto);
            salida = new PrintWriter(socket.getOutputStream(), true);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Enviar nombre al servidor
            salida.println(nombre);

            // Iniciar thread para recibir mensajes
            new Thread(this::recibirMensajes).start();

            if (listener != null) {
                listener.onConexionCambiada(true);
            }
            return true;
        } catch (IOException e) {
            System.err.println("[ERROR] No se pudo conectar: " + e.getMessage());
            if (listener != null) {
                listener.onConexionCambiada(false);
            }
            return false;
        }
    }

    public void enviarMensaje(String mensaje) {
        if (salida != null) {
            salida.println(mensaje);
        }
    }

    private void recibirMensajes() {
        try {
            String mensaje;
            while ((mensaje = entrada.readLine()) != null) {
                if (listener != null) {
                    listener.onMensajeRecibido(mensaje);
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Error recibiendo mensajes: " + e.getMessage());
        } finally {
            desconectar();
        }
    }

    public void desconectar() {
        try {
            if (salida != null) {
                salida.println("DESCONECTAR");
            }
            if (socket != null) {
                socket.close();
            }
            if (listener != null) {
                listener.onConexionCambiada(false);
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Error al desconectar: " + e.getMessage());
        }
    }

    public boolean estaConectado() {
        return socket != null && socket.isConnected();
    }

    public String getNombre() {
        return nombre;
    }
}
