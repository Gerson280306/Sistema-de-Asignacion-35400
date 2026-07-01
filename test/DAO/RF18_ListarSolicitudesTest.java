package DAO;

import Modelo.Solicitud;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF18_ListarSolicitudesTest {

    private final SolicitudDAO dao = new SolicitudDAO();

    @Test
    @Order(1)
    @DisplayName("CP01 - El listado devuelve todas las solicitudes registradas")
    void cp01_listadoDevuelveTodasLasSolicitudes() {
        // ACT
        List<Solicitud> lista = dao.listarTodos();

        // ASSERT
        assertNotNull(lista);
        assertFalse(lista.isEmpty(),
                "Ya existen solicitudes en la BD semilla, la lista no debe estar vacia");
    }

    @Test
    @Order(2)
    @Disabled("Requiere BD de pruebas sin solicitudes para no borrar datos reales. "
            + "Verificar manualmente con una BD nueva vacia.")
    @DisplayName("CP02 - Sin solicitudes registradas muestra lista vacia (requiere BD separada)")
    void cp02_sinSolicitudes_requiereBDSeparada() { }
}
