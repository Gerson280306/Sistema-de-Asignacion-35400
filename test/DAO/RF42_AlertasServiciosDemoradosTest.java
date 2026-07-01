package DAO;

import Modelo.Solicitud;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF42_AlertasServiciosDemoradosTest {

    private final SolicitudDAO dao = new SolicitudDAO();

    @Test
    @Order(1)
    @DisplayName("CP01 - La lista de servicios demorados no es null y solo incluye estados abiertos")
    void cp01_listaServiciosDemorados_soloEstadosAbiertos() {
        // ACT
        List<Solicitud> demorados = dao.listarParaAutoTerminar();

        // ASSERT
        assertNotNull(demorados);
        for (Solicitud s : demorados) {
            assertNotEquals("COMPLETADA", s.getEstado(),
                    "Un servicio demorado no puede estar COMPLETADO");
            assertNotEquals("CANCELADA", s.getEstado(),
                    "Un servicio demorado no puede estar CANCELADO");
        }
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - Un servicio sin tiempo estimado definido igual aparece en demorados si su fecha paso")
    void cp02_servicioSinTiempoEstimado_apareceEnDemoradosSiFechaPaso() {
        // ARRANGE: solicitud con fecha de ayer y sin observaciones de tiempo estimado
        int idSolicitud = crearSolicitudVencida();

        // ACT
        List<Solicitud> demorados = dao.listarParaAutoTerminar();

        // ASSERT
        assertTrue(demorados.stream()
                .anyMatch(s -> s.getIdSolicitud() == idSolicitud),
                "Una solicitud vencida sin tiempo estimado debe aparecer en la lista de demorados");
    }

    private int crearSolicitudVencida() {
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
            ps.setString(3, "QA RF42 demorado sin tiempo estimado");
            ps.setString(4, "ALTA"); ps.setString(5, "ASIGNADA");
            ps.setDate(6, java.sql.Date.valueOf(LocalDate.now().minusDays(2)));
            ps.setString(7, "09:00"); ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys(); keys.next(); return keys.getInt(1);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
