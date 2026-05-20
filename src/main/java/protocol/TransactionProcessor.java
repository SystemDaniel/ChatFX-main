package protocol;

import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Procesador de transacciones bancarias - Formato del Banco
 * Estructura: TRANSAC|TIPO_OPERACION|CUENTA_ORIGEN|CUENTA_DESTINO|MONTO|CONCEPTO
 * Respuesta: RESPUESTA|STATUS|DESCRIPCION
 */
public class TransactionProcessor {
    private Map<Long, BankAccount> cuentas;
    private List<Transaction> historial;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TransactionProcessor() {
        this.cuentas = Collections.synchronizedMap(new HashMap<>());
        this.historial = Collections.synchronizedList(new ArrayList<>());
        inicializarCuentasPrueba();
    }

    /**
     * Inicializa cuentas de prueba
     */
    private void inicializarCuentasPrueba() {
        cuentas.put(10000001L, new BankAccount(10000001L, "Juan Pérez", 5000.0));
        cuentas.put(10000002L, new BankAccount(10000002L, "María García", 3500.0));
        cuentas.put(10000003L, new BankAccount(10000003L, "Carlos López", 10000.0));
        
        System.out.println("[BANCO] Cuentas de prueba cargadas:");
        for (BankAccount cuenta : cuentas.values()) {
            System.out.println("  - " + cuenta.getTitular() + " (" + cuenta.getNumero() + "): $" + cuenta.getSaldo());
        }
    }

    /**
     * Procesa una trama según su tipo de operación
     * @param trama Frame con formato: TRANSAC|TIPO|CUENTA_ORIGEN|CUENTA_DESTINO|MONTO|CONCEPTO
     * @return Frame respuesta: RESPUESTA|STATUS|DESCRIPCION
     */
    public Frame procesarTrama(Frame trama) {
        if (trama == null) {
            System.err.println("[BANCO] Error: Trama nula");
            return new Frame(ProtocolConstants.STATUS_ERROR, "Trama nula recibida");
        }

        if (!FrameParser.esValida(trama)) {
            String error = FrameParser.obtenerErrorValidacion(trama.toString());
            System.err.println("[BANCO] Trama inválida: " + error);
            return new Frame(ProtocolConstants.STATUS_ERROR, error);
        }

        String tipoOperacion = trama.getTipoOperacion();
        System.out.println("[BANCO] Procesando: " + tipoOperacion);

        switch (tipoOperacion.toUpperCase()) {
            case ProtocolConstants.TIPO_DEPOSITO:
                return procesarDeposito(trama);
            case ProtocolConstants.TIPO_RETIRO:
                return procesarRetiro(trama);
            case ProtocolConstants.TIPO_CONSULTA:
                return procesarConsulta(trama);
            case ProtocolConstants.TIPO_DEBITO:
                return procesarDebito(trama);
            case ProtocolConstants.TIPO_TRANSFERENCIA:
                return procesarTransferencia(trama);
            default:
                System.err.println("[BANCO] Operación no reconocida: " + tipoOperacion);
                return new Frame(ProtocolConstants.STATUS_ERROR, "Operación no reconocida: " + tipoOperacion);
        }
    }

    /**
     * Procesa DEPOSITO: mueve fondos a una cuenta
     * TRANSAC|DEPOSITO|CUENTA_ORIGEN|CUENTA_DESTINO|MONTO|CONCEPTO
     */
    private Frame procesarDeposito(Frame trama) {
        try {
            long cuentaDestino = trama.getCuentaDestino();
            double monto = trama.getMonto();
            String concepto = trama.getConcepto();

            // Validar monto
            if (monto <= 0) {
                System.out.println("[BANCO] Depósito rechazado: Monto inválido (" + monto + ")");
                return new Frame(ProtocolConstants.STATUS_ERROR, ProtocolConstants.ERR_MONTO_INVALIDO);
            }

            // Validar cuenta destino
            BankAccount cuenta = cuentas.get(cuentaDestino);
            if (cuenta == null) {
                System.out.println("[BANCO] Depósito rechazado: Cuenta " + cuentaDestino + " no existe");
                return new Frame(ProtocolConstants.STATUS_ERROR, ProtocolConstants.ERR_CUENTA_NO_EXISTE);
            }

            // Realizar depósito
            double saldoAnterior = cuenta.getSaldo();
            cuenta.depositar(monto);
            double saldoNuevo = cuenta.getSaldo();

            String respuesta = String.format("Depósito exitoso. Saldo anterior: $%.2f, Nuevo: $%.2f", saldoAnterior, saldoNuevo);
            System.out.println("[BANCO] ✓ " + respuesta);
            
            registrarTransaccion(ProtocolConstants.TIPO_DEPOSITO, cuentaDestino, monto, ProtocolConstants.STATUS_OK);
            return new Frame(ProtocolConstants.STATUS_OK, respuesta);

        } catch (Exception e) {
            System.err.println("[BANCO] Error en depósito: " + e.getMessage());
            return new Frame(ProtocolConstants.STATUS_ERROR, "Error procesando depósito: " + e.getMessage());
        }
    }

    /**
     * Procesa RETIRO: usa cuenta 9999 como destino
     * TRANSAC|RETIRO|CUENTA_ORIGEN|9999|MONTO|CONCEPTO
     */
    private Frame procesarRetiro(Frame trama) {
        try {
            long cuentaOrigen = trama.getCuentaOrigen();
            double monto = trama.getMonto();
            String concepto = trama.getConcepto();

            // Validar monto
            if (monto <= 0) {
                System.out.println("[BANCO] Retiro rechazado: Monto inválido");
                return new Frame(ProtocolConstants.STATUS_ERROR, ProtocolConstants.ERR_MONTO_INVALIDO);
            }

            // Validar cuenta origen
            BankAccount cuenta = cuentas.get(cuentaOrigen);
            if (cuenta == null) {
                System.out.println("[BANCO] Retiro rechazado: Cuenta " + cuentaOrigen + " no existe");
                return new Frame(ProtocolConstants.STATUS_ERROR, ProtocolConstants.ERR_CUENTA_NO_EXISTE);
            }

            // Validar saldo suficiente
            if (cuenta.getSaldo() < monto) {
                String error = String.format("Saldo insuficiente. Disponible: $%.2f, Solicitado: $%.2f", 
                    cuenta.getSaldo(), monto);
                System.out.println("[BANCO] Retiro rechazado: " + error);
                return new Frame(ProtocolConstants.STATUS_ERROR, ProtocolConstants.ERR_SALDO_INSUFICIENTE + ": " + error);
            }

            // Realizar retiro
            double saldoAnterior = cuenta.getSaldo();
            cuenta.retirar(monto);
            double saldoNuevo = cuenta.getSaldo();

            String respuesta = String.format("Retiro exitoso. Saldo anterior: $%.2f, Nuevo: $%.2f", saldoAnterior, saldoNuevo);
            System.out.println("[BANCO] ✓ " + respuesta);
            
            registrarTransaccion(ProtocolConstants.TIPO_RETIRO, cuentaOrigen, monto, ProtocolConstants.STATUS_OK);
            return new Frame(ProtocolConstants.STATUS_OK, respuesta);

        } catch (Exception e) {
            System.err.println("[BANCO] Error en retiro: " + e.getMessage());
            return new Frame(ProtocolConstants.STATUS_ERROR, "Error procesando retiro: " + e.getMessage());
        }
    }
 
    /**
     * Procesa CONSULTA: usa destino = 9999 y monto = 0.0
     * TRANSAC|CONSULTA|CUENTA_ORIGEN|9999|0.0|CONCEPTO
     */
    private Frame procesarConsulta(Frame trama) {
        try {
            long cuentaOrigen = trama.getCuentaOrigen();

            // Validar cuenta
            BankAccount cuenta = cuentas.get(cuentaOrigen);
            if (cuenta == null) {
                System.out.println("[BANCO] Consulta rechazada: Cuenta " + cuentaOrigen + " no existe");
                return new Frame(ProtocolConstants.STATUS_ERROR, ProtocolConstants.ERR_CUENTA_NO_EXISTE);
            }

            String respuesta = String.format("Saldo de %s (Cuenta %d): $%.2f", 
                cuenta.getTitular(), cuenta.getNumero(), cuenta.getSaldo());
            System.out.println("[BANCO] ✓ " + respuesta);
            
            registrarTransaccion(ProtocolConstants.TIPO_CONSULTA, cuentaOrigen, 0, ProtocolConstants.STATUS_OK);
            return new Frame(ProtocolConstants.STATUS_OK, respuesta);

        } catch (Exception e) {
            System.err.println("[BANCO] Error en consulta: " + e.getMessage());
            return new Frame(ProtocolConstants.STATUS_ERROR, "Error procesando consulta: " + e.getMessage());
        }
    }

    /**
     * Procesa DEBITO: cobro automático de una cuenta
     * TRANSAC|DEBITO|CUENTA_ORIGEN|CUENTA_DESTINO|MONTO|CONCEPTO
    */
    private Frame procesarDebito(Frame trama) {
        try {
            long cuentaOrigen = trama.getCuentaOrigen();
            long cuentaDestino = trama.getCuentaDestino();
            double monto = trama.getMonto();
            String concepto = trama.getConcepto();

            // Validar monto
            if (monto <= 0) {
                System.out.println("[BANCO] Débito rechazado: Monto inválido");
                return new Frame(ProtocolConstants.STATUS_ERROR, ProtocolConstants.ERR_MONTO_INVALIDO);
            }

            // Validar cuentas
            BankAccount origen = cuentas.get(cuentaOrigen);
            if (origen == null) {
                System.out.println("[BANCO] Débito rechazado: Cuenta origen no existe");
                return new Frame(ProtocolConstants.STATUS_ERROR, ProtocolConstants.ERR_CUENTA_NO_EXISTE);
            }

            // Validar saldo
            if (origen.getSaldo() < monto) {
                return new Frame(ProtocolConstants.STATUS_ERROR, ProtocolConstants.ERR_SALDO_INSUFICIENTE);
            }

            // Realizar débito
            double saldoAnterior = origen.getSaldo();
            origen.retirar(monto);
            double saldoNuevo = origen.getSaldo();

            String respuesta = String.format("Débito exitoso. Saldo anterior: $%.2f, Nuevo: $%.2f", saldoAnterior, saldoNuevo);
            System.out.println("[BANCO] ✓ " + respuesta);
            
            registrarTransaccion(ProtocolConstants.TIPO_DEBITO, cuentaOrigen, monto, ProtocolConstants.STATUS_OK);
            return new Frame(ProtocolConstants.STATUS_OK, respuesta);

        } catch (Exception e) {
            System.err.println("[BANCO] Error en débito: " + e.getMessage());
            return new Frame(ProtocolConstants.STATUS_ERROR, "Error procesando débito: " + e.getMessage());
        }
    }  

    /**
     * Procesa TRANSFERENCIA: transfiere fondos entre cuentas
     * TRANSAC|TRANSFERENCIA|CUENTA_ORIGEN|CUENTA_DESTINO|MONTO|CONCEPTO
     */
    private Frame procesarTransferencia(Frame trama) {
        try {
            long cuentaOrigen = trama.getCuentaOrigen();
            long cuentaDestino = trama.getCuentaDestino();
            double monto = trama.getMonto();
            String concepto = trama.getConcepto();

            // Validar monto
            if (monto <= 0) {
                return new Frame(ProtocolConstants.STATUS_ERROR, ProtocolConstants.ERR_MONTO_INVALIDO);
            }

            // No puede transferir a la misma cuenta
            if (cuentaOrigen == cuentaDestino) {
                return new Frame(ProtocolConstants.STATUS_ERROR, ProtocolConstants.ERR_CUENTA_ORIGEN_IGUAL_DESTINO);
            }

            // Validar ambas cuentas
            BankAccount origen = cuentas.get(cuentaOrigen);
            BankAccount destino = cuentas.get(cuentaDestino);

            if (origen == null || destino == null) {
                System.out.println("[BANCO] Transferencia rechazada: Cuenta no existe");
                return new Frame(ProtocolConstants.STATUS_ERROR, ProtocolConstants.ERR_CUENTA_NO_EXISTE);
            }

            // Validar saldo origen
            if (origen.getSaldo() < monto) {
                return new Frame(ProtocolConstants.STATUS_ERROR, ProtocolConstants.ERR_SALDO_INSUFICIENTE);
            }

            // Realizar transferencia
            double saldoOrigenAntes = origen.getSaldo();
            double saldoDestinoAntes = destino.getSaldo();

            origen.retirar(monto);
            destino.depositar(monto);

            String respuesta = String.format("Transferencia exitosa. De $%.2f a $%.2f", saldoOrigenAntes, saldoDestinoAntes);
            System.out.println("[BANCO] ✓ " + respuesta);
            
            registrarTransaccion(ProtocolConstants.TIPO_TRANSFERENCIA, cuentaOrigen, monto, ProtocolConstants.STATUS_OK);
            return new Frame(ProtocolConstants.STATUS_OK, respuesta);

        } catch (Exception e) {
            System.err.println("[BANCO] Error en transferencia: " + e.getMessage());
            return new Frame(ProtocolConstants.STATUS_ERROR, "Error procesando transferencia: " + e.getMessage());
        }
    }

    /**
     * Registra una transacción en el historial
     */
    private void registrarTransaccion(String tipo, long cuenta, double monto, String estado) {
        historial.add(new Transaction(tipo, cuenta, monto, estado));
    }

    /**
     * Obtiene el historial de transacciones
     */
    public List<Transaction> obtenerHistorial() {
        return new ArrayList<>(historial);
    }

    /**
     * Obtiene una cuenta
     */
    public BankAccount obtenerCuenta(long numeroCuenta) {
        return cuentas.get(numeroCuenta);
    }

    /**
     * Obtiene todas las cuentas (para reportes)
     */
    public Collection<BankAccount> obtenerTodasLasCuentas() {
        return new ArrayList<>(cuentas.values());
    }

    /**
     * Clase interna para representar una cuenta bancaria
     */
    public static class BankAccount {
        private long numero;
        private String titular;
        private double saldo;

        public BankAccount(long numero, String titular, double saldoInicial) {
            this.numero = numero;
            this.titular = titular;
            this.saldo = saldoInicial;
        }

        public synchronized void depositar(double monto) {
            this.saldo += monto;
        }

        public synchronized void retirar(double monto) {
            this.saldo -= monto;
        }

        public long getNumero() {
            return numero;
        }

        public String getTitular() {
            return titular;
        }

        public synchronized double getSaldo() {
            return saldo;
        }
    }

    /**
     * Clase interna para representar una transacción en el historial
     */
    public static class Transaction {
        private String tipo;
        private long cuenta;
        private double monto;
        private String estado;
        private LocalDateTime fecha;

        public Transaction(String tipo, long cuenta, double monto, String estado) {
            this.tipo = tipo;
            this.cuenta = cuenta;
            this.monto = monto;
            this.estado = estado;
            this.fecha = LocalDateTime.now();
        }

        public String getTipo() {
            return tipo;
        }

        public long getCuenta() {
            return cuenta;
        }

        public double getMonto() {
            return monto;
        }

        public String getEstado() {
            return estado;
        }

        public LocalDateTime getFecha() {
            return fecha;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s - Cuenta: %d, Monto: %.2f - %s", 
                fecha, tipo, cuenta, monto, estado);
        }
    }
}
