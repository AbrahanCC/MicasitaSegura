package service;

import dao.AreaComunDAO;
import dao.AreaComunDAOImpl;
import dao.ReservaDAO;
import dao.ReservaDAOImpl;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import model.AreaComun;
import model.Reserva;
import model.Usuario;

public class ReservaService {

    private final AreaComunDAO areaDAO = new AreaComunDAOImpl();
    private final ReservaDAO reservaDAO = new ReservaDAOImpl();

    public List<AreaComun> listarAreasActivas() throws Exception {
        return areaDAO.listarActivas();
    }

    public List<Reserva> listarPorUsuario(int usuarioId) throws Exception {
        return reservaDAO.listarPorUsuario(usuarioId);
    }

    public int crearReserva(Usuario usuario, int areaId, LocalDate fecha,
                            LocalTime ini, LocalTime fin) throws Exception {
        // Precondiciones básicas del CU (adaptar si tienes roles en sesión)
        if (usuario == null || usuario.getId() <= 0) {
            throw new IllegalArgumentException("Sesión no válida.");
        }
        if (fecha == null || ini == null || fin == null || !ini.isBefore(fin)) {
            throw new IllegalArgumentException("Datos de fecha/hora inválidos.");
        }

        // FA05: disponibilidad
        boolean ocupado = reservaDAO.existeSolapamiento(areaId, fecha, ini, fin);
        if (ocupado) {
            throw new IllegalStateException("El salón no está disponible en el horario seleccionado, por favor elija otro.");
        }

        Reserva r = new Reserva();
        r.setAreaId(areaId);
        r.setUsuarioId(usuario.getId());
        r.setFecha(fecha);
        r.setHoraInicio(ini);
        r.setHoraFin(fin);
        r.setEstado("CREADA");

        return reservaDAO.crear(r);
    }

    public void cancelarReserva(int reservaId, Usuario solicitante) throws Exception {
        if (solicitante == null || solicitante.getId() <= 0) {
            throw new IllegalArgumentException("Sesión no válida.");
        }
        // (Opcional) verificar que la reserva pertenezca al solicitante o que sea admin.
        reservaDAO.cancelar(reservaId);
    }
}
