package app;

import java.security.MessageDigest;
import java.sql.*;

public class UsuarioManager {
    private static UsuarioManager instancia;

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
            return false;
        } catch (SQLException e) {
            System.err.println("[ERROR] Error en login: " + e.getMessage());
            return false;
        }
    }

    public boolean registrar(String usuario, String contraseña, String email) {
        if (usuario.isEmpty() || contraseña.isEmpty()) {
            System.out.println("[REGISTRO] Usuario y contraseña no pueden estar vacíos");
            return false;
        }

        try {
            String query = "INSERT INTO usuarios (usuario, contraseña, email) VALUES (?, ?, ?)";
            PreparedStatement pstmt = DatabaseInit.getConexion().prepareStatement(query);
            pstmt.setString(1, usuario);
            pstmt.setString(2, hashear(contraseña));
            pstmt.setString(3, email);
            pstmt.executeUpdate();
            System.out.println("[REGISTRO] Usuario registrado: " + usuario);
            return true;
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println("[REGISTRO] El usuario ya existe");
            } else {
                System.err.println("[ERROR] Error en registro: " + e.getMessage());
            }
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
}
