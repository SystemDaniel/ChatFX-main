package protocol;

/**
 * Parser para convertir strings en Frame objects y validar formato.
 * Maneja el parsing de tramas según el protocolo definido.
 */
public class FrameParser {
    
    /**
     * Parsea un string y lo convierte en un Frame object
     * @param data String en formato: <INICIO>|TIPO:tipo|CAMPO1:valor1|CAMPO2:valor2|FIN
     * @return Frame object o null si está mal formado
     */
    public static Frame parsear(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }

        // Verificar inicio y fin
        if (!data.startsWith("<INICIO>") || !data.endsWith("|FIN")) {
            return null;
        }

        // Remover marcas de inicio y fin
        String contenido = data.substring("<INICIO>|".length());
        contenido = contenido.substring(0, contenido.length() - "|FIN".length());

        // Dividir por pipes
        String[] partes = contenido.split("\\|");
        
        if (partes.length == 0) {
            return null;
        }

        // Primera parte debe ser TIPO:tipo
        String[] tipoPartes = partes[0].split(":", 2);
        if (tipoPartes.length != 2 || !tipoPartes[0].equals("TIPO")) {
            return null;
        }

        String tipo = tipoPartes[1];
        Frame frame = new Frame(tipo);

        // Procesar campos restantes
        for (int i = 1; i < partes.length; i++) {
            String[] campoParte = partes[i].split(":", 2);
            if (campoParte.length == 2) {
                frame.campo(campoParte[0], campoParte[1]);
            }
        }

        return frame;
    }

    /**
     * Valida que una trama sea válida
     */
    public static boolean esValida(Frame frame) {
        if (frame == null) {
            return false;
        }

        String tipo = frame.getTipo();
        
        // Validar según tipo de operación
        switch (tipo) {
            case "DEPOSITO":
                return frame.existe("CUENTA") && frame.existe("MONTO") && frame.obtenerDouble("MONTO") > 0;
            
            case "RETIRO":
                return frame.existe("CUENTA") && frame.existe("MONTO") && frame.obtenerDouble("MONTO") > 0;
            
            case "CONSULTA":
                return frame.existe("CUENTA");
            
            case "RESPUESTA":
                return frame.existe("ESTADO") && frame.existe("MENSAJE");
            
            case "TRANSFERENCIA":
                return frame.existe("CUENTA_ORIGEN") && frame.existe("CUENTA_DESTINO") && 
                       frame.existe("MONTO") && frame.obtenerDouble("MONTO") > 0;
            
            default:
                return false;
        }
    }

    /**
     * Obtiene una descripción legible del error en la trama
     */
    public static String obtenerErrorValidacion(String data) {
        if (data == null || data.isEmpty()) {
            return "Trama vacía";
        }

        if (!data.startsWith("<INICIO>")) {
            return "Falta marcador de inicio <INICIO>";
        }

        if (!data.endsWith("|FIN")) {
            return "Falta marcador de fin |FIN";
        }

        String contenido = data.substring("<INICIO>|".length());
        contenido = contenido.substring(0, contenido.length() - "|FIN".length());
        
        String[] partes = contenido.split("\\|");
        
        if (partes.length == 0) {
            return "Trama sin contenido";
        }

        String[] tipoPartes = partes[0].split(":", 2);
        if (tipoPartes.length != 2 || !tipoPartes[0].equals("TIPO")) {
            return "Formato de TIPO incorrecto";
        }

        return "Formato válido pero operación no reconocida";
    }
}
