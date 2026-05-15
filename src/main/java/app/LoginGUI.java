package app;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import client.ClienteGUI;
import server.ServidorGUI;

public class LoginGUI extends Application {
    private TextField campoUsuario;
    private PasswordField campoContraseña;
    private PasswordField campoContraseña2;
    private Label labelEstado;
    private Label lblContraseña2;
    private boolean esRegistro = false;
    private UsuarioManager usuarioManager = UsuarioManager.getInstance();
    private Stage miStage;
    private VBox panel;

    @Override
    public void start(Stage primaryStage) {
        miStage = primaryStage;
        primaryStage.setTitle("MercadoCentro - Login");
        primaryStage.setWidth(400);
        primaryStage.setHeight(550);
        primaryStage.setOnCloseRequest(e -> cerrar());

        panel = crearPanelLogin();
        Scene scene = new Scene(panel);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox crearPanelLogin() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(40));
        container.setAlignment(Pos.TOP_CENTER);
        container.setStyle("-fx-background-color: #f0f0f0;");

        Label titulo = new Label("MercadoCentro");
        titulo.setStyle("-fx-font-size: 32; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label subtitulo = new Label("Inicia sesión o regístrate");
        subtitulo.setStyle("-fx-font-size: 14; -fx-text-fill: #666666;");

        Label lblUsuario = new Label("Usuario:");
        lblUsuario.setStyle("-fx-font-size: 12; -fx-text-fill: #333333;");
        campoUsuario = new TextField();
        campoUsuario.setPromptText("Nombre de usuario");
        campoUsuario.setPrefHeight(40);

        Label lblContraseña = new Label("Contraseña:");
        lblContraseña.setStyle("-fx-font-size: 12; -fx-text-fill: #333333;");
        campoContraseña = new PasswordField();
        campoContraseña.setPromptText("Contraseña");
        campoContraseña.setPrefHeight(40);

        lblContraseña2 = new Label("Confirmar contraseña:");
        lblContraseña2.setStyle("-fx-font-size: 12; -fx-text-fill: #333333;");
        lblContraseña2.setVisible(false);
        lblContraseña2.setManaged(false);
        
        campoContraseña2 = new PasswordField();
        campoContraseña2.setPromptText("Confirma tu contraseña");
        campoContraseña2.setPrefHeight(40);
        campoContraseña2.setVisible(false);
        campoContraseña2.setManaged(false);

        labelEstado = new Label("");
        labelEstado.setStyle("-fx-text-fill: #d32f2f;");

        Button btnLogin = new Button("Iniciar Sesión");
        btnLogin.setPrefHeight(45);
        btnLogin.setStyle("-fx-font-size: 14; -fx-padding: 10; -fx-background-color: #2196F3; -fx-text-fill: white;");
        btnLogin.setOnAction(e -> realizarLogin());

        Button btnRegistro = new Button("Registrarse");
        btnRegistro.setPrefHeight(45);
        btnRegistro.setStyle("-fx-font-size: 14; -fx-padding: 10; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnRegistro.setOnAction(e -> mostrarRegistro());

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setPrefHeight(45);
        btnCancelar.setStyle("-fx-font-size: 14; -fx-padding: 10; -fx-background-color: #757575; -fx-text-fill: white;");
        btnCancelar.setOnAction(e -> cerrar());

        container.getChildren().addAll(
                titulo,
                subtitulo,
                new Separator(),
                lblUsuario,
                campoUsuario,
                lblContraseña,
                campoContraseña,
                lblContraseña2,
                campoContraseña2,
                labelEstado,
                new Separator(),
                btnLogin,
                btnRegistro,
                btnCancelar
        );

        return container;
    }

    private void realizarLogin() {
        String usuario = campoUsuario.getText();
        String contraseña = campoContraseña.getText();

        if (usuario.isEmpty() || contraseña.isEmpty()) {
            mostrarError("Completa todos los campos");
            return;
        }

        if (esRegistro) {
            String contraseña2 = campoContraseña2.getText();
            if (!contraseña.equals(contraseña2)) {
                mostrarError("Las contraseñas no coinciden");
                return;
            }

            if (usuarioManager.registrar(usuario, contraseña, "")) {
                mostrarExito("¡Registro exitoso! Ahora inicia sesión");
                mostrarLogin();
            } else {
                mostrarError("Error en registro. Usuario puede estar en uso");
            }
        } else {
            if (usuarioManager.login(usuario, contraseña)) {
                mostrarExito("¡Login exitoso!");
                abrirChat(usuario);
            } else {
                mostrarError("Usuario o contraseña incorrectos");
            }
        }
    }

    private void mostrarRegistro() {
        esRegistro = true;
        lblContraseña2.setVisible(true);
        lblContraseña2.setManaged(true);
        campoContraseña2.setVisible(true);
        campoContraseña2.setManaged(true);
        limpiarCampos();
        mostrarInfo("Ingresa tus datos para registrarte");
    }

    private void mostrarLogin() {
        esRegistro = false;
        lblContraseña2.setVisible(false);
        lblContraseña2.setManaged(false);
        campoContraseña2.setVisible(false);
        campoContraseña2.setManaged(false);
        limpiarCampos();
        labelEstado.setText("");
    }

    private void limpiarCampos() {
        campoUsuario.clear();
        campoContraseña.clear();
        campoContraseña2.clear();
    }

    private void mostrarError(String mensaje) {
        labelEstado.setText(mensaje);
        labelEstado.setStyle("-fx-text-fill: #d32f2f;");
    }

    private void mostrarExito(String mensaje) {
        labelEstado.setText(mensaje);
        labelEstado.setStyle("-fx-text-fill: #4CAF50;");
    }

    private void mostrarInfo(String mensaje) {
        labelEstado.setText(mensaje);
        labelEstado.setStyle("-fx-text-fill: #666666;");
    }

    private void abrirChat(String usuario) {
        try {
            abrirServidorAutomatico();
            Thread.sleep(1000);
            abrirClienteConexion(usuario);
            miStage.close();
        } catch (Exception e) {
            System.err.println("[ERROR] Error abriendo chat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void abrirServidorAutomatico() {
        try {
            server.ServidorGUI servidor = new server.ServidorGUI();
            Stage ventanaServidor = new Stage();
            servidor.start(ventanaServidor);
            System.out.println("[INFO] Servidor iniciado automáticamente");
        } catch (Exception e) {
            System.err.println("[ERROR] No se pudo iniciar servidor: " + e.getMessage());
        }
    }

    private void abrirClienteConexion(String usuario) {
        try {
            ClienteGUI clienteGUI = new ClienteGUI(usuario);
            Stage ventanaCliente = new Stage();
            clienteGUI.start(ventanaCliente);
            clienteGUI.conectarAutomatico();
            System.out.println("[INFO] Cliente iniciado y conectándose automáticamente");
        } catch (Exception e) {
            System.err.println("[ERROR] No se pudo abrir cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cerrar() {
        DatabaseInit.cerrar();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
