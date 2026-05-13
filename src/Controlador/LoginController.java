package Controlador;

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
 * Ubicación: src/Controlador/LoginController.java
 * Cubre: CUS-8 → R48 (Validar datos), R49 (Acceso gerente)
 */
public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    @FXML
    public void handleLogin() {
        String usuario = txtUsuario.getText().trim();
        String password = txtPassword.getText().trim();

        // Validación básica de campos vacíos (R48)
        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor ingresa usuario y contraseña.");
            return;
        }

        // TODO: Reemplazar con DAO.UsuarioDAO.autenticar(usuario, password)
        // boolean acceso = UsuarioDAO.autenticar(usuario, password);
        boolean acceso = usuario.equals("admin") && password.equals("1234"); // provisional

        if (acceso) {
            abrirMenuPrincipal();
        } else {
            mostrarError("Usuario o contraseña incorrectos.");
        }
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
    }

    private void abrirMenuPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/Vista/MenuPrincipalView.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Sistema de Asignación");
            stage.setMaximized(true);
        } catch (Exception e) {
            mostrarError("Error al cargar el sistema: " + e.getMessage());
        }
    }
}
