package app;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Generador de vouchers en formato JPEG
 * Crea recibos profesionales de transacciones bancarias
 */
public class VoucherGenerator {
    
    private static final int ANCHO = 400;
    private static final int ALTO = 600;
    private static final Color COLOR_FONDO = Color.WHITE;
    private static final Color COLOR_ENCABEZADO = new Color(33, 150, 243);
    private static final Color COLOR_TEXTO = Color.BLACK;
    private static final Color COLOR_LINEA = new Color(200, 200, 200);
    
    private String numeroTransaccion;
    private String tipoOperacion;
    private String usuario;
    private String numeroCuenta;
    private double monto;
    private double saldoAnterior;
    private double saldoNuevo;
    private String fecha;
    private String hora;
    
    public VoucherGenerator(String numeroTransaccion, String tipoOperacion, String usuario,
                           String numeroCuenta, double monto, double saldoAnterior, 
                           double saldoNuevo) {
        this.numeroTransaccion = numeroTransaccion;
        this.tipoOperacion = tipoOperacion;
        this.usuario = usuario;
        this.numeroCuenta = numeroCuenta;
        this.monto = monto;
        this.saldoAnterior = saldoAnterior;
        this.saldoNuevo = saldoNuevo;
        
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
        Date ahora = new Date();
        this.fecha = formatoFecha.format(ahora);
        this.hora = formatoHora.format(ahora);
    }
    
    /**
     * Genera la imagen del voucher
     */
    public BufferedImage generarVoucher() {
        BufferedImage imagen = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagen.createGraphics();
        
        // Activar anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Fondo blanco
        g2d.setColor(COLOR_FONDO);
        g2d.fillRect(0, 0, ANCHO, ALTO);
        
        int y = 20;
        
        // Encabezado
        y = dibujarEncabezado(g2d, y);
        y += 20;
        
        // Línea separadora
        g2d.setColor(COLOR_LINEA);
        g2d.drawLine(20, y, ANCHO - 20, y);
        y += 15;
        
        // Información de la transacción
        y = dibujarSeccion(g2d, y, "INFORMACIÓN DE TRANSACCIÓN");
        y += 5;
        y = dibujarCampo(g2d, y, "Tipo:", tipoOperacion);
        y = dibujarCampo(g2d, y, "Transacción:", numeroTransaccion);
        y = dibujarCampo(g2d, y, "Usuario:", usuario);
        y = dibujarCampo(g2d, y, "Cuenta:", numeroCuenta);
        y += 10;
        
        // Línea separadora
        g2d.setColor(COLOR_LINEA);
        g2d.drawLine(20, y, ANCHO - 20, y);
        y += 15;
        
        // Montos
        y = dibujarSeccion(g2d, y, "DETALLES FINANCIEROS");
        y += 5;
        y = dibujarCampoMoneda(g2d, y, "Monto:", monto);
        y = dibujarCampoMoneda(g2d, y, "Saldo Anterior:", saldoAnterior);
        y = dibujarCampoMoneda(g2d, y, "Saldo Nuevo:", saldoNuevo);
        y += 10;
        
        // Línea separadora
        g2d.setColor(COLOR_LINEA);
        g2d.drawLine(20, y, ANCHO - 20, y);
        y += 15;
        
        // Fecha y hora
        y = dibujarSeccion(g2d, y, "FECHA Y HORA");
        y += 5;
        y = dibujarCampo(g2d, y, "Fecha:", fecha);
        y = dibujarCampo(g2d, y, "Hora:", hora);
        y += 20;
        
        // Línea separadora final
        g2d.setColor(COLOR_LINEA);
        g2d.drawLine(20, y, ANCHO - 20, y);
        y += 15;
        
        // Pie de página
        dibujarPiePagina(g2d, y);
        
        g2d.dispose();
        return imagen;
    }
    
    private int dibujarEncabezado(Graphics2D g2d, int y) {
        g2d.setColor(COLOR_ENCABEZADO);
        g2d.fillRect(0, 0, ANCHO, 60);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 28));
        String encabezado = "VOUCHER";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (ANCHO - fm.stringWidth(encabezado)) / 2;
        g2d.drawString(encabezado, x, 40);
        
        return 60;
    }
    
    private int dibujarSeccion(Graphics2D g2d, int y, String titulo) {
        g2d.setColor(new Color(66, 66, 66));
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString(titulo, 30, y);
        return y + 20;
    }
    
    private int dibujarCampo(Graphics2D g2d, int y, String etiqueta, String valor) {
        g2d.setColor(COLOR_TEXTO);
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        g2d.drawString(etiqueta, 30, y);
        
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.drawString(valor, 150, y);
        
        return y + 20;
    }
    
    private int dibujarCampoMoneda(Graphics2D g2d, int y, String etiqueta, double valor) {
        String valorFormato = String.format("$%.2f", valor);
        return dibujarCampo(g2d, y, etiqueta, valorFormato);
    }
    
    private void dibujarPiePagina(Graphics2D g2d, int y) {
        g2d.setColor(new Color(150, 150, 150));
        g2d.setFont(new Font("Arial", Font.ITALIC, 9));
        String footer = "Gracias por usar nuestro servicio";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (ANCHO - fm.stringWidth(footer)) / 2;
        g2d.drawString(footer, x, y);
        
        String footer2 = "Conserve este recibo";
        x = (ANCHO - fm.stringWidth(footer2)) / 2;
        g2d.drawString(footer2, x, y + 15);
    }
    
    /**
     * Guarda el voucher en archivo JPEG
     */
    public boolean guardarEnArchivo(File archivo) {
        try {
            BufferedImage voucher = generarVoucher();
            ImageIO.write(voucher, "jpg", archivo);
            System.out.println("[SUCCESS] Voucher guardado en: " + archivo.getAbsolutePath());
            return true;
        } catch (IOException e) {
            System.err.println("[ERROR] No se pudo guardar el voucher: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Genera el nombre de archivo automático
     */
    public static String generarNombreArchivo(String numeroTransaccion) {
        return "VOUCHER_" + numeroTransaccion + "_" + System.currentTimeMillis() + ".jpg";
    }
}
