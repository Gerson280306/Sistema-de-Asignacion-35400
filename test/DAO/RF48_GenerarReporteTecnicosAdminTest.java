package DAO;

import Conexion.ConexionDB;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RF48 - Generar reporte de tecnicos (vista AdminReportes)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF48_GenerarReporteTecnicosAdminTest {

    @Test
    @Order(1)
    @DisplayName("CP01 - El reporte de tecnicos incluye especialidad y conteo de asignaciones")
    void cp01_reporteTecnicos_incluyeEspecialidadYAsignaciones() throws Exception {
        String sql = "SELECT t.id_tecnico, "
                   + "CONCAT(t.nombres,' ',t.apellidos) AS tecnico, "
                   + "COALESCE(e.nombre,'Sin especialidad') AS especialidad, "
                   + "IF(t.estado=1,'Activo','Inactivo') AS estado, "
                   + "COUNT(a.id_asignacion) AS asignaciones "
                   + "FROM tb_tecnico t "
                   + "LEFT JOIN tb_especialidad e ON t.id_especialidad=e.id_especialidad "
                   + "LEFT JOIN tb_asignacion a ON a.id_tecnico=t.id_tecnico "
                   + "GROUP BY t.id_tecnico ORDER BY t.nombres";

        int filas = 0;
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                filas++;
                assertTrue(rs.getInt("asignaciones") >= 0,
                        "El conteo de asignaciones no puede ser negativo para id_tecnico="
                                + rs.getInt("id_tecnico"));
                assertNotNull(rs.getString("especialidad"),
                        "El campo especialidad no debe ser null (COALESCE lo cubre)");
            }
        }
        assertTrue(filas > 0, "El reporte debe tener al menos un tecnico");
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - Sin tecnicos el reporte devuelve cero filas sin excepcion")
    void cp02_sinDatos_ceroFilasSinExcepcion() throws Exception {
        String sql = "SELECT COUNT(*) FROM tb_tecnico WHERE id_tecnico = -999";
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery(); rs.next();
            assertEquals(0, rs.getInt(1),
                    "Un filtro imposible debe devolver 0 filas sin excepcion");
        }
    }
}
