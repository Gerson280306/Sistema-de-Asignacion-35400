package DAO;

import Modelo.Asignacion;
import Modelo.Tecnico;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RF24 — Reasignacion de tecnico
 *
 * AsignacionDAO.cambiarEstado() acepta un nuevoTecnico para la reasignacion.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF24_ReasignacionTecnicoTest {

    private final AsignacionDAO asignacionDAO = new AsignacionDAO();
    private final SolicitudDAO  solicitudDAO  = new SolicitudDAO();
    private final TecnicoDAO    tecnicoDAO    = new TecnicoDAO();

    @Test
    @Order(1)
    @DisplayName("CP01 - Reasignacion a otro tecnico actualiza el tecnico en la asignacion")
    void cp01_reasignacionValida_actualizaTecnico() {
        // ARRANGE: crear solicitud y asignarla al primer tecnico activo
        List<Tecnico> activos = tecnicoDAO.listarActivos();
        assertTrue(activos.size() >= 2,
                "Se necesitan al menos 2 tecnicos activos para reasignar");

        int idSolicitud = crearSolicitudPendiente();
        Tecnico tecnico1 = activos.get(0);
        Tecnico tecnico2 = activos.get(1);

        Asignacion a = new Asignacion();
        a.setIdSolicitud(idSolicitud);
        a.setIdTecnico(tecnico1.getIdTecnico());
        a.setTipoAsignacion("AUTOMATICA");
        a.setFechaProgramada(LocalDateTime.now().plusDays(1));
        a.setEstadoAsignacion("ASIGNADA");
        asignacionDAO.guardar(a);
        int idAsignacion = asignacionDAO.buscarIdPorSolicitud(idSolicitud);

        // ACT: reasignar al tecnico2
        boolean reasignado = asignacionDAO.cambiarEstado(
                idAsignacion, "ASIGNADA", tecnico2.getIdTecnico(), "Reasignacion QA RF24");

        // ASSERT
        assertTrue(reasignado, "La reasignacion debe ejecutarse correctamente");
        assertEquals(tecnico2.getIdTecnico(), obtenerIdTecnicoAsignacion(idAsignacion),
                "El tecnico de la asignacion debe ser ahora el tecnico2");
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - Reasignacion con tecnico inexistente falla sin lanzar excepcion")
    void cp02_reasignacionTecnicoInexistente_devuelveFalse() {
        // ARRANGE
        int idSolicitud = crearSolicitudPendiente();
        Tecnico tecnico = tecnicoDAO.listarActivos().get(0);
        Asignacion a = new Asignacion();
        a.setIdSolicitud(idSolicitud);
        a.setIdTecnico(tecnico.getIdTecnico());
        a.setTipoAsignacion("AUTOMATICA");
        a.setFechaProgramada(LocalDateTime.now().plusDays(1));
        a.setEstadoAsignacion("ASIGNADA");
        asignacionDAO.guardar(a);
        int idAsignacion = asignacionDAO.buscarIdPorSolicitud(idSolicitud);

        // ACT
        boolean resultado = asignacionDAO.cambiarEstado(
                idAsignacion, "ASIGNADA", 777777, "Tecnico inexistente");

        // ASSERT
        assertFalse(resultado,
                "No debe reasignarse a un tecnico con id inexistente");
    }

    private int crearSolicitudPendiente() {
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
            ps.setString(3, "QA RF24"); ps.setString(4, "ALTA");
            ps.setString(5, "PENDIENTE");
            ps.setDate(6, java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
            ps.setString(7, "11:00");
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys(); keys.next(); return keys.getInt(1);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private int obtenerIdTecnicoAsignacion(int idAsignacion) {
        try (PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT id_tecnico FROM tb_asignacion WHERE id_asignacion=?")) {
            ps.setInt(1, idAsignacion);
            ResultSet rs = ps.executeQuery(); rs.next(); return rs.getInt(1);
        } catch (Exception e) { return -1; }
    }
}
