package Controlador;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ReporteController {

    @FXML private ComboBox cmbTipoReporte;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private ComboBox cmbFiltroTecnico;
    @FXML private TableView tblReporte;
    @FXML private Label lblTituloTabla;
    @FXML private Label lblContador;
    @FXML private Label kpiTotalSolicitudes;
    @FXML private Label kpiAtendidas;
    @FXML private Label kpiPendientes;
    @FXML private Label kpiTiempoPromedio;
    @FXML private Label kpiEficiencia;

    @FXML
    public void initialize() {
        cmbTipoReporte.getItems().addAll(
            "Rendimiento por técnico",
            "Solicitudes por período",
            "Clientes atendidos"
        );
        dpDesde.setValue(java.time.LocalDate.now().withDayOfMonth(1));
        dpHasta.setValue(java.time.LocalDate.now());
        // TODO: cmbFiltroTecnico.setItems(TecnicoDAO.listarTodos());
    }

    @FXML public void cambiarTipoReporte() {
        String tipo = (String) cmbTipoReporte.getValue();
        if (tipo == null) return;
        lblTituloTabla.setText("Resultados: " + tipo);
    }

    @FXML public void generarReporte() {
        if (cmbTipoReporte.getValue() == null) return;
        // TODO: ReporteDAO.generar(tipo, desde, hasta, tecnico)
        kpiTotalSolicitudes.setText("0");
        kpiAtendidas.setText("0");
        kpiPendientes.setText("0");
        kpiTiempoPromedio.setText("0 h");
        kpiEficiencia.setText("0%");
        lblContador.setText("0 registros");
    }

    @FXML public void limpiar() {
        cmbTipoReporte.setValue(null);
        cmbFiltroTecnico.setValue(null);
        dpDesde.setValue(java.time.LocalDate.now().withDayOfMonth(1));
        dpHasta.setValue(java.time.LocalDate.now());
        kpiTotalSolicitudes.setText("—");
        kpiAtendidas.setText("—");
        kpiPendientes.setText("—");
        kpiTiempoPromedio.setText("—");
        kpiEficiencia.setText("—");
    }

    @FXML public void exportarExcel() {
        // TODO: exportar con Apache POI
    }

    @FXML public void exportarPdf() {
        // TODO: exportar con iText o JasperReports
    }
}
