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
        primaryStage.setOnCloseRequest(e -> {
            DatabaseInit.cerrar();
            System.exit(0);
        });
        
        abrirLogin();
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
