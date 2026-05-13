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

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ChatFX - Menú Principal");
        primaryStage.setWidth(400);
        primaryStage.setHeight(300);

        VBox root = new VBox(20);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f0f0f0;");

        // Título
        Label titulo = new Label("ChatFX");
        titulo.setStyle("-fx-font-size: 32; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label subtitulo = new Label("Sistema de Chat en Tiempo Real");
        subtitulo.setStyle("-fx-font-size: 14; -fx-text-fill: #666666;");

        // Botón para iniciar servidor local o mostrar servidor
        Button btnServidor = new Button("Mostrar Servidor Activo");
        btnServidor.setStyle("-fx-font-size: 14; -fx-padding: 15; -fx-min-width: 200; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnServidor.setOnAction(e -> iniciarServidor());

        // Botón para conectar a otro chat por IP/puerto
        Button btnCliente = new Button("Conectar a Chat Remoto");
        btnCliente.setStyle("-fx-font-size: 14; -fx-padding: 15; -fx-min-width: 200; -fx-background-color: #2196F3; -fx-text-fill: white;");
        btnCliente.setOnAction(e -> abrirChat());

        // Información
        Label info = new Label("Selecciona una opción para comenzar");
        info.setStyle("-fx-font-size: 12; -fx-text-fill: #999999;");

        root.getChildren().addAll(titulo, subtitulo, new Label(""), btnServidor, btnCliente, new Label(""), info);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        try {
            ServidorGUI servidor = new ServidorGUI();
            Stage ventanaServidor = new Stage();
            servidor.start(ventanaServidor);
            ventanaServidor.setOnCloseRequest(event -> servidor.detenerServidor());

            primaryStage.setOnCloseRequest(event -> {
                servidor.detenerServidor();
                ventanaServidor.close();
            });
        } catch (Exception e) {
            System.err.println("[ERROR] No se pudo iniciar servidor automático: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void iniciarServidor() {
        try {
            ServidorGUI servidor = new ServidorGUI();
            Stage ventanaServidor = new Stage();
            servidor.start(ventanaServidor);
        } catch (Exception e) {
            System.err.println("[ERROR] No se pudo iniciar servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void abrirChat() {
        try {
            ClienteGUI clienteGUI = new ClienteGUI();
            Stage ventanaCliente = new Stage();
            clienteGUI.start(ventanaCliente);
        } catch (Exception e) {
            System.err.println("[ERROR] No se pudo abrir chat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
