package Controlador;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class HistorialController {

    @FXML private TextField txtBuscar;
    @FXML private ComboBox cmbFiltroTecnico;
    @FXML private ComboBox cmbFiltroCliente;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private TableView tblHistorial;
    @FXML private Label lblTotalRegistros;
    @FXML private Label lblSinSeleccion;
    @FXML private VBox panelDetalle;
    @FXML private Label detCliente;
    @FXML private Label detTecnico;
    @FXML private Label detTipo;
    @FXML private Label detEstado;
    @FXML private Label detFechaAsig;
    @FXML private Label detFechaAten;
    @FXML private Label detDescripcion;
    @FXML private Label detNotas;

    @FXML
    public void initialize() {
        // TODO: cmbFiltroTecnico.setItems(TecnicoDAO.listarTodos());
        // TODO: cmbFiltroCliente.setItems(ClienteDAO.listarTodos());
        // TODO: tblHistorial.setItems(HistorialDAO.listarTodos());
    }

    @FXML public void buscar() {
        // TODO: filtrar por txtBuscar
    }

    @FXML public void filtrar() {
        // TODO: filtrar por técnico, cliente, fechas
    }

    @FXML public void limpiarFiltros() {
        txtBuscar.clear();
        cmbFiltroTecnico.setValue(null);
        cmbFiltroCliente.setValue(null);
        dpDesde.setValue(null);
        dpHasta.setValue(null);
    }

    @FXML public void verDetalle() {
        Object sel = tblHistorial.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        lblSinSeleccion.setVisible(false);
        lblSinSeleccion.setManaged(false);
        panelDetalle.setVisible(true);
        panelDetalle.setManaged(true);
        // TODO: poblar labels con datos del registro seleccionado
        detCliente.setText("Cliente del registro");
        detTecnico.setText("Técnico del registro");
        detTipo.setText("Tipo de servicio");
        detEstado.setText("Completada");
        detFechaAsig.setText("01/05/2026");
        detFechaAten.setText("02/05/2026");
        detDescripcion.setText("Descripción del servicio realizado.");
        detNotas.setText("Sin notas de cierre.");
    }

    @FXML public void exportarHistorial() {
        // TODO: exportar tblHistorial a Excel con Apache POI
    }
}
