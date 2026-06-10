package Controlador;

import DAO.UsuarioDAO;
import Modelo.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

/**
 * AdminCuentasController.java
 * Gestión de cuentas de gestores (SUPERVISOR / OPERADOR).
 * Formulario siempre visible a la derecha (estilo ClienteController).
 * Rol siempre SUPERVISOR (campo oculto).
 */
public class AdminCuentasController implements ControladorConUsuario {

    // ── Tabla ─────────────────────────────────────────────────────────────────
    @FXML private TableView<Usuario>             tblCuentas;
    @FXML private TableColumn<Usuario, Integer>  colId;
    @FXML private TableColumn<Usuario, String>   colUsername;
    @FXML private TableColumn<Usuario, String>   colNombre;
    @FXML private TableColumn<Usuario, String>   colEmail;
    @FXML private TableColumn<Usuario, String>   colEstado;
    @FXML private TableColumn<Usuario, String>   colUltimoAcceso;
    @FXML private TableColumn<Usuario, String>   colFechaCreacion;

    // ── Búsqueda ──────────────────────────────────────────────────────────────
    @FXML private TextField txtBuscar;
    @FXML private Label     lblContador;

    // ── Formulario ────────────────────────────────────────────────────────────
    @FXML private Label         lblTituloForm;
    @FXML private TextField     txtFUsername;
    @FXML private TextField     txtFNombre;
    @FXML private TextField     txtFEmail;
    @FXML private PasswordField txtFPassword;
    @FXML private PasswordField txtFPasswordConf;
    @FXML private Label         lblFormError;
    @FXML private Button        btnToggleEstado;

    // ── Estado interno ────────────────────────────────────────────────────────
    private final UsuarioDAO dao = new UsuarioDAO();
    private ObservableList<Usuario> todos = FXCollections.observableArrayList();
    private Usuario seleccionado = null;

    @FXML
    public void initialize() {
        configurarTabla();
        cargarDatos();
        txtBuscar.textProperty().addListener((obs, old, val) -> filtrar(val));
    }

    @Override
    public void setUsuarioActual(Usuario usuario) {}

    // ─── Selección en tabla ───────────────────────────────────────────────────

    @FXML
    public void seleccionarCuenta(MouseEvent event) {
        Usuario u = tblCuentas.getSelectionModel().getSelectedItem();
        if (u == null) return;
        seleccionado = u;

        lblTituloForm.setText("Editar cuenta");
        txtFUsername.setText(u.getUsername());
        txtFNombre.setText(u.getNombreCompleto());
        txtFEmail.setText(u.getEmail());
        txtFPassword.clear();
        txtFPasswordConf.clear();
        lblFormError.setText("");

        // Botón activar/desactivar
        btnToggleEstado.setDisable(false);
        if (u.getEstado() == 1) {
            btnToggleEstado.setText("Desactivar");
            btnToggleEstado.getStyleClass().setAll("btn-danger");
        } else {
            btnToggleEstado.setText("Activar");
            btnToggleEstado.getStyleClass().setAll("btn-success");
        }
    }

    // ─── Botón "Nueva cuenta" ─────────────────────────────────────────────────

    @FXML
    public void nuevaCuenta() {
        limpiarFormulario();
    }

    // ─── Guardar (crea o edita según seleccionado) ────────────────────────────

    @FXML
    public void guardarCuenta() {
        lblFormError.setText("");

        String username = txtFUsername.getText().trim();
        String nombre   = txtFNombre.getText().trim();
        String email    = txtFEmail.getText().trim();
        String pass     = txtFPassword.getText();
        String passConf = txtFPasswordConf.getText();

        if (username.isEmpty() || nombre.isEmpty()) {
            lblFormError.setText("Usuario y nombre son obligatorios.");
            return;
        }

        int excludeId = seleccionado != null ? seleccionado.getIdUsuario() : -1;
        if (dao.usernameExiste(username, excludeId)) {
            lblFormError.setText("El nombre de usuario ya existe.");
            return;
        }

        Usuario u = new Usuario();
        u.setUsername(username);
        u.setNombreCompleto(nombre);
        u.setEmail(email.isEmpty() ? null : email);
        u.setRol("SUPERVISOR");
        u.setEstado(1);

        if (seleccionado == null) {
            // ── Crear nueva cuenta ──
            if (pass.length() < 6) {
                lblFormError.setText("La contraseña debe tener al menos 6 caracteres.");
                return;
            }
            if (!pass.equals(passConf)) {
                lblFormError.setText("Las contraseñas no coinciden.");
                return;
            }
            if (dao.insertar(u, pass)) {
                alerta("Cuenta creada correctamente.", Alert.AlertType.INFORMATION);
                limpiarFormulario();
                cargarDatos();
            } else {
                lblFormError.setText("Error al crear la cuenta.");
            }
        } else {
            // ── Editar cuenta existente ──
            u.setIdUsuario(seleccionado.getIdUsuario());
            u.setEstado(seleccionado.getEstado());

            boolean ok = dao.actualizar(u);

            // Cambiar contraseña solo si se escribió algo
            if (ok && !pass.isEmpty()) {
                if (pass.length() < 6) {
                    lblFormError.setText("La contraseña debe tener al menos 6 caracteres.");
                    return;
                }
                if (!pass.equals(passConf)) {
                    lblFormError.setText("Las contraseñas no coinciden.");
                    return;
                }
                ok = dao.cambiarPassword(seleccionado.getIdUsuario(), pass);
            }

            if (ok) {
                alerta("Cuenta actualizada.", Alert.AlertType.INFORMATION);
                limpiarFormulario();
                cargarDatos();
            } else {
                lblFormError.setText("Error al actualizar la cuenta.");
            }
        }
    }

    // ─── Activar / Desactivar ─────────────────────────────────────────────────

    @FXML
    public void toggleEstado() {
        if (seleccionado == null) return;
        int nuevoEstado = seleccionado.getEstado() == 1 ? 0 : 1;
        String accion   = nuevoEstado == 1 ? "activar" : "desactivar";

        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Deseas " + accion + " la cuenta de " + seleccionado.getUsername() + "?",
            ButtonType.YES, ButtonType.NO);
        conf.setHeaderText(null);
        conf.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                if (dao.toggleEstado(seleccionado.getIdUsuario(), nuevoEstado)) {
                    limpiarFormulario();
                    cargarDatos();
                } else {
                    alerta("No se pudo cambiar el estado.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    // ─── Limpiar / cancelar ───────────────────────────────────────────────────

    @FXML
    public void limpiarFormulario() {
        seleccionado = null;
        lblTituloForm.setText("Nueva cuenta");
        txtFUsername.clear();
        txtFNombre.clear();
        txtFEmail.clear();
        txtFPassword.clear();
        txtFPasswordConf.clear();
        lblFormError.setText("");
        btnToggleEstado.setDisable(true);
        btnToggleEstado.setText("Desactivar");
        btnToggleEstado.getStyleClass().setAll("btn-danger");
        tblCuentas.getSelectionModel().clearSelection();
    }

    // ─── Internos ─────────────────────────────────────────────────────────────

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoTexto"));
        colUltimoAcceso.setCellValueFactory(new PropertyValueFactory<>("ultimoAccesoStr"));
        colFechaCreacion.setCellValueFactory(new PropertyValueFactory<>("fechaCreacionStr"));
        tblCuentas.setItems(todos);
    }

    private void cargarDatos() {
        todos.setAll(dao.listarGestores());
        lblContador.setText(todos.size() + " cuentas registradas");
    }

    private void filtrar(String texto) {
        if (texto == null || texto.isBlank()) {
            tblCuentas.setItems(todos);
            lblContador.setText(todos.size() + " cuentas registradas");
            return;
        }
        String lower = texto.toLowerCase();
        ObservableList<Usuario> filtrados = FXCollections.observableArrayList();
        for (Usuario u : todos) {
            if (u.getUsername().toLowerCase().contains(lower)
                    || u.getNombreCompleto().toLowerCase().contains(lower)) {
                filtrados.add(u);
            }
        }
        tblCuentas.setItems(filtrados);
        lblContador.setText(filtrados.size() + " cuentas encontradas");
    }

    private void alerta(String msg, Alert.AlertType tipo) {
        Alert a = new Alert(tipo, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
