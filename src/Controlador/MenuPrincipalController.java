package Controlador;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MenuPrincipalController {

    @FXML private Label lblModuloActual;
    @FXML private Label lblFechaHora;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblNombreUsuario;
    @FXML private Label kpiClientes;
    @FXML private Label kpiSolicitudes;
    @FXML private Label kpiTecnicos;
    @FXML private Label kpiAsignaciones;
    @FXML private TableView tblUltimasSolicitudes;
    @FXML private StackPane contentArea;
    @FXML private Button btnDashboard;
    @FXML private Button btnClientes;
    @FXML private Button btnSolicitudes;
    @FXML private Button btnTecnicos;
    @FXML private Button btnAsignacionAuto;
    @FXML private Button btnGestionAsignaciones;
    @FXML private Button btnHistorial;
    @FXML private Button btnReportes;

    // Guardamos el dashboard original al iniciar
    private Node dashboardOriginal;

    @FXML
    public void initialize() {
        String fecha = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy",
                    new Locale("es", "PE")));
        lblFechaHora.setText(capitalize(fecha));

        // Guardar el dashboard que ya viene en el contentArea por defecto
        if (!contentArea.getChildren().isEmpty()) {
            dashboardOriginal = contentArea.getChildren().get(0);
        }
    }

    @FXML public void mostrarDashboard() {
        marcarActivo(btnDashboard);
        lblModuloActual.setText("Panel principal");
        // Restaurar el dashboard original guardado al iniciar
        if (dashboardOriginal != null) {
            contentArea.getChildren().setAll(dashboardOriginal);
        }
    }

    @FXML public void mostrarClientes() {
        marcarActivo(btnClientes);
        lblModuloActual.setText("Gestión de clientes");
        cargarVista("ClienteView.fxml");
    }

    @FXML public void mostrarSolicitudes() {
        marcarActivo(btnSolicitudes);
        lblModuloActual.setText("Gestión de solicitudes");
        cargarVista("SolicitudView.fxml");
    }

    @FXML public void mostrarTecnicos() {
        marcarActivo(btnTecnicos);
        lblModuloActual.setText("Gestión de técnicos");
        cargarVista("TecnicoView.fxml");
    }

    @FXML public void mostrarAsignacionAuto() {
        marcarActivo(btnAsignacionAuto);
        lblModuloActual.setText("Asignación automática");
        cargarVista("AsignacionView.fxml");
    }

    @FXML public void mostrarGestionAsignaciones() {
        marcarActivo(btnGestionAsignaciones);
        lblModuloActual.setText("Gestión de asignaciones");
        cargarVista("AsignacionView.fxml");
    }

    @FXML public void mostrarHistorial() {
        marcarActivo(btnHistorial);
        lblModuloActual.setText("Historial y seguimiento");
        cargarVista("HistorialView.fxml");
    }

    @FXML public void mostrarReportes() {
        marcarActivo(btnReportes);
        lblModuloActual.setText("Reportes");
        cargarVista("ReporteView.fxml");
    }

    @FXML public void cerrarSesion() {
        try {
            URL url = getClass().getResource("../Vista/LoginView.fxml");
            if (url == null) url = getClass().getResource("/Vista/LoginView.fxml");
            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root, 860, 540));
            stage.setMaximized(false);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarVista(String nombreArchivo) {
        try {
            URL url = getClass().getResource("../Vista/" + nombreArchivo);
            if (url == null) url = getClass().getResource("/Vista/" + nombreArchivo);
            if (url == null) {
                mostrarError("No se encontró: " + nombreArchivo);
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Node vista = loader.load();
            contentArea.getChildren().setAll(vista);
        } catch (Exception e) {
            mostrarError("Error al cargar " + nombreArchivo + ":\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarError(String mensaje) {
        Label lbl = new Label(mensaje);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #C62828;");
        lbl.setWrapText(true);
        contentArea.getChildren().setAll(lbl);
    }

    private void marcarActivo(Button activo) {
        Button[] todos = {btnDashboard, btnClientes, btnSolicitudes, btnTecnicos,
                          btnAsignacionAuto, btnGestionAsignaciones,
                          btnHistorial, btnReportes};
        for (Button b : todos) {
            if (b != null) b.getStyleClass().setAll("sidebar-item");
        }
        if (activo != null) activo.getStyleClass().setAll("sidebar-item-active");
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
