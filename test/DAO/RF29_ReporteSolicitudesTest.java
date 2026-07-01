package DAO;

import Conexion.ConexionDB;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RF29 — Reporte de solicitudes
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF29_ReporteSolicitudesTest {

    @Test
    @Order(1)
    @DisplayName("CP01 - El reporte de solicitudes devuelve registros del rango indicado")
    void cp01_reporteSolicitudes_devuelveRegistros() throws Exception {
        // Rango amplio que cubre todos los datos semilla
        String desde = "2000-01-01 00:00:00";
        String hasta = "2099-12-31 23:59:59";

        String sql = "SELECT s.id_solicitud, s.estado, s.prioridad, "
                   + "CONCAT(c.nombres,' ',c.apellidos) AS cliente, "
                   + "ts.nombre AS tipo_servicio "
                   + "FROM tb_solicitud s "
                   + "JOIN tb_cliente c ON s.id_cliente=c.id_cliente "
                   + "JOIN tb_tipo_servicio ts ON s.id_tipo_servicio=ts.id_tipo_servicio "
                   + "WHERE s.fecha_registro BETWEEN ? AND ? "
                   + "ORDER BY s.id_solicitud DESC";

        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setString(1, desde); ps.setString(2, hasta);
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next(),
                    "El reporte con rango amplio debe devolver al menos una solicitud");
        }
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - KPI de pendientes, completadas y canceladas no superan el total")
    void cp02_kpiParciales_noSuperanTotal() throws Exception {
        String desde = "2000-01-01 00:00:00";
        String hasta = "2099-12-31 23:59:59";

        int total      = contarConFiltro(desde, hasta, null);
        int pendientes = contarConFiltro(desde, hasta, "PENDIENTE");
        int completadas= contarConFiltro(desde, hasta, "COMPLETADA");
        int canceladas = contarConFiltro(desde, hasta, "CANCELADA");

        assertTrue(pendientes  <= total, "Pendientes no pueden superar el total");
        assertTrue(completadas <= total, "Completadas no pueden superar el total");
        assertTrue(canceladas  <= total, "Canceladas no pueden superar el total");
        assertTrue(pendientes + completadas + canceladas <= total,
                "La suma de los tres grupos no puede superar el total");
    }

    @Test
    @Order(3)
    @DisplayName("CP03 - Rango sin solicitudes devuelve cero registros sin excepcion")
    void cp03_rangoSinSolicitudes_devuelveCero() throws Exception {
        String desde = "1900-01-01 00:00:00";
        String hasta = "1900-01-31 23:59:59";

        int count = contarConFiltro(desde, hasta, null);
        assertEquals(0, count,
                "Un rango historico sin datos debe devolver 0 sin lanzar excepcion");
    }

    private int contarConFiltro(String desde, String hasta, String estado) throws Exception {
        String sql = estado == null
            ? "SELECT COUNT(*) FROM tb_solicitud WHERE fecha_registro BETWEEN ? AND ?"
            : "SELECT COUNT(*) FROM tb_solicitud WHERE estado=? AND fecha_registro BETWEEN ? AND ?";
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion().prepareStatement(sql)) {
            if (estado == null) {
                ps.setString(1, desde); ps.setString(2, hasta);
            } else {
                ps.setString(1, estado); ps.setString(2, desde); ps.setString(3, hasta);
            }
            ResultSet rs = ps.executeQuery(); rs.next(); return rs.getInt(1);
        }
    }
}
