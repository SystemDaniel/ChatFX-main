package app;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import client.ClienteGUI;
import server.ServidorGUI;

public class App extends Application {
    private String usuarioActual;
    private boolean esMenuPrincipal = false;

    public App() {
        this.usuarioActual = null;
        this.esMenuPrincipal = false;
    }

    public App(String usuario) {
        this.usuarioActual = usuario;
        this.esMenuPrincipal = true;
    }

    @Override
    public void start(Stage primaryStage) {
        if (esMenuPrincipal) {
            mostrarMenuPrincipal(primaryStage);
        } else {
            primaryStage.setOnCloseRequest(e -> {
                DatabaseInit.cerrar();
                System.exit(0);
            });
            abrirLogin();
        }
    }

    private void mostrarMenuPrincipal(Stage primaryStage) {
        primaryStage.setTitle("MercadoCentro - Menú Principal");
        primaryStage.setWidth(450);
        primaryStage.setHeight(500);
        primaryStage.setOnCloseRequest(e -> {
            DatabaseInit.cerrar();
            System.exit(0);
        });

        VBox container = new VBox(20);
        container.setPadding(new Insets(50));
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color: #f0f0f0;");

        Label titulo = new Label("MercadoCentro");
        titulo.setStyle("-fx-font-size: 32; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label bienvenida = new Label("¡Bienvenido, " + usuarioActual + "!");
        bienvenida.setStyle("-fx-font-size: 16; -fx-text-fill: #666666;");

        Label subtitulo = new Label("¿Qué deseas hacer?");
        subtitulo.setStyle("-fx-font-size: 14; -fx-text-fill: #999999;");

        Button btnServidor = new Button("Abrir Servidor");
        btnServidor.setPrefHeight(50);
        btnServidor.setPrefWidth(300);
        btnServidor.setStyle("-fx-font-size: 14; -fx-padding: 10; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
        btnServidor.setOnAction(e -> iniciarServidor());

        Button btnCliente = new Button("Abrir Cliente");
        btnCliente.setPrefHeight(50);
        btnCliente.setPrefWidth(300);
        btnCliente.setStyle("-fx-font-size: 14; -fx-padding: 10; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");
        btnCliente.setOnAction(e -> abrirChat());

        /**Button btnAmbos = new Button("Abrir Servidor y Cliente");
        btnAmbos.setPrefHeight(50);
        btnAmbos.setPrefWidth(300);
        btnAmbos.setStyle("-fx-font-size: 14; -fx-padding: 10; -fx-background-color: #FF9800; -fx-text-fill: white; -fx-cursor: hand;");
        btnAmbos.setOnAction(e -> abrirAmbos());*/

        Button btnSalir = new Button("Cerrar Sesión");
        btnSalir.setPrefHeight(50);
        btnSalir.setPrefWidth(300);
        btnSalir.setStyle("-fx-font-size: 14; -fx-padding: 10; -fx-background-color: #757575; -fx-text-fill: white; -fx-cursor: hand;");
        btnSalir.setOnAction(e -> {
            System.out.println("[INFO] Sesión cerrada: " + usuarioActual);
            DatabaseInit.cerrar();
            System.exit(0);
        });

        container.getChildren().addAll(
                titulo,
                bienvenida,
                new Label(),
                subtitulo,
                btnServidor,
                btnCliente,
                //btnAmbos,
                new Label(),
                btnSalir
        );

        Scene scene = new Scene(container);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void abrirLogin() {
        try {
            LoginGUI loginGUI = new LoginGUI();
            Stage ventanaLogin = new Stage();
            loginGUI.start(ventanaLogin);
        } catch (Exception e) {
            System.err.println("[ERROR] No se pudo abrir login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void iniciarServidor() {
        try {
            ServidorGUI servidor = new ServidorGUI();
            Stage ventanaServidor = new Stage();
            servidor.start(ventanaServidor);
            System.out.println("[INFO] Servidor abierto");
        } catch (Exception e) {
            System.err.println("[ERROR] No se pudo iniciar servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void abrirChat() {
        try {
            ClienteGUI clienteGUI = new ClienteGUI(usuarioActual);
            Stage ventanaCliente = new Stage();
            clienteGUI.start(ventanaCliente);
            System.out.println("[INFO] Cliente abierto");
        } catch (Exception e) {
            System.err.println("[ERROR] No se pudo abrir chat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**private void abrirAmbos() {
        iniciarServidor();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        abrirChat();
    }*/

    public static void main(String[] args) {
        launch(args);
    }
}
