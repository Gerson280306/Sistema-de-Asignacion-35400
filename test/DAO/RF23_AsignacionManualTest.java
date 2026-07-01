package DAO;

import Modelo.Asignacion;
import Modelo.Tecnico;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RF23 — Asignacion manual de tecnico
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF23_AsignacionManualTest {

    private final AsignacionDAO asignacionDAO = new AsignacionDAO();
    private final SolicitudDAO  solicitudDAO  = new SolicitudDAO();
    private final TecnicoDAO    tecnicoDAO    = new TecnicoDAO();

    @Test
    @Order(1)
    @DisplayName("CP01 - Asignacion manual guarda tipo MANUAL y cambia estado a ASIGNADA")
    void cp01_asignacionManual_guardaTipoManual() {
        // ARRANGE
        int idSolicitud = crearSolicitudPendiente();
        Tecnico tecnico = tecnicoDAO.listarActivos().get(0);

        Asignacion a = new Asignacion();
        a.setIdSolicitud(idSolicitud);
        a.setIdTecnico(tecnico.getIdTecnico());
        a.setTipoAsignacion("MANUAL");
        a.setFechaProgramada(LocalDateTime.now().plusDays(1));
        a.setEstadoAsignacion("ASIGNADA");
        a.setObservaciones("Asignacion de prueba QA RF23");

        // ACT
        boolean guardado = asignacionDAO.guardar(a);
        solicitudDAO.cambiarEstado(idSolicitud, "ASIGNADA");

        // ASSERT
        assertTrue(guardado);
        int idAsignacion = asignacionDAO.buscarIdPorSolicitud(idSolicitud);
        assertTrue(idAsignacion > 0, "Debe existir una asignacion para la solicitud");

        // Verificar que quedo como MANUAL
        String tipo = obtenerTipoAsignacion(idAsignacion);
        assertEquals("MANUAL", tipo,
                "El tipo de asignacion debe ser MANUAL cuando el gestor la hace a mano");
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - Tecnico inexistente no puede ser asignado manualmente")
    void cp02_tecnicoInexistente_noSeGuarda() {
        // ARRANGE
        int idSolicitud = crearSolicitudPendiente();
        Asignacion a = new Asignacion();
        a.setIdSolicitud(idSolicitud);
        a.setIdTecnico(888888);
        a.setTipoAsignacion("MANUAL");
        a.setFechaProgramada(LocalDateTime.now().plusDays(1));
        a.setEstadoAsignacion("ASIGNADA");

        // ACT
        boolean guardado = asignacionDAO.guardar(a);

        // ASSERT
        assertFalse(guardado,
                "No debe guardarse una asignacion manual con tecnico inexistente");
    }

    @Test
    @Order(3)
    @DisplayName("CP03 - buscarIdPorSolicitud devuelve valor negativo si la solicitud no tiene asignacion")
    void cp03_buscarIdPorSolicitudSinAsignar_devuelveNegativo() {
        // ARRANGE: solicitud nueva, sin asignacion
        int idSolicitud = crearSolicitudPendiente();

        // ACT
        int idAsignacion = asignacionDAO.buscarIdPorSolicitud(idSolicitud);

        // ASSERT: el DAO devuelve -1 cuando no existe asignacion
        assertTrue(idAsignacion <= 0,
                "Una solicitud sin asignar debe devolver un valor <= 0 (convencion: -1)");
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
            ps.setString(3, "QA RF23"); ps.setString(4, "MEDIA");
            ps.setString(5, "PENDIENTE");
            ps.setDate(6, java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
            ps.setString(7, "10:00");
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys(); keys.next(); return keys.getInt(1);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private String obtenerTipoAsignacion(int idAsignacion) {
        try (PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT tipo_asignacion FROM tb_asignacion WHERE id_asignacion=?")) {
            ps.setInt(1, idAsignacion);
            ResultSet rs = ps.executeQuery();
            rs.next(); return rs.getString(1);
        } catch (Exception e) { return null; }
    }
}
