package Controlador;

import DAO.UsuarioDAO;
import Modelo.Usuario;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * LoginController.java
 * Autenticación real desde tb_usuario (SHA-256).
 * Redirige según rol:
 *   ADMIN      → MenuAdminView.fxml
 *   SUPERVISOR / OPERADOR → MenuPrincipalView.fxml
 */
public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    public void handleLogin() {
        String username = txtUsuario.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor ingresa usuario y contraseña.");
            return;
        }

        Usuario usuario = usuarioDAO.autenticar(username, password);

        if (usuario == null) {
            mostrarError("Usuario o contraseña incorrectos.");
            return;
        }

        // Redirigir según rol
        if ("ADMIN".equals(usuario.getRol())) {
            abrirVista("/Vista/MenuAdminView.fxml", "Administración — Sistema de Asignación", usuario);
        } else {
            abrirVista("/Vista/MenuPrincipalView.fxml", "Sistema de Asignación", usuario);
        }
    }

    private void abrirVista(String fxmlPath, String titulo, Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Pasar el usuario al controlador destino si implementa la interfaz
            Object ctrl = loader.getController();
            if (ctrl instanceof ControladorConUsuario) {
                ((ControladorConUsuario) ctrl).setUsuarioActual(usuario);
            }

            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
            stage.setMaximized(true);
        } catch (Exception e) {
            mostrarError("Error al cargar el sistema: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
    }
}
