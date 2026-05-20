package protocol;

/**
 * Constantes del protocolo de comunicación - Formato del Banco
 * Estructura: TRANSAC|TIPO_OPERACION|CUENTA_ORIGEN|CUENTA_DESTINO|MONTO|CONCEPTO
 */
public class ProtocolConstants {
    
    // ==================== DELIMITADORES ====================
    public static final String DELIMITER_TRANSAC = "TRANSAC";
    public static final String DELIMITER_RESPUESTA = "RESPUESTA";
    public static final String DELIMITER_CAMPO = "|";
    
    // ==================== TIPOS DE OPERACIÓN ====================
    public static final String TIPO_DEPOSITO = "DEPOSITO";
    public static final String TIPO_RETIRO = "RETIRO";
    public static final String TIPO_CONSULTA = "CONSULTA";
    public static final String TIPO_DEBITO = "DEBITO";
    public static final String TIPO_TRANSFERENCIA = "TRANSFERENCIA";
    
    // ==================== CUENTA ESPECIAL ====================
    public static final long CUENTA_COMODIN_RETIRO = 9999L;  // Para retiros
    public static final long CUENTA_COMODIN_CONSULTA = 9999L; // Para consultas
    
    // ==================== CÓDIGOS DE RESPUESTA ====================
    public static final String STATUS_OK = "OK";
    public static final String STATUS_ERROR = "ERROR";
    
    // ==================== ERRORES ESPECÍFICOS ====================
    public static final String ERR_CUENTA_NO_EXISTE = "Cuenta no existe";
    public static final String ERR_SALDO_INSUFICIENTE = "Saldo insuficiente";
    public static final String ERR_MONTO_INVALIDO = "Monto inválido";
    public static final String ERR_OPERACION_INVALIDA = "Operación no permitida";
    public static final String ERR_PARAMETROS_INVALIDOS = "Parámetros inválidos";
    public static final String ERR_CUENTA_ORIGEN_IGUAL_DESTINO = "Cuenta origen no puede ser igual a destino";
    
    // ==================== ÍNDICES DE CAMPOS EN TRAMA ====================
    public static final int IDX_TIPO_COMANDO = 0;      // TRANSAC o RESPUESTA
    public static final int IDX_TIPO_OPERACION = 1;    // DEPOSITO, RETIRO, CONSULTA, etc.
    public static final int IDX_CUENTA_ORIGEN = 2;
    public static final int IDX_CUENTA_DESTINO = 3;
    public static final int IDX_MONTO = 4;
    public static final int IDX_CONCEPTO = 5;
    
    // Para respuestas
    public static final int IDX_RESPUESTA_STATUS = 1;
    public static final int IDX_RESPUESTA_DESCRIPCION = 2;
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    /**
     * Verifica si un comando es válido
     */
    public static boolean esOperacionValida(String tipo) {
        return tipo.equals(TIPO_DEPOSITO) ||
               tipo.equals(TIPO_RETIRO) ||
               tipo.equals(TIPO_CONSULTA) ||
               tipo.equals(TIPO_DEBITO) ||
               tipo.equals(TIPO_TRANSFERENCIA);
    }
    
    /**
     * Obtiene descripción de la operación
     */
    public static String getOperationDescription(String tipo) {
        switch (tipo) {
            case TIPO_DEPOSITO:
                return "Depósito Bancario";
            case TIPO_RETIRO:
                return "Retiro Bancario";
            case TIPO_CONSULTA:
                return "Consulta de Saldo";
            case TIPO_DEBITO:
                return "Débito Automático";
            case TIPO_TRANSFERENCIA:
                return "Transferencia Bancaria";
            default:
                return "Operación desconocida";
        }
    }
}
