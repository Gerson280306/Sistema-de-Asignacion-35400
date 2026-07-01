package DAO;

import Modelo.Solicitud;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF38_AlertasPendientesHoyTest {

    private final SolicitudDAO dao = new SolicitudDAO();

    @Test
    @Order(1)
    @DisplayName("CP01 - listarPendientesHoy no devuelve null")
    void cp01_listarPendientesHoy_noDevuelveNull() {
        List<Solicitud> hoy = dao.listarPendientesHoy();

        assertNotNull(hoy);
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - Todas las alertas son de solicitudes PENDIENTE")
    void cp02_todasSonPendiente() {
        List<Solicitud> hoy = dao.listarPendientesHoy();

        for (Solicitud s : hoy) {
            assertEquals("PENDIENTE", s.getEstado(),
                    "listarPendientesHoy solo debe devolver solicitudes PENDIENTE");
        }
    }

    @Test
    @Order(3)
    @DisplayName("CP03 - Solicitud PENDIENTE creada para hoy aparece en las alertas")
    void cp03_solicitudPendienteHoy_apareceEnAlertas() {
        // ARRANGE: solicitud para HOY en estado PENDIENTE
        crearSolicitudHoyPendiente();

        // ACT
        List<Solicitud> hoy = dao.listarPendientesHoy();

        // ASSERT
        assertFalse(hoy.isEmpty(),
                "Debe aparecer al menos la solicitud recien creada para hoy");
    }

    private void crearSolicitudHoyPendiente() {
        try {
            PreparedStatement psC = Conexion.ConexionDB.getInstancia().getConexion()
                    .prepareStatement("SELECT id_cliente FROM tb_cliente LIMIT 1");
            ResultSet rsC = psC.executeQuery(); rsC.next(); int idCliente = rsC.getInt(1);
            PreparedStatement psT = Conexion.ConexionDB.getInstancia().getConexion()
                    .prepareStatement("SELECT id_tipo_servicio FROM tb_tipo_servicio LIMIT 1");
            ResultSet rsT = psT.executeQuery(); rsT.next(); int idTipo = rsT.getInt(1);
            PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                    .prepareStatement("INSERT INTO tb_solicitud (id_cliente, id_tipo_servicio,"
                        + " descripcion, prioridad, estado, fecha_solicitada, horario_preferido)"
                        + " VALUES (?,?,?,?,?,?,?)");
            ps.setInt(1, idCliente); ps.setInt(2, idTipo);
            ps.setString(3, "QA RF38 alerta hoy");
            ps.setString(4, "ALTA"); ps.setString(5, "PENDIENTE");
            ps.setDate(6, java.sql.Date.valueOf(LocalDate.now()));
            ps.setString(7, "09:00"); ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
