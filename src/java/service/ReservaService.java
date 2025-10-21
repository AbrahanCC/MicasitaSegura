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

    public List<AreaComun> listarActivas() throws Exception {
        return areaDAO.listarActivas();
    }

    public List<Reserva> listarPorUsuario(int usuarioId) throws Exception {
        return reservaDAO.listarPorUsuario(usuarioId);
    }

    public int crearReserva(Usuario usuario, int areaId, LocalDate fecha,
                            LocalTime ini, LocalTime fin) throws Exception {
        if (usuario == null || usuario.getId() <= 0) {
            throw new IllegalArgumentException("Sesión no válida.");
        }
        if (fecha == null || ini == null || fin == null || !ini.isBefore(fin)) {
            throw new IllegalArgumentException("Datos de fecha/hora inválidos.");
        }

        // Verificar disponibilidad (FA05)
        if (reservaDAO.existeSolapamiento(areaId, fecha, ini, fin)) {
            throw new IllegalStateException("El salón no está disponible en el horario seleccionado, por favor elija otro.");
        }

        // Crear
        Reserva r = new Reserva();
        r.setAreaId(areaId);
        r.setUsuarioId(usuario.getId());
        r.setFecha(fecha);
        r.setHoraInicio(ini);
        r.setHoraFin(fin);
        r.setEstado("CREADA");

        int idGenerado = reservaDAO.crear(r);

        // Notificación (RN4)
        String nombreArea = obtenerNombreArea(areaId);
        new NotificacionService().enqueueReservaCreada(
            usuario.getCorreo(),
            nombreArea,
            fecha.toString(),
            ini.toString(),
            fin.toString()
        );

        return idGenerado;
    }

    public void cancelarReserva(int reservaId, Usuario solicitante) throws Exception {
        if (solicitante == null || solicitante.getId() <= 0) {
            throw new IllegalArgumentException("Sesión no válida.");
        }
        // Cancelación lógica
        reservaDAO.cancelar(reservaId);
    }

    private String obtenerNombreArea(int areaId) {
        try {
            for (AreaComun a : listarActivas()) {
                if (a.getId() == areaId) return a.getNombre();
            }
        } catch (Exception ignore) {}
        return "Área";
    }
}
