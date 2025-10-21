package dao;

import java.util.List;
import model.AreaComun;

public interface AreaComunDAO {
    List<AreaComun> listarActivas() throws Exception;
}
