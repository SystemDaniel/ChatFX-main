package app;

import java.security.MessageDigest;
import java.sql.*;

public class UsuarioManager {
    private static UsuarioManager instancia;
    private String ultimoError = "";

    private UsuarioManager() {
        DatabaseInit.inicializarBD();
    }

    public static UsuarioManager getInstance() {
        if (instancia == null) {
            instancia = new UsuarioManager();
        }
        return instancia;
    }

    public boolean login(String usuario, String contraseña) {
        try {
            String query = "SELECT contraseña FROM usuarios WHERE usuario = ?";
            PreparedStatement pstmt = DatabaseInit.getConexion().prepareStatement(query);
            pstmt.setString(1, usuario);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String contraseñaHasheada = rs.getString("contraseña");
                String contraseñaIngresada = hashear(contraseña);
                return contraseñaHasheada.equals(contraseñaIngresada);
            }
            ultimoError = "Usuario no encontrado";
            return false;
        } catch (SQLException e) {
            ultimoError = "Error en login: " + e.getMessage();
            System.err.println("[ERROR] " + ultimoError);
            return false;
        }
    }

    public boolean registrar(String usuario, String contraseña, String email) {
        if (usuario.isEmpty() || contraseña.isEmpty()) {
            ultimoError = "Usuario y contraseña no pueden estar vacíos";
            System.out.println("[REGISTRO] " + ultimoError);
            return false;
        }

        try {
            String query = "INSERT INTO usuarios (usuario, contraseña, email) VALUES (?, ?, ?)";
            PreparedStatement pstmt = DatabaseInit.getConexion().prepareStatement(query);
            pstmt.setString(1, usuario);
            pstmt.setString(2, hashear(contraseña));
            pstmt.setString(3, email);
            pstmt.executeUpdate();
            ultimoError = "";
            System.out.println("[REGISTRO] Usuario registrado: " + usuario);
            return true;
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                ultimoError = "El usuario ya existe";
                System.out.println("[REGISTRO] " + ultimoError);
            } else {
                ultimoError = "Error en BD: " + e.getMessage();
                System.err.println("[ERROR] " + ultimoError);
            }
            return false;
        } catch (Exception e) {
            ultimoError = "Error inesperado: " + e.getMessage();
            System.err.println("[ERROR] " + ultimoError);
            return false;
        }
    }

    public boolean usuarioExiste(String usuario) {
        try {
            String query = "SELECT id FROM usuarios WHERE usuario = ?";
            PreparedStatement pstmt = DatabaseInit.getConexion().prepareStatement(query);
            pstmt.setString(1, usuario);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("[ERROR] Error verificando usuario: " + e.getMessage());
            return false;
        }
    }

    private String hashear(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(texto.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            System.err.println("[ERROR] Error hasheando contraseña: " + e.getMessage());
            return "";
        }
    }

    public String getUltimoError() {
        return ultimoError;
    }
}
