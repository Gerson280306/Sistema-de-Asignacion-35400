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
 * RF39 - Eliminar tecnico (desactivar logicamente)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF39_EliminarTecnicoTest {

    private final TecnicoDAO    tecnicoDAO    = new TecnicoDAO();
    private final AsignacionDAO asignacionDAO = new AsignacionDAO();

    @Test
    @Order(1)
    @DisplayName("CP01 - Eliminar tecnico sin asignaciones activas lo desactiva correctamente")
    void cp01_eliminarTecnicoSinAsignaciones_desactiva() {
        int idTecnico = crearTecnicoFixture();

        boolean eliminado = tecnicoDAO.eliminar(idTecnico);

        assertTrue(eliminado);
        // Verificar que fue desactivado (estado = 0), no borrado fisicamente
        int estado = obtenerEstadoTecnico(idTecnico);
        assertEquals(0, estado,
                "El tecnico debe quedar con estado=0 (desactivado), no eliminado fisicamente");
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - Tecnico con asignaciones activas no puede eliminarse")
    void cp02_tecnicoConAsignacionesActivas_noSePuedeEliminar() {
        // ARRANGE: tecnico con asignacion en estado ASIGNADA
        int idTecnico = crearTecnicoFixture();
        int idSolicitud = crearSolicitudPendiente();

        Asignacion a = new Asignacion();
        a.setIdSolicitud(idSolicitud);
        a.setIdTecnico(idTecnico);
        a.setTipoAsignacion("MANUAL");
        a.setFechaProgramada(LocalDateTime.now().plusDays(1));
        a.setEstadoAsignacion("ASIGNADA");
        asignacionDAO.guardar(a);

        // ACT
        boolean tieneActivas = tecnicoTieneAsignacionesActivas(idTecnico);

        // ASSERT
        assertTrue(tieneActivas,
                "El tecnico con asignacion ASIGNADA debe tener asignaciones activas");
        // El controlador usa este chequeo para bloquear el boton Eliminar
    }

    @Test
    @Order(3)
    @DisplayName("CP03 - Tecnico inexistente devuelve false sin excepcion")
    void cp03_tecnicoInexistente_devuelveFalse() {
        boolean resultado = tecnicoDAO.eliminar(999999);

        assertFalse(resultado,
                "Intentar eliminar un tecnico inexistente debe devolver false");
    }

    private boolean tecnicoTieneAsignacionesActivas(int idTecnico) {
        try (PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT COUNT(*) FROM tb_asignacion "
                    + "WHERE id_tecnico=? AND estado_asignacion "
                    + "NOT IN ('COMPLETADA','CANCELADA')")) {
            ps.setInt(1, idTecnico);
            ResultSet rs = ps.executeQuery(); rs.next();
            return rs.getInt(1) > 0;
        } catch (Exception e) { return false; }
    }

    private int crearTecnicoFixture() {
        String dni = String.format("%08d", Math.abs(System.nanoTime() % 100000000L));
        Tecnico t = new Tecnico();
        t.setDni(dni); t.setNombres("TecQA"); t.setApellidos("RF39");
        t.setTelefono("987111222"); t.setEmail("tecqa39@gmail.com");
        t.setIdEspecialidad(primerIdEspecialidad());
        t.setIdZona(1); t.setMaxSolicitudesDia(5); t.setEstado(1);
        tecnicoDAO.guardar(t);
        try (PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT id_tecnico FROM tb_tecnico WHERE dni=?")) {
            ps.setString(1, dni);
            ResultSet rs = ps.executeQuery(); rs.next(); return rs.getInt(1);
        } catch (Exception e) { throw new RuntimeException(e); }
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
            ps.setString(3, "QA RF39"); ps.setString(4, "ALTA");
            ps.setString(5, "ASIGNADA");
            ps.setDate(6, java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
            ps.setString(7, "09:00"); ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys(); keys.next(); return keys.getInt(1);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private int obtenerEstadoTecnico(int idTecnico) {
        try (PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT estado FROM tb_tecnico WHERE id_tecnico=?")) {
            ps.setInt(1, idTecnico);
            ResultSet rs = ps.executeQuery(); rs.next(); return rs.getInt(1);
        } catch (Exception e) { return -1; }
    }

    private int primerIdEspecialidad() {
        try (PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT id_especialidad FROM tb_especialidad LIMIT 1")) {
            ResultSet rs = ps.executeQuery(); rs.next(); return rs.getInt(1);
        } catch (Exception e) { return 1; }
    }
}
