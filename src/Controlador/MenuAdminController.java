package Controlador;

import Modelo.Usuario;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.net.URL;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * MenuAdminController.java
 * Panel principal del Administrador.
 * Módulos: Dashboard · Gestión de Cuentas · Reportes
 */
public class MenuAdminController implements ControladorConUsuario {

    @FXML private Label lblModuloActual;
    @FXML private Label lblNombreAdmin;
    @FXML private StackPane contentArea;

    @FXML private Button btnDashboard;
    @FXML private Button btnCuentas;
    @FXML private Button btnReportes;

    private Node dashboardOriginal;
    private Usuario usuarioActual;

    @Override
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
        if (lblNombreAdmin != null && usuario != null) {
            lblNombreAdmin.setText(usuario.getNombreCompleto());
        }
    }

    @FXML
    public void initialize() {
        if (!contentArea.getChildren().isEmpty()) {
            dashboardOriginal = contentArea.getChildren().get(0);
        }
    }

    @FXML public void mostrarDashboard() {
        marcarActivo(btnDashboard);
        lblModuloActual.setText("Panel principal");
        if (dashboardOriginal != null) {
            contentArea.getChildren().setAll(dashboardOriginal);
        }
    }

    @FXML public void mostrarCuentas() {
        marcarActivo(btnCuentas);
        lblModuloActual.setText("Gestión de cuentas");
        cargarVista("AdminCuentasView.fxml");
    }

    @FXML public void mostrarReportes() {
        marcarActivo(btnReportes);
        lblModuloActual.setText("Reportes del sistema");
        cargarVista("AdminReportesView.fxml");
    }

  @FXML
    public void cerrarSesion() {

    Alert alert =
            new Alert(Alert.AlertType.CONFIRMATION);

    alert.setTitle("Cerrar sesión");
    alert.setHeaderText(null);

    alert.setContentText(
            "¿Está seguro que desea cerrar sesión?"
    );

    Optional<ButtonType> result =
            alert.showAndWait();

    if (!result.isPresent()
            || result.get() != ButtonType.OK) {
        return;
    }

    try {

        FXMLLoader loader =
                new FXMLLoader(
                        getClass().getResource(
                                "/Vista/LoginView.fxml"));

        Parent root = loader.load();

        Stage stage =
                (Stage) contentArea
                        .getScene()
                        .getWindow();

        stage.setScene(new Scene(root));

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    // ─── Internos ────────────────────────────────────────────────────────────

    private void cargarVista(String nombreArchivo) {
        try {
            URL url = getClass().getResource("/Vista/" + nombreArchivo);
            if (url == null) {
                mostrarError("No se encontró: " + nombreArchivo);
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Node vista = loader.load();

            // Propagar usuario si el sub-controlador lo necesita
            Object ctrl = loader.getController();
            if (ctrl instanceof ControladorConUsuario && usuarioActual != null) {
                ((ControladorConUsuario) ctrl).setUsuarioActual(usuarioActual);
            }

            contentArea.getChildren().setAll(vista);
        } catch (Exception e) {
            mostrarError("Error al cargar " + nombreArchivo + ": " + e.getMessage());
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
        Button[] todos = {btnDashboard, btnCuentas, btnReportes};
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
