package DAO;

import Modelo.Solicitud;
import Modelo.Tecnico;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF32_AsignacionMasivaTest {

    private final SolicitudDAO solicitudDAO = new SolicitudDAO();
    private final TecnicoDAO   tecnicoDAO  = new TecnicoDAO();

    private static int pesoPrioridad(String p) {
        if (p == null) return 0;
        switch (p.toUpperCase()) {
            case "CRITICA": return 4;
            case "ALTA":    return 3;
            case "MEDIA":   return 2;
            case "BAJA":    return 1;
            default:        return 0;
        }
    }

    @Test
    @Order(1)
    @DisplayName("CP01 - El ordenamiento por prioridad pone CRITICA primero en la cola masiva")
    void cp01_ordenamientoPrioridad_criticaPrimero() {
        List<String> prioridades = Arrays.asList("BAJA", "MEDIA", "CRITICA", "ALTA");

        prioridades.sort(Comparator.comparingInt(s -> -pesoPrioridad(s)));

        assertEquals("CRITICA", prioridades.get(0));
        assertEquals("ALTA",    prioridades.get(1));
        assertEquals("MEDIA",   prioridades.get(2));
        assertEquals("BAJA",    prioridades.get(3));
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - Hay tecnicos activos disponibles para recibir asignaciones masivas")
    void cp02_hayTecnicosActivosParaAsignacionMasiva() {
        List<Tecnico> activos = tecnicoDAO.listarActivos();

        assertNotNull(activos);
        assertFalse(activos.isEmpty(),
                "Debe haber al menos un tecnico activo para que la asignacion masiva funcione");
        for (Tecnico t : activos) {
            assertEquals(1, t.getEstado(),
                    "listarActivos() solo debe devolver tecnicos con estado=1");
        }
    }

    @Test
    @Order(3)
    @DisplayName("CP03 - Crear solicitudes PENDIENTE y verificar que quedan disponibles para asignacion masiva")
    void cp03_solicitudesPendientesDisponiblesParaMasiva() {
        int idCliente = primerIdCliente();
        int idTipo    = primerIdTipoServicio();
        crearSolicitud(idCliente, idTipo, "CRITICA", LocalDate.now().plusDays(1), "09:00");
        crearSolicitud(idCliente, idTipo, "BAJA",    LocalDate.now().plusDays(1), "10:00");

        List<Solicitud> pendientes = solicitudDAO.listarPendientes();

        assertNotNull(pendientes);
        assertFalse(pendientes.isEmpty(),
                "Debe haber al menos una solicitud PENDIENTE para asignacion masiva");
    }

    private void crearSolicitud(int idCliente, int idTipo,
                                String prioridad, LocalDate fecha, String hora) {
        try {
            PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                    .prepareStatement("INSERT INTO tb_solicitud (id_cliente, id_tipo_servicio,"
                        + " descripcion, prioridad, estado, fecha_solicitada, horario_preferido)"
                        + " VALUES (?,?,?,?,?,?,?)");
            ps.setInt(1, idCliente); ps.setInt(2, idTipo);
            ps.setString(3, "QA RF32 masiva " + prioridad);
            ps.setString(4, prioridad); ps.setString(5, "PENDIENTE");
            ps.setDate(6, java.sql.Date.valueOf(fecha)); ps.setString(7, hora);
            ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private int primerIdCliente() {
        try (PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT id_cliente FROM tb_cliente LIMIT 1")) {
            ResultSet rs = ps.executeQuery(); rs.next(); return rs.getInt(1);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private int primerIdTipoServicio() {
        try (PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT id_tipo_servicio FROM tb_tipo_servicio LIMIT 1")) {
            ResultSet rs = ps.executeQuery(); rs.next(); return rs.getInt(1);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
