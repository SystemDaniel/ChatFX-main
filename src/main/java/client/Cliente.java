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

            // El servidor espera el nombre como primera línea (texto plano)
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
     * Crea y envía una trama de DEPOSITO
     * Formato: TRANSAC|DEPOSITO|CUENTA_ORIGEN|CUENTA_DESTINO|MONTO|CONCEPTO
     */
    public void enviarDeposito(long miCuenta, long cuentaDestino, double monto, String concepto) {
        Frame trama = new Frame(protocol.ProtocolConstants.TIPO_DEPOSITO, miCuenta, cuentaDestino, monto, concepto);
        enviarTrama(trama);
        System.out.println("[CLIENTE] Depósito enviado: $" + monto + " a cuenta " + cuentaDestino);
    }

    /**
     * Crea y envía una trama de RETIRO
     * Formato: TRANSAC|RETIRO|CUENTA_ORIGEN|9999|MONTO|CONCEPTO
     */
    public void enviarRetiro(long miCuenta, double monto, String concepto) {
        Frame trama = new Frame(protocol.ProtocolConstants.TIPO_RETIRO, miCuenta, 9999L, monto, concepto);
        enviarTrama(trama);
        System.out.println("[CLIENTE] Retiro enviado: $" + monto + " de cuenta " + miCuenta);
    }

    /**
     * Crea y envía una trama de CONSULTA
     * Formato: TRANSAC|CONSULTA|CUENTA_ORIGEN|9999|0.0|CONCEPTO
     */
    public void enviarConsulta(long miCuenta, String concepto) {
        Frame trama = new Frame(protocol.ProtocolConstants.TIPO_CONSULTA, miCuenta, 9999L, 0.0, concepto);
        enviarTrama(trama);
        System.out.println("[CLIENTE] Consulta enviada para cuenta " + miCuenta);
    }

    /**
     * Crea y envía una trama de TRANSFERENCIA
     * Formato: TRANSAC|TRANSFERENCIA|CUENTA_ORIGEN|CUENTA_DESTINO|MONTO|CONCEPTO
     */
    public void enviarTransferencia(long cuentaOrigen, long cuentaDestino, double monto, String concepto) {
        Frame trama = new Frame(protocol.ProtocolConstants.TIPO_TRANSFERENCIA, cuentaOrigen, cuentaDestino, monto, concepto);
        enviarTrama(trama);
        System.out.println("[CLIENTE] Transferencia enviada: $" + monto + " de " + cuentaOrigen + " a " + cuentaDestino);
    }

    private void recibirMensajes() {
        try {
            String linea;
            while ((linea = entrada.readLine()) != null) {
                // Intentar parsear como trama del protocolo del banco
                if (linea.startsWith("TRANSAC") || linea.startsWith("RESPUESTA")) {
                    Frame frame = FrameParser.parsear(linea);
                    if (frame != null && listener != null) {
                        listener.onTramaRecibida(frame);
                        listener.onMensajeRecibido(formatearTrama(frame));
                    } else if (listener != null) {
                        listener.onError("Trama mal formada: " + linea);
                    }
                } else {
                    // Mensaje de texto plano (compatibilidad)
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
        if (frame.esRespuesta()) {
            String status = frame.getStatus();
            String descripcion = frame.getDescripcion();
            
            if ("OK".equalsIgnoreCase(status)) {
                return "✓ " + descripcion;
            } else {
                return "❌ " + descripcion;
            }
        } else {
            return "[" + frame.getTipoOperacion() + "] Solicitud enviada";
        }
    }

    public void desconectar() {
        try {
            if (salida != null) {
                // Enviar comando simple de desconexión
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
