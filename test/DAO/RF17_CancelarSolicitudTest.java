package DAO;

import Modelo.Solicitud;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF17_CancelarSolicitudTest {

    private final SolicitudDAO dao = new SolicitudDAO();

    @Test
    @Order(1)
    @DisplayName("CP01 - Cancelacion de solicitud PENDIENTE con motivo valido cambia estado a CANCELADA")
    void cp01_cancelacionPendienteConMotivo_cambiaEstado() {
        // ARRANGE: crear una solicitud PENDIENTE nueva
        int idSolicitud = crearSolicitudPendiente();

        // ACT
        boolean cancelado = dao.cancelarConMotivo(idSolicitud, "Motivo de prueba QA RF17");

        // ASSERT
        assertTrue(cancelado, "Debe poder cancelarse una solicitud en estado PENDIENTE");
        assertEquals("CANCELADA", obtenerEstado(idSolicitud));
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - Intentar cancelar sin motivo es rechazado")
    void cp02_cancelarSinMotivo_esRechazado() {
        // ARRANGE
        int idSolicitud = crearSolicitudPendiente();

        // ACT
        boolean sinMotivo      = dao.cancelarConMotivo(idSolicitud, "");
        boolean motivoNulo     = dao.cancelarConMotivo(idSolicitud, null);
        boolean motivoEspacios = dao.cancelarConMotivo(idSolicitud, "   ");

        // ASSERT
        assertFalse(sinMotivo,      "Motivo vacio debe ser rechazado");
        assertFalse(motivoNulo,     "Motivo null debe ser rechazado");
        assertFalse(motivoEspacios, "Motivo solo con espacios debe ser rechazado");
    }

    @Test
    @Order(3)
    @DisplayName("CP03 - Solicitud ya CANCELADA o COMPLETADA no puede volver a cancelarse")
    void cp03_solicitudYaCerrada_noPuedeCancelarse() {
        // ARRANGE: solicitud ya cancelada (de CP01)
        int idCancelada = crearSolicitudEnEstado("CANCELADA");
        int idCompletada = crearSolicitudEnEstado("COMPLETADA");

        // ACT
        boolean intentoCancelada  = dao.cancelarConMotivo(idCancelada,  "Reintento");
        boolean intentoCompletada = dao.cancelarConMotivo(idCompletada, "Reintento");

        // ASSERT
        assertFalse(intentoCancelada,  "No se puede cancelar una solicitud ya CANCELADA");
        assertFalse(intentoCompletada, "No se puede cancelar una solicitud ya COMPLETADA");
    }

    private int crearSolicitudPendiente() {
        return crearSolicitudEnEstado("PENDIENTE");
    }

    private int crearSolicitudEnEstado(String estado) {
        try {
            PreparedStatement psCliente = Conexion.ConexionDB.getInstancia().getConexion()
                    .prepareStatement("SELECT id_cliente FROM tb_cliente LIMIT 1");
            ResultSet rsC = psCliente.executeQuery(); rsC.next();
            int idCliente = rsC.getInt(1);

            PreparedStatement psTipo = Conexion.ConexionDB.getInstancia().getConexion()
                    .prepareStatement("SELECT id_tipo_servicio FROM tb_tipo_servicio LIMIT 1");
            ResultSet rsT = psTipo.executeQuery(); rsT.next();
            int idTipo = rsT.getInt(1);

            PreparedStatement psIns = Conexion.ConexionDB.getInstancia().getConexion()
                    .prepareStatement("INSERT INTO tb_solicitud "
                            + "(id_cliente, id_tipo_servicio, descripcion, prioridad, estado, "
                            + " fecha_solicitada, horario_preferido) "
                            + "VALUES (?,?,?,?,?,?,?)",
                            java.sql.Statement.RETURN_GENERATED_KEYS);
            psIns.setInt(1, idCliente);
            psIns.setInt(2, idTipo);
            psIns.setString(3, "QA RF17 fixture estado " + estado);
            psIns.setString(4, "MEDIA");
            psIns.setString(5, estado);
            psIns.setDate(6, java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
            psIns.setString(7, "09:00");
            psIns.executeUpdate();
            ResultSet keys = psIns.getGeneratedKeys();
            keys.next();
            return keys.getInt(1);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo crear solicitud fixture: " + e.getMessage(), e);
        }
    }

    private String obtenerEstado(int idSolicitud) {
        try (PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT estado FROM tb_solicitud WHERE id_solicitud=?")) {
            ps.setInt(1, idSolicitud);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getString(1);
        } catch (Exception e) { return null; }
    }
}
