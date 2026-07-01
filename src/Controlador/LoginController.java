package Controlador;

import Util.Log;

import DAO.UsuarioDAO;
import Modelo.ResultadoLogin;
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

        if (camposVacios(username, password)) {
            mostrarError("Por favor ingresa usuario y contraseña.");
            return;
        }

        ResultadoLogin resultado = usuarioDAO.autenticarDetallado(username, password);

        switch (resultado.getEstado()) {
            case CUENTA_BLOQUEADA:
                mostrarError("Cuenta bloqueada por intentos fallidos. Un administrador debe reactivarla para que puedas volver a ingresar.");
                return;
            case CREDENCIALES_INVALIDAS:
                if (resultado.getIntentosRestantes() > 0) {
                    mostrarError("Usuario o contraseña incorrectos. Advertencia: te queda"
                            + (resultado.getIntentosRestantes() == 1 ? "" : "n") + " "
                            + resultado.getIntentosRestantes()
                            + (resultado.getIntentosRestantes() == 1 ? " intento" : " intentos")
                            + " antes de que la cuenta se bloquee.");
                } else {
                    mostrarError("Usuario o contraseña incorrectos.");
                }
                return;
            case OK:
            default:
                break;
        }

        Usuario usuario = resultado.getUsuario();

        // Redirigir según rol
        if ("ADMIN".equals(usuario.getRol())) {
            abrirVista("/Vista/MenuAdminView.fxml", "Administración — Sistema de Asignación", usuario);
        } else {
            abrirVista("/Vista/MenuPrincipalView.fxml", "Sistema de Asignación", usuario);
        }
    }

    /**
     * Valida si usuario y/o contraseña están vacíos (RF01-CP02).
     * Extraído como método puro para poder probarse sin instanciar componentes de JavaFX.
     */
    public static boolean camposVacios(String username, String password) {
        return username == null || username.isEmpty() || password == null || password.isEmpty();
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
            Log.error(e);
        }
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
    }
}
