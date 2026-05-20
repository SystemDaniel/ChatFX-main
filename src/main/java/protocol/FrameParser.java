package protocol;

/**
 * Parser para convertir strings en Frame objects - Formato del Banco
 * Maneja parsing de tramas: TRANSAC|TIPO|CUENTA_ORIGEN|CUENTA_DESTINO|MONTO|CONCEPTO
 *                          RESPUESTA|STATUS|DESCRIPCION
 */
public class FrameParser {
    
    /**
     * Parsea un string y lo convierte en un Frame object
     * @param data String en formato: TRANSAC|TIPO|CUENTA_ORIGEN|CUENTA_DESTINO|MONTO|CONCEPTO
     * @return Frame object o null si está mal formado
     */
    public static Frame parsear(String data) {
        if (data == null || data.isEmpty()) {
            System.err.println("[PARSER] Error: Trama vacía");
            return null;
        }

        // Dividir por pipes
        String[] partes = data.split("\\|", -1);

        if (partes.length < 2) {
            System.err.println("[PARSER] Error: Trama incompleta. Partes: " + partes.length);
            return null;
        }

        // Verificar si es transacción o respuesta
        String tipoComando = partes[0];

        if (tipoComando.equals(ProtocolConstants.DELIMITER_TRANSAC)) {
            // Es una transacción: TRANSAC|TIPO|CUENTA_ORIGEN|CUENTA_DESTINO|MONTO|CONCEPTO
            if (partes.length < 6) {
                System.err.println("[PARSER] Error: Transacción incompleta. Esperaba 6 campos, recibió: " + partes.length);
                return null;
            }

            String tipoOperacion = partes[1];
            
            if (!ProtocolConstants.esOperacionValida(tipoOperacion)) {
                System.err.println("[PARSER] Error: Tipo de operación inválido: " + tipoOperacion);
                return null;
            }

            try {
                long cuentaOrigen = Long.parseLong(partes[2]);
                long cuentaDestino = Long.parseLong(partes[3]);
                double monto = Double.parseDouble(partes[4]);
                String concepto = partes.length > 5 ? partes[5] : "";

                Frame frame = new Frame(tipoOperacion, cuentaOrigen, cuentaDestino, monto, concepto);
                System.out.println("[PARSER] ✓ Transacción parseada: " + tipoOperacion);
                return frame;

            } catch (NumberFormatException e) {
                System.err.println("[PARSER] Error: Números mal formados - " + e.getMessage());
                return null;
            }

        } else if (tipoComando.equals(ProtocolConstants.DELIMITER_RESPUESTA)) {
            // Es una respuesta: RESPUESTA|STATUS|DESCRIPCION
            if (partes.length < 3) {
                System.err.println("[PARSER] Error: Respuesta incompleta");
                return null;
            }

            String status = partes[1];
            String descripcion = partes.length > 2 ? partes[2] : "";

            Frame frame = new Frame(status, descripcion);
            System.out.println("[PARSER] ✓ Respuesta parseada: " + status);
            return frame;

        } else {
            System.err.println("[PARSER] Error: Comando desconocido: " + tipoComando);
            return null;
        }
    }

    /**
     * Valida que una trama sea válida
     */
    public static boolean esValida(Frame frame) {
        if (frame == null) {
            return false;
        }

        if (frame.esRespuesta()) {
            // Una respuesta es válida si tiene STATUS y DESCRIPCION
            return !frame.getStatus().isEmpty();
        } else {
            // Una transacción es válida si tiene todos los campos requeridos
            String tipoOp = frame.getTipoOperacion();
            
            if (!ProtocolConstants.esOperacionValida(tipoOp)) {
                return false;
            }

            switch (tipoOp) {
                case ProtocolConstants.TIPO_CONSULTA:
                    // CONSULTA solo necesita cuenta origen
                    return frame.getCuentaOrigen() > 0;

                case ProtocolConstants.TIPO_RETIRO:
                    // RETIRO necesita cuenta origen y monto
                    return frame.getCuentaOrigen() > 0 && frame.getMonto() > 0;

                case ProtocolConstants.TIPO_DEPOSITO:
                case ProtocolConstants.TIPO_DEBITO:
                case ProtocolConstants.TIPO_TRANSFERENCIA:
                    // Estas necesitan origen, destino y monto
                    return frame.getCuentaOrigen() > 0 && 
                           frame.getCuentaDestino() > 0 && 
                           frame.getMonto() > 0;

                default:
                    return false;
            }
        }
    }

    /**
     * Obtiene una descripción legible del error en la trama
     */
    public static String obtenerErrorValidacion(String data) {
        if (data == null || data.isEmpty()) {
            return "Trama vacía";
        }

        String[] partes = data.split("\\|", -1);

        if (partes.length < 2) {
            return "Trama incompleta o mal formada";
        }

        String tipoComando = partes[0];

        if (tipoComando.equals(ProtocolConstants.DELIMITER_TRANSAC)) {
            if (partes.length < 6) {
                return "Transacción incompleta. Necesita 6 campos, recibió: " + partes.length;
            }

            try {
                Long.parseLong(partes[2]); // Cuenta origen
                Long.parseLong(partes[3]); // Cuenta destino
                Double.parseDouble(partes[4]); // Monto
            } catch (NumberFormatException e) {
                return "Números mal formados en transacción";
            }

        } else if (tipoComando.equals(ProtocolConstants.DELIMITER_RESPUESTA)) {
            if (partes.length < 3) {
                return "Respuesta incompleta";
            }
        } else {
            return "Comando desconocido: " + tipoComando;
        }

        return "Formato válido";
    }
}
