package app;

import java.sql.*;
import java.nio.file.Paths;

public class DatabaseInit {
    private static final String DB_URL;

    static {
        String dbPath;
        try {
            // Busca el .db junto al ejecutable/JAR
            java.net.URL location = DatabaseInit.class.getProtectionDomain()
                    .getCodeSource().getLocation();
            java.nio.file.Path jarPath = java.nio.file.Paths.get(location.toURI()).getParent();
            dbPath = "jdbc:sqlite:" + jarPath.resolve("chatfx.db").toString();
        } catch (Exception e) {
            // Fallback al directorio actual
            dbPath = "jdbc:sqlite:" + java.nio.file.Paths.get(
                    System.getProperty("user.dir"), "chatfx.db").toString();
        }
        DB_URL = dbPath;
    }
    
    private static Connection conexion;

    public static void inicializarBD() {
        try {
            System.out.println("[DB] Intentando conectar a: " + DB_URL);
            Class.forName("org.sqlite.JDBC");
            conexion = DriverManager.getConnection(DB_URL);
            System.out.println("[DB] ✓ Conexión exitosa");
            crearTablas();
            inicializarCuentasPrueba();
            System.out.println("[DB] Base de datos inicializada correctamente");
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] Driver SQLite no encontrado: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("[ERROR] No se pudo conectar a la BD en: " + DB_URL);
            System.err.println("[ERROR] Detalles: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[ERROR] Error inesperado en BD: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void crearTablas() throws SQLException {
        String sqlUsuarios = "CREATE TABLE IF NOT EXISTS usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "usuario TEXT UNIQUE NOT NULL," +
                "contraseña TEXT NOT NULL," +
                "email TEXT," +
                "fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        String sqlCuentas = "CREATE TABLE IF NOT EXISTS cuentas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "numero_cuenta LONG UNIQUE NOT NULL," +
                "titular TEXT NOT NULL," +
                "saldo REAL DEFAULT 0.0," +
                "fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        String sqlTransacciones = "CREATE TABLE IF NOT EXISTS transacciones (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tipo TEXT NOT NULL," +
                "numero_cuenta LONG NOT NULL," +
                "monto REAL," +
                "estado TEXT," +
                "fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (numero_cuenta) REFERENCES cuentas(numero_cuenta)" +
                ")";

        try (Statement stmt = conexion.createStatement()) {
            stmt.execute(sqlUsuarios);
            System.out.println("[DB] Tabla 'usuarios' creada/verificada");
            
            stmt.execute(sqlCuentas);
            System.out.println("[DB] Tabla 'cuentas' creada/verificada");
            
            stmt.execute(sqlTransacciones);
            System.out.println("[DB] Tabla 'transacciones' creada/verificada");
            
            // Crear índice en numero_cuenta para búsquedas rápidas
            String sqlIndice = "CREATE INDEX IF NOT EXISTS idx_numero_cuenta ON cuentas(numero_cuenta)";
            stmt.execute(sqlIndice);
        }
    }

    private static void inicializarCuentasPrueba() throws SQLException {
        String verificar = "SELECT COUNT(*) FROM cuentas";
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(verificar)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String[] cuentas = AccountNumberGenerator.generarMultiplesCuentas(3);
                
                String sql = "INSERT INTO cuentas (numero_cuenta, titular, saldo) VALUES (?, ?, ?)";
                try (PreparedStatement stmt2 = conexion.prepareStatement(sql)) {
                    stmt2.setLong(1, Long.parseLong(cuentas[0]));
                    stmt2.setString(2, "Juan Pérez");
                    stmt2.setDouble(3, 5000.0);
                    stmt2.executeUpdate();
                    
                    stmt2.setLong(1, Long.parseLong(cuentas[1]));
                    stmt2.setString(2, "María García");
                    stmt2.setDouble(3, 3500.0);
                    stmt2.executeUpdate();
                    
                    stmt2.setLong(1, Long.parseLong(cuentas[2]));
                    stmt2.setString(2, "Carlos López");
                    stmt2.setDouble(3, 10000.0);
                    stmt2.executeUpdate();
                    
                    System.out.println("[DB] Cuentas de prueba inicializadas");
                }
            }
        }
    }

    public static Connection getConexion() {
        try {
            if (conexion == null) {
                System.out.println("[DB] Conexión nula, reinicializando...");
                inicializarBD();
            } else if (conexion.isClosed()) {
                System.out.println("[DB] Conexión cerrada, reabriendo...");
                inicializarBD();
            }
            if (conexion == null) {
                System.err.println("[ERROR] No se pudo establecer conexión a la BD");
            }
            return conexion;
        } catch (SQLException e) {
            System.err.println("[ERROR] Error al verificar conexión: " + e.getMessage());
            return conexion;
        }
    }

    public static void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("[DB] Conexión cerrada");
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] No se pudo cerrar la BD: " + e.getMessage());
        }
    }
}
