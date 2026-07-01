package DAO;

import Modelo.Solicitud;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF37_AutoTerminarTest {

    private final SolicitudDAO dao = new SolicitudDAO();

    @Test
    @Order(1)
    @DisplayName("CP01 - listarParaAutoTerminar no devuelve null")
    void cp01_listarParaAutoTerminar_noDevuelveNull() {
        List<Solicitud> lista = dao.listarParaAutoTerminar();

        assertNotNull(lista);
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - Las solicitudes del listado no estan en estado COMPLETADA ni CANCELADA")
    void cp02_listaNoContieneEstadosCerrados() {
        List<Solicitud> lista = dao.listarParaAutoTerminar();

        for (Solicitud s : lista) {
            assertNotEquals("COMPLETADA", s.getEstado(),
                    "listarParaAutoTerminar no debe incluir solicitudes COMPLETADAS");
            assertNotEquals("CANCELADA", s.getEstado(),
                    "listarParaAutoTerminar no debe incluir solicitudes CANCELADAS");
        }
    }

    @Test
    @Order(3)
    @DisplayName("CP03 - cambiarEstado a COMPLETADA simula el cierre automatico correctamente")
    void cp03_cambiarEstadoACompletada_cierraAutomaticamente() {
        // ARRANGE: solicitud con fecha pasada (ayer) para que entre en el listado
        int idSolicitud = crearSolicitudConFechaPasada();

        // ACT
        boolean cerrado = dao.cambiarEstado(idSolicitud, "COMPLETADA");

        // ASSERT
        assertTrue(cerrado);
        assertEquals("COMPLETADA", obtenerEstado(idSolicitud));
    }

    private int crearSolicitudConFechaPasada() {
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
                        + " VALUES (?,?,?,?,?,?,?)", java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, idCliente); ps.setInt(2, idTipo);
            ps.setString(3, "QA RF37 auto terminar");
            ps.setString(4, "BAJA"); ps.setString(5, "ASIGNADA");
            ps.setDate(6, java.sql.Date.valueOf(LocalDate.now().minusDays(1)));
            ps.setString(7, "09:00"); ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys(); keys.next(); return keys.getInt(1);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private String obtenerEstado(int id) {
        try (PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT estado FROM tb_solicitud WHERE id_solicitud=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery(); rs.next(); return rs.getString(1);
        } catch (Exception e) { return null; }
    }
}
