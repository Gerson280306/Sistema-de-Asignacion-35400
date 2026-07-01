package DAO;

import Modelo.Asignacion;
import Modelo.Solicitud;
import Modelo.Tecnico;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF22_AsignacionAutomaticaTest {

    private final AsignacionDAO asignacionDAO = new AsignacionDAO();
    private final SolicitudDAO  solicitudDAO  = new SolicitudDAO();
    private final TecnicoDAO    tecnicoDAO    = new TecnicoDAO();

    @Test
    @Order(1)
    @DisplayName("CP01 - Asignacion automatica guarda la asignacion y cambia estado a ASIGNADA")
    void cp01_asignacionAutomatica_guardaYCambiaEstado() {
        // ARRANGE
        int idSolicitud = crearSolicitudPendiente();
        Tecnico tecnico = tecnicoDAO.listarActivos().get(0);

        Asignacion a = new Asignacion();
        a.setIdSolicitud(idSolicitud);
        a.setIdTecnico(tecnico.getIdTecnico());
        a.setTipoAsignacion("AUTOMATICA");
        a.setFechaProgramada(LocalDateTime.now().plusDays(1));
        a.setEstadoAsignacion("ASIGNADA");

        // ACT
        boolean guardado = asignacionDAO.guardar(a);
        boolean estadoCambiado = solicitudDAO.cambiarEstado(idSolicitud, "ASIGNADA");

        // ASSERT
        assertTrue(guardado,       "La asignacion debe guardarse en la BD");
        assertTrue(estadoCambiado, "El estado de la solicitud debe cambiar a ASIGNADA");
        assertEquals("ASIGNADA", obtenerEstadoSolicitud(idSolicitud));
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - No se puede asignar dos veces la misma solicitud (restriccion UNIQUE)")
    void cp02_dobleAsignacionMismaSolicitud_falla() {
        // ARRANGE: solicitud ya asignada (de CP01 o nueva)
        int idSolicitud = crearSolicitudPendiente();
        Tecnico tecnico = tecnicoDAO.listarActivos().get(0);

        Asignacion primera = new Asignacion();
        primera.setIdSolicitud(idSolicitud);
        primera.setIdTecnico(tecnico.getIdTecnico());
        primera.setTipoAsignacion("AUTOMATICA");
        primera.setFechaProgramada(LocalDateTime.now().plusDays(1));
        primera.setEstadoAsignacion("ASIGNADA");
        asignacionDAO.guardar(primera);

        Asignacion segunda = new Asignacion();
        segunda.setIdSolicitud(idSolicitud); // mismo id_solicitud -> viola UNIQUE
        segunda.setIdTecnico(tecnico.getIdTecnico());
        segunda.setTipoAsignacion("AUTOMATICA");
        segunda.setFechaProgramada(LocalDateTime.now().plusDays(1));
        segunda.setEstadoAsignacion("ASIGNADA");

        // ACT
        boolean resultado = asignacionDAO.guardar(segunda);

        // ASSERT
        assertFalse(resultado,
                "Asignar dos veces la misma solicitud debe fallar por la restriccion UNIQUE de la BD");
    }

    @Test
    @Order(3)
    @DisplayName("CP03 - Asignacion sin tecnico disponible no debe guardarse")
    void cp03_sinTecnicoDisponible_noSeGuarda() {
        // ARRANGE: tecnico con id inexistente
        int idSolicitud = crearSolicitudPendiente();
        Asignacion a = new Asignacion();
        a.setIdSolicitud(idSolicitud);
        a.setIdTecnico(999999); // no existe
        a.setTipoAsignacion("AUTOMATICA");
        a.setFechaProgramada(LocalDateTime.now().plusDays(1));
        a.setEstadoAsignacion("ASIGNADA");

        // ACT
        boolean guardado = asignacionDAO.guardar(a);

        // ASSERT
        assertFalse(guardado,
                "No se debe poder guardar una asignacion con un id_tecnico inexistente");
    }

    private int crearSolicitudPendiente() {
        try {
            PreparedStatement psC = Conexion.ConexionDB.getInstancia().getConexion()
                    .prepareStatement("SELECT id_cliente FROM tb_cliente LIMIT 1");
            ResultSet rsC = psC.executeQuery(); rsC.next();
            int idCliente = rsC.getInt(1);

            PreparedStatement psT = Conexion.ConexionDB.getInstancia().getConexion()
                    .prepareStatement("SELECT id_tipo_servicio FROM tb_tipo_servicio LIMIT 1");
            ResultSet rsT = psT.executeQuery(); rsT.next();
            int idTipo = rsT.getInt(1);

            PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                    .prepareStatement(
                        "INSERT INTO tb_solicitud (id_cliente, id_tipo_servicio, descripcion, "
                        + "prioridad, estado, fecha_solicitada, horario_preferido) "
                        + "VALUES (?,?,?,?,?,?,?)",
                        java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, idCliente);
            ps.setInt(2, idTipo);
            ps.setString(3, "QA RF22 fixture");
            ps.setString(4, "ALTA");
            ps.setString(5, "PENDIENTE");
            ps.setDate(6, java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
            ps.setString(7, "09:00");
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys(); keys.next();
            return keys.getInt(1);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo crear solicitud fixture RF22: " + e.getMessage(), e);
        }
    }

    private String obtenerEstadoSolicitud(int idSolicitud) {
        try (PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT estado FROM tb_solicitud WHERE id_solicitud=?")) {
            ps.setInt(1, idSolicitud);
            ResultSet rs = ps.executeQuery();
            rs.next(); return rs.getString(1);
        } catch (Exception e) { return null; }
    }
}
