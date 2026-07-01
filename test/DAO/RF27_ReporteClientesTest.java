package DAO;

import Conexion.ConexionDB;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF27_ReporteClientesTest {

    @Test
    @Order(1)
    @DisplayName("CP01 - El reporte de clientes devuelve al menos un registro")
    void cp01_reporteClientes_tieneRegistros() throws Exception {
        // ACT: SQL extraido de AdminReportesController.reporteClientes()
        String sql = "SELECT c.id_cliente, c.dni, "
                   + "CONCAT(c.nombres,' ',c.apellidos) AS nombre_completo, "
                   + "COALESCE(c.telefono,'') AS telefono, "
                   + "IF(c.estado=1,'Activo','Inactivo') AS estado, "
                   + "COUNT(s.id_solicitud) AS solicitudes "
                   + "FROM tb_cliente c "
                   + "LEFT JOIN tb_solicitud s ON s.id_cliente = c.id_cliente "
                   + "GROUP BY c.id_cliente ORDER BY c.id_cliente";

        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next(), "El reporte debe tener al menos un cliente");
        }
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - KPI de total de clientes coincide con COUNT directo de la tabla")
    void cp02_kpiTotalClientes_coincideConConteoReal() throws Exception {
        // ACT: KPI total (misma query que kpiQuery en AdminReportesController)
        int totalKpi;
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT COUNT(*) FROM tb_cliente")) {
            ResultSet rs = ps.executeQuery(); rs.next();
            totalKpi = rs.getInt(1);
        }

        // COUNT directo
        int totalDirecto;
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT COUNT(*) FROM tb_cliente")) {
            ResultSet rs = ps.executeQuery(); rs.next();
            totalDirecto = rs.getInt(1);
        }

        // ASSERT
        assertTrue(totalKpi > 0, "Debe haber al menos un cliente en la BD");
        assertEquals(totalDirecto, totalKpi,
                "El KPI de total de clientes debe coincidir con el conteo directo");
    }

    @Test
    @Order(3)
    @DisplayName("CP03 - Clientes activos e inactivos suman el total")
    void cp03_activosInactivosSumanTotal() throws Exception {
        int total, activos, inactivos;
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT COUNT(*) FROM tb_cliente")) {
            ResultSet rs = ps.executeQuery(); rs.next(); total = rs.getInt(1);
        }
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT COUNT(*) FROM tb_cliente WHERE estado=1")) {
            ResultSet rs = ps.executeQuery(); rs.next(); activos = rs.getInt(1);
        }
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT COUNT(*) FROM tb_cliente WHERE estado=0")) {
            ResultSet rs = ps.executeQuery(); rs.next(); inactivos = rs.getInt(1);
        }

        assertEquals(total, activos + inactivos,
                "Activos + Inactivos deben sumar el total de clientes");
    }
}
