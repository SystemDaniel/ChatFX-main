package protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase que representa una trama de datos en el protocolo de transacciones.
 * Formato: <INICIO>|TIPO:operacion|CAMPO1:valor1|CAMPO2:valor2|FIN
 */
public class Frame {
    private String tipo;
    private Map<String, String> campos;
    private long timestamp;
    private String id;

    public Frame(String tipo) {
        this.tipo = tipo;
        this.campos = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
        this.id = "FRAME_" + System.nanoTime();
    }

    /**
     * Agrega un campo a la trama
     */
    public Frame campo(String clave, String valor) {
        if (valor == null) {
            valor = "";
        }
        this.campos.put(clave, valor.toString());
        return this;
    }

    /**
     * Agrega un campo numérico a la trama
     */
    public Frame campo(String clave, long valor) {
        this.campos.put(clave, String.valueOf(valor));
        return this;
    }

    /**
     * Agrega un campo de moneda a la trama
     */
    public Frame campo(String clave, double valor) {
        this.campos.put(clave, String.format("%.2f", valor));
        return this;
    }

    /**
     * Obtiene un campo de la trama
     */
    public String obtener(String clave) {
        return campos.getOrDefault(clave, "");
    }

    /**
     * Obtiene un campo como número largo
     */
    public long obtenerLong(String clave) {
        try {
            return Long.parseLong(obtener(clave));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Obtiene un campo como número decimal
     */
    public double obtenerDouble(String clave) {
        try {
            return Double.parseDouble(obtener(clave));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Verifica si existe un campo
     */
    public boolean existe(String clave) {
        return campos.containsKey(clave);
    }

    /**
     * Convierte la trama a string en formato de protocolo
     * Formato: <INICIO>|TIPO:tipo|CAMPO1:valor1|CAMPO2:valor2|FIN
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<INICIO>|TIPO:").append(tipo);
        
        for (Map.Entry<String, String> entry : campos.entrySet()) {
            sb.append("|").append(entry.getKey()).append(":").append(entry.getValue());
        }
        
        sb.append("|FIN");
        return sb.toString();
    }

    /**
     * Obtiene el tipo de trama
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * Obtiene todos los campos
     */
    public Map<String, String> getCampos() {
        return new HashMap<>(campos);
    }

    /**
     * Obtiene el timestamp de creación
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Obtiene el ID único de la trama
     */
    public String getId() {
        return id;
    }
}
