package DAO;

import Conexion.ConexionDB;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RF28 — Reporte de tecnicos
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF28_ReporteTecnicosTest {

    @Test
    @Order(1)
    @DisplayName("CP01 - El reporte de tecnicos devuelve al menos un registro")
    void cp01_reporteTecnicos_tieneRegistros() throws Exception {
        String sql = "SELECT t.id_tecnico, "
                   + "CONCAT(t.nombres,' ',t.apellidos) AS tecnico, "
                   + "COALESCE(e.nombre,'') AS especialidad, "
                   + "IF(t.estado=1,'Activo','Inactivo') AS estado, "
                   + "COUNT(a.id_asignacion) AS asignaciones "
                   + "FROM tb_tecnico t "
                   + "LEFT JOIN tb_especialidad e ON t.id_especialidad=e.id_especialidad "
                   + "LEFT JOIN tb_asignacion a ON a.id_tecnico=t.id_tecnico "
                   + "GROUP BY t.id_tecnico ORDER BY t.nombres";

        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next(), "El reporte de tecnicos debe tener al menos un registro");
        }
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - Todos los tecnicos del reporte tienen especialidad asignada")
    void cp02_tecnicosDelReporteTienenEspecialidad() throws Exception {
        String sql = "SELECT t.id_tecnico, COALESCE(e.nombre,'') AS especialidad "
                   + "FROM tb_tecnico t "
                   + "LEFT JOIN tb_especialidad e ON t.id_especialidad=e.id_especialidad";

        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String especialidad = rs.getString("especialidad");
                assertFalse(especialidad == null || especialidad.isEmpty(),
                        "Tecnico id=" + rs.getInt("id_tecnico")
                                + " no tiene especialidad asignada");
            }
        }
    }

    @Test
    @Order(3)
    @DisplayName("CP03 - KPI de tecnicos activos e inactivos suman el total")
    void cp03_activosInactivosSumanTotal() throws Exception {
        int total, activos, inactivos;
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT COUNT(*) FROM tb_tecnico")) {
            ResultSet rs = ps.executeQuery(); rs.next(); total = rs.getInt(1);
        }
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT COUNT(*) FROM tb_tecnico WHERE estado=1")) {
            ResultSet rs = ps.executeQuery(); rs.next(); activos = rs.getInt(1);
        }
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT COUNT(*) FROM tb_tecnico WHERE estado=0")) {
            ResultSet rs = ps.executeQuery(); rs.next(); inactivos = rs.getInt(1);
        }

        assertEquals(total, activos + inactivos,
                "Activos + Inactivos deben sumar el total de tecnicos");
    }
}
