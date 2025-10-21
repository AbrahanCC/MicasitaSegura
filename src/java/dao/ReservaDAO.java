package dao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import model.Reserva;

public interface ReservaDAO {
    List<Reserva> listarPorUsuario(int usuarioId) throws Exception;
    boolean existeSolapamiento(int areaId, LocalDate fecha, LocalTime ini, LocalTime fin) throws Exception;
    int crear(Reserva r) throws Exception;
    void cancelar(int reservaId) throws Exception;
}
