package client;

import protocol.Frame;
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
import app.AccountNumberGenerator;
import app.VoucherGenerator;
import java.io.File;

public class ClienteGUI extends Application implements Cliente.ClienteListener {
    private TextArea areaChat;
    private VBox panelMensajes;
    private ScrollPane scrollMensajes;
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
    private String usuarioAutenticado;
    
    // Botones de transacciones
    private Button btnDeposito;
    private Button btnRetiro;
    private Button btnConsulta;
    private Button btnVoucher;
    
    // Variables de transacción (para generar voucher)
    private String ultimoNumeroTransaccion = "";
    private String ultimoTipoOperacion = "";
    private double ultimoMonto = 0;
    private double ultimoSaldoAnterior = 0;
    private double ultimoSaldoNuevo = 0;
    private File ultimoArchivoVoucher = null;
    
    // Número de cuenta único
    private String numeroCuentaUsuario = "";

    public ClienteGUI() {
        this.usuarioAutenticado = null;
        this.numeroCuentaUsuario = AccountNumberGenerator.generarNumeroCuenta();
    }

    public ClienteGUI(String usuario) {
        this.usuarioAutenticado = usuario;
        this.numeroCuentaUsuario = AccountNumberGenerator.generarNumeroCuenta();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("MercadoCentro - Cliente de Transacciones Bancarias");
        primaryStage.setWidth(800);
        primaryStage.setHeight(750);

        BorderPane root = new BorderPane();

        // Panel superior - Conexión
        HBox panelConexion = crearPanelConexion();
        root.setTop(panelConexion);

        // Panel central - Chat y Transacciones
        HBox panelCentral = crearPanelCentral();
        root.setCenter(panelCentral);

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

        Label lblNombre = new Label("Usuario:");
        campoNombre = new TextField();
        campoNombre.setPromptText("Tu nombre de usuario");
        campoNombre.setPrefWidth(140);
        if (usuarioAutenticado != null) {
            campoNombre.setText(usuarioAutenticado);
            campoNombre.setEditable(false);
            campoNombre.setStyle("-fx-opacity: 0.8;");
        } else {
            campoNombre.setText("Usuario" + System.currentTimeMillis() % 1000);
        }

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

    private HBox crearPanelCentral() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(10));

        // Panel izquierdo - Chat
        VBox panelChat = crearPanelChat();
        
        // Panel derecho - Transacciones
        VBox panelTransacciones = crearPanelTransacciones();

        panel.getChildren().addAll(panelChat, new Separator(javafx.geometry.Orientation.VERTICAL), panelTransacciones);
        HBox.setHgrow(panelChat, Priority.ALWAYS);

        return panel;
    }

    private VBox crearPanelChat() {
        VBox panel = new VBox();

        Label titulo = new Label("Chat y Notificaciones");
        titulo.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        // VBox para las burbujas de mensajes
        panelMensajes = new VBox(8);
        panelMensajes.setPadding(new Insets(10));
        panelMensajes.setStyle("-fx-background-color: #ffffff;");
        panelMensajes.setFillWidth(false);

        scrollMensajes = new ScrollPane(panelMensajes);
        scrollMensajes.setFitToWidth(true);
        scrollMensajes.setStyle("-fx-control-inner-background: #ffffff;");

        panel.getChildren().addAll(titulo, scrollMensajes);
        VBox.setVgrow(scrollMensajes, Priority.ALWAYS);

        return panel;
    }

    private void agregarMensajeBurbuja(String nombre, String mensaje, boolean esPropio) {
        HBox contenedor = new HBox();
        contenedor.setPadding(new Insets(5, 10, 5, 10));

        Label labelMensaje = new Label(mensaje);
        labelMensaje.setStyle(
            "-fx-font-size: 12;" +
            "-fx-text-fill: #000000;" +
            "-fx-wrap-text: true;" +
            "-fx-padding: 10;" +
            "-fx-background-color: #ADD8E6;" +
            "-fx-background-radius: 10;" +
            "-fx-border-radius: 10;"
        );
        labelMensaje.setWrapText(true);
        labelMensaje.setMaxWidth(300);

        if (esPropio) {
            // Mensajes propios a la derecha
            HBox.setHgrow(new Label(""), Priority.ALWAYS);
            contenedor.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            contenedor.getChildren().add(labelMensaje);
        } else {
            // Mensajes de otros a la izquierda
            Label labelNombre = new Label(nombre + ": ");
            labelNombre.setStyle("-fx-font-size: 10; -fx-font-weight: bold; -fx-text-fill: #666666;");
            
            VBox vboxMensaje = new VBox(2);
            vboxMensaje.getChildren().addAll(labelNombre, labelMensaje);
            
            contenedor.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            contenedor.getChildren().add(vboxMensaje);
        }

        panelMensajes.getChildren().add(contenedor);
        scrollMensajes.setVvalue(1.0);
    }

    private VBox crearPanelTransacciones() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(280);
        panel.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 10;");

        Label titulo = new Label("Operaciones Bancarias");
        titulo.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        // Mostrar número de cuenta del usuario (inmutable)
        Label lblMiCuenta = new Label("Mi Número de Cuenta:");
        lblMiCuenta.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #2196F3;");
        
        TextField campoMiCuenta = new TextField();
        campoMiCuenta.setText(numeroCuentaUsuario);
        campoMiCuenta.setEditable(false);
        campoMiCuenta.setStyle("-fx-font-size: 12; -fx-padding: 8; -fx-opacity: 0.8; -fx-control-inner-background: #e8f4f8;");
        campoMiCuenta.setPrefHeight(32);

        // Campos para transacciones
        Label lblCuenta = new Label("Cuenta Destino:");
        TextField campoCuenta = new TextField();
        campoCuenta.setPromptText("Ej: 10000001");
        campoCuenta.setText("10000001");

        Label lblMonto = new Label("Monto:");
        TextField campoMonto = new TextField();
        campoMonto.setPromptText("Ej: 100.00");

        // Botones de operaciones
        btnDeposito = new Button("DEPÓSITO");
        btnDeposito.setPrefWidth(130);
        btnDeposito.setStyle("-fx-font-size: 11; -fx-padding: 8; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnDeposito.setDisable(true);
        btnDeposito.setOnAction(e -> {
            try {
                long cuenta = Long.parseLong(campoCuenta.getText().trim());
                double monto = Double.parseDouble(campoMonto.getText().trim());
                cliente.enviarDeposito(cuenta, monto);
            } catch (NumberFormatException ex) {
                mostrarAlerta("Error", "Datos inválidos");
            }
        });

        btnRetiro = new Button("RETIRO");
        btnRetiro.setPrefWidth(130);
        btnRetiro.setStyle("-fx-font-size: 11; -fx-padding: 8; -fx-background-color: #f44336; -fx-text-fill: white;");
        btnRetiro.setDisable(true);
        btnRetiro.setOnAction(e -> {
            try {
                long cuenta = Long.parseLong(campoCuenta.getText().trim());
                double monto = Double.parseDouble(campoMonto.getText().trim());
                cliente.enviarRetiro(cuenta, monto);
            } catch (NumberFormatException ex) {
                mostrarAlerta("Error", "Datos inválidos");
            }
        });

        btnConsulta = new Button("CONSULTA");
        btnConsulta.setPrefWidth(130);
        btnConsulta.setStyle("-fx-font-size: 11; -fx-padding: 8; -fx-background-color: #2196F3; -fx-text-fill: white;");
        btnConsulta.setDisable(true);
        btnConsulta.setOnAction(e -> {
            try {
                long cuenta = Long.parseLong(campoCuenta.getText().trim());
                cliente.enviarConsulta(cuenta);
            } catch (NumberFormatException ex) {
                mostrarAlerta("Error", "Datos inválidos");
            }
        });

        btnVoucher = new Button("GENERAR VOUCHER");
        btnVoucher.setPrefWidth(260);
        btnVoucher.setStyle("-fx-font-size: 11; -fx-padding: 8; -fx-background-color: #FF9800; -fx-text-fill: white;");
        btnVoucher.setDisable(true);
        btnVoucher.setOnAction(e -> generarYGuardarVoucher());

        HBox botones1 = new HBox(5);
        botones1.getChildren().addAll(btnDeposito, btnRetiro);

        HBox botones2 = new HBox(5);
        botones2.getChildren().addAll(btnConsulta);
        
        HBox botones3 = new HBox(5);
        botones3.getChildren().addAll(btnVoucher);

        // Información de cuentas disponibles
        Label lblInfo = new Label("Cuentas de Prueba:");
        lblInfo.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
        
        TextArea areaCuentas = new TextArea();
        areaCuentas.setEditable(false);
        areaCuentas.setWrapText(true);
        areaCuentas.setPrefHeight(100);
        areaCuentas.setStyle("-fx-font-size: 10; -fx-control-inner-background: #f9f9f9;");
        areaCuentas.setText("Cuenta: 10000001\nTitular: Juan Pérez\nSaldo: $5,000.00\n\nCuenta: 10000002\nTitular: María García\nSaldo: $3,500.00\n\nCuenta: 10000003\nTitular: Carlos López\nSaldo: $10,000.00");

        panel.getChildren().addAll(
            titulo,
            lblMiCuenta, campoMiCuenta,
            new Separator(),
            lblCuenta, campoCuenta,
            lblMonto, campoMonto,
            new Separator(),
            botones1, botones2, botones3,
            new Separator(),
            lblInfo, areaCuentas
        );

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

        String host = campoHost.getText().trim();
        if (host.isEmpty()) {
            mostrarAlerta("Error", "Ingresa la IP del servidor");
            return;
        }

        int puerto;
        try {
            puerto = Integer.parseInt(campoPuerto.getText().trim());
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Puerto inválido");
            return;
        }

        cliente = new Cliente(nombre, host, puerto, this);
        if (cliente.conectar()) {
            campoNombre.setDisable(true);
            btnConectar.setDisable(true);
            btnDesconectar.setDisable(false);
            campoMensaje.setDisable(false);
            btnEnviar.setDisable(false);
            
            // Habilitar botones de transacciones
            agregarMensajeBurbuja("INFO", "Conectado como: " + nombre, false);
            agregarMensajeBurbuja("INFO", "Sistema de transacciones bancarias activo", false);
        } else {
            mostrarAlerta("Error", "No se pudo conectar al servidor");
        }
    }

    public void conectarAutomatico() {
        Platform.runLater(() -> {
            try {
                Thread.sleep(500);
                String nombre = campoNombre.getText().trim();
                if (!nombre.isEmpty()) {
                    cliente = new Cliente(nombre, this);
                    if (cliente.conectar()) {
                        campoNombre.setDisable(true);
                        btnConectar.setDisable(true);
                        btnDesconectar.setDisable(false);
                        campoMensaje.setDisable(false);
                        btnEnviar.setDisable(false);
                        
                        agregarMensajeBurbuja("INFO", "Conectado automáticamente como: " + nombre, false);
                        agregarMensajeBurbuja("INFO", "Sistema de transacciones bancarias activo", false);
                        System.out.println("[SUCCESS] Cliente conectado automáticamente");
                    }
                }
            } catch (Exception e) {
                System.err.println("[ERROR] Error en conexión automática: " + e.getMessage());
            }
        });
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
            agregarMensajeBurbuja("Yo", mensaje, true);
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
            // Parsear mensaje con formato "[Usuario]: Contenido"
            if (mensaje.contains("]:")) {
                int endUser = mensaje.indexOf("]:");
                String usuario = mensaje.substring(1, endUser);
                String contenido = mensaje.substring(endUser + 2).trim();
                agregarMensajeBurbuja(usuario, contenido, false);
            } else {
                // Si no tiene formato estándar, mostrar como mensaje del sistema
                agregarMensajeBurbuja("SISTEMA", mensaje, false);
            }
        });
    }

    @Override
    public void onTramaRecibida(Frame frame) {
        Platform.runLater(() -> {
            String respuesta = formatearTramaParaMostrar(frame);
            agregarMensajeBurbuja("SERVIDOR", respuesta, false);
        });
    }

    @Override
    public void onConexionCambiada(boolean conectado) {
        Platform.runLater(() -> {
            this.conectado = conectado;
            if (conectado) {
                estadoLabel.setText("Estado: Conectado");
                estadoLabel.setStyle("-fx-font-size: 12; -fx-text-fill: green;");
                
                // Habilitar botones de transacciones
                btnDeposito.setDisable(false);
                btnRetiro.setDisable(false);
                btnConsulta.setDisable(false);
                btnVoucher.setDisable(false);
                
                campoNombre.setDisable(true);
                btnConectar.setDisable(true);
                btnDesconectar.setDisable(false);
                campoMensaje.setDisable(false);
                btnEnviar.setDisable(false);
                agregarMensajeBurbuja("INFO", "Conectado como: " + campoNombre.getText(), false);
                agregarMensajeBurbuja("INFO", "Sistema de transacciones bancarias activo", false);
            } else {
                estadoLabel.setText("Estado: Desconectado");
                estadoLabel.setStyle("-fx-font-size: 12; -fx-text-fill: red;");
                
                // Deshabilitar botones de transacciones
                btnDeposito.setDisable(true);
                btnRetiro.setDisable(true);
                btnConsulta.setDisable(true);
                btnVoucher.setDisable(true);
                
                campoNombre.setDisable(false);
                btnConectar.setDisable(false);
                btnDesconectar.setDisable(true);
                campoMensaje.setDisable(true);
                btnEnviar.setDisable(true);
                areaChat.appendText("[INFO] Desconectado del servidor\n");
            }
        });
    }

    @Override
    public void onError(String error) {
        Platform.runLater(() -> {
            areaChat.appendText("[ERROR] " + error + "\n");
        });
    }

    /**
     * Formatea una trama para mostrarla de manera legible
     */
    private String formatearTramaParaMostrar(Frame frame) {
        StringBuilder sb = new StringBuilder();
        String tipo = frame.getTipo();

        if ("RESPUESTA".equals(tipo)) {
            String estado = frame.obtener("ESTADO");
            String mensaje = frame.obtener("MENSAJE");
            
            sb.append("[RESPUESTA] ");
            if ("OK".equals(estado)) {
                sb.append("✓ ").append(mensaje);
                
                if (frame.existe("CUENTA")) {
                    sb.append(" | Cuenta: ").append(frame.obtener("CUENTA"));
                }
                if (frame.existe("SALDO")) {
                    sb.append(" | Saldo: $").append(frame.obtener("SALDO"));
                }
                if (frame.existe("MONTO_DEPOSITADO")) {
                    sb.append(" | Monto Depositado: $").append(frame.obtener("MONTO_DEPOSITADO"));
                }
                if (frame.existe("MONTO_RETIRADO")) {
                    sb.append(" | Monto Retirado: $").append(frame.obtener("MONTO_RETIRADO"));
                }
            } else {
                sb.append("✗ ").append(mensaje);
                if (frame.existe("CODIGO")) {
                    sb.append(" [").append(frame.obtener("CODIGO")).append("]");
                }
            }
        } else if ("NOTIFICACION".equals(tipo)) {
            String tipoNotif = frame.obtener("TIPO");
            String usuario = frame.obtener("USUARIO");
            sb.append("[NOTIFICACIÓN] ").append(usuario).append(" se ha ").append(tipoNotif.toLowerCase());
        } else {
            sb.append("[").append(tipo).append("] ");
            for (var entry : frame.getCampos().entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(" ");
            }
        }

        return sb.toString();
    }

    private void generarYGuardarVoucher() {
        if (ultimoNumeroTransaccion.isEmpty()) {
            mostrarAlerta("Error", "No hay transacción reciente para generar voucher");
            return;
        }
        
        try {
            String nombreArchivo = VoucherGenerator.generarNombreArchivo(ultimoNumeroTransaccion);
            
            VoucherGenerator generador = new VoucherGenerator(
                ultimoNumeroTransaccion,
                ultimoTipoOperacion,
                campoNombre.getText(),
                "12345",
                ultimoMonto,
                ultimoSaldoAnterior,
                ultimoSaldoNuevo
            );
            
            // Crear carpeta Vouchers si no existe
            File carpetaVouchers = new File(System.getProperty("user.home"), "Descargas/Vouchers");
            if (!carpetaVouchers.exists()) {
                carpetaVouchers.mkdirs();
            }
            
            File archivo = new File(carpetaVouchers, nombreArchivo);
            
            if (generador.guardarEnArchivo(archivo)) {
                mostrarAlerta("Éxito", "Voucher generado en:\n" + archivo.getAbsolutePath());
            } else {
                mostrarAlerta("Error", "No se pudo generar el voucher");
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al generar voucher: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
