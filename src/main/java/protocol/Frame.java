package protocol;

/**
 * Clase que representa una trama de datos del Banco.
 * Formato de Transacción: TRANSAC|TIPO_OPERACION|CUENTA_ORIGEN|CUENTA_DESTINO|MONTO|CONCEPTO
 * Formato de Respuesta: RESPUESTA|STATUS|DESCRIPCION
 */
public class Frame {
    private String[] campos;
    private long timestamp;
    private String id;
    private boolean esRespuesta;

    /**
     * Constructor para crear una trama de transacción
     * @param tipoOperacion DEPOSITO, RETIRO, CONSULTA, DEBITO, TRANSFERENCIA
     * @param cuentaOrigen Número de cuenta origen
     * @param cuentaDestino Número de cuenta destino (9999 para retiros/consultas)
     * @param monto Monto de la transacción
     * @param concepto Concepto/descripción de la transacción
     */
    public Frame(String tipoOperacion, long cuentaOrigen, long cuentaDestino, double monto, String concepto) {
        this.campos = new String[6];
        this.campos[0] = ProtocolConstants.DELIMITER_TRANSAC;
        this.campos[1] = tipoOperacion;
        this.campos[2] = String.valueOf(cuentaOrigen);
        this.campos[3] = String.valueOf(cuentaDestino);
        this.campos[4] = String.format("%.2f", monto);
        this.campos[5] = concepto != null ? concepto : "";
        
        this.timestamp = System.currentTimeMillis();
        this.id = "FRAME_" + System.nanoTime();
        this.esRespuesta = false;
    }

    /**
     * Constructor para crear una trama de respuesta
     * @param status OK o ERROR
     * @param descripcion Descripción del resultado
     */
    public Frame(String status, String descripcion) {
        this.campos = new String[3];
        this.campos[0] = ProtocolConstants.DELIMITER_RESPUESTA;
        this.campos[1] = status;
        this.campos[2] = descripcion != null ? descripcion : "";
        
        this.timestamp = System.currentTimeMillis();
        this.id = "RESPUESTA_" + System.nanoTime();
        this.esRespuesta = true;
    }

    /**
     * Parsea un string en formato de trama y retorna un Frame
     */
    public static Frame parsearDesdeString(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }

        String[] partes = data.split("\\|", -1);
        
        if (partes.length < 2) {
            return null;
        }

        Frame frame = new Frame(partes[1], "");
        frame.campos = partes;
        
        if (partes[0].equals(ProtocolConstants.DELIMITER_RESPUESTA)) {
            frame.esRespuesta = true;
        } else if (partes[0].equals(ProtocolConstants.DELIMITER_TRANSAC)) {
            frame.esRespuesta = false;
        }

        return frame;
    }

    /**
     * Obtiene un campo por índice
     */
    public String obtener(int indice) {
        if (indice >= 0 && indice < campos.length) {
            return campos[indice];
        }
        return "";
    }

    /**
     * Obtiene un campo como número largo
     */
    public long obtenerLong(int indice) {
        try {
            return Long.parseLong(obtener(indice));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Obtiene un campo como número decimal
     */
    public double obtenerDouble(int indice) {
        try {
            return Double.parseDouble(obtener(indice));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Obtiene el tipo de operación
     */
    public String getTipoOperacion() {
        return obtener(ProtocolConstants.IDX_TIPO_OPERACION);
    }

    /**
     * Obtiene la cuenta origen
     */
    public long getCuentaOrigen() {
        return obtenerLong(ProtocolConstants.IDX_CUENTA_ORIGEN);
    }

    /**
     * Obtiene la cuenta destino
     */
    public long getCuentaDestino() {
        return obtenerLong(ProtocolConstants.IDX_CUENTA_DESTINO);
    }

    /**
     * Obtiene el monto
     */
    public double getMonto() {
        return obtenerDouble(ProtocolConstants.IDX_MONTO);
    }

    /**
     * Obtiene el concepto
     */
    public String getConcepto() {
        return obtener(ProtocolConstants.IDX_CONCEPTO);
    }

    /**
     * Obtiene el status de respuesta
     */
    public String getStatus() {
        return obtener(ProtocolConstants.IDX_RESPUESTA_STATUS);
    }

    /**
     * Obtiene la descripción de respuesta
     */
    public String getDescripcion() {
        return obtener(ProtocolConstants.IDX_RESPUESTA_DESCRIPCION);
    }

    /**
     * Verifica si es una respuesta
     */
    public boolean esRespuesta() {
        return esRespuesta;
    }

    /**
     * Convierte la trama a string en formato de protocolo
     * TRANSAC|TIPO|CUENTA_ORIGEN|CUENTA_DESTINO|MONTO|CONCEPTO
     * RESPUESTA|STATUS|DESCRIPCION
     */
    @Override
    public String toString() {
        return String.join(ProtocolConstants.DELIMITER_CAMPO, campos);
    }

    /**
     * Obtiene el ID único de la trama
     */
    public String getId() {
        return id;
    }

    /**
     * Obtiene el timestamp de creación
     */
    public long getTimestamp() {
        return timestamp;
    }
}
