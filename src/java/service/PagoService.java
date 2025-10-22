package service;

import dao.*;
import model.MetodoPago;
import model.Pago;
import model.PagoTipo;
import model.Usuario;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

public class PagoService {

    private final PagoDAO pagoDAO = new PagoDAOImpl();
    private final PagoTipoDAO tipoDAO = new PagoTipoDAOImpl();
    private final MetodoPagoDAO metodoDAO = new MetodoPagoDAOImpl();

    public List<Pago> listarPorUsuario(int usuarioId) throws Exception {
        return pagoDAO.listarPorUsuario(usuarioId);
    }

    public List<PagoTipo> tiposActivos() {
        return tipoDAO.listarActivos();
    }

    public Pago obtener(int id) throws Exception {
        return pagoDAO.obtener(id);
    }
        
    public MetodoPago obtenerMetodo(int id) {
    return metodoDAO.obtener(id);
}

    /** RN5: calcular mes a pagar + mora */
    public Pago preCalcular(Usuario u, int tipoId) throws Exception {
        if (u == null || u.getId() <= 0) throw new IllegalArgumentException("Sesión no válida");

        PagoTipo tipo = tipoDAO.obtenerPorId(tipoId);
        if (tipo == null || !tipo.isActivo()) throw new IllegalArgumentException("Tipo de pago inválido");

        LocalDate ultimo = pagoDAO.ultimoMesPagado(u.getId(), tipoId);
        LocalDate mesAPagar;
        if (ultimo == null) {
            LocalDate alta = obtenerFechaCreacionUsuario(u.getId());
            YearMonth ym = YearMonth.of(alta.getYear(), alta.getMonth());
            mesAPagar = ym.atDay(1);
        } else {
            YearMonth next = YearMonth.from(ultimo).plusMonths(1);
            mesAPagar = next.atDay(1);
        }

        double recargo = 0.0;
        if ("MANTENIMIENTO".equalsIgnoreCase(tipo.getCodigo())) {
            LocalDate hoy = LocalDate.now();
            LocalDate limite = mesAPagar.withDayOfMonth(5);
            if (hoy.isAfter(limite)) {
                long dias = java.time.temporal.ChronoUnit.DAYS.between(limite, hoy);
                if (dias > 0) recargo = dias * 25.0;
            }
        }

        Pago p = new Pago();
        p.setUsuarioId(u.getId());
        p.setTipoId(tipo.getId());
        p.setTipoNombre(tipo.getNombre());
        p.setMesAPagar(mesAPagar);
        p.setMontoBase(tipo.getMonto());
        p.setRecargo(recargo);
        p.setTotal(tipo.getMonto() + recargo);
        p.setFechaPago(LocalDateTime.now());
        p.setStatus("PAGADO"); // estado al registrar
        p.setMetodo("TARJETA");
        return p;
    }

    /** Registrar el pago y devolver id */
    public int registrarPago(Usuario u, int tipoId, String observaciones,
                             Integer metodoExistenteId,
                             boolean guardarNuevaTarjeta,
                             String numeroTarjeta, String marca, String nombreTitular,
                             int mesExp, int anioExp, String cvv) throws Exception {
        if (u == null || u.getId() <= 0) throw new IllegalArgumentException("Sesión no válida");

        Pago calculado = preCalcular(u, tipoId);

        Integer metodoId = metodoExistenteId;
        if (metodoId == null && guardarNuevaTarjeta) {
            MetodoPago mp = new MetodoPago();
            mp.setIdUsuario(u.getId());
            mp.setMarca(marca != null ? marca : detectarMarca(numeroTarjeta));
            mp.setNombreTitular(nombreTitular);
            mp.setUltimos4(ultimos4(numeroTarjeta));
            mp.setMesExpiracion(mesExp);
            mp.setAnioExpiracion(anioExp);
            mp.setPanCifrado(null);
            mp.setVectorInicializacion(null);
            mp.setToken(null);
            metodoId = metodoDAO.crear(mp);
        }

        Pago p = new Pago();
        p.setUsuarioId(u.getId());
        p.setTipoId(tipoId);
        p.setMesAPagar(calculado.getMesAPagar());
        p.setFechaPago(LocalDateTime.now());
        p.setMontoBase(calculado.getMontoBase());
        p.setRecargo(calculado.getRecargo());
        p.setTotal(calculado.getTotal());
        p.setObservaciones(observaciones);
        p.setMetodoPagoId(metodoId);
        p.setMetodo("TARJETA");
        p.setStatus("PAGADO");

        int id = pagoDAO.crear(p);

        try {
            String asunto = "Pago realizado con éxito";
            String cuerpo  = "Estimado(a) " + u.getNombre() + ",<br>" +
                    "Su pago de <b>" + calculado.getTipoNombre() + "</b> correspondiente a <b>" +
                    calculado.getMesAPagar().getMonth() + " " + calculado.getMesAPagar().getYear() + "</b> " +
                    "por un total de <b>Q" + String.format("%.2f", calculado.getTotal()) + "</b> se registró correctamente.";
            new NotificacionService().enqueueEmail(u.getCorreo(), asunto, cuerpo);
        } catch (Exception ignore) {}

        return id;
    }

    public List<MetodoPago> metodosActivos(int usuarioId) {
        return metodoDAO.listarActivosPorUsuario(usuarioId);
    }

    // --- utils BD ---
    private LocalDate obtenerFechaCreacionUsuario(int usuarioId) throws Exception {
        String sql = "SELECT COALESCE(DATE(fecha_creacion), CURDATE()) FROM usuarios WHERE id=?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDate(1).toLocalDate();
            }
        }
        return LocalDate.now();
    }

    // --- utils tarjetas ---
    private static String ultimos4(String pan) {
        if (pan == null || pan.length() < 4) return "0000";
        return pan.substring(pan.length() - 4);
    }
    private static String detectarMarca(String pan) {
        if (pan == null) return "CARD";
        if (pan.startsWith("4")) return "VISA";
        if (pan.matches("^5[1-5].*")) return "MASTERCARD";
        if (pan.matches("^3[47].*")) return "AMEX";
        return "CARD";
    }
}
