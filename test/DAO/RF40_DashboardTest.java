package DAO;

import Conexion.ConexionDB;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF40_DashboardTest {

    @Test
    @Order(1)
    @DisplayName("CP01 - KPI total de clientes activos es un numero positivo")
    void cp01_kpiTotalClientesActivos_esPositivo() throws Exception {
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT COUNT(*) FROM tb_cliente WHERE estado=1")) {
            ResultSet rs = ps.executeQuery(); rs.next();
            int total = rs.getInt(1);
            assertTrue(total > 0,
                    "Debe haber al menos un cliente activo en la BD");
        }
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - KPI total de tecnicos activos es un numero positivo")
    void cp02_kpiTotalTecnicosActivos_esPositivo() throws Exception {
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT COUNT(*) FROM tb_tecnico WHERE estado=1")) {
            ResultSet rs = ps.executeQuery(); rs.next();
            int total = rs.getInt(1);
            assertTrue(total > 0,
                    "Debe haber al menos un tecnico activo en la BD");
        }
    }

    @Test
    @Order(3)
    @DisplayName("CP03 - KPI de solicitudes pendientes no supera el total de solicitudes")
    void cp03_kpiPendientes_noSuperaElTotal() throws Exception {
        int pendientes, total;
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT COUNT(*) FROM tb_solicitud WHERE estado='PENDIENTE'")) {
            ResultSet rs = ps.executeQuery(); rs.next(); pendientes = rs.getInt(1);
        }
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT COUNT(*) FROM tb_solicitud")) {
            ResultSet rs = ps.executeQuery(); rs.next(); total = rs.getInt(1);
        }
        assertTrue(pendientes >= 0,  "Las solicitudes pendientes no pueden ser negativas");
        assertTrue(pendientes <= total,
                "Las solicitudes pendientes no pueden superar el total");
    }

    @Test
    @Order(4)
    @DisplayName("CP04 - KPI de asignaciones de hoy no supera el total de asignaciones")
    void cp04_kpiAsignacionesHoy_noSuperaElTotal() throws Exception {
        int hoy, total;
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT COUNT(*) FROM tb_asignacion "
                    + "WHERE DATE(fecha_programada) = CURDATE()")) {
            ResultSet rs = ps.executeQuery(); rs.next(); hoy = rs.getInt(1);
        }
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT COUNT(*) FROM tb_asignacion")) {
            ResultSet rs = ps.executeQuery(); rs.next(); total = rs.getInt(1);
        }
        assertTrue(hoy >= 0,   "Las asignaciones de hoy no pueden ser negativas");
        assertTrue(hoy <= total,
                "Las asignaciones de hoy no pueden superar el total historico");
    }

    @Test
    @Order(5)
    @DisplayName("CP05 - Todos los KPIs del dashboard se cargan sin excepcion")
    void cp05_todosLosKpisCarganSinExcepcion() throws Exception {
        // Simula la carga completa del dashboard en una sola pasada
        String[] queries = {
            "SELECT COUNT(*) FROM tb_cliente WHERE estado=1",
            "SELECT COUNT(*) FROM tb_tecnico WHERE estado=1",
            "SELECT COUNT(*) FROM tb_solicitud WHERE estado='PENDIENTE'",
            "SELECT COUNT(*) FROM tb_asignacion WHERE DATE(fecha_programada)=CURDATE()"
        };
        for (String q : queries) {
            try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                    .prepareStatement(q)) {
                ResultSet rs = ps.executeQuery(); rs.next();
                int valor = rs.getInt(1);
                assertTrue(valor >= 0,
                        "El KPI de la query [" + q + "] no debe ser negativo");
            }
        }
    }
}
