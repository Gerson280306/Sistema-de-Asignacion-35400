package DAO;

import Modelo.Solicitud;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF26_ListarAsignacionesTest {

    private final SolicitudDAO solicitudDAO = new SolicitudDAO();

    @Test
    @Order(1)
    @DisplayName("CP01 - El listado devuelve solicitudes incluyendo las asignadas")
    void cp01_listadoIncluyeSolicitudesAsignadas() {
        // ACT
        List<Solicitud> todas = solicitudDAO.listarTodos();

        // ASSERT
        assertNotNull(todas);
        assertFalse(todas.isEmpty(),
                "Ya hay datos en la BD semilla, el listado no debe venir vacio");

        boolean hayAsignadas = todas.stream()
                .anyMatch(s -> "ASIGNADA".equalsIgnoreCase(s.getEstado()));
        assertTrue(hayAsignadas,
                "Debe haber al menos una solicitud en estado ASIGNADA en la BD semilla");
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - listarPendientes devuelve una lista no nula (puede estar vacia si no hay pendientes)")
    void cp02_listarPendientes_noDevuelveNull() {
        // ACT
        List<Solicitud> pendientes = solicitudDAO.listarPendientes();

        // ASSERT: el metodo nunca debe devolver null, aunque la lista este vacia
        assertNotNull(pendientes,
                "listarPendientes() nunca debe devolver null");
        // Todas las solicitudes del resultado deben tener estado PENDIENTE
        for (Solicitud s : pendientes) {
            assertEquals("PENDIENTE", s.getEstado(),
                    "listarPendientes() solo debe devolver solicitudes en estado PENDIENTE");
        }
    }
}
