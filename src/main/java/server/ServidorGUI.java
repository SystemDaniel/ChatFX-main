package server;

import protocol.Frame;
import protocol.FrameParser;
import protocol.TransactionProcessor;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServidorGUI extends Application {
    private TextArea logArea;
    private ListView<String> listaClientes;
    private Label estadoLabel;
    private Button btnIniciar;
    private Button btnDetener;
    private Button btnAbrirChat;
    private int puertoServidor = 12345;
    private Set<ClientHandlerGUI> clientes = Collections.synchronizedSet(new HashSet<>());
    private ServerSocket serverSocket;
    private boolean activo = false;
    private TransactionProcessor processor;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ChatFX - Servidor de Transacciones Bancarias");
        primaryStage.setWidth(900);
        primaryStage.setHeight(650);

        processor = new TransactionProcessor();

        BorderPane root = new BorderPane();

        // Panel superior
        VBox topPanel = crearPanelSuperior();
        root.setTop(topPanel);

        // Panel central - Log y Transacciones
        HBox centerBox = crearPanelCentral();
        root.setCenter(centerBox);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> detenerServidor());
        primaryStage.show();

        log("[INFO] Servidor de Transacciones Iniciado");
        log("[INFO] Iniciando servicio automáticamente...");
        iniciarServidor();
    }

    private VBox crearPanelSuperior() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        HBox topBox = new HBox(10);
        topBox.setStyle("-fx-alignment: center-left;");

        btnIniciar = new Button("Iniciar Servidor");
        btnIniciar.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        btnIniciar.setOnAction(e -> iniciarServidor());

        btnDetener = new Button("Detener Servidor");
        btnDetener.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        btnDetener.setDisable(true);
        btnDetener.setOnAction(e -> detenerServidor());

        btnAbrirChat = new Button("Abrir Cliente");
        btnAbrirChat.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        btnAbrirChat.setOnAction(e -> abrirCliente());

        Button btnLimpiar = new Button("Limpiar Log");
        btnLimpiar.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        btnLimpiar.setOnAction(e -> logArea.clear());

        estadoLabel = new Label("Estado: Parado");
        estadoLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: red;");

        topBox.getChildren().addAll(btnIniciar, btnDetener, btnAbrirChat, btnLimpiar, new Separator(javafx.geometry.Orientation.VERTICAL), estadoLabel);
        topBox.setPrefHeight(40);

        panel.getChildren().add(topBox);
        return panel;
    }

    private HBox crearPanelCentral() {
        HBox centerBox = new HBox(10);
        centerBox.setPadding(new Insets(10));

        // Panel izquierdo - Log
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(400);
        logArea.setStyle("-fx-font-size: 11;");
        
        ScrollPane scrollLog = new ScrollPane(logArea);
        scrollLog.setFitToWidth(true);

        // Panel central - Clientes
        VBox panelClientes = crearPanelDeClientes();

        // Panel derecho - Cuentas
        VBox panelCuentas = crearPanelDeCuentas();

        centerBox.getChildren().addAll(scrollLog, panelClientes, panelCuentas);
        HBox.setHgrow(scrollLog, javafx.scene.layout.Priority.ALWAYS);

        return centerBox;
    }

    private VBox crearPanelDeClientes() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
        panel.setPrefWidth(200);

        Label titulo = new Label("Clientes Conectados");
        titulo.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        listaClientes = new ListView<>();
        listaClientes.setPrefHeight(400);

        Label contadorLabel = new Label("Total: 0");
        contadorLabel.setStyle("-fx-font-size: 12;");

        panel.getChildren().addAll(titulo, listaClientes, contadorLabel);
        return panel;
    }

    private VBox crearPanelDeCuentas() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
        panel.setPrefWidth(250);

        Label titulo = new Label("Cuentas Bancarias");
        titulo.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        TextArea areaCuentas = new TextArea();
        areaCuentas.setEditable(false);
        areaCuentas.setWrapText(true);
        areaCuentas.setStyle("-fx-font-size: 10; -fx-control-inner-background: #f9f9f9;");

        // Cargar información de cuentas
        StringBuilder sb = new StringBuilder();
        for (var cuenta : processor.obtenerTodasLasCuentas()) {
            sb.append(String.format("Cuenta: %d\nTitular: %s\nSaldo: $%.2f\n\n", 
                cuenta.getNumero(), cuenta.getTitular(), cuenta.getSaldo()));
        }
        areaCuentas.setText(sb.toString());

        Button btnRefrescar = new Button("Actualizar");
        btnRefrescar.setPrefWidth(150);
        btnRefrescar.setStyle("-fx-font-size: 11; -fx-padding: 8;");
        btnRefrescar.setOnAction(e -> {
            sb.setLength(0);
            for (var cuenta : processor.obtenerTodasLasCuentas()) {
                sb.append(String.format("Cuenta: %d\nTitular: %s\nSaldo: $%.2f\n\n", 
                    cuenta.getNumero(), cuenta.getTitular(), cuenta.getSaldo()));
            }
            areaCuentas.setText(sb.toString());
        });

        panel.getChildren().addAll(titulo, areaCuentas, btnRefrescar);
        VBox.setVgrow(areaCuentas, javafx.scene.layout.Priority.ALWAYS);
        return panel;
    }

    public void iniciarServidor() {
        if (activo) {
            log("[ADVERTENCIA] Servidor ya está en ejecución");
            return;
        }

        btnIniciar.setDisable(true);
        btnDetener.setDisable(false);
        btnAbrirChat.setDisable(false);

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(puertoServidor);
                activo = true;

                Platform.runLater(() -> {
                    estadoLabel.setText("Estado: Activo");
                    estadoLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: green;");
                    log("[INFO] Servidor escuchando en puerto " + puertoServidor);
                    log("[INFO] Sistema de transacciones bancarias activo");
                });

                while (activo) {
                    Socket clienteSocket = serverSocket.accept();
                    ClientHandlerGUI handler = new ClientHandlerGUI(clienteSocket, this, processor);
                    clientes.add(handler);
                    new Thread(handler).start();
                }
            } catch (IOException e) {
                if (activo) {
                    Platform.runLater(() -> log("[ERROR] Error en servidor: " + e.getMessage()));
                }
            }
        }).start();
    }

    public void detenerServidor() {
        if (!activo) {
            return;
        }
        activo = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            for (ClientHandlerGUI cliente : clientes) {
                cliente.desconectar();
            }
            clientes.clear();
        } catch (IOException e) {
            log("[ERROR] Error al detener: " + e.getMessage());
        }

        Platform.runLater(() -> {
            btnIniciar.setDisable(false);
            btnDetener.setDisable(true);
            btnAbrirChat.setDisable(false);
            estadoLabel.setText("Estado: Parado");
            estadoLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: red;");
            listaClientes.getItems().clear();
            log("[INFO] Servidor detenido");
        });
    }

    public synchronized void actualizarListaClientes() {
        Platform.runLater(() -> {
            listaClientes.getItems().clear();
            for (ClientHandlerGUI cliente : clientes) {
                listaClientes.getItems().add(cliente.getNombre());
            }
        });
    }

    public void log(String mensaje) {
        Platform.runLater(() -> logArea.appendText(mensaje + "\n"));
    }

    public synchronized void difundirMensaje(String mensaje, ClientHandlerGUI remitente) {
        for (ClientHandlerGUI cliente : clientes) {
            if (cliente != remitente) {
                cliente.enviarMensaje(mensaje);
            }
        }
    }

    public synchronized void difundirTrama(Frame trama, ClientHandlerGUI remitente) {
        for (ClientHandlerGUI cliente : clientes) {
            if (cliente != remitente) {
                cliente.enviarTrama(trama);
            }
        }
    }

    public synchronized void removerCliente(ClientHandlerGUI cliente) {
        clientes.remove(cliente);
        actualizarListaClientes();
    }

    private void abrirCliente() {
        try {
            client.ClienteGUI clienteGUI = new client.ClienteGUI();
            Stage ventanaCliente = new Stage();
            clienteGUI.start(ventanaCliente);
        } catch (Exception e) {
            log("[ERROR] No se pudo abrir cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class ClientHandlerGUI implements Runnable {
    private Socket socket;
    private PrintWriter salida;
    private BufferedReader entrada;
    private String nombre;
    private ServidorGUI servidor;
    private TransactionProcessor processor;

    public ClientHandlerGUI(Socket socket, ServidorGUI servidor, TransactionProcessor processor) {
        this.socket = socket;
        this.servidor = servidor;
        this.processor = processor;
        try {
            salida = new PrintWriter(socket.getOutputStream(), true);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            servidor.log("[ERROR] Error en ClientHandler: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            String primeraLinea = entrada.readLine();
            
            if (primeraLinea != null && primeraLinea.startsWith("<INICIO>")) {
                Frame registroFrame = FrameParser.parsear(primeraLinea);
                if (registroFrame != null && registroFrame.getTipo().equals("REGISTRO")) {
                    this.nombre = registroFrame.obtener("USUARIO");
                } else {
                    this.nombre = "Cliente_" + System.currentTimeMillis() % 1000;
                }
            } else {
                this.nombre = primeraLinea != null ? primeraLinea : "Cliente_" + System.currentTimeMillis() % 1000;
            }
            
            servidor.log("[CONEXIÓN] " + nombre + " conectado desde " + socket.getInetAddress().getHostAddress());
            servidor.actualizarListaClientes();
            
            Frame notificacion = new Frame("NOTIFICACION")
                .campo("TIPO", "CONEXION")
                .campo("USUARIO", nombre)
                .campo("MENSAJE", nombre + " se ha conectado");
            servidor.difundirTrama(notificacion, this);

            String linea;
            while ((linea = entrada.readLine()) != null) {
                procesarLinea(linea);
            }
        } catch (IOException e) {
            servidor.log("[ERROR] Conexión perdida con " + nombre);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            servidor.removerCliente(this);
            Frame notificacion = new Frame("NOTIFICACION")
                .campo("TIPO", "DESCONEXION")
                .campo("USUARIO", nombre);
            servidor.difundirTrama(notificacion, this);
            servidor.log("[DESCONEXIÓN] " + nombre + " desconectado");
        }
    }

    private void procesarLinea(String linea) {
        if (linea == null || linea.isEmpty()) {
            return;
        }

        if (linea.startsWith("<INICIO>")) {
            Frame frame = FrameParser.parsear(linea);
            if (frame == null) {
                Frame error = new Frame("RESPUESTA")
                    .campo("ESTADO", "ERROR")
                    .campo("MENSAJE", "Trama mal formada")
                    .campo("CODIGO", "TRAMA_INVALIDA");
                enviarTrama(error);
                servidor.log("[ERROR] Trama mal formada de " + nombre);
                return;
            }

            procesarTrama(frame);
        } else {
            if (linea.equals("DESCONECTAR")) {
                return;
            }
            servidor.log("[" + nombre + "]: " + linea);
            servidor.difundirMensaje("[" + nombre + "]: " + linea, this);
        }
    }

    private void procesarTrama(Frame trama) {
        servidor.log("[TRAMA] Tipo: " + trama.getTipo() + " De: " + nombre);
        
        switch (trama.getTipo().toUpperCase()) {
            case "DEPOSITO":
            case "RETIRO":
            case "CONSULTA":
            case "TRANSFERENCIA":
                procesarTransaccion(trama);
                break;
            
            case "DESCONEXION":
                servidor.log("[DESCONEXION] " + nombre + " solicitó desconexión");
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

    private void procesarTransaccion(Frame trama) {
        Frame respuesta = processor.procesarTrama(trama);
        enviarTrama(respuesta);
        logTransaccion(trama, respuesta);
    }

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
        
        servidor.log(log.toString());
    }

    public void enviarMensaje(String mensaje) {
        salida.println(mensaje);
    }

    public void enviarTrama(Frame trama) {
        if (salida != null && trama != null) {
            salida.println(trama.toString());
        }
    }

    public String getNombre() {
        return nombre;
    }

    public void desconectar() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
