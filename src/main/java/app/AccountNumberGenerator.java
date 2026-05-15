package app;

import java.sql.*;

/**
 * Generador de números de cuenta únicos
 * Genera números aleatorios de 8 dígitos sin repetirse
 * Valida unicidad contra la base de datos
 */
public class AccountNumberGenerator {
    private static final int MAX_INTENTOS = 10;
    
    /**
     * Genera un número de cuenta único de 8 dígitos
     * Verifica contra la base de datos para asegurar unicidad
     * Rango: 10000000 - 99999999
     */
    public static String generarNumeroCuenta() {
        Connection conexion = DatabaseInit.getConexion();
        
        for (int intento = 0; intento < MAX_INTENTOS; intento++) {
            long numeroCuenta = 10000000L + (long)(Math.random() * 90000000L);
            String numStr = String.valueOf(numeroCuenta);
            
            if (esUnico(numeroCuenta, conexion)) {
                return numStr;
            }
        }
        
        throw new RuntimeException("No se pudo generar un número de cuenta único después de " + MAX_INTENTOS + " intentos");
    }
    
    /**
     * Verifica si un número de cuenta ya existe en la base de datos
     */
    private static boolean esUnico(long numero, Connection conexion) {
        try {
            String sql = "SELECT COUNT(*) FROM cuentas WHERE numero_cuenta = ?";
            try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
                stmt.setLong(1, numero);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) == 0;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[WARN] Error verificando unicidad: " + e.getMessage());
        }
        return true;
    }
    
    /**
     * Genera múltiples números de cuenta únicos
     */
    public static String[] generarMultiplesCuentas(int cantidad) {
        String[] cuentas = new String[cantidad];
        for (int i = 0; i < cantidad; i++) {
            cuentas[i] = generarNumeroCuenta();
        }
        return cuentas;
    }
    
    /**
     * Valida que un número de cuenta sea válido (8 dígitos)
     */
    public static boolean esValido(String numeroCuenta) {
        try {
            long numero = Long.parseLong(numeroCuenta);
            return numeroCuenta.length() == 8 && numero >= 10000000L && numero <= 99999999L;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
