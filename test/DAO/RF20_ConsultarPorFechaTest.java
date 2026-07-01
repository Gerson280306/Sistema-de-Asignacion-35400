package DAO;

import Modelo.Solicitud;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF20_ConsultarPorFechaTest {

    private final SolicitudDAO dao = new SolicitudDAO();

    @Test
    @Order(1)
    @DisplayName("CP01 - Consulta por fecha exacta devuelve solo solicitudes de ese dia")
    void cp01_consultaPorFechaExacta_devuelveSoloDesdEseDia() {
        // ARRANGE: buscar una fecha que ya tenga solicitudes en la BD semilla
        LocalDate fechaConDatos = obtenerFechaConSolicitudes();
        assumeFechaDisponible(fechaConDatos);

        // ACT
        List<Solicitud> resultado = dao.listarPorRango(fechaConDatos, fechaConDatos);

        // ASSERT
        assertFalse(resultado.isEmpty(),
                "Debe devolver solicitudes para una fecha con datos existentes");
        for (Solicitud s : resultado) {
            if (s.getFechaSolicitada() != null) {
                assertEquals(fechaConDatos, s.getFechaSolicitada(),
                        "Todas las solicitudes deben ser de la fecha consultada");
            }
        }
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - Rango invalido con fecha nula devuelve mensaje de error claro")
    void cp02_rangoInvalidoFechaNula_devuelveMensajeError() {
        // ACT
        String error = SolicitudDAO.validarRangoFechas(null, LocalDate.now());

        // ASSERT
        assertNotNull(error, "Una fecha nula debe generar un mensaje de error");
        assertFalse(error.isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("CP03 - Consulta por fecha sin solicitudes devuelve lista vacia")
    void cp03_fechaSinSolicitudes_listaVacia() {
        // ARRANGE: fecha en el futuro lejano sin datos
        LocalDate fechaSinDatos = LocalDate.of(2088, 6, 15);

        // ACT
        List<Solicitud> resultado = dao.listarPorRango(fechaSinDatos, fechaSinDatos);

        // ASSERT
        assertTrue(resultado.isEmpty(),
                "Una fecha sin solicitudes debe devolver lista vacia");
    }

    /** Devuelve la primera fecha_solicitada que ya tenga registros en la BD. */
    private LocalDate obtenerFechaConSolicitudes() {
        try (PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT fecha_solicitada FROM tb_solicitud "
                        + "WHERE fecha_solicitada IS NOT NULL LIMIT 1")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                java.sql.Date d = rs.getDate(1);
                return d != null ? d.toLocalDate() : null;
            }
        } catch (Exception e) { /* ignorar */ }
        return null;
    }

    private void assumeFechaDisponible(LocalDate fecha) {
        org.junit.jupiter.api.Assumptions.assumeTrue(fecha != null,
                "No hay fechas con solicitudes en la BD para probar CP01");
    }
}
