package DAO;

import Conexion.ConexionDB;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RF49 - Generar reporte de solicitudes (vista AdminReportes)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF49_GenerarReporteSolicitudesAdminTest {

    private static final String DESDE = "2000-01-01 00:00:00";
    private static final String HASTA  = "2099-12-31 23:59:59";

    @Test
    @Order(1)
    @DisplayName("CP01 - El reporte de solicitudes muestra cliente y tipo de servicio")
    void cp01_reporteSolicitudes_muestraClienteYTipo() throws Exception {
        String sql = "SELECT s.id_solicitud, s.estado, s.prioridad, "
                   + "CONCAT(c.nombres,' ',c.apellidos) AS cliente, "
                   + "ts.nombre AS tipo_servicio "
                   + "FROM tb_solicitud s "
                   + "JOIN tb_cliente c ON s.id_cliente=c.id_cliente "
                   + "JOIN tb_tipo_servicio ts ON s.id_tipo_servicio=ts.id_tipo_servicio "
                   + "WHERE s.fecha_registro BETWEEN ? AND ? "
                   + "ORDER BY s.id_solicitud DESC";

        int filas = 0;
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setString(1, DESDE); ps.setString(2, HASTA);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                filas++;
                assertNotNull(rs.getString("cliente"),
                        "El campo cliente no debe ser null");
                assertNotNull(rs.getString("tipo_servicio"),
                        "El campo tipo_servicio no debe ser null");
            }
        }
        assertTrue(filas > 0, "El reporte debe tener al menos una solicitud");
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - Rango sin solicitudes devuelve cero filas sin excepcion")
    void cp02_rangoSinDatos_ceroFilas() throws Exception {
        String sql = "SELECT COUNT(*) FROM tb_solicitud "
                   + "WHERE fecha_registro BETWEEN ? AND ?";
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setString(1, "1900-01-01 00:00:00");
            ps.setString(2, "1900-01-31 23:59:59");
            ResultSet rs = ps.executeQuery(); rs.next();
            assertEquals(0, rs.getInt(1),
                    "Un rango historico sin datos debe devolver 0 sin excepcion");
        }
    }
}
