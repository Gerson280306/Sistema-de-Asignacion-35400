package Controlador;

import DAO.ClienteDAO;
import Modelo.Cliente;
import Modelo.Zona;
import Conexion.ConexionDB;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteController {

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TableView<Cliente> tblClientes;
    @FXML private TableColumn<Cliente,Integer> colId;
    @FXML private TableColumn<Cliente,String> colDni, colNombre, colTelefono,
                                               colEmail, colDireccion, colEstado;
    @FXML private Label lblContador, lblTituloForm, lblMensaje;

    @FXML private TextField txtDni, txtNombres, txtApellidos, txtTelefono, txtEmail, txtDireccion;
    @FXML private ComboBox<Zona> cmbDistrito;
    @FXML private TextArea txtReferencia;
    @FXML private Button btnGuardar, btnEliminar;

    @FXML private StackPane toastPane;

    private final ClienteDAO dao = new ClienteDAO();
    private Cliente clienteSeleccionado = null;
    private ObservableList<Cliente> todosLosClientes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        if (colEstado != null)
            colEstado.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getEstado() == 1 ? "Activo" : "Inactivo"));

        cmbFiltroEstado.setItems(FXCollections.observableArrayList("Todos","Activo","Inactivo"));
        cmbFiltroEstado.setValue("Todos");

        btnEliminar.setDisable(true);
        btnGuardar.setDisable(true);

        // DNI: solo dígitos
        txtDni.textProperty().addListener((o, v, n) -> {
            if (!n.matches("\\d*")) txtDni.setText(n.replaceAll("[^\\d]",""));
            validarEnTiempoReal();
        });
        // Teléfono: solo dígitos, exactamente 9
        txtTelefono.textProperty().addListener((o, v, n) -> {
            if (!n.matches("\\d*")) txtTelefono.setText(n.replaceAll("[^\\d]",""));
            validarEnTiempoReal();
        });
        // Nombres y apellidos: solo letras y espacios
        txtNombres.textProperty().addListener((o, v, n) -> {
            if (!n.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]*"))
                txtNombres.setText(v);
            validarEnTiempoReal();
        });
        txtApellidos.textProperty().addListener((o, v, n) -> {
            if (!n.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]*"))
                txtApellidos.setText(v);
            validarEnTiempoReal();
        });
        txtDireccion.textProperty().addListener((o, v, n) -> validarEnTiempoReal());
        txtEmail.textProperty().addListener((o, v, n) -> validarEnTiempoReal());

        cargarDistritos();
        cargarTabla();
    }

    private void validarEnTiempoReal() {
        btnGuardar.setDisable(
            txtDni.getText().trim().isEmpty()
         || txtNombres.getText().trim().isEmpty()
         || txtApellidos.getText().trim().isEmpty()
        );
    }

    private void cargarDistritos() {
        List<Zona> lista = new ArrayList<>();
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT id_zona, nombre FROM tb_zona WHERE estado=1 ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(new Zona(rs.getInt(1), rs.getString(2)));
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        cmbDistrito.setItems(FXCollections.observableArrayList(lista));
    }

    private void cargarTabla() {
        todosLosClientes = FXCollections.observableArrayList(dao.listarTodos());
        aplicarFiltros();
    }

    @FXML public void nuevoCliente()   { limpiarFormulario(); }
    @FXML public void buscarCliente()  { aplicarFiltros(); }
    @FXML public void filtrarEstado()  { aplicarFiltros(); }

    private void aplicarFiltros() {
        String txt    = txtBuscar.getText().trim().toLowerCase();
        String estado = cmbFiltroEstado.getValue();

        ObservableList<Cliente> filtrados = FXCollections.observableArrayList();
        for (Cliente c : todosLosClientes) {
            boolean okTexto = txt.isEmpty()
                || c.getNombreCompleto().toLowerCase().contains(txt)
                || (c.getDni()       != null && c.getDni().contains(txt))
                || (c.getTelefono()  != null && c.getTelefono().contains(txt))
                || (c.getEmail()     != null && c.getEmail().toLowerCase().contains(txt))
                || (c.getDireccion() != null && c.getDireccion().toLowerCase().contains(txt));
            boolean okEstado = estado == null || "Todos".equals(estado)
                || ("Activo".equals(estado)   && c.getEstado() == 1)
                || ("Inactivo".equals(estado) && c.getEstado() == 0);
            if (okTexto && okEstado) filtrados.add(c);
        }
        tblClientes.setItems(filtrados);
        lblContador.setText(filtrados.size() + " clientes");
    }

    @FXML public void seleccionarCliente() {
        Cliente sel = tblClientes.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        clienteSeleccionado = sel;
        lblTituloForm.setText("Editar cliente");
        actualizarBoton(sel.getEstado() == 1);
        btnEliminar.setDisable(false);

        txtDni.setText(nvl(sel.getDni()));
        txtNombres.setText(nvl(sel.getNombres()));
        txtApellidos.setText(nvl(sel.getApellidos()));
        txtTelefono.setText(nvl(sel.getTelefono()));
        txtEmail.setText(nvl(sel.getEmail()));
        txtDireccion.setText(nvl(sel.getDireccion()));
        txtReferencia.setText(nvl(sel.getReferencia()));

        cmbDistrito.getItems().stream()
            .filter(z -> z.getIdZona() == sel.getIdZona())
            .findFirst().ifPresent(cmbDistrito::setValue);
    }

    @FXML public void guardarCliente() {
        String error = validarCompleto();
        if (error != null) { mostrarMensaje(error, false); return; }

        Cliente c = new Cliente();
        c.setDni(txtDni.getText().trim());
        c.setNombres(txtNombres.getText().trim());
        c.setApellidos(txtApellidos.getText().trim());
        c.setTelefono(txtTelefono.getText().trim());
        c.setEmail(txtEmail.getText().trim());
        c.setDireccion(txtDireccion.getText().trim());
        c.setReferencia(txtReferencia.getText().trim());
        c.setIdZona(cmbDistrito.getValue() != null ? cmbDistrito.getValue().getIdZona() : 0);
        c.setEstado(clienteSeleccionado == null ? 1 : clienteSeleccionado.getEstado());

        boolean ok;
        boolean esNuevo = (clienteSeleccionado == null);
        if (esNuevo) {
            if (dao.existeDni(c.getDni(), 0)) { mostrarMensaje("El DNI/RUC ya está registrado.", false); return; }
            ok = dao.guardar(c);
        } else {
            if (dao.existeDni(c.getDni(), clienteSeleccionado.getIdCliente())) { mostrarMensaje("El DNI/RUC ya está registrado.", false); return; }
            c.setIdCliente(clienteSeleccionado.getIdCliente());
            ok = dao.actualizar(c);
        }
        if (ok) {
            mostrarToast(esNuevo ? "✔  Cliente nuevo registrado" : "✔  Cliente actualizado", true);
            mostrarMensaje(esNuevo ? "Cliente registrado." : "Cliente actualizado.", true);
            cargarTabla(); limpiarFormulario();
        } else {
            mostrarMensaje("Error al guardar.", false);
        }
    }

    @FXML public void eliminarCliente() {
        if (clienteSeleccionado == null) return;
        boolean estaActivo = clienteSeleccionado.getEstado() == 1;
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Deseas " + (estaActivo ? "desactivar" : "activar")
            + " a " + clienteSeleccionado.getNombreCompleto() + "?",
            ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null); a.setTitle("Confirmar");
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                clienteSeleccionado.setEstado(estaActivo ? 0 : 1);
                if (dao.actualizar(clienteSeleccionado)) {
                    String accionHecha = estaActivo ? "desactivado" : "activado";
                    mostrarToast("✔  Cliente " + accionHecha, true);
                    mostrarMensaje("Cliente " + accionHecha + ".", true);
                    cargarTabla(); limpiarFormulario();
                }
            }
        });
    }

    @FXML public void limpiarFormulario() {
        clienteSeleccionado = null;
        txtDni.clear(); txtNombres.clear(); txtApellidos.clear();
        txtTelefono.clear(); txtEmail.clear(); txtDireccion.clear();
        txtReferencia.clear(); cmbDistrito.setValue(null);
        lblMensaje.setText(""); lblTituloForm.setText("Nuevo cliente");
        btnEliminar.setDisable(true);
        actualizarBoton(true);
        btnGuardar.setDisable(true);
        tblClientes.getSelectionModel().clearSelection();
    }

    private void actualizarBoton(boolean estaActivo) {
        if (estaActivo) {
            btnEliminar.setText("Desactivar");
            btnEliminar.setStyle("-fx-background-color: #C62828; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 9 20 9 20; -fx-background-radius: 6; -fx-cursor: hand;");
        } else {
            btnEliminar.setText("Activar");
            btnEliminar.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 9 20 9 20; -fx-background-radius: 6; -fx-cursor: hand;");
        }
    }

    private String validarCompleto() {
        String dni = txtDni.getText().trim();
        if (dni.isEmpty()) return "El DNI/RUC es obligatorio.";
        if (!dni.matches("\\d{8}") && !dni.matches("\\d{11}"))
            return "DNI: 8 dígitos, RUC: 11 dígitos.";

        String nombres = txtNombres.getText().trim();
        if (nombres.isEmpty()) return "Los nombres son obligatorios.";
        if (nombres.length() < 3) return "Los nombres deben tener al menos 3 caracteres.";
        if (!nombres.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+"))
            return "Los nombres solo deben contener letras.";

        String apellidos = txtApellidos.getText().trim();
        if (apellidos.isEmpty()) return "Los apellidos son obligatorios.";
        if (apellidos.length() < 3) return "Los apellidos deben tener al menos 3 caracteres.";
        if (!apellidos.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+"))
            return "Los apellidos solo deben contener letras.";

        String tel = txtTelefono.getText().trim();
        if (!tel.isEmpty() && !tel.matches("\\d{9}"))
            return "El teléfono debe tener exactamente 9 dígitos.";

        String dir = txtDireccion.getText().trim();
        if (!dir.isEmpty() && dir.length() < 5)
            return "La dirección debe tener al menos 5 caracteres.";

        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !email.matches("^[\\w._%+\\-]+@gmail\\.com$"))
            return "El correo debe terminar en @gmail.com.";

        return null;
    }

    private void mostrarMensaje(String t, boolean ok) {
        lblMensaje.setText(t);
        lblMensaje.setStyle(ok ? "-fx-text-fill: #2E7D32; -fx-font-size: 12px;"
                               : "-fx-text-fill: #C62828; -fx-font-size: 12px;");
    }

    private void mostrarToast(String mensaje, boolean exito) {
        if (toastPane == null) return;
        Label toast = new Label(mensaje);
        toast.setWrapText(false);
        toast.setStyle(
            "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white;" +
            "-fx-padding: 10 20 10 20; -fx-background-radius: 18;" +
            (exito ? "-fx-background-color: #2E7D32;" : "-fx-background-color: #C62828;") +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.28), 8, 0, 0, 2);"
        );
        StackPane.setAlignment(toast, Pos.CENTER);
        toastPane.getChildren().add(toast);
        FadeTransition fadeIn  = new FadeTransition(Duration.millis(200), toast);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);
        PauseTransition pausa  = new PauseTransition(Duration.seconds(2.2));
        FadeTransition fadeOut = new FadeTransition(Duration.millis(350), toast);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);
        SequentialTransition seq = new SequentialTransition(fadeIn, pausa, fadeOut);
        seq.setOnFinished(e -> toastPane.getChildren().remove(toast));
        seq.play();
    }

    private String nvl(String s) { return s == null ? "" : s; }
}
