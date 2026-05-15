package protocol;

/**
 * Constantes del protocolo de comunicación
 * Define códigos de error, tipos de operación, delimitadores y confirmaciones
 */
public class ProtocolConstants {
    
    // ==================== DELIMITADORES ====================
    public static final String DELIMITER_INICIO = "<INICIO>";
    public static final String DELIMITER_FIN = "|FIN";
    public static final String DELIMITER_CAMPO = "|";
    public static final String DELIMITER_VALOR = ":";
    
    // ==================== TIPOS DE OPERACIÓN ====================
    public static final String TIPO_MENSAJE = "MENSAJE";
    public static final String TIPO_DEPOSITO = "DEPOSITO";
    public static final String TIPO_RETIRO = "RETIRO";
    public static final String TIPO_CONSULTA = "CONSULTA";
    public static final String TIPO_CONFIRMACION = "CONFIRMACION";
    public static final String TIPO_VOUCHER = "VOUCHER";
    public static final String TIPO_ERROR = "ERROR";
    
    // ==================== CÓDIGOS DE ESTADO ====================
    // Éxito
    public static final String STATUS_OK_200 = "OK_200";
    public static final String STATUS_ACK = "ACK";
    
    // Errores
    public static final String STATUS_NACK = "NACK";
    public static final String STATUS_ERR_001 = "ERR_001"; // Usuario no encontrado
    public static final String STATUS_ERR_002 = "ERR_002"; // Saldo insuficiente
    public static final String STATUS_ERR_003 = "ERR_003"; // Cuenta inválida
    public static final String STATUS_ERR_004 = "ERR_004"; // Operación no permitida
    public static final String STATUS_ERR_005 = "ERR_005"; // Timeout de conexión
    public static final String STATUS_ERR_006 = "ERR_006"; // Autenticación fallida
    public static final String STATUS_ERR_007 = "ERR_007"; // Parámetros inválidos
    
    // ==================== CAMPOS ESTÁNDAR ====================
    public static final String CAMPO_TIPO = "TIPO";
    public static final String CAMPO_STATUS = "STATUS";
    public static final String CAMPO_ID_TRANSACCION = "ID_TRANS";
    public static final String CAMPO_USUARIO = "USUARIO";
    public static final String CAMPO_CUENTA = "CUENTA";
    public static final String CAMPO_MONTO = "MONTO";
    public static final String CAMPO_SALDO_ANTERIOR = "SALDO_ANT";
    public static final String CAMPO_SALDO_NUEVO = "SALDO_NUEVO";
    public static final String CAMPO_FECHA = "FECHA";
    public static final String CAMPO_HORA = "HORA";
    public static final String CAMPO_MENSAJE = "MENSAJE";
    public static final String CAMPO_DESCRIPCION = "DESC";
    public static final String CAMPO_TIMESTAMP = "TIMESTAMP";
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    /**
     * Obtiene la descripción de un código de error
     */
    public static String getErrorDescription(String statusCode) {
        switch (statusCode) {
            case STATUS_OK_200:
                return "Operación exitosa";
            case STATUS_ACK:
                return "Confirmación recibida";
            case STATUS_NACK:
                return "Error en la transmisión";
            case STATUS_ERR_001:
                return "Usuario no encontrado";
            case STATUS_ERR_002:
                return "Saldo insuficiente";
            case STATUS_ERR_003:
                return "Número de cuenta inválido";
            case STATUS_ERR_004:
                return "Operación no permitida";
            case STATUS_ERR_005:
                return "Timeout de conexión";
            case STATUS_ERR_006:
                return "Autenticación fallida";
            case STATUS_ERR_007:
                return "Parámetros inválidos";
            default:
                return "Error desconocido: " + statusCode;
        }
    }
    
    /**
     * Verifica si un código de estado es de éxito
     */
    public static boolean isSuccess(String statusCode) {
        return STATUS_OK_200.equals(statusCode) || STATUS_ACK.equals(statusCode);
    }
    
    /**
     * Verifica si un código de estado es de error
     */
    public static boolean isError(String statusCode) {
        return statusCode != null && statusCode.startsWith("ERR_");
    }
    
    /**
     * Obtiene descripción del tipo de operación
     */
    public static String getOperationDescription(String tipo) {
        switch (tipo) {
            case TIPO_MENSAJE:
                return "Mensaje de Chat";
            case TIPO_DEPOSITO:
                return "Depósito Bancario";
            case TIPO_RETIRO:
                return "Retiro Bancario";
            case TIPO_CONSULTA:
                return "Consulta de Saldo";
            case TIPO_CONFIRMACION:
                return "Confirmación";
            case TIPO_VOUCHER:
                return "Generación de Voucher";
            case TIPO_ERROR:
                return "Error del Sistema";
            default:
                return tipo;
        }
    }
}
