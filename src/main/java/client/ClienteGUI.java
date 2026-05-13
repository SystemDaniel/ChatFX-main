package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ClienteGUI extends Application implements Cliente.ClienteListener {
    private TextArea areaChat;
    private TextField campoMensaje;
    private TextField campoNombre;
    private TextField campoHost;
    private TextField campoPuerto;
    private Button btnConectar;
    private Button btnDesconectar;
    private Button btnEnviar;
    private Label estadoLabel;
    private Cliente cliente;
    private boolean conectado = false;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ChatFX - Cliente");
        primaryStage.setWidth(600);
        primaryStage.setHeight(500);

        BorderPane root = new BorderPane();

        // Panel superior - Conexión
        HBox panelConexion = crearPanelConexion();
        root.setTop(panelConexion);

        // Panel central - Chat
        VBox panelChat = crearPanelChat();
        root.setCenter(panelChat);

        // Panel inferior - Envío de mensajes
        HBox panelEnvio = crearPanelEnvio();
        root.setBottom(panelEnvio);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox crearPanelConexion() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        Label lblNombre = new Label("Nombre:");
        campoNombre = new TextField();
        campoNombre.setPromptText("Tu nombre de usuario");
        campoNombre.setPrefWidth(140);
        campoNombre.setText("Usuario" + System.currentTimeMillis() % 1000);

        Label lblHost = new Label("IP:");
        campoHost = new TextField();
        campoHost.setPromptText("127.0.0.1");
        campoHost.setPrefWidth(120);
        campoHost.setText("localhost");

        Label lblPuerto = new Label("Puerto:");
        campoPuerto = new TextField();
        campoPuerto.setPromptText("12345");
        campoPuerto.setPrefWidth(80);
        campoPuerto.setText("12345");

        btnConectar = new Button("Conectar");
        btnConectar.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        btnConectar.setOnAction(e -> conectarAlServidor());

        btnDesconectar = new Button("Desconectar");
        btnDesconectar.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        btnDesconectar.setDisable(true);
        btnDesconectar.setOnAction(e -> desconectarDelServidor());

        estadoLabel = new Label("Estado: Desconectado");
        estadoLabel.setStyle("-fx-font-size: 12; -fx-text-fill: red;");

        Separator separator = new Separator(javafx.geometry.Orientation.VERTICAL);

        panel.getChildren().addAll(lblNombre, campoNombre, lblHost, campoHost, lblPuerto, campoPuerto, btnConectar, btnDesconectar, separator, estadoLabel);
        HBox.setHgrow(estadoLabel, Priority.ALWAYS);

        return panel;
    }

    private VBox crearPanelChat() {
        VBox panel = new VBox();
        panel.setPadding(new Insets(10));

        Label titulo = new Label("Chat");
        titulo.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        areaChat = new TextArea();
        areaChat.setEditable(false);
        areaChat.setWrapText(true);
        areaChat.setStyle("-fx-font-size: 12; -fx-control-inner-background: #f5f5f5;");

        ScrollPane scroll = new ScrollPane(areaChat);
        scroll.setFitToWidth(true);

        panel.getChildren().addAll(titulo, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        return panel;
    }

    private HBox crearPanelEnvio() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");

        campoMensaje = new TextField();
        campoMensaje.setPromptText("Escribe tu mensaje aquí...");
        campoMensaje.setDisable(true);
        campoMensaje.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) {
                enviarMensaje();
            }
        });

        btnEnviar = new Button("Enviar");
        btnEnviar.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        btnEnviar.setDisable(true);
        btnEnviar.setOnAction(e -> enviarMensaje());

        panel.getChildren().addAll(campoMensaje, btnEnviar);
        HBox.setHgrow(campoMensaje, Priority.ALWAYS);

        return panel;
    }

    private void conectarAlServidor() {
        String nombre = campoNombre.getText().trim();
        if (nombre.isEmpty()) {
            mostrarAlerta("Error", "Ingresa un nombre de usuario");
            return;
        }

        cliente = new Cliente(nombre, this);
        if (cliente.conectar()) {
            campoNombre.setDisable(true);
            btnConectar.setDisable(true);
            btnDesconectar.setDisable(false);
            campoMensaje.setDisable(false);
            btnEnviar.setDisable(false);
            areaChat.appendText("[INFO] Conectado como: " + nombre + "\n");
        } else {
            mostrarAlerta("Error", "No se pudo conectar al servidor");
        }
    }

    private void desconectarDelServidor() {
        if (cliente != null) {
            cliente.desconectar();
        }
    }

    private void enviarMensaje() {
        String mensaje = campoMensaje.getText().trim();
        if (!mensaje.isEmpty() && cliente != null) {
            cliente.enviarMensaje(mensaje);
            areaChat.appendText("[Yo]: " + mensaje + "\n");
            campoMensaje.clear();
            campoMensaje.requestFocus();
        }
    }

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    @Override
    public void onMensajeRecibido(String mensaje) {
        Platform.runLater(() -> {
            areaChat.appendText(mensaje + "\n");
        });
    }

    @Override
    public void onConexionCambiada(boolean conectado) {
        Platform.runLater(() -> {
            this.conectado = conectado;
            if (conectado) {
                estadoLabel.setText("Estado: Conectado");
                estadoLabel.setStyle("-fx-font-size: 12; -fx-text-fill: green;");
            } else {
                estadoLabel.setText("Estado: Desconectado");
                estadoLabel.setStyle("-fx-font-size: 12; -fx-text-fill: red;");
                campoNombre.setDisable(false);
                btnConectar.setDisable(false);
                btnDesconectar.setDisable(true);
                campoMensaje.setDisable(true);
                btnEnviar.setDisable(true);
                areaChat.appendText("[INFO] Desconectado del servidor\n");
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
