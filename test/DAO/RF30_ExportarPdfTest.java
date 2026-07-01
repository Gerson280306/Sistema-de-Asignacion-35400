package DAO;

import Conexion.ConexionDB;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF30_ExportarPdfTest {

    @Test
    @Order(1)
    @DisplayName("CP01 - Hay datos en la BD para que el PDF de clientes no salga vacio")
    void cp01_hayDatosParaPdfClientes() throws Exception {
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT COUNT(*) FROM tb_cliente")) {
            ResultSet rs = ps.executeQuery(); rs.next();
            assertTrue(rs.getInt(1) > 0,
                    "Debe haber al menos un cliente para que el PDF de clientes tenga contenido");
        }
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - Hay datos en la BD para que el PDF de tecnicos no salga vacio")
    void cp02_hayDatosParaPdfTecnicos() throws Exception {
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT COUNT(*) FROM tb_tecnico")) {
            ResultSet rs = ps.executeQuery(); rs.next();
            assertTrue(rs.getInt(1) > 0,
                    "Debe haber al menos un tecnico para que el PDF de tecnicos tenga contenido");
        }
    }

    @Test
    @Order(3)
    @DisplayName("CP03 - Hay datos en la BD para que el PDF de solicitudes no salga vacio")
    void cp03_hayDatosParaPdfSolicitudes() throws Exception {
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT COUNT(*) FROM tb_solicitud")) {
            ResultSet rs = ps.executeQuery(); rs.next();
            assertTrue(rs.getInt(1) > 0,
                    "Debe haber al menos una solicitud para que el PDF de solicitudes tenga contenido");
        }
    }

    @Test
    @Order(4)
    @Disabled("UI-only: AdminReportesController.exportarPDF() abre un FileChooser de JavaFX "
            + "y escribe el PDF en disco. Verificar manualmente: seleccionar tipo de reporte "
            + "-> clic en 'Exportar PDF' -> elegir ruta -> confirmar que el archivo se genera "
            + "y tiene el contenido correcto.")
    @DisplayName("CP04 - Exportacion fisica del PDF (evidencia manual)")
    void cp04_exportacionFisicaPdf_requiereEvidenciaManual() { }
}
