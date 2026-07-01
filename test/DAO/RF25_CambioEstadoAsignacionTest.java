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
 * RF25 — Cambio de estado de asignacion
 * Estados validos: ASIGNADA -> EN_CAMINO -> EN_PROCESO -> COMPLETADA | CANCELADA
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF25_CambioEstadoAsignacionTest {

    private final AsignacionDAO asignacionDAO = new AsignacionDAO();
    private final SolicitudDAO  solicitudDAO  = new SolicitudDAO();
    private final TecnicoDAO    tecnicoDAO    = new TecnicoDAO();

    @Test
    @Order(1)
    @DisplayName("CP01 - Cambio de estado ASIGNADA a EN_CAMINO es valido")
    void cp01_asignadaAEnCamino_valido() {
        int idAsignacion = crearAsignacionEnEstado("ASIGNADA");

        boolean ok = asignacionDAO.cambiarEstado(idAsignacion, "EN_CAMINO", 0, null);

        assertTrue(ok);
        assertEquals("EN_CAMINO", obtenerEstadoAsignacion(idAsignacion));
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - Cambio de estado EN_CAMINO a EN_PROCESO es valido")
    void cp02_enCaminoAEnProceso_valido() {
        int idAsignacion = crearAsignacionEnEstado("ASIGNADA");
        asignacionDAO.cambiarEstado(idAsignacion, "EN_CAMINO", 0, null);

        boolean ok = asignacionDAO.cambiarEstado(idAsignacion, "EN_PROCESO", 0, null);

        assertTrue(ok);
        assertEquals("EN_PROCESO", obtenerEstadoAsignacion(idAsignacion));
    }

    @Test
    @Order(3)
    @DisplayName("CP03 - Cambio de estado EN_PROCESO a COMPLETADA cierra la asignacion")
    void cp03_enProcesoACompletada_cierra() {
        int idAsignacion = crearAsignacionEnEstado("ASIGNADA");
        asignacionDAO.cambiarEstado(idAsignacion, "EN_CAMINO",  0, null);
        asignacionDAO.cambiarEstado(idAsignacion, "EN_PROCESO", 0, null);

        boolean ok = asignacionDAO.cambiarEstado(idAsignacion, "COMPLETADA", 0, null);

        assertTrue(ok);
        assertEquals("COMPLETADA", obtenerEstadoAsignacion(idAsignacion));
    }

    @Test
    @Order(4)
    @DisplayName("CP04 - Estado invalido devuelve false sin romper la BD")
    void cp04_estadoInvalido_devuelveFalse() {
        int idAsignacion = crearAsignacionEnEstado("ASIGNADA");

        // El enum de la BD rechaza valores desconocidos -> INSERT falla -> devuelve false
        boolean ok = asignacionDAO.cambiarEstado(idAsignacion, "ESTADO_INEXISTENTE", 0, null);

        assertFalse(ok,
                "Un estado que no existe en el enum de la BD debe devolver false");
        // El estado original no debe cambiar
        assertEquals("ASIGNADA", obtenerEstadoAsignacion(idAsignacion));
    }

    private int crearAsignacionEnEstado(String estadoAsig) {
        try {
            PreparedStatement psC = Conexion.ConexionDB.getInstancia().getConexion()
                    .prepareStatement("SELECT id_cliente FROM tb_cliente LIMIT 1");
            ResultSet rsC = psC.executeQuery(); rsC.next(); int idCliente = rsC.getInt(1);
            PreparedStatement psT = Conexion.ConexionDB.getInstancia().getConexion()
                    .prepareStatement("SELECT id_tipo_servicio FROM tb_tipo_servicio LIMIT 1");
            ResultSet rsT = psT.executeQuery(); rsT.next(); int idTipo = rsT.getInt(1);

            PreparedStatement psS = Conexion.ConexionDB.getInstancia().getConexion()
                    .prepareStatement("INSERT INTO tb_solicitud (id_cliente, id_tipo_servicio,"
                        + " descripcion, prioridad, estado, fecha_solicitada, horario_preferido)"
                        + " VALUES (?,?,?,?,?,?,?)", java.sql.Statement.RETURN_GENERATED_KEYS);
            psS.setInt(1, idCliente); psS.setInt(2, idTipo);
            psS.setString(3, "QA RF25"); psS.setString(4, "MEDIA");
            psS.setString(5, "ASIGNADA");
            psS.setDate(6, java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
            psS.setString(7, "09:00"); psS.executeUpdate();
            ResultSet keysS = psS.getGeneratedKeys(); keysS.next();
            int idSolicitud = keysS.getInt(1);

            Tecnico tecnico = tecnicoDAO.listarActivos().get(0);
            Asignacion a = new Asignacion();
            a.setIdSolicitud(idSolicitud);
            a.setIdTecnico(tecnico.getIdTecnico());
            a.setTipoAsignacion("AUTOMATICA");
            a.setFechaProgramada(LocalDateTime.now().plusDays(1));
            a.setEstadoAsignacion(estadoAsig);
            asignacionDAO.guardar(a);

            return asignacionDAO.buscarIdPorSolicitud(idSolicitud);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private String obtenerEstadoAsignacion(int idAsignacion) {
        try (PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT estado_asignacion FROM tb_asignacion WHERE id_asignacion=?")) {
            ps.setInt(1, idAsignacion);
            ResultSet rs = ps.executeQuery(); rs.next(); return rs.getString(1);
        } catch (Exception e) { return null; }
    }
}
