package server;

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

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ChatFX - Servidor");
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);

        BorderPane root = new BorderPane();

        // Panel superior
        VBox topPanel = crearPanelSuperior();
        root.setTop(topPanel);

        // Panel central - Log
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(400);
        
        ScrollPane scrollLog = new ScrollPane(logArea);
        scrollLog.setFitToWidth(true);

        // Panel derecho - Lista de clientes
        VBox rightPanel = crearPanelDeClientes();

        HBox centerBox = new HBox(10);
        centerBox.setPadding(new Insets(10));
        centerBox.getChildren().addAll(scrollLog, rightPanel);
        HBox.setHgrow(scrollLog, javafx.scene.layout.Priority.ALWAYS);

        root.setCenter(centerBox);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> detenerServidor());
        primaryStage.show();

        log("[INFO] Servidor iniciado. Iniciando servicio automáticamente...");
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

        btnAbrirChat = new Button("Abrir Chat");
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

    private VBox crearPanelDeClientes() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 0 1;");
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
                });

                while (activo) {
                    Socket clienteSocket = serverSocket.accept();
                    ClientHandlerGUI handler = new ClientHandlerGUI(clienteSocket, this);
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
            log("[ERROR] No se pudo abrir chat: " + e.getMessage());
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

    public ClientHandlerGUI(Socket socket, ServidorGUI servidor) {
        this.socket = socket;
        this.servidor = servidor;
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
            nombre = entrada.readLine();
            servidor.log("[CONEXIÓN] " + nombre + " conectado desde " + socket.getInetAddress().getHostAddress());
            servidor.actualizarListaClientes();
            servidor.difundirMensaje("[SISTEMA] " + nombre + " se ha conectado", this);

            String mensaje;
            while ((mensaje = entrada.readLine()) != null) {
                if (mensaje.equals("DESCONECTAR")) {
                    break;
                }
                servidor.log("[" + nombre + "]: " + mensaje);
                servidor.difundirMensaje("[" + nombre + "]: " + mensaje, this);
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
            servidor.difundirMensaje("[SISTEMA] " + nombre + " se ha desconectado", this);
            servidor.log("[DESCONEXIÓN] " + nombre + " desconectado");
        }
    }

    public void enviarMensaje(String mensaje) {
        salida.println(mensaje);
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
