package DAO;

import Conexion.ConexionDB;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF50_ExportarReportePdfAdminTest {

    @Test
    @Order(1)
    @DisplayName("CP01a - Hay datos de clientes para generar el PDF sin que salga vacio")
    void cp01a_hayDatosClientes_pdfNoVacio() throws Exception {
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT COUNT(*) FROM tb_cliente WHERE estado=1")) {
            ResultSet rs = ps.executeQuery(); rs.next();
            assertTrue(rs.getInt(1) > 0,
                    "Debe haber clientes activos para el PDF de clientes");
        }
    }

    @Test
    @Order(2)
    @DisplayName("CP01b - Hay datos de tecnicos para generar el PDF sin que salga vacio")
    void cp01b_hayDatosTecnicos_pdfNoVacio() throws Exception {
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT COUNT(*) FROM tb_tecnico WHERE estado=1")) {
            ResultSet rs = ps.executeQuery(); rs.next();
            assertTrue(rs.getInt(1) > 0,
                    "Debe haber tecnicos activos para el PDF de tecnicos");
        }
    }

    @Test
    @Order(3)
    @DisplayName("CP01c - Hay datos de solicitudes para generar el PDF sin que salga vacio")
    void cp01c_hayDatosSolicitudes_pdfNoVacio() throws Exception {
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT COUNT(*) FROM tb_solicitud")) {
            ResultSet rs = ps.executeQuery(); rs.next();
            assertTrue(rs.getInt(1) > 0,
                    "Debe haber solicitudes para el PDF de solicitudes");
        }
    }

    @Test
    @Order(4)
    @Disabled("CP01 fisico - UI: AdminReportesController.exportarPDF() abre un FileChooser. "
            + "Verificar manualmente: seleccionar tipo de reporte -> clic 'Exportar PDF' "
            + "-> elegir ruta -> confirmar que el archivo se genera con contenido correcto.")
    @DisplayName("CP01 - Exportacion fisica del PDF (evidencia manual)")
    void cp01_exportacionFisica_requiereEvidenciaManual() { }

    @Test
    @Order(5)
    @Disabled("CP02 - Error al generar PDF: el metodo exportarPDF() tiene try-catch que "
            + "atrapa cualquier IOException/DocumentException e imprime el error sin relanzarlo. "
            + "Verificar manualmente desconectando la impresora virtual / disco lleno y "
            + "comprobando que la aplicacion no se cierra y muestra un mensaje al usuario.")
    @DisplayName("CP02 - Error al generar PDF no cierra la aplicacion (evidencia manual)")
    void cp02_errorAlGenerarPdf_requiereEvidenciaManual() { }
}
