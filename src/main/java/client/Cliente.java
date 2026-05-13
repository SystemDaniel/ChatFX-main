package client;

import protocol.Frame;
import protocol.FrameParser;
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
        void onTramaRecibida(Frame frame);
        void onConexionCambiada(boolean conectado);
        void onError(String error);
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

            // Enviar nombre al servidor como trama de registro
            Frame registroFrame = new Frame("REGISTRO").campo("USUARIO", nombre);
            enviarTrama(registroFrame);

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

    /**
     * Envía una trama al servidor
     */
    public void enviarTrama(Frame frame) {
        if (salida != null && frame != null) {
            String tramaCodificada = frame.toString();
            salida.println(tramaCodificada);
            System.out.println("[ENVIADO] " + tramaCodificada);
        }
    }

    /**
     * Envía un mensaje de texto (compatibilidad hacia atrás)
     */
    public void enviarMensaje(String mensaje) {
        if (salida != null) {
            salida.println(mensaje);
        }
    }

    /**
     * Crea y envía una trama de depósito
     */
    public void enviarDeposito(long cuenta, double monto) {
        Frame trama = new Frame("DEPOSITO")
            .campo("CUENTA", cuenta)
            .campo("MONTO", monto);
        enviarTrama(trama);
    }

    /**
     * Crea y envía una trama de retiro
     */
    public void enviarRetiro(long cuenta, double monto) {
        Frame trama = new Frame("RETIRO")
            .campo("CUENTA", cuenta)
            .campo("MONTO", monto);
        enviarTrama(trama);
    }

    /**
     * Crea y envía una trama de consulta
     */
    public void enviarConsulta(long cuenta) {
        Frame trama = new Frame("CONSULTA")
            .campo("CUENTA", cuenta);
        enviarTrama(trama);
    }

    /**
     * Crea y envía una trama de transferencia
     */
    public void enviarTransferencia(long cuentaOrigen, long cuentaDestino, double monto) {
        Frame trama = new Frame("TRANSFERENCIA")
            .campo("CUENTA_ORIGEN", cuentaOrigen)
            .campo("CUENTA_DESTINO", cuentaDestino)
            .campo("MONTO", monto);
        enviarTrama(trama);
    }

    private void recibirMensajes() {
        try {
            String linea;
            while ((linea = entrada.readLine()) != null) {
                // Intentar parsear como trama
                if (linea.startsWith("<INICIO>")) {
                    Frame frame = FrameParser.parsear(linea);
                    if (frame != null) {
                        if (listener != null) {
                            listener.onTramaRecibida(frame);
                        }
                        // También enviar como texto para compatibilidad
                        if (listener != null) {
                            listener.onMensajeRecibido(formatearTrama(frame));
                        }
                    } else {
                        if (listener != null) {
                            listener.onError("Trama mal formada: " + linea);
                        }
                    }
                } else {
                    // Mensaje de texto plano
                    if (listener != null) {
                        listener.onMensajeRecibido(linea);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Error recibiendo mensajes: " + e.getMessage());
        } finally {
            desconectar();
        }
    }

    /**
     * Formatea una trama para mostrarla de manera legible
     */
    private String formatearTrama(Frame frame) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(frame.getTipo()).append("] ");
        
        switch (frame.getTipo()) {
            case "RESPUESTA":
                sb.append(frame.obtener("ESTADO")).append(" - ").append(frame.obtener("MENSAJE"));
                if (frame.existe("SALDO")) {
                    sb.append(" | Saldo: $").append(frame.obtener("SALDO"));
                }
                break;
            default:
                for (var entry : frame.getCampos().entrySet()) {
                    sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(" ");
                }
        }
        
        return sb.toString();
    }

    public void desconectar() {
        try {
            if (salida != null) {
                Frame desconexionFrame = new Frame("DESCONEXION");
                salida.println(desconexionFrame.toString());
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
