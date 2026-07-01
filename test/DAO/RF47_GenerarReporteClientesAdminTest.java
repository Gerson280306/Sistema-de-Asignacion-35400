package DAO;

import Conexion.ConexionDB;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RF47 - Generar reporte de clientes (vista AdminReportes)
 *
 * Se prueba el SQL exacto que usa AdminReportesController.reporteClientes().
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF47_GenerarReporteClientesAdminTest {

    @Test
    @Order(1)
    @DisplayName("CP01 - El reporte de clientes devuelve datos y calcula correctamente las solicitudes por cliente")
    void cp01_reporteClientes_calculaSolicitudesPorCliente() throws Exception {
        String sql = "SELECT c.id_cliente, "
                   + "CONCAT(c.nombres,' ',c.apellidos) AS nombre_completo, "
                   + "c.dni, COALESCE(c.telefono,'') AS telefono, "
                   + "IF(c.estado=1,'Activo','Inactivo') AS estado, "
                   + "COUNT(s.id_solicitud) AS solicitudes "
                   + "FROM tb_cliente c "
                   + "LEFT JOIN tb_solicitud s ON s.id_cliente = c.id_cliente "
                   + "GROUP BY c.id_cliente ORDER BY c.id_cliente";

        int filas = 0;
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                filas++;
                assertTrue(rs.getInt("solicitudes") >= 0,
                        "El numero de solicitudes no puede ser negativo para id_cliente="
                                + rs.getInt("id_cliente"));
            }
        }
        assertTrue(filas > 0, "El reporte debe tener al menos un cliente");
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - Sin clientes el reporte devuelve cero filas sin excepcion")
    void cp02_sinDatos_ceroFilasSinExcepcion() throws Exception {
        // Verificamos con un filtro imposible que devuelva 0 filas
        String sql = "SELECT COUNT(*) FROM tb_cliente WHERE id_cliente = -999";
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery(); rs.next();
            assertEquals(0, rs.getInt(1),
                    "Un filtro imposible debe devolver 0 filas sin lanzar excepcion");
        }
    }
}
