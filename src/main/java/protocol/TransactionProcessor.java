package protocol;

import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Procesador de transacciones bancarias.
 * Mantiene información de cuentas y procesa operaciones.
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
        cuentas.put(12345L, new BankAccount(12345L, "Juan Pérez", 5000.0));
        cuentas.put(54321L, new BankAccount(54321L, "María García", 3500.0));
        cuentas.put(99999L, new BankAccount(99999L, "Carlos López", 10000.0));
    }

    /**
     * Procesa una trama de depósito
     */
    public Frame procesarDeposito(Frame trama) {
        Frame respuesta = new Frame("RESPUESTA");
        respuesta.campo("ID_OPERACION", trama.getId());

        try {
            long numeroCuenta = trama.obtenerLong("CUENTA");
            double monto = trama.obtenerDouble("MONTO");

            if (monto <= 0) {
                respuesta.campo("ESTADO", "ERROR");
                respuesta.campo("MENSAJE", "Monto debe ser mayor a 0");
                respuesta.campo("CODIGO", "MONTO_INVALIDO");
                return respuesta;
            }

            BankAccount cuenta = cuentas.get(numeroCuenta);
            if (cuenta == null) {
                respuesta.campo("ESTADO", "ERROR");
                respuesta.campo("MENSAJE", "Cuenta no encontrada");
                respuesta.campo("CODIGO", "CUENTA_NO_EXISTE");
                return respuesta;
            }

            double saldoAnterior = cuenta.getSaldo();
            cuenta.depositar(monto);
            double saldoNuevo = cuenta.getSaldo();

            respuesta.campo("ESTADO", "OK");
            respuesta.campo("MENSAJE", "Depósito realizado exitosamente");
            respuesta.campo("CUENTA", numeroCuenta);
            respuesta.campo("MONTO_DEPOSITADO", monto);
            respuesta.campo("SALDO_ANTERIOR", saldoAnterior);
            respuesta.campo("SALDO_NUEVO", saldoNuevo);
            respuesta.campo("CODIGO", "OPERACION_EXITOSA");

            registrarTransaccion("DEPOSITO", numeroCuenta, monto, "OK");

        } catch (Exception e) {
            respuesta.campo("ESTADO", "ERROR");
            respuesta.campo("MENSAJE", "Error procesando depósito: " + e.getMessage());
            respuesta.campo("CODIGO", "ERROR_INTERNO");
        }

        return respuesta;
    }

    /**
     * Procesa una trama de retiro
     */
    public Frame procesarRetiro(Frame trama) {
        Frame respuesta = new Frame("RESPUESTA");
        respuesta.campo("ID_OPERACION", trama.getId());

        try {
            long numeroCuenta = trama.obtenerLong("CUENTA");
            double monto = trama.obtenerDouble("MONTO");

            if (monto <= 0) {
                respuesta.campo("ESTADO", "ERROR");
                respuesta.campo("MENSAJE", "Monto debe ser mayor a 0");
                respuesta.campo("CODIGO", "MONTO_INVALIDO");
                return respuesta;
            }

            BankAccount cuenta = cuentas.get(numeroCuenta);
            if (cuenta == null) {
                respuesta.campo("ESTADO", "ERROR");
                respuesta.campo("MENSAJE", "Cuenta no encontrada");
                respuesta.campo("CODIGO", "CUENTA_NO_EXISTE");
                return respuesta;
            }

            if (cuenta.getSaldo() < monto) {
                respuesta.campo("ESTADO", "ERROR");
                respuesta.campo("MENSAJE", "Saldo insuficiente");
                respuesta.campo("CODIGO", "SALDO_INSUFICIENTE");
                respuesta.campo("SALDO_DISPONIBLE", cuenta.getSaldo());
                respuesta.campo("MONTO_SOLICITADO", monto);
                return respuesta;
            }

            double saldoAnterior = cuenta.getSaldo();
            cuenta.retirar(monto);
            double saldoNuevo = cuenta.getSaldo();

            respuesta.campo("ESTADO", "OK");
            respuesta.campo("MENSAJE", "Retiro realizado exitosamente");
            respuesta.campo("CUENTA", numeroCuenta);
            respuesta.campo("MONTO_RETIRADO", monto);
            respuesta.campo("SALDO_ANTERIOR", saldoAnterior);
            respuesta.campo("SALDO_NUEVO", saldoNuevo);
            respuesta.campo("CODIGO", "OPERACION_EXITOSA");

            registrarTransaccion("RETIRO", numeroCuenta, monto, "OK");

        } catch (Exception e) {
            respuesta.campo("ESTADO", "ERROR");
            respuesta.campo("MENSAJE", "Error procesando retiro: " + e.getMessage());
            respuesta.campo("CODIGO", "ERROR_INTERNO");
        }

        return respuesta;
    }

    /**
     * Procesa una trama de consulta de saldo
     */
    public Frame procesarConsulta(Frame trama) {
        Frame respuesta = new Frame("RESPUESTA");
        respuesta.campo("ID_OPERACION", trama.getId());

        try {
            long numeroCuenta = trama.obtenerLong("CUENTA");

            BankAccount cuenta = cuentas.get(numeroCuenta);
            if (cuenta == null) {
                respuesta.campo("ESTADO", "ERROR");
                respuesta.campo("MENSAJE", "Cuenta no encontrada");
                respuesta.campo("CODIGO", "CUENTA_NO_EXISTE");
                return respuesta;
            }

            respuesta.campo("ESTADO", "OK");
            respuesta.campo("MENSAJE", "Consulta realizada exitosamente");
            respuesta.campo("CUENTA", numeroCuenta);
            respuesta.campo("TITULAR", cuenta.getTitular());
            respuesta.campo("SALDO", cuenta.getSaldo());
            respuesta.campo("CODIGO", "OPERACION_EXITOSA");

            registrarTransaccion("CONSULTA", numeroCuenta, 0, "OK");

        } catch (Exception e) {
            respuesta.campo("ESTADO", "ERROR");
            respuesta.campo("MENSAJE", "Error procesando consulta: " + e.getMessage());
            respuesta.campo("CODIGO", "ERROR_INTERNO");
        }

        return respuesta;
    }

    /**
     * Procesa una trama de transferencia
     */
    public Frame procesarTransferencia(Frame trama) {
        Frame respuesta = new Frame("RESPUESTA");
        respuesta.campo("ID_OPERACION", trama.getId());

        try {
            long cuentaOrigen = trama.obtenerLong("CUENTA_ORIGEN");
            long cuentaDestino = trama.obtenerLong("CUENTA_DESTINO");
            double monto = trama.obtenerDouble("MONTO");

            if (monto <= 0) {
                respuesta.campo("ESTADO", "ERROR");
                respuesta.campo("MENSAJE", "Monto debe ser mayor a 0");
                respuesta.campo("CODIGO", "MONTO_INVALIDO");
                return respuesta;
            }

            if (cuentaOrigen == cuentaDestino) {
                respuesta.campo("ESTADO", "ERROR");
                respuesta.campo("MENSAJE", "No puedes transferir a la misma cuenta");
                respuesta.campo("CODIGO", "CUENTA_ORIGEN_IGUAL_DESTINO");
                return respuesta;
            }

            BankAccount origen = cuentas.get(cuentaOrigen);
            BankAccount destino = cuentas.get(cuentaDestino);

            if (origen == null || destino == null) {
                respuesta.campo("ESTADO", "ERROR");
                respuesta.campo("MENSAJE", "Una o ambas cuentas no existen");
                respuesta.campo("CODIGO", "CUENTA_NO_EXISTE");
                return respuesta;
            }

            if (origen.getSaldo() < monto) {
                respuesta.campo("ESTADO", "ERROR");
                respuesta.campo("MENSAJE", "Saldo insuficiente en cuenta origen");
                respuesta.campo("CODIGO", "SALDO_INSUFICIENTE");
                respuesta.campo("SALDO_DISPONIBLE", origen.getSaldo());
                return respuesta;
            }

            origen.retirar(monto);
            destino.depositar(monto);

            respuesta.campo("ESTADO", "OK");
            respuesta.campo("MENSAJE", "Transferencia realizada exitosamente");
            respuesta.campo("CUENTA_ORIGEN", cuentaOrigen);
            respuesta.campo("CUENTA_DESTINO", cuentaDestino);
            respuesta.campo("MONTO_TRANSFERIDO", monto);
            respuesta.campo("SALDO_ORIGEN", origen.getSaldo());
            respuesta.campo("SALDO_DESTINO", destino.getSaldo());
            respuesta.campo("CODIGO", "OPERACION_EXITOSA");

            registrarTransaccion("TRANSFERENCIA", cuentaOrigen, monto, "OK");

        } catch (Exception e) {
            respuesta.campo("ESTADO", "ERROR");
            respuesta.campo("MENSAJE", "Error procesando transferencia: " + e.getMessage());
            respuesta.campo("CODIGO", "ERROR_INTERNO");
        }

        return respuesta;
    }

    /**
     * Procesa una trama según su tipo
     */
    public Frame procesarTrama(Frame trama) {
        if (trama == null) {
            Frame error = new Frame("RESPUESTA");
            error.campo("ESTADO", "ERROR");
            error.campo("MENSAJE", "Trama nula recibida");
            error.campo("CODIGO", "TRAMA_NULA");
            return error;
        }

        if (!FrameParser.esValida(trama)) {
            Frame error = new Frame("RESPUESTA");
            error.campo("ESTADO", "ERROR");
            error.campo("MENSAJE", "Trama inválida: " + FrameParser.obtenerErrorValidacion(trama.toString()));
            error.campo("CODIGO", "TRAMA_INVALIDA");
            return error;
        }

        switch (trama.getTipo().toUpperCase()) {
            case "DEPOSITO":
                return procesarDeposito(trama);
            case "RETIRO":
                return procesarRetiro(trama);
            case "CONSULTA":
                return procesarConsulta(trama);
            case "TRANSFERENCIA":
                return procesarTransferencia(trama);
            default:
                Frame error = new Frame("RESPUESTA");
                error.campo("ESTADO", "ERROR");
                error.campo("MENSAJE", "Tipo de operación no reconocida: " + trama.getTipo());
                error.campo("CODIGO", "OPERACION_DESCONOCIDA");
                return error;
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
